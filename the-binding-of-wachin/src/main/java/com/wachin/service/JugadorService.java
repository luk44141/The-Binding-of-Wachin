package com.wachin.service;

import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.exception.ExperienciaInsuficienteException;
import com.wachin.exception.InvocacionNoDisponibleException;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;

/**
 * Aplica reglas de experiencia, energía e invocaciones delegando cambios al modelo Jugador.
 */
public class JugadorService {

    /**
     * Suma experiencia si la cantidad recibida es positiva.
     */
    public int ganarExperiencia(Jugador jugador, int experiencia) {
        if (jugador == null || experiencia <= 0) return 0;
        jugador.ganarExperiencia(experiencia);
        int nivelesGanados = 0;
        while (jugador.getExperiencia() >= experienciaNecesaria(jugador.getNivel())) {
            int necesaria = experienciaNecesaria(jugador.getNivel());
            try { jugador.subirNivel(necesaria); }
            catch (ExperienciaInsuficienteException e) {
                throw new IllegalStateException("La experiencia cambió durante el ascenso de nivel.", e);
            }
            jugador.aumentarVidaMaxima(10);
            jugador.restaurarVidaCompleta();
            nivelesGanados++;
        }
        return nivelesGanados;
    }

    /** Cada nivel requiere 100 XP: con 50 XP por sala se sube un nivel cada dos salas. */
    public int experienciaNecesaria(int nivelActual) {
        if (nivelActual < 1) throw new IllegalArgumentException("El nivel debe ser positivo.");
        return 100;
    }

    /**
     * Comprueba la experiencia, aumenta el nivel y descuenta el costo requerido.
     */
    public void subirNivel(Jugador jugador, int experienciaNecesaria)
            throws ExperienciaInsuficienteException {

        if (jugador == null) {
            return;
        }

        jugador.subirNivel(experienciaNecesaria);
    }

    /**
     * Valida el costo y descuenta energía disponible.
     */
    public void gastarEnergia(Jugador jugador, int cantidad)
            throws EnergiaInsuficienteException {

        if (jugador == null) {
            return;
        }

        jugador.gastarEnergia(cantidad);
    }

    /**
     * Recupera energía sin exceder el máximo del jugador.
     */
    public void recuperarEnergia(Jugador jugador, int cantidad) {

        if (jugador == null) {
            return;
        }

        jugador.recuperarEnergia(cantidad);
    }

    /**
     * Valida los objetos recibidos y descuenta la energía de la invocación.
     */
    public void usarInvocacion(Jugador jugador, Invocacion invocacion)
            throws EnergiaInsuficienteException,
                   InvocacionNoDisponibleException {

        if (jugador == null || invocacion == null) {
            throw new InvocacionNoDisponibleException(
                    "La invocacion no esta disponible."
            );
        }

        jugador.gastarEnergia(invocacion.getCostoEnergia());
    }
}
