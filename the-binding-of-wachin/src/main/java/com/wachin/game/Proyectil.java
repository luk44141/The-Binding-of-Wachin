package com.wachin.game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Proyectil {

    private double x;
    private double y;

    private final double velocidadX;
    private final double velocidadY;

    private final int dano;

    private final Circle visual;

    public Proyectil(
            double x,
            double y,
            double velocidadX,
            double velocidadY,
            int dano) {

        this.x = x;
        this.y = y;

        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;

        this.dano = dano;

        visual = new Circle(x, y, 6);
        visual.setFill(Color.WHITE);
        visual.setStroke(Color.YELLOW);
    }

    public void actualizar(double deltaTiempo) {

        x += velocidadX * deltaTiempo;
        y += velocidadY * deltaTiempo;

        visual.setCenterX(x);
        visual.setCenterY(y);
    }

    public boolean estaFueraDeLaSala() {

        return x < 50 ||
               x > 950 ||
               y < 50 ||
               y > 650;
    }

    public Circle getVisual() {
        return visual;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDano() {
        return dano;
    }
}