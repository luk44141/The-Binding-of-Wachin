package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
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

    public Sala generarSala(int numeroSala) {
        if (numeroSala < 1) {
            throw new IllegalArgumentException(
                    "El número de sala debe ser positivo."
            );
        }

        int dificultad = 1 + (numeroSala - 1) / 3;

        int cantidadEnemigos = Math.min(
                4 + random.nextInt(3) + (numeroSala - 1) / 2,
                15
        );

        List<Enemigo> enemigos = new ArrayList<>();

        for (int i = 0; i < cantidadEnemigos; i++) {
            enemigos.add(crearEnemigo(dificultad));
        }

        return new Sala(numeroSala, dificultad, enemigos);
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