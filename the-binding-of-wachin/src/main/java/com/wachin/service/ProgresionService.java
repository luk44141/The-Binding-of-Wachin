package com.wachin.service;

import com.wachin.model.Jefe;
import java.util.HashSet;
import java.util.Set;

/** Única fuente de verdad para el conteo global, jefes y tipo de recompensa. */
public final class ProgresionService {
    public enum Recompensa { AUMENTAR_VIDA_MAXIMA, ELEGIR_CARTA, SIN_RECOMPENSA_DEFINIDA }
    public record UbicacionSala(int mapa, int profundidad) { }

    private int numeroSalaGlobal;
    private final Set<Integer> recompensasEntregadas = new HashSet<>();

    public int registrarEntradaSala() { return ++numeroSalaGlobal; }
    public int getNumeroSalaGlobal() { return numeroSalaGlobal; }
    public void restaurarNumeroSalaGlobal(int numero) {
        if (numero < 0) throw new IllegalArgumentException("El número global de sala no puede ser negativo.");
        numeroSalaGlobal = numero;
    }
    public boolean marcarRecompensaEntregada(int sala) {
        if (sala < 1) return false;
        return recompensasEntregadas.add(sala);
    }
    public boolean recompensaEntregada(int sala) { return recompensasEntregadas.contains(sala); }
    /** Guarda por separado si ya se genero la carta de una sala par usando su numero negativo. */
    public boolean marcarCartaGenerada(int sala) {
        return sala > 0 && recompensasEntregadas.add(-sala);
    }
    public boolean cartaGenerada(int sala) {
        return sala > 0 && recompensasEntregadas.contains(-sala);
    }
    public Set<Integer> getRecompensasEntregadas() { return Set.copyOf(recompensasEntregadas); }
    public void restaurarRecompensasEntregadas(Set<Integer> salas) {
        recompensasEntregadas.clear();
        if (salas != null) recompensasEntregadas.addAll(salas);
    }

    public static Jefe.Tipo jefeDeSala(int numeroGlobal) {
        return switch (numeroGlobal) {
            case 5 -> Jefe.Tipo.SALA_10; // Conserva el jefe original de la sala 5.
            case 10 -> Jefe.Tipo.EMISOR_RAYOS;
            case 15 -> Jefe.Tipo.SALA_15;
            case 20 -> Jefe.Tipo.SALA_20;
            default -> numeroGlobal >= 25 && (numeroGlobal - 25) % 20 == 0
                    ? Jefe.Tipo.EMISOR_RAYOS : null;
        };
    }

    public static boolean esCierreDeProgresion(int numeroGlobal) { return false; }

    /** La profundidad se reinicia cada cinco salas globales; el mapa avanza al siguiente bloque. */
    public static UbicacionSala ubicacionDeSala(int numeroGlobal) {
        if (numeroGlobal < 1) throw new IllegalArgumentException("El número global de sala debe ser positivo.");
        return new UbicacionSala(((numeroGlobal - 1) / 5) + 1, ((numeroGlobal - 1) % 5) + 1);
    }

    public static boolean esFinDeMapa(int numeroGlobal) {
        return numeroGlobal > 0 && numeroGlobal % 5 == 0;
    }

    public static Recompensa recompensaDeSala(int numeroGlobal) {
        return numeroGlobal == 10 ? Recompensa.AUMENTAR_VIDA_MAXIMA : null;
    }
}
