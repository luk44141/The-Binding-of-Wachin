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
/**
 * Genera salas, enemigos y jefes con atributos escalados por dificultad.
 */
public class SalaService {
    private final Random random = new Random();

    /**
     * Ejecuta la operación esSalaJefe dentro de la responsabilidad de esta clase.
     */
    public boolean esSalaJefe(int numeroSala) { return ProgresionService.jefeDeSala(numeroSala) != null; }
    /**
     * Ejecuta la operación obtenerNumeroMapa dentro de la responsabilidad de esta clase.
     */
    public int obtenerNumeroMapa(int numeroSala) { return ProgresionService.ubicacionDeSala(numeroSala).mapa(); }
    /**
     * Calcula el nivel y genera enemigos escalados, excepto en una sala de jefe.
     */
    public Sala generarSala(int numeroSala) { return generarSala(numeroSala, numeroSala, esSalaJefe(numeroSala), obtenerNumeroMapa(numeroSala)); }

    /**
     * Calcula el nivel y genera enemigos escalados, excepto en una sala de jefe.
     */
    public Sala generarSala(int identificador, int profundidad, boolean esJefe) {
        return generarSala(identificador, profundidad, esJefe, 1);
    }

    /**
     * Calcula el nivel y genera enemigos escalados, excepto en una sala de jefe.
     */
    public Sala generarSala(int identificador, int profundidad, boolean esJefe, int mapa) {
        if (identificador < 1 || profundidad < 1 || mapa < 1) {
            throw new IllegalArgumentException("Identificador, profundidad y mapa deben ser positivos.");
        }
        int nivel = (mapa - 1) * 5 + profundidad;
        List<Enemigo> enemigos = new ArrayList<>();
        if (!esJefe) {
            // Aumenta la cantidad y los atributos en cada mapa y profundidad.
            int cantidad = Math.min(4 + random.nextInt(3) + nivel / 2, 14);
            for (int i = 0; i < cantidad; i++) enemigos.add(crearEnemigo(nivel, i % 4 == 3));
        }
        return new Sala(identificador, Math.max(1, nivel), enemigos);
    }

    /**
     * Construye un jefe cuyos atributos escalan con el mapa.
     */
    public Jefe generarJefe(int numeroSala) { return generarJefe(numeroSala, obtenerNumeroMapa(numeroSala)); }

    /**
     * Construye un jefe cuyos atributos escalan con el mapa.
     */
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

    /** Construye un jefe definido para su hito global, con atributos propios. */
    public Jefe generarJefe(com.wachin.model.Jefe.Tipo tipo) {
        if (tipo == null) throw new IllegalArgumentException("El tipo de jefe es obligatorio.");
        int vida;
        int dano;
        double velocidad;
        String nombre;
        String especial;
        int extra;
        switch (tipo) {
            case SALA_10 -> { vida = 1500; dano = 18; velocidad = 105; nombre = "Guardián"; especial = "EMBESTIDA"; extra = 19; }
            case SALA_15 -> { vida = 2600; dano = 24; velocidad = 82; nombre = "Artillero"; especial = "RAFAGA_RADIAL_EMBESTIDA"; extra = 24; }
            case SALA_20 -> { vida = 4600; dano = 32; velocidad = 135; nombre = "Palinni, el Incesante"; especial = "DOS_FASES_ZONAS_DE_IMPACTO"; extra = 32; }
            default -> throw new IllegalArgumentException("Tipo de jefe desconocido.");
        }
        try {
            return new Jefe(nombre, vida, dano, "JEFE", velocidad,
                    tipo.ordinal() + 1, especial, extra, tipo);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo generar el jefe.", e);
        }
    }

    /**
     * Crea un enemigo Poli con vida, daño y velocidad determinados por el nivel.
     */
    private Enemigo crearEnemigo(int nivel, boolean aDistancia) {
        /**
         * Guarda vida para que la clase mantenga su estado interno.
         */
        int vida = 38 + nivel * 17;
        /**
         * Guarda dano para que la clase mantenga su estado interno.
         */
        int dano = 7 + nivel * 3;
        double velocidad = Math.min(85 + nivel * 7.0, 250);
        try {
            return new Enemigo(aDistancia ? "Tirador" : "Poli", vida, dano,
                    aDistancia ? "TIRADOR" : "POLI", velocidad);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo generar un enemigo Poli.", e);
        }
    }
}
