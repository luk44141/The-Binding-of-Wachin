package com.wachin.view.game;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

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
    private final Group visual = new Group();
    private final Node puertaCerrada;
    private final Node puertaAbierta;

    /**
     * Guarda abierta para que la clase mantenga su estado interno.
     */
    private boolean abierta;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public PuertaVisual(Direccion direccion) {
        if (direccion == null) throw new IllegalArgumentException("La dirección de la puerta es obligatoria.");
        this.direccion = direccion;
        double x;
        double y;
        double ancho;
        double alto;

        switch (direccion) {
            case ARRIBA:
                x = 500; y = 60; ancho = 70; alto = 35;
                break;

            case ABAJO:
                x = 500; y = 640; ancho = 70; alto = 35;
                break;

            case IZQUIERDA:
                x = 60; y = 350; ancho = 35; alto = 70;
                break;

            case DERECHA:
                x = 940; y = 350; ancho = 35; alto = 70;
                break;

            default:
                throw new IllegalArgumentException(
                        "Dirección de puerta inválida."
                );
        }

        puertaCerrada = AssetVisual.crearBloque("assets/images/environment/doors/closed.png",
                x, y, ancho, alto, Color.rgb(100, 45, 25), Color.BLACK, 3);
        puertaAbierta = AssetVisual.crearBloque("assets/images/environment/doors/open.png",
                x, y, ancho, alto, Color.GOLD, Color.BLACK, 3);
        visual.getChildren().addAll(puertaCerrada, puertaAbierta);

        setAbierta(false);
    }

    /**
     * Actualiza abierta mediante el método de acceso de la clase.
     */
    public void setAbierta(boolean abierta) {
        this.abierta = abierta;

        puertaAbierta.setVisible(abierta);
        puertaCerrada.setVisible(!abierta);
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
    public Group getVisual() {
        return visual;
    }
}
