package com.wachin.service;

import com.wachin.model.CartaTipo;
import com.wachin.model.Jugador;

/** Aplica una sola vez cada carta y calcula los efectos agregados sobre atributos. */
public final class MejorasService {
    public void aplicarCarta(Jugador jugador, CartaTipo carta) {
        if (jugador == null || carta == null) throw new IllegalArgumentException("Jugador y carta son obligatorios.");
        switch (carta) {
            case DANO -> jugador.aumentarBonoDanoProyectiles(carta.getValor());
            case VELOCIDAD -> jugador.aumentarVelocidad(carta.getValor());
            case VIDA_MAXIMA -> jugador.aumentarVidaMaxima(carta.getValor());
            case DANO_INVOCACIONES -> jugador.aumentarBonoDanoInvocaciones(carta.getValor());
            case SEGUNDO_ESPACIO_INVOCACION -> jugador.desbloquearSegundoEspacioInvocacion();
        }
    }
    public int calcularDanoProyectil(Jugador jugador, int danoBase) {
        return Math.max(1, danoBase + (jugador == null ? 0 : jugador.getBonoDanoProyectiles()));
    }
    public int calcularDanoInvocacion(Jugador jugador, int danoBase) {
        return Math.max(1, danoBase + (jugador == null ? 0 : jugador.getBonoDanoInvocaciones()));
    }
}
