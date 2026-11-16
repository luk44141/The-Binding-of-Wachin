package com.wachin.view.game;

import com.wachin.model.Proyectil;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/** Representación gráfica que sincroniza la posición de un modelo de proyectil. */
public final class ProyectilVisual {

    private final Proyectil modelo;
    private final String rutaAsset;
    private final Color color;
    private final Group nodo = new Group();
    private final ImageView sprite;
    private final double tamano;
    private final boolean esRayo;
    private double edad;

    /**
     * @param color color del resplandor, la estela y las chispas; también del dibujo por código
     *              cuando falta el sprite
     * @param rayo  {@code true} para proyectiles alargados (láser)
     */
    public ProyectilVisual(Proyectil modelo, String rutaAsset, Color color, boolean rayo) {
        if (modelo == null || rutaAsset == null || rutaAsset.isBlank()) {
            throw new IllegalArgumentException("El modelo y la ruta visual del proyectil son obligatorios.");
        }
        this.modelo = modelo;
        this.rutaAsset = rutaAsset;
        this.color = color;
        this.esRayo = rayo;
        this.tamano = rayo ? 30 : modelo.esHostil() ? 24 : 28;
        Image imagen = AssetVisual.imagen(java.util.List.of(rutaAsset), "proyectil_" + rutaAsset + "_" + color,
                () -> rayo ? Dibujos.rayo(color) : Dibujos.orbe(color));
        sprite = AssetVisual.vista(imagen, tamano, tamano);
        nodo.getChildren().add(sprite);
        nodo.setRotate(Math.toDegrees(Math.atan2(modelo.getVelocidadY(), modelo.getVelocidadX())) + 90);
        nodo.setMouseTransparent(true);
        sincronizar();
    }

    public void actualizar(double deltaTiempo) {
        modelo.actualizar(deltaTiempo);
        edad += deltaTiempo;
        double pulso = 1 + 0.08 * Math.sin(edad * 22);
        sprite.setScaleX(pulso);
        sprite.setScaleY(esRayo ? 1 : pulso);
        sincronizar();
    }

    private void sincronizar() {
        AssetVisual.moverAlCentro(nodo, modelo.getX(), modelo.getY());
    }

    public Proyectil getModelo() { return modelo; }
    public String getRutaAsset() { return rutaAsset; }
    public Color getColor() { return color; }
    public Node getVisual() { return nodo; }
    public double getRadioColision() { return tamano / 2.0; }
}
