package com.wachin.service;

import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import com.wachin.exception.InvocacionNoDisponibleException;

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
    public List<Invocacion> getInvocacionesActivas() { return Collections.unmodifiableList(invocacionesActivas); }
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
    public void agregarInvocacion(Invocacion invocacion) {
        if (invocacion == null) return;
        int espacio = obtenerEspacioLibre();
        if (espacio < 0) throw new IllegalStateException("No hay espacios libres para invocaciones.");
        try { equiparInvocacion(invocacion, espacio); }
        catch (InvocacionNoDisponibleException e) { throw new IllegalStateException(e.getMessage(), e); }
    }
    public Invocacion equiparInvocacion(Invocacion invocacion, int espacio)
            throws InvocacionNoDisponibleException {
        if (jugador == null || invocacion == null) throw new InvocacionNoDisponibleException("Invocación no disponible.");
        int limite = jugador.tieneSegundoEspacioInvocacion() ? 2 : 1;
        if (espacio < 0 || espacio >= limite) throw new InvocacionNoDisponibleException("Espacio de invocación inválido.");
        if (limite == 1 && !invocacionesActivas.isEmpty()
                && invocacionesActivas.getFirst().getTipoInvocacion() == invocacion.getTipoInvocacion()) {
            throw new InvocacionNoDisponibleException("Ese tipo de invocación ya ocupa el único espacio.");
        }
        if (espacio < invocacionesActivas.size()) return invocacionesActivas.set(espacio, invocacion);
        if (espacio == invocacionesActivas.size()) { invocacionesActivas.add(invocacion); return null; }
        throw new InvocacionNoDisponibleException("No se pueden dejar espacios vacíos entre invocaciones.");
    }
    public int obtenerEspacioLibre() {
        int limite = jugador != null && jugador.tieneSegundoEspacioInvocacion() ? 2 : 1;
        return invocacionesActivas.size() < limite ? invocacionesActivas.size() : -1;
    }
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
    public int derrotarEnemigo(Enemigo enemigo, JugadorService jugadorService) {
        if (enemigo == null || jugador == null || enemigo.getVida() > 0) return 0;
        eliminarEnemigo(enemigo);
        return jugadorService.ganarExperiencia(jugador, enemigo instanceof Jefe ? 150 : 25);
    }

}
