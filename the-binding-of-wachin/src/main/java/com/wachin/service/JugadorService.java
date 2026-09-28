package com.wachin.service;

import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.exception.ExperienciaInsuficienteException;
import com.wachin.exception.InvocacionNoDisponibleException;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;

public class JugadorService {

    public void ganarExperiencia(Jugador jugador, int experiencia) {

        if (jugador == null) {
            return;
        }

        jugador.ganarExperiencia(experiencia);
    }

    public void subirNivel(Jugador jugador, int experienciaNecesaria)
            throws ExperienciaInsuficienteException {

        if (jugador == null) {
            return;
        }

        jugador.subirNivel(experienciaNecesaria);
    }

    public void gastarEnergia(Jugador jugador, int cantidad)
            throws EnergiaInsuficienteException {

        if (jugador == null) {
            return;
        }

        jugador.gastarEnergia(cantidad);
    }

    public void recuperarEnergia(Jugador jugador, int cantidad) {

        if (jugador == null) {
            return;
        }

        jugador.recuperarEnergia(cantidad);
    }

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