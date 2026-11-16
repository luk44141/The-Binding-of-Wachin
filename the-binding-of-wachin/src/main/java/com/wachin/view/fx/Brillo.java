package com.wachin.view.fx;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Resplandores y sombras pregenerados como imágenes. Rellenar un degradado radial en cada
 * fotograma es mucho más caro que copiar un bitmap, así que cada brillo se rasteriza una vez
 * y se reutiliza en todos los nodos que lo necesitan.
 */
public final class Brillo {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private Brillo() { }

    /** Disco con degradado radial del color indicado: opaco {@code alfa} en el centro y transparente en el borde. */
    public static synchronized Image radial(Color color, double alfa) {
        String clave = color + "/" + alfa;
        return CACHE.computeIfAbsent(clave, k -> {
            int lado = 128;
            Canvas lienzo = new Canvas(lado, lado);
            GraphicsContext g = lienzo.getGraphicsContext2D();
            g.setFill(new RadialGradient(0, 0, lado / 2.0, lado / 2.0, lado / 2.0, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(color.getRed(), color.getGreen(), color.getBlue(), alfa)),
                    new Stop(1, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0))));
            g.fillOval(0, 0, lado, lado);
            return capturar(lienzo);
        });
    }

    /** Sombra negra suave para el suelo bajo una entidad. */
    public static Image sombra() {
        return radial(Color.BLACK, 0.6);
    }

    /** Viñeta de pantalla: transparente en el centro y oscura en los bordes (se escala al tamaño de la escena). */
    public static synchronized Image vineta(Color borde, double alfaBorde, double inicio) {
        String clave = "vineta/" + borde + "/" + alfaBorde + "/" + inicio;
        return CACHE.computeIfAbsent(clave, k -> {
            int ancho = 250;
            int alto = 175;
            Canvas lienzo = new Canvas(ancho, alto);
            GraphicsContext g = lienzo.getGraphicsContext2D();
            g.setFill(new RadialGradient(0, 0, ancho / 2.0, alto / 2.0, Math.max(ancho, alto) * 0.72, false,
                    CycleMethod.NO_CYCLE, new Stop(inicio, Color.TRANSPARENT),
                    new Stop(1, Color.color(borde.getRed(), borde.getGreen(), borde.getBlue(), alfaBorde))));
            g.fillRect(0, 0, ancho, alto);
            return capturar(lienzo);
        });
    }

    /** Crea un nodo de imagen con el brillo, centrado en el origen local y con el diámetro indicado. */
    public static ImageView nodoRadial(Color color, double alfa, double diametro) {
        ImageView vista = new ImageView(radial(color, alfa));
        vista.setFitWidth(diametro);
        vista.setFitHeight(diametro);
        vista.setX(-diametro / 2);
        vista.setY(-diametro / 2);
        vista.setSmooth(true);
        vista.setMouseTransparent(true);
        return vista;
    }

    private static Image capturar(Canvas lienzo) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }
}
