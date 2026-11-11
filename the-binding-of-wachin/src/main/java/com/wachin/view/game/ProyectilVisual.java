package com.wachin.view.game;

import com.wachin.model.Proyectil;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/** Representación gráfica que sincroniza la posición de un modelo de proyectil. */
public final class ProyectilVisual {
    private static final double TAMANO_VISUAL = 22;
    private final Proyectil modelo;
    private final String rutaAsset;
    private final Node visual;

    public ProyectilVisual(Proyectil modelo, String rutaAsset) {
        if (modelo == null || rutaAsset == null || rutaAsset.isBlank()) {
            throw new IllegalArgumentException("El modelo y la ruta visual del proyectil son obligatorios.");
        }
        this.modelo = modelo;
        this.rutaAsset = rutaAsset;
        boolean hostil = modelo.esHostil();
        this.visual = hostil || modelo.getOrigen() == Proyectil.Origen.INVOCACION
                ? AssetVisual.crearBloque(rutaAsset, modelo.getX(), modelo.getY(),
                        TAMANO_VISUAL * 0.7, TAMANO_VISUAL * 0.7,
                        hostil ? Color.ORANGERED : Color.CYAN,
                        hostil ? Color.DARKRED : Color.DARKBLUE, 1.5)
                : AssetVisual.crear(rutaAsset, modelo.getX(), modelo.getY(),
                        TAMANO_VISUAL, TAMANO_VISUAL, Color.WHITE, Color.YELLOW, 1.5);
        this.visual.setRotate(Math.toDegrees(Math.atan2(
                modelo.getVelocidadY(), modelo.getVelocidadX())) + 90);
    }

    public void actualizar(double deltaTiempo) {
        modelo.actualizar(deltaTiempo);
        AssetVisual.moverAlCentro(visual, modelo.getX(), modelo.getY(),
                TAMANO_VISUAL, TAMANO_VISUAL);
    }

    public Proyectil getModelo() { return modelo; }
    public String getRutaAsset() { return rutaAsset; }
    public Node getVisual() { return visual; }
    public double getRadioColision() { return TAMANO_VISUAL / 2.0; }
}
