package com.wachin.view.game;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.InputStream;

/** Carga assets del classpath y conserva un visual de respaldo si falta el PNG. */
/**
 * Carga recursos gráficos del classpath y crea figuras de respaldo cuando faltan imágenes.
 */
public final class AssetVisual {

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    private AssetVisual() {
    }

    /**
     * Construye los controles del menú y conecta la acción principal con la selección del usuario.
     */
    public static Node crear(
            String ruta,
            double centroX,
            double centroY,
            double ancho,
            double alto,
            Color colorRespaldo,
            Color bordeRespaldo,
            double grosorBorde) {

        String rutaClasspath = ruta.startsWith("/") ? ruta : "/" + ruta;
        InputStream recurso = AssetVisual.class.getResourceAsStream(rutaClasspath);

        if (recurso != null) {
            try (InputStream entrada = recurso) {
                Image imagen = new Image(entrada);
                if (!imagen.isError()) {
                    ImageView vista = new ImageView(imagen);
                    vista.setFitWidth(ancho);
                    vista.setFitHeight(alto);
                    vista.setPreserveRatio(true);
                    vista.setSmooth(false);
                    vista.setX(centroX - ancho / 2.0);
                    vista.setY(centroY - alto / 2.0);
                    vista.setMouseTransparent(true);
                    return vista;
                }
            } catch (Exception ignored) {
                // Se usa el visual de respaldo si el archivo no se puede leer.
            }
        }

        Circle respaldo = new Circle(centroX, centroY, Math.min(ancho, alto) / 2.0);
        respaldo.setFill(colorRespaldo);
        respaldo.setStroke(bordeRespaldo);
        respaldo.setStrokeWidth(grosorBorde);
        respaldo.setMouseTransparent(true);
        return respaldo;
    }

    /**
     * Mueve la imagen o figura para que su centro coincida con las coordenadas indicadas.
     */
    public static void moverAlCentro(Node visual, double centroX, double centroY, double ancho, double alto) {
        if (visual instanceof ImageView) {
            ImageView imagen = (ImageView) visual;
            imagen.setX(centroX - ancho / 2.0);
            imagen.setY(centroY - alto / 2.0);
        } else if (visual instanceof Circle) {
            Circle circulo = (Circle) visual;
            circulo.setCenterX(centroX);
            circulo.setCenterY(centroY);
        }
    }
}
