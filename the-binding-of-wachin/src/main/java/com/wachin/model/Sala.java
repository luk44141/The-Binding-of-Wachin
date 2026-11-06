package com.wachin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de datos de una sala, con su dificultad y enemigos.
 */
public class Sala {

    /**
     * Guarda numero para que la clase mantenga su estado interno.
     */
    private final int numero;
    /**
     * Guarda dificultad para que la clase mantenga su estado interno.
     */
    private final int dificultad;
    /**
     * Guarda enemigos para que la clase mantenga su estado interno.
     */
    private final List<Enemigo> enemigos;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Sala(int numero, int dificultad, List<Enemigo> enemigos) {
        if (numero < 1) {
            throw new IllegalArgumentException(
                    "El número de sala debe ser mayor que cero."
            );
        }

        if (dificultad < 1) {
            throw new IllegalArgumentException(
                    "La dificultad debe ser mayor que cero."
            );
        }

        this.numero = numero;
        this.dificultad = dificultad;
        this.enemigos = new ArrayList<>(enemigos);
    }

    /**
     * Devuelve número de sala sin exponer directamente el atributo.
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Devuelve dificultad sin exponer directamente el atributo.
     */
    public int getDificultad() {
        return dificultad;
    }

    /**
     * Devuelve enemigos sin exponer directamente el atributo.
     */
    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }
}
