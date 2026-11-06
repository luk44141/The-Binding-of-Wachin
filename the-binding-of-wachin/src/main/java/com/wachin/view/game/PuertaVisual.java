package com.wachin.view.game;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Representa una salida y muestra visualmente si está abierta o cerrada.
 */
public class PuertaVisual {

    /**
     * Define una opción cerrada disponible para la configuración del juego.
     */
    public enum Direccion {
        ARRIBA,
        ABAJO,
        IZQUIERDA,
        DERECHA
    }

    /**
     * Guarda direccion para que la clase mantenga su estado interno.
     */
    private final Direccion direccion;
    /**
     * Guarda visual para que la clase mantenga su estado interno.
     */
    private final Rectangle visual;

    /**
     * Guarda abierta para que la clase mantenga su estado interno.
     */
    private boolean abierta;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
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

    /**
     * Actualiza abierta mediante el método de acceso de la clase.
     */
    public void setAbierta(boolean abierta) {
        this.abierta = abierta;

        if (abierta) {
            visual.setFill(Color.GOLD);
        } else {
            visual.setFill(Color.rgb(100, 45, 25));
        }
    }

    /**
     * Comprueba esta condición a partir del estado actual y devuelve el resultado.
     */
    public boolean estaAbierta() {
        return abierta;
    }

    /**
     * Devuelve direccion sin exponer directamente el atributo.
     */
    public Direccion getDireccion() {
        return direccion;
    }

    /**
     * Devuelve nodo visual sin exponer directamente el atributo.
     */
    public Rectangle getVisual() {
        return visual;
    }
}
