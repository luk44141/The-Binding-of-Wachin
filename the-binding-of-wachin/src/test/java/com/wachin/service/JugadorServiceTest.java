package com.wachin.service;

import com.wachin.model.Jugador;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JugadorServiceTest {
    @Test
    public void subeCadaCienExperienciaYRecuperaTodaLaVida() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        jugador.recibirDano(60);
        JugadorService servicio = new JugadorService();

        assertEquals(100, servicio.experienciaNecesaria(1));
        assertEquals(100, servicio.experienciaNecesaria(2));
        assertEquals(0, servicio.ganarExperiencia(jugador, 99));
        assertEquals(1, servicio.ganarExperiencia(jugador, 1));
        assertEquals(2, jugador.getNivel());
        assertEquals(110, jugador.getVidaMaxima());
        assertEquals(110, jugador.getVida());
        assertEquals(0, jugador.getExperiencia());

        jugador.recibirDano(35);
        assertEquals(1, servicio.ganarExperiencia(jugador, 100));
        assertEquals(3, jugador.getNivel());
        assertEquals(120, jugador.getVidaMaxima());
        assertEquals(120, jugador.getVida());
    }

    @Test
    public void experienciaSobrantePermiteVariosNivelesEnUnaRecompensa() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        assertEquals(2, new JugadorService().ganarExperiencia(jugador, 210));
        assertEquals(3, jugador.getNivel());
        assertEquals(120, jugador.getVidaMaxima());
        assertEquals(120, jugador.getVida());
        assertEquals(10, jugador.getExperiencia());
    }
}
