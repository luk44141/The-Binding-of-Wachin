package com.wachin.service;

import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene el estado de juego y administra las entidades activas y sus recompensas.
 */
public class JuegoService {
    /**
     * Guarda jugador para que la clase mantenga su estado interno.
     */
    private Jugador jugador;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private final List<Invocacion> invocacionesActivas = new ArrayList<>();
    /**
     * Guarda jefe para que la clase mantenga su estado interno.
     */
    private Jefe jefe;

    /**
     * Crea una vista y un controlador nuevos, enlaza los controles y arranca la partida elegida.
     */
    public void iniciarPartida(Jugador jugador) {
        if (jugador == null) throw new IllegalArgumentException("El jugador no puede ser null.");
        this.jugador = jugador;
        enemigos.clear();
        invocacionesActivas.clear();
        jefe = null;
    }

    /**
     * Devuelve jugador sin exponer directamente el atributo.
     */
    public Jugador getJugador() { return jugador; }
    /**
     * Devuelve enemigos sin exponer directamente el atributo.
     */
    public List<Enemigo> getEnemigos() { return enemigos; }
    /**
     * Devuelve invocaciones activas sin exponer directamente el atributo.
     */
    public List<Invocacion> getInvocacionesActivas() { return invocacionesActivas; }
    /**
     * Devuelve jefe sin exponer directamente el atributo.
     */
    public Jefe getJefe() { return jefe; }

    /**
     * Agrega el grupo visual del enemigo al escenario.
     */
    public void agregarEnemigo(Enemigo enemigo) {
        if (enemigo == null) return;
        if (!enemigos.contains(enemigo)) enemigos.add(enemigo);
        if (enemigo instanceof Jefe) jefe = (Jefe) enemigo;
    }

    /**
     * Ejecuta la operación agregarInvocacion dentro de la responsabilidad de esta clase.
     */
    public void agregarInvocacion(Invocacion invocacion) { if (invocacion != null) invocacionesActivas.add(invocacion); }
    /**
     * Ejecuta la operación eliminarEnemigo dentro de la responsabilidad de esta clase.
     */
    public void eliminarEnemigo(Enemigo enemigo) {
        enemigos.remove(enemigo);
        if (jefe == enemigo) jefe = null;
    }
    /**
     * Ejecuta la operación eliminarInvocacion dentro de la responsabilidad de esta clase.
     */
    public void eliminarInvocacion(Invocacion invocacion) { invocacionesActivas.remove(invocacion); }
    /**
     * Comprueba que el jugador exista y conserve vida positiva.
     */
    public boolean jugadorEstaVivo() { return jugador != null && jugador.getVida() > 0; }

    /**
     * Elimina al enemigo derrotado y concede experiencia al jugador.
     */
    public void derrotarEnemigo(Enemigo enemigo, JugadorService jugadorService) {
        if (enemigo == null || jugador == null || enemigo.getVida() > 0) return;
        eliminarEnemigo(enemigo);
        jugadorService.ganarExperiencia(jugador, enemigo instanceof Jefe ? 150 : 25);
    }

}
