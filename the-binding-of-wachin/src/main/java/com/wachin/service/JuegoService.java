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

    private final List<Enemigo> enemigos;
    private final List<Invocacion> invocacionesActivas;

    private Jefe jefe;

    public JuegoService() {

        enemigos = new ArrayList<>();
        invocacionesActivas = new ArrayList<>();
    }

    public void iniciarPartida(Jugador jugador) {

        this.jugador = jugador;

        enemigos.clear();
        invocacionesActivas.clear();
        jefe = null;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    public List<Invocacion> getInvocacionesActivas() {
        return invocacionesActivas;
    }

    public Jefe getJefe() {
        return jefe;
    }

    public void agregarEnemigo(Enemigo enemigo) {

        if (enemigo != null) {
            enemigos.add(enemigo);
        }
    }

    public void agregarJefe(Jefe jefe) {

        this.jefe = jefe;
    }

    public void agregarInvocacion(Invocacion invocacion) {

        if (invocacion != null) {
            invocacionesActivas.add(invocacion);
        }
    }

    public void eliminarEnemigo(Enemigo enemigo) {

        enemigos.remove(enemigo);
    }

    public void eliminarInvocacion(Invocacion invocacion) {

        invocacionesActivas.remove(invocacion);
    }

    public boolean jugadorEstaVivo() {

        return jugador != null && jugador.getVida() > 0;
    }

    public boolean hayEnemigos() {

        return !enemigos.isEmpty();
    }

    public boolean salaCompletada() {

        return enemigos.isEmpty() && jefe == null;
    }

    public void eliminarEntidadesDerrotadas() {

        enemigos.removeIf(enemigo -> enemigo.getVida() <= 0);

        if (jefe != null && jefe.getVida() <= 0) {
            jefe = null;
        }

        invocacionesActivas.removeIf(
                invocacion -> invocacion.getVida() <= 0
        );
    }

    public List<Entidad> obtenerEntidadesActivas() {

        List<Entidad> entidades = new ArrayList<>();

        if (jugador != null) {
            entidades.add(jugador);
        }

        entidades.addAll(enemigos);

        if (jefe != null) {
            entidades.add(jefe);
        }

        entidades.addAll(invocacionesActivas);

        return entidades;
    }
}