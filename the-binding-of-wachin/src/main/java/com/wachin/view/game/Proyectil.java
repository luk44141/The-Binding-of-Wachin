package com.wachin.view.game;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;

public class Proyectil {
    private double x;
    private double y;
    private double xAnterior;
    private double yAnterior;
    private final double velocidadX;
    private final double velocidadY;
    private final int dano;
    private final Node visual;
    private static final double TAMANO_VISUAL = 22;

    /** Constructor de compatibilidad: usa la lagrima por defecto. */
    public Proyectil(double x, double y, double velocidadX, double velocidadY, int dano) {
        this(x, y, velocidadX, velocidadY, dano, "assets/images/projectiles/lagrima.png");
    }

    public Proyectil(double x, double y, double velocidadX, double velocidadY,
                    int dano, String rutaAsset) {
        this(x, y, velocidadX, velocidadY, dano, rutaAsset, 0, -1);
    }

    public Proyectil(double x, double y, double velocidadX, double velocidadY,
                    int dano, String rutaAsset, double direccionX, double direccionY) {
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
        this.dano = Math.max(1, dano);
        double angulo = Math.toDegrees(Math.atan2(direccionY, direccionX)) + 90;
        // La ruta llega de una opcion cerrada del menu, nunca de texto arbitrario.
        this.visual = AssetVisual.crear(rutaAsset, x, y, TAMANO_VISUAL, TAMANO_VISUAL,
                Color.WHITE, Color.YELLOW, 1.5);
        this.visual.setRotate(angulo);
    }

    public void actualizar(double deltaTiempo) {
        xAnterior = x;
        yAnterior = y;
        x += velocidadX * deltaTiempo;
        y += velocidadY * deltaTiempo;
        AssetVisual.moverAlCentro(visual, x, y, TAMANO_VISUAL, TAMANO_VISUAL);
    }

    public boolean estaFueraDeLaSala() { return x < 50 || x > 950 || y < 50 || y > 650; }
    public Node getVisual() { return visual; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getXAnterior() { return xAnterior; }
    public double getYAnterior() { return yAnterior; }
    public int getDano() { return dano; }
    public double getRadioColision() { return TAMANO_VISUAL / 2.0; }
}
