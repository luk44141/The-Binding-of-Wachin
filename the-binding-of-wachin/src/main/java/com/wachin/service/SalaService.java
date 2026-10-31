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

/** Genera enemigos usando una unica identidad visual: poli.png. */
public class SalaService {
    private final Random random = new Random();

    public boolean esSalaJefe(int numeroSala) { return numeroSala > 0 && numeroSala % 5 == 0; }
    public int obtenerNumeroMapa(int numeroSala) { return ((numeroSala - 1) / 5) + 1; }
    public Sala generarSala(int numeroSala) { return generarSala(numeroSala, numeroSala, esSalaJefe(numeroSala), obtenerNumeroMapa(numeroSala)); }

    public Sala generarSala(int identificador, int profundidad, boolean esJefe) {
        return generarSala(identificador, profundidad, esJefe, 1);
    }

    public Sala generarSala(int identificador, int profundidad, boolean esJefe, int mapa) {
        if (identificador < 1 || profundidad < 1 || mapa < 1) {
            throw new IllegalArgumentException("Identificador, profundidad y mapa deben ser positivos.");
        }
        int nivel = (mapa - 1) * 4 + profundidad;
        List<Enemigo> enemigos = new ArrayList<>();
        if (!esJefe) {
            // Aumenta la cantidad y los atributos en cada mapa y profundidad.
            int cantidad = Math.min(4 + random.nextInt(3) + nivel / 2, 14);
            for (int i = 0; i < cantidad; i++) enemigos.add(crearEnemigo(nivel));
        }
        return new Sala(identificador, Math.max(1, nivel), enemigos);
    }

    public Jefe generarJefe(int numeroSala) { return generarJefe(numeroSala, obtenerNumeroMapa(numeroSala)); }

    public Jefe generarJefe(int identificador, int mapa) {
        int nivel = Math.max(1, mapa);
        int vida = 1500 + (nivel - 1) * 250;
        int dano = 18 + (nivel - 1) * 8;
        double velocidad = Math.min(105 + (nivel - 1) * 13.0, 235);
        try {
            return new Jefe("Guardian del mapa " + mapa, vida, dano, "JEFE", velocidad,
                    nivel, "EMBESTIDA", 12 + nivel * 7);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo generar el jefe.", e);
        }
    }

    private Enemigo crearEnemigo(int nivel) {
        // Antes se elegian seis tipos con sprites inexistentes. Ahora todos son Poli.
        int vida = 38 + nivel * 17;
        int dano = 7 + nivel * 3;
        double velocidad = Math.min(85 + nivel * 7.0, 250);
        try {
            return new Enemigo("Poli", vida, dano, "POLI", velocidad);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo generar un enemigo Poli.", e);
        }
    }
}
