package com.wachin.service;

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

    public void iniciarPartida(Jugador jugador) {
        if (jugador == null) throw new IllegalArgumentException("El jugador no puede ser null.");
        this.jugador = jugador;
        enemigos.clear();
        invocacionesActivas.clear();
        jefe = null;
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

    public void agregarInvocacion(Invocacion invocacion) { if (invocacion != null) invocacionesActivas.add(invocacion); }
    public void eliminarEnemigo(Enemigo enemigo) {
        enemigos.remove(enemigo);
        if (jefe == enemigo) jefe = null;
    }
    public void eliminarInvocacion(Invocacion invocacion) { invocacionesActivas.remove(invocacion); }
    public boolean jugadorEstaVivo() { return jugador != null && jugador.getVida() > 0; }

    public void derrotarEnemigo(Enemigo enemigo, JugadorService jugadorService) {
        if (enemigo == null || jugador == null || enemigo.getVida() > 0) return;
        eliminarEnemigo(enemigo);
        jugadorService.ganarExperiencia(jugador, enemigo instanceof Jefe ? 150 : 25);
    }

}
