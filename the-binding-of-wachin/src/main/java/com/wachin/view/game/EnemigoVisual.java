package com.wachin.view.game;

import com.wachin.model.Enemigo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class EnemigoVisual {

    private final Enemigo enemigo;
    private final Circle visual;
    private final double velocidad;

    private double x;
    private double y;

    public EnemigoVisual(
            Enemigo enemigo,
            double x,
            double y) {

        this.enemigo = enemigo;
        this.x = x;
        this.y = y;
        this.velocidad = enemigo.getVelocidad();

        visual = new Circle(x, y, 22);
        visual.setFill(obtenerColor(enemigo.getNombre()));
        visual.setStroke(Color.BLACK);
        visual.setStrokeWidth(2);
    }

    private Color obtenerColor(String nombre) {
        switch (nombre) {
            case "Slime":
                return Color.LIMEGREEN;
            case "Murcielago":
                return Color.DARKSLATEBLUE;
            case "Demonio":
                return Color.CRIMSON;
            case "Monstruo":
                return Color.DARKORANGE;
            case "Esqueleto":
                return Color.LIGHTGRAY;
            case "Fantasma":
                return Color.LIGHTSKYBLUE;
            default:
                return Color.CRIMSON;
        }
    }

    public void actualizar(
            double jugadorX,
            double jugadorY,
            double deltaTiempo) {

        double diferenciaX = jugadorX - x;
        double diferenciaY = jugadorY - y;

        double distancia = Math.sqrt(
                diferenciaX * diferenciaX
                        + diferenciaY * diferenciaY
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
        x = Math.max(75, Math.min(925, x));
        y = Math.max(75, Math.min(625, y));
    }

    public boolean tocaJugador(
            double jugadorX,
            double jugadorY) {

        double diferenciaX = jugadorX - x;
        double diferenciaY = jugadorY - y;

        return Math.sqrt(
                diferenciaX * diferenciaX
                        + diferenciaY * diferenciaY
        ) < 45;
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