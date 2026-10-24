package com.wachin.view.game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PuertaVisual {

    public enum Direccion {
        ARRIBA,
        ABAJO,
        IZQUIERDA,
        DERECHA
    }

    private final Direccion direccion;
    private final Rectangle visual;

    private boolean abierta;

    public PuertaVisual(Direccion direccion) {
        this.direccion = direccion;

        switch (direccion) {
            case ARRIBA:
                visual = new Rectangle(465, 43, 70, 35);
                break;

            case ABAJO:
                visual = new Rectangle(465, 622, 70, 35);
                break;

            case IZQUIERDA:
                visual = new Rectangle(43, 315, 35, 70);
                break;

            case DERECHA:
                visual = new Rectangle(922, 315, 35, 70);
                break;

            default:
                throw new IllegalArgumentException(
                        "Dirección de puerta inválida."
                );
        }

        visual.setStroke(Color.BLACK);
        visual.setStrokeWidth(3);

        setAbierta(false);
    }

    public void setAbierta(boolean abierta) {
        this.abierta = abierta;

        if (abierta) {
            visual.setFill(Color.GOLD);
        } else {
            visual.setFill(Color.rgb(100, 45, 25));
        }
    }

    public boolean estaAbierta() {
        return abierta;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public Rectangle getVisual() {
        return visual;
    }
}