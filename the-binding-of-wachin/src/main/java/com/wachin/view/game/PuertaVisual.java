package com.wachin.view.game;

import com.wachin.view.fx.Brillo;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Puerta en una de las cuatro paredes. Cerrada muestra el sprite de la puerta (o una puerta de
 * hierro dibujada por código) con un candado rojo; abierta, un umbral oscuro con un portal
 * luminoso. Las salas de jefe usan una variante dorada.
 */
public final class PuertaVisual {

    /** Direcciones posibles de las salidas. */
    public enum Direccion { ARRIBA, ABAJO, IZQUIERDA, DERECHA }

    private static final double ANCHO = 78;
    private static final double PROFUNDIDAD = 58;

    private final Direccion direccion;
    private final Group visual = new Group();
    private final Group contenido = new Group();
    private final Group cerrada = new Group();
    private final Group abierta = new Group();
    private final ImageView portal = new ImageView();
    private final Rectangle candado = new Rectangle(-6, -2, 12, 10);
    private boolean estaAbierta;
    private boolean esJefe;
    private double tiempo;

    public PuertaVisual(Direccion direccion) {
        if (direccion == null) throw new IllegalArgumentException("La dirección de la puerta es obligatoria.");
        this.direccion = direccion;
        double x;
        double y;
        double giro;
        switch (direccion) {
            case ARRIBA -> { x = 500; y = 26; giro = 0; }
            case ABAJO -> { x = 500; y = 674; giro = 180; }
            case IZQUIERDA -> { x = 26; y = 350; giro = -90; }
            default -> { x = 974; y = 350; giro = 90; }
        }
        visual.setTranslateX(x);
        visual.setTranslateY(y);
        visual.setRotate(giro);
        visual.setMouseTransparent(true);
        construir(false);
        visual.getChildren().add(contenido);
        setAbierta(false);
    }

    /** Reconstruye las piezas con los colores de sala normal o de jefe. */
    public final void setJefe(boolean jefe) {
        if (jefe == esJefe && !contenido.getChildren().isEmpty()) return;
        esJefe = jefe;
        construir(jefe);
        setAbierta(estaAbierta);
    }

