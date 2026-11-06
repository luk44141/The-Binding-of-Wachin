package com.wachin.controller;

import com.wachin.view.game.PuertaVisual;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mantiene el grafo procedural de salas y su progreso. */
/**
 * Administra el grafo procedural de salas, sus salidas y las visitas realizadas.
 */
public final class MapaController {
    /**
     * Agrupa comportamiento y estado propio dentro de la arquitectura del proyecto.
     */
    public static final class SalaNodo {
        /**
         * Guarda id para que la clase mantenga su estado interno.
         */
        private final int id;
        /**
         * Guarda profundidad para que la clase mantenga su estado interno.
         */
        private final int profundidad;
        /**
         * Guarda jefe para que la clase mantenga su estado interno.
         */
        private final boolean jefe;
        /**
         * Guarda mapa para que la clase mantenga su estado interno.
         */
        private final int mapa;
        /**
         * Guarda salidas para que la clase mantenga su estado interno.
         */
        private final Map<PuertaVisual.Direccion, SalaNodo> salidas =
                new EnumMap<>(PuertaVisual.Direccion.class);

        private SalaNodo(int id, int profundidad, boolean jefe, int mapa) {
            this.id = id;
            this.profundidad = profundidad;
            this.jefe = jefe;
            this.mapa = mapa;
        }
        public int getId() { return id; }
        public int getProfundidad() { return profundidad; }
        public boolean esJefe() { return jefe; }
        public int getMapa() { return mapa; }
        public Set<PuertaVisual.Direccion> getDireccionesSalida() { return Collections.unmodifiableSet(salidas.keySet()); }
    }

    private final Set<Integer> visitadas = new HashSet<>();
    private final Map<Integer, SalaNodo> jefes = new HashMap<>();
    /**
     * Constante que define siguienteid para este componente.
     */
    private int siguienteId = 1;
    /**
     * Constante que define numerosala para este componente.
     */
    private int numeroSala;
    /**
     * Guarda actual para que la clase mantenga su estado interno.
     */
    private SalaNodo actual;

    /**
     * Prepara el estado inicial y pone en marcha este componente.
     */
    public SalaNodo iniciar() {
        SalaNodo inicio = crearSala(1, false, 1);
        entrar(inicio, null);
        return actual;
    }

    /**
     * Busca el nodo conectado a la salida e ingresa si todavía no fue visitado.
     */
    public SalaNodo entrarPor(PuertaVisual.Direccion salida) {
        SalaNodo destino = actual == null ? null : actual.salidas.get(salida);
        if (destino == null || visitadas.contains(destino.id)) return null;
        return entrar(destino, salida);
    }

    /**
     * Actualiza sala actual, registra la visita y prepara las salidas.
     */
    private SalaNodo entrar(SalaNodo sala, PuertaVisual.Direccion puertaEntrada) {
        actual = sala;
        numeroSala++;
        visitadas.add(sala.id);
        prepararSalidas(sala, puertaEntrada == null ? null : opuesta(puertaEntrada));
        return sala;
    }

    /**
     * Asigna un identificador y crea un nodo con mapa, profundidad y tipo.
     */
    private SalaNodo crearSala(int profundidad, boolean jefe, int mapa) {
        return new SalaNodo(siguienteId++, profundidad, jefe, mapa);
    }

    /**
     * Reutiliza el nodo de jefe del mapa o lo crea si todavía no existe.
     */
    private SalaNodo obtenerJefe(int mapa) {
        return jefes.computeIfAbsent(mapa, m -> crearSala(5, true, m));
    }

    /**
     * Genera conexiones según profundidad y enlaza las salas de jefe y del mapa siguiente.
     */
    private void prepararSalidas(SalaNodo sala, PuertaVisual.Direccion entrada) {
        if (!sala.salidas.isEmpty()) return;
        if (sala.jefe) {
            PuertaVisual.Direccion direccion = elegirDirecciones(entrada, 1).get(0);
            sala.salidas.put(direccion, crearSala(1, false, sala.mapa + 1));
            return;
        }
        List<PuertaVisual.Direccion> direcciones = elegirDirecciones(entrada,
                sala.profundidad >= 4 ? 1 : 2);
        if (sala.profundidad >= 4) {
            sala.salidas.put(direcciones.get(0), obtenerJefe(sala.mapa));
        } else {
            for (PuertaVisual.Direccion direccion : direcciones) {
                sala.salidas.put(direccion, crearSala(sala.profundidad + 1, false, sala.mapa));
            }
        }
    }

    /**
     * Mezcla las direcciones disponibles y toma la cantidad de salidas requerida.
     */
    private List<PuertaVisual.Direccion> elegirDirecciones(PuertaVisual.Direccion entrada, int cantidad) {
        List<PuertaVisual.Direccion> disponibles = new ArrayList<>(List.of(PuertaVisual.Direccion.values()));
        if (entrada != null) disponibles.remove(entrada);
        Collections.shuffle(disponibles);
        return new ArrayList<>(disponibles.subList(0, Math.min(cantidad, disponibles.size())));
    }

    /**
     * Devuelve la dirección contraria a la puerta de entrada.
     */
    private PuertaVisual.Direccion opuesta(PuertaVisual.Direccion direccion) {
        switch (direccion) {
            case ARRIBA: return PuertaVisual.Direccion.ABAJO;
            case ABAJO: return PuertaVisual.Direccion.ARRIBA;
            case IZQUIERDA: return PuertaVisual.Direccion.DERECHA;
            case DERECHA: return PuertaVisual.Direccion.IZQUIERDA;
            default: throw new IllegalArgumentException("Dirección inválida.");
        }
    }

    /**
     * Devuelve sala actual sin exponer directamente el atributo.
     */
    public SalaNodo getActual() { return actual; }
    /**
     * Devuelve número de sala sin exponer directamente el atributo.
     */
    public int getNumeroSala() { return numeroSala; }
    /**
     * Devuelve destino conectado sin exponer directamente el atributo.
     */
    public SalaNodo getDestino(PuertaVisual.Direccion direccion) {
        return actual == null ? null : actual.salidas.get(direccion);
    }
    /**
     * Comprueba esta condición a partir del estado actual y devuelve el resultado.
     */
    public boolean fueVisitada(SalaNodo sala) { return sala != null && visitadas.contains(sala.id); }
}
