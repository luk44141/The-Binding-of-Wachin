package com.wachin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sala {

    private final int numero;
    private final int dificultad;
    private final List<Enemigo> enemigos;

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

    public int getNumero() {
        return numero;
    }

    public int getDificultad() {
        return dificultad;
    }

    public List<Enemigo> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }
}