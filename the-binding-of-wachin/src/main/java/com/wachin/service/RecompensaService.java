package com.wachin.service;

import com.wachin.model.Jugador;

/** Aplica efectos de recompensas sobre modelos sin depender de vistas. */
public final class RecompensaService {
    public static final String AUMENTO_VIDA_MAXIMA_25 = "AUMENTO_VIDA_MAXIMA_25";

    public int otorgarAumentoVidaMaxima(Jugador jugador, double porcentajeBase) {
        if (jugador == null || !Double.isFinite(porcentajeBase) || porcentajeBase <= 0) {
            throw new IllegalArgumentException("Jugador o porcentaje de recompensa inválido.");
        }
        int aumento = Math.max(1, (int) Math.round(jugador.getVidaMaximaInicial() * porcentajeBase));
        jugador.aumentarVidaMaxima(aumento);
        return aumento;
    }

    /** Aplica recompensas recogibles reconocidas; los códigos desconocidos se dejan en el suelo. */
    public boolean aplicarObjetoSuelo(Jugador jugador, String codigo) {
        if (jugador == null || codigo == null) return false;
        if (AUMENTO_VIDA_MAXIMA_25.equals(codigo)) {
            otorgarAumentoVidaMaxima(jugador, 0.25);
            return true;
        }
        return false;
    }
}
