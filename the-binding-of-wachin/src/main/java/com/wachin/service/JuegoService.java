package com.wachin.service;

import com.wachin.model.Entidad;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import java.util.ArrayList;
import java.util.List;

public class JuegoService {
    private Jugador jugador;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private final List<Invocacion> invocacionesActivas = new ArrayList<>();
    private Jefe jefe;
    private boolean partidaTerminada;
    private boolean partidaGanada;

    public void iniciarPartida(Jugador jugador) {
        if (jugador == null) throw new IllegalArgumentException("El jugador no puede ser null.");
        this.jugador = jugador;
        enemigos.clear();
        invocacionesActivas.clear();
        jefe = null;
        partidaTerminada = false;
        partidaGanada = false;
    }

    public Jugador getJugador() { return jugador; }
    public List<Enemigo> getEnemigos() { return enemigos; }
    public List<Invocacion> getInvocacionesActivas() { return invocacionesActivas; }
    public Jefe getJefe() { return jefe; }

    public void agregarEnemigo(Enemigo enemigo) {
        if (enemigo == null) return;
        if (!enemigos.contains(enemigo)) enemigos.add(enemigo);
        if (enemigo instanceof Jefe) jefe = (Jefe) enemigo;
    }

    public void agregarJefe(Jefe jefe) { agregarEnemigo(jefe); }
    public void agregarInvocacion(Invocacion invocacion) { if (invocacion != null) invocacionesActivas.add(invocacion); }
    public void eliminarEnemigo(Enemigo enemigo) {
        enemigos.remove(enemigo);
        if (jefe == enemigo) jefe = null;
    }
    public void eliminarInvocacion(Invocacion invocacion) { invocacionesActivas.remove(invocacion); }
    public void limpiarEnemigos() { enemigos.clear(); jefe = null; }
    public boolean jugadorEstaVivo() { return jugador != null && jugador.getVida() > 0; }
    public boolean hayEnemigos() { return !enemigos.isEmpty(); }
    public boolean salaCompletada() { return enemigos.isEmpty(); }

    public void verificarEstado() {
        if (!jugadorEstaVivo()) { partidaTerminada = true; partidaGanada = false; }
        else if (salaCompletada()) { partidaTerminada = true; partidaGanada = true; }
    }

    public boolean isPartidaTerminada() { return partidaTerminada; }
    public boolean isPartidaGanada() { return partidaGanada; }

    public void derrotarEnemigo(Enemigo enemigo, JugadorService jugadorService) {
        if (enemigo == null || jugador == null || enemigo.getVida() > 0) return;
        eliminarEnemigo(enemigo);
        jugadorService.ganarExperiencia(jugador, enemigo instanceof Jefe ? 150 : 25);
    }

    public List<Entidad> obtenerEntidadesActivas() {
        List<Entidad> entidades = new ArrayList<>();
        if (jugador != null) entidades.add(jugador);
        entidades.addAll(enemigos); // El jefe ya esta en esta lista; no duplicarlo.
        entidades.addAll(invocacionesActivas);
        return entidades;
    }
}
