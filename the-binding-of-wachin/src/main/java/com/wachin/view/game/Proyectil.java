package com.wachin.view.game;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;

/**
 * Representa un disparo, conserva su trayectoria y actualiza su posición visual.
 */
public class Proyectil {
    /**
     * Guarda x para que la clase mantenga su estado interno.
     */
    private double x;
    /**
     * Guarda y para que la clase mantenga su estado interno.
     */
    private double y;
    /**
     * Constante que define xanterior para este componente.
     */
    private double xAnterior;
    /**
     * Constante que define yanterior para este componente.
     */
    private double yAnterior;
    /**
     * Constante que define velocidadx para este componente.
     */
    private final double velocidadX;
    /**
     * Constante que define velocidady para este componente.
     */
    private final double velocidadY;
    /**
     * Guarda dano para que la clase mantenga su estado interno.
     */
    private final int dano;
    /**
     * Guarda visual para que la clase mantenga su estado interno.
     */
    private final Node visual;
    /**
     * Constante que define tamano visual para este componente.
     */
    private static final double TAMANO_VISUAL = 22;

    /** Constructor de compatibilidad: usa la lagrima por defecto. */
    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Proyectil(double x, double y, double velocidadX, double velocidadY, int dano) {
        this(x, y, velocidadX, velocidadY, dano, "assets/images/projectiles/lagrima.png");
    }

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Proyectil(double x, double y, double velocidadX, double velocidadY,
                    int dano, String rutaAsset) {
        this(x, y, velocidadX, velocidadY, dano, rutaAsset, 0, -1);
    }

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
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

    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    public void actualizar(double deltaTiempo) {
        xAnterior = x;
        yAnterior = y;
        x += velocidadX * deltaTiempo;
        y += velocidadY * deltaTiempo;
        AssetVisual.moverAlCentro(visual, x, y, TAMANO_VISUAL, TAMANO_VISUAL);
    }

    /**
     * Indica si el proyectil sobrepasó los límites definidos de la sala.
     */
    public boolean estaFueraDeLaSala() { return x < 50 || x > 950 || y < 50 || y > 650; }
    /**
     * Devuelve nodo visual sin exponer directamente el atributo.
     */
    public Node getVisual() { return visual; }
    /**
     * Devuelve coordenada X sin exponer directamente el atributo.
     */
    public double getX() { return x; }
    /**
     * Devuelve coordenada Y sin exponer directamente el atributo.
     */
    public double getY() { return y; }
    /**
     * Devuelve coordenada X anterior sin exponer directamente el atributo.
     */
    public double getXAnterior() { return xAnterior; }
    /**
     * Devuelve coordenada Y anterior sin exponer directamente el atributo.
     */
    public double getYAnterior() { return yAnterior; }
    /**
     * Devuelve daño sin exponer directamente el atributo.
     */
    public int getDano() { return dano; }
    /**
     * Devuelve radio de colisión sin exponer directamente el atributo.
     */
    public double getRadioColision() { return TAMANO_VISUAL / 2.0; }
}
