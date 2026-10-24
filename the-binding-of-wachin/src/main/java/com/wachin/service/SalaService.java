
package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.model.Sala;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SalaService {

    private final Random random = new Random();

    private static final String[] TIPOS_ENEMIGOS = {
            "Slime",
            "Murcielago",
            "Demonio",
            "Monstruo",
            "Esqueleto",
            "Fantasma"
    };

    public boolean esSalaJefe(int numeroSala) {
        return numeroSala > 0 && numeroSala % 5 == 0;
    }

    public int obtenerNumeroMapa(int numeroSala) {
        return ((numeroSala - 1) / 5) + 1;
    }

    public Sala generarSala(int numeroSala) {
        return generarSala(
                numeroSala,
                numeroSala,
                esSalaJefe(numeroSala)
        );
    }

    public Sala generarSala(
            int identificador,
            int profundidad,
            boolean esJefe) {

        if (identificador < 1 || profundidad < 1) {
            throw new IllegalArgumentException(
                    "El identificador y la profundidad deben ser positivos."
            );
        }

        int dificultad = 1 + (profundidad - 1) / 3;
        List<Enemigo> enemigos = new ArrayList<>();

        if (!esJefe) {
            int cantidadEnemigos = Math.min(
                    4 + random.nextInt(3) + (profundidad - 1) / 2,
                    15
            );

            for (int i = 0; i < cantidadEnemigos; i++) {
                enemigos.add(crearEnemigo(dificultad));
            }
        }

        return new Sala(identificador, dificultad, enemigos);
    }

    public Jefe generarJefe(int numeroSala) {
        return generarJefe(
                numeroSala,
                obtenerNumeroMapa(numeroSala)
        );
    }

    public Jefe generarJefe(int identificador, int mapa) {
        int vida = 250 + (mapa - 1) * 100;
        int dano = 12 + (mapa - 1) * 3;

        double velocidad = Math.min(
                65 + (mapa - 1) * 3.0,
                110
        );

        try {
            return new Jefe(
                    "Guardian del mapa " + mapa,
                    vida,
                    dano,
                    "JEFE",
                    velocidad,
                    mapa,
                    "EMBESTIDA",
                    5 + mapa * 2
            );

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudo generar el jefe.",
                    e
            );
        }
    }

    private Enemigo crearEnemigo(int dificultad) {
        String nombre = TIPOS_ENEMIGOS[
                random.nextInt(TIPOS_ENEMIGOS.length)
        ];

        int vida = 30 + dificultad * 8;
        int dano = 4 + dificultad;
        double velocidad = Math.min(
                70 + dificultad * 2.0,
                180
        );

        switch (nombre) {
            case "Slime":
                vida += 15;
                velocidad *= 0.75;
                break;

            case "Murcielago":
                vida -= 5;
                velocidad *= 1.4;
                break;

            case "Demonio":
                dano += 3;
                break;

            case "Monstruo":
                vida += 25;
                velocidad *= 0.7;
                break;

            case "Esqueleto":
                dano += 1;
                velocidad *= 1.1;
                break;

            case "Fantasma":
                velocidad *= 1.2;
                break;

            default:
                break;
        }

        try {
            return new Enemigo(
                    nombre,
                    Math.max(10, vida),
                    Math.max(1, dano),
                    nombre.toUpperCase(),
                    velocidad
            );

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudo generar un enemigo.",
                    e
            );
        }
    }
}
