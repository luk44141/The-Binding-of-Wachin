package com.wachin.game;

import com.wachin.model.Enemigo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class EnemigoVisual {

    private final Enemigo enemigo;

    private double x;
    private double y;

    private final Circle visual;

    private final double velocidad = 70;

    public EnemigoVisual(
            Enemigo enemigo,
            double x,
            double y) {

        this.enemigo = enemigo;

        this.x = x;
        this.y = y;

        visual = new Circle(x, y, 22);

        visual.setFill(Color.CRIMSON);
        visual.setStroke(Color.DARKRED);
        visual.setStrokeWidth(3);
    }

    public void actualizar(
            double jugadorX,
            double jugadorY,
            double deltaTiempo) {

        double diferenciaX = jugadorX - x;
        double diferenciaY = jugadorY - y;

        double distancia = Math.sqrt(
                diferenciaX * diferenciaX +
                diferenciaY * diferenciaY
        );

        if (distancia > 1) {

            double direccionX = diferenciaX / distancia;
            double direccionY = diferenciaY / distancia;

            x += direccionX * velocidad * deltaTiempo;
            y += direccionY * velocidad * deltaTiempo;

            limitarMovimiento();

            visual.setCenterX(x);
            visual.setCenterY(y);
        }
    }

    private void limitarMovimiento() {

        if (x < 75) {
            x = 75;
        }

        if (x > 925) {
            x = 925;
        }

        if (y < 75) {
            y = 75;
        }

        if (y > 625) {
            y = 625;
        }
    }

    public boolean tocaJugador(
            double jugadorX,
            double jugadorY) {

        double diferenciaX = jugadorX - x;
        double diferenciaY = jugadorY - y;

        double distancia = Math.sqrt(
                diferenciaX * diferenciaX +
                diferenciaY * diferenciaY
        );

        return distancia < 45;
    }

    public Enemigo getEnemigo() {
        return enemigo;
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
}