    private void construir(boolean jefe) {
        contenido.getChildren().clear();
        cerrada.getChildren().clear();
        abierta.getChildren().clear();
        Color acento = jefe ? Color.web("#ffc93a") : Color.web("#4fd6e8");
        // Marco de piedra
        Rectangle marco = new Rectangle(-ANCHO / 2, -PROFUNDIDAD / 2, ANCHO, PROFUNDIDAD);
        marco.setArcWidth(10);
        marco.setArcHeight(10);
        marco.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4a4258")), new Stop(1, Color.web("#241f30"))));
        marco.setStroke(Color.web("#0a0810"));
        marco.setStrokeWidth(3);
        // Puerta cerrada: sprite o placas de hierro
        List<String> rutasCerrada = new ArrayList<>();
        if (jefe) rutasCerrada.add(CatalogoAssets.PUERTA_JEFE_CERRADA);
        rutasCerrada.addAll(CatalogoAssets.conHistoricos(CatalogoAssets.PUERTA_CERRADA));
        Image sprite = AssetVisual.buscar(rutasCerrada).orElse(null);
        boolean spriteDeJefe = jefe && AssetVisual.existe(List.of(CatalogoAssets.PUERTA_JEFE_CERRADA));
        if (sprite != null) {
            Rectangle alcoba = new Rectangle(-ANCHO / 2 + 10, -PROFUNDIDAD / 2 + 6, ANCHO - 20, PROFUNDIDAD - 8);
            alcoba.setFill(Color.web("#08060d"));
            double alto = PROFUNDIDAD - 8;
            ImageView puerta = new ImageView(sprite);
            puerta.setViewport(new javafx.geometry.Rectangle2D(64, 32, 128, 224));
            puerta.setSmooth(false);
            puerta.setPreserveRatio(true);
            puerta.setFitHeight(alto);
            puerta.setX(-alto * 128 / 224 / 2);
            puerta.setY(-alto / 2 + 2);
            if (jefe && !spriteDeJefe) puerta.setEffect(new javafx.scene.effect.ColorAdjust(0.12, 0.5, -0.1, 0));
            cerrada.getChildren().addAll(alcoba, puerta);
        } else {
            Rectangle hoja = new Rectangle(-ANCHO / 2 + 8, -PROFUNDIDAD / 2 + 7, ANCHO - 16, PROFUNDIDAD - 12);
            hoja.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, jefe ? Color.web("#5a3a12") : Color.web("#4a2a1e")),
                    new Stop(0.5, jefe ? Color.web("#7a521a") : Color.web("#6a3c28")),
                    new Stop(1, jefe ? Color.web("#5a3a12") : Color.web("#4a2a1e"))));
            hoja.setStroke(Color.BLACK);
            hoja.setStrokeWidth(3);
            cerrada.getChildren().add(hoja);
            for (int i = -1; i <= 1; i++) {
                Line banda = new Line(i * 22, -PROFUNDIDAD / 2 + 8, i * 22, PROFUNDIDAD / 2 - 6);
                banda.setStroke(Color.web("#14101a"));
                banda.setStrokeWidth(4);
                cerrada.getChildren().add(banda);
            }
        }
        candado.setArcWidth(3);
        candado.setArcHeight(3);
        candado.setFill(jefe ? Color.web("#ffc93a") : Color.web("#ff4560"));
        candado.setStroke(Color.BLACK);
        Rectangle arco = new Rectangle(-4, -9, 8, 9);
        arco.setFill(Color.TRANSPARENT);
        arco.setStroke(Color.web("#d8d2e6"));
        arco.setStrokeWidth(2.5);
        arco.setArcWidth(8);
        arco.setArcHeight(8);
        cerrada.getChildren().addAll(arco, candado);
        // Puerta abierta: umbral oscuro y portal
        Rectangle umbral = new Rectangle(-ANCHO / 2 + 10, -PROFUNDIDAD / 2 + 6, ANCHO - 20, PROFUNDIDAD - 8);
        umbral.setFill(new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#000000")), new Stop(1, Color.web("#0b0a10"))));
        portal.setImage(Brillo.radial(acento, 0.75));
        portal.setFitWidth(60);
        portal.setFitHeight(60);
        portal.setX(-30);
        portal.setY(-30);
        portal.setTranslateY(-8);
        abierta.getChildren().add(umbral);
        List<String> rutasAbierta = new ArrayList<>();
        if (jefe) rutasAbierta.add(CatalogoAssets.PUERTA_JEFE_ABIERTA);
        rutasAbierta.add(CatalogoAssets.PUERTA_ABIERTA);
        AssetVisual.buscar(rutasAbierta).ifPresent(imagen -> {
            ImageView puertaAbierta = AssetVisual.vista(imagen, ANCHO - 20, PROFUNDIDAD - 8);
            puertaAbierta.setTranslateY(-1);
            abierta.getChildren().add(puertaAbierta);
        });
        abierta.getChildren().add(portal);
        Rectangle borde = new Rectangle(-ANCHO / 2 + 10, -PROFUNDIDAD / 2 + 6, ANCHO - 20, PROFUNDIDAD - 8);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(acento);
        borde.setStrokeWidth(2.5);
        Rectangle resplandor = new Rectangle(-ANCHO / 2 + 7, -PROFUNDIDAD / 2 + 3, ANCHO - 14, PROFUNDIDAD - 2);
        resplandor.setArcWidth(8);
        resplandor.setArcHeight(8);
        resplandor.setFill(Color.TRANSPARENT);
        resplandor.setStroke(Color.color(acento.getRed(), acento.getGreen(), acento.getBlue(), 0.28));
        resplandor.setStrokeWidth(7);
        abierta.getChildren().addAll(resplandor, borde);
        contenido.getChildren().addAll(marco, cerrada, abierta);
    }

    /** Anima el pulso del portal y el candado. */
    public void actualizar(double dt) {
        tiempo += dt;
        if (!visual.isVisible()) return;
        if (estaAbierta) {
            double pulso = 1 + 0.14 * Math.sin(tiempo * 4);
            portal.setScaleX(pulso);
            portal.setScaleY(pulso);
            portal.setOpacity(0.75 + 0.25 * Math.sin(tiempo * 3.3));
        } else {
            candado.setOpacity(0.6 + 0.4 * Math.abs(Math.sin(tiempo * 2.4)));
        }
    }

    public void setAbierta(boolean abierta) {
        this.estaAbierta = abierta;
        this.abierta.setVisible(abierta);
        this.cerrada.setVisible(!abierta);
    }

    public boolean estaAbierta() { return estaAbierta; }
    public Direccion getDireccion() { return direccion; }
    public Group getVisual() { return visual; }
}
