package com.wachin.view.game;

import javafx.scene.Node;
import javafx.scene.paint.Color;

public class Proyectil {

    private double x;
    private double y;
    private final double velocidadX;
    private final double velocidadY;
    private final int dano;
    private final Node visual;
    private static final double TAMANO_VISUAL = 22;

    public Proyectil(double x, double y, double velocidadX, double velocidadY, int dano) {
        this.x = x;
        this.y = y;
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
        this.dano = dano;
        this.visual = AssetVisual.crear(
                "assets/images/projectiles/proyectil.png",
                x, y, TAMANO_VISUAL, TAMANO_VISUAL,
                Color.WHITE, Color.YELLOW, 1.5
        );
    }

    public void actualizar(double deltaTiempo) {
        x += velocidadX * deltaTiempo;
        y += velocidadY * deltaTiempo;
        AssetVisual.moverAlCentro(visual, x, y, TAMANO_VISUAL, TAMANO_VISUAL);
    }

    public boolean estaFueraDeLaSala() {
        return x < 50 || x > 950 || y < 50 || y > 650;
    }

    public Node getVisual() { return visual; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getDano() { return dano; }
}
