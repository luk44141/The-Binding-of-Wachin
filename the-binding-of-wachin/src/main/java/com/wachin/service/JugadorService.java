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
    public void ganarExperiencia(Jugador jugador, int experiencia) {

        if (jugador == null) {
            return;
        }

        jugador.ganarExperiencia(experiencia);
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
