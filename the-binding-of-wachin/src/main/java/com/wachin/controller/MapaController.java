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
import com.wachin.service.ProgresionService;

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
        private int numeroGlobal;
        /**
         * Guarda mapa para que la clase mantenga su estado interno.
         */
        private final int mapa;
        /**
         * Guarda salidas para que la clase mantenga su estado interno.
         */
        private final Map<PuertaVisual.Direccion, SalaNodo> salidas =
                new EnumMap<>(PuertaVisual.Direccion.class);
        private PuertaVisual.Direccion entrada;

        private SalaNodo(int id, int profundidad, int mapa) {
            this.id = id;
            this.profundidad = profundidad;
            this.mapa = mapa;
        }
        public int getId() { return id; }
        public int getProfundidad() { return profundidad; }
        public boolean esJefe() { return ProgresionService.jefeDeSala(numeroGlobal) != null; }
        public int getNumeroGlobal() { return numeroGlobal; }
        public int getMapa() { return mapa; }
        public PuertaVisual.Direccion getDireccionEntrada() { return entrada; }
        public Set<PuertaVisual.Direccion> getDireccionesSalida() { return Collections.unmodifiableSet(salidas.keySet()); }
    }

    private final Set<Integer> visitadas = new HashSet<>();
    private final ProgresionService progresion;
    /**
     * Constante que define siguienteid para este componente.
     */
    private int siguienteId = 1;
    /**
     * Constante que define numerosala para este componente.
     */
    /**
     * Guarda actual para que la clase mantenga su estado interno.
     */
    private SalaNodo actual;

    public MapaController() { this(new ProgresionService()); }

    public MapaController(ProgresionService progresion) {
        if (progresion == null) throw new IllegalArgumentException("El servicio de progresión es obligatorio.");
        this.progresion = progresion;
    }

    /**
     * Prepara el estado inicial y pone en marcha este componente.
     */
    public SalaNodo iniciar() {
        SalaNodo inicio = crearSala(1, 1, 1);
        entrar(inicio, null);
        return actual;
    }

    /** Reconstruye el nodo actual y continúa el grafo desde el estado guardado. */
    public SalaNodo restaurarSala(int numeroGlobal, int mapa, int profundidad,
            Set<PuertaVisual.Direccion> direcciones) {
        return restaurarSala(numeroGlobal, mapa, profundidad, direcciones, null);
    }

    public SalaNodo restaurarSala(int numeroGlobal, int mapa, int profundidad,
            Set<PuertaVisual.Direccion> direcciones, PuertaVisual.Direccion entrada) {
        if (numeroGlobal < 1 || mapa < 1 || profundidad < 1) {
            throw new IllegalArgumentException("Progreso de mapa guardado inválido.");
        }
        ProgresionService.UbicacionSala ubicacion = ProgresionService.ubicacionDeSala(numeroGlobal);
        if (mapa != ubicacion.mapa() || profundidad != ubicacion.profundidad()) {
            throw new IllegalArgumentException("El mapa y la profundidad no coinciden con la sala global guardada.");
        }
        progresion.restaurarNumeroSalaGlobal(numeroGlobal);
        siguienteId = numeroGlobal + 1;
        SalaNodo sala = new SalaNodo(numeroGlobal, profundidad, mapa);
        sala.numeroGlobal = numeroGlobal;
        sala.entrada = entrada;
        actual = sala;
        visitadas.clear();
        visitadas.add(sala.id);
        if (direcciones != null && !direcciones.isEmpty()) {
            for (PuertaVisual.Direccion direccion : direcciones) {
                sala.salidas.put(direccion, crearSalaSiguiente());
            }
        } else {
            prepararSalidas(sala, null);
        }
        return sala;
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
        sala.numeroGlobal = progresion.registrarEntradaSala();
        sala.entrada = puertaEntrada == null ? null : opuesta(puertaEntrada);
        visitadas.add(sala.id);
        prepararSalidas(sala, sala.entrada);
        return sala;
    }

    /**
     * Asigna un identificador y crea un nodo con mapa, profundidad y tipo.
     */
    private SalaNodo crearSala(int numeroGlobal, int profundidad, int mapa) {
        siguienteId = Math.max(siguienteId, numeroGlobal + 1);
        SalaNodo sala = new SalaNodo(numeroGlobal, profundidad, mapa);
        sala.numeroGlobal = numeroGlobal;
        return sala;
    }

    /**
     * Genera conexiones según profundidad y enlaza las salas de jefe y del mapa siguiente.
     */
    private void prepararSalidas(SalaNodo sala, PuertaVisual.Direccion entrada) {
        if (!sala.salidas.isEmpty()) return;
        List<PuertaVisual.Direccion> direcciones = elegirDirecciones(entrada,
                progresion.esFinDeMapa(sala.numeroGlobal) ? 1 : 2);
        if (progresion.esFinDeMapa(sala.numeroGlobal)) {
            sala.salidas.put(direcciones.get(0), crearSalaSiguiente());
        } else {
            for (PuertaVisual.Direccion direccion : direcciones) {
                sala.salidas.put(direccion, crearSalaSiguiente());
            }
        }
    }

    private SalaNodo crearSalaSiguiente() {
        ProgresionService.UbicacionSala ubicacion = ProgresionService.ubicacionDeSala(
                progresion.getNumeroSalaGlobal() + 1);
        int siguienteGlobal = progresion.getNumeroSalaGlobal() + 1;
        return crearSala(siguienteGlobal, ubicacion.profundidad(), ubicacion.mapa());
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
    public int getNumeroSala() { return progresion.getNumeroSalaGlobal(); }
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
