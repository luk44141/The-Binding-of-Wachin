package com.wachin.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JugadorCadenciaTest {

    @Test
    public void alIniciarPuedeDispararYLuegoDebeEsperar() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        assertTrue(jugador.puedeDisparar());
        jugador.iniciarRecargaDisparo(0.24);
        assertFalse(jugador.puedeDisparar());
        jugador.reducirRecargaDisparo(0.1);
        assertFalse(jugador.puedeDisparar());
        jugador.reducirRecargaDisparo(0.2);
        assertTrue(jugador.puedeDisparar());
        assertEquals(0, jugador.getRecargaDisparo(), 0.0);
    }

    @Test
    public void ignoraTiemposNegativosYRechazaRecargasInvalidas() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        jugador.iniciarRecargaDisparo(1);
        jugador.reducirRecargaDisparo(-5);
        assertEquals(1, jugador.getRecargaDisparo(), 0.0);
        try {
            jugador.iniciarRecargaDisparo(-1);
            org.junit.Assert.fail("Debía rechazar una recarga negativa.");
        } catch (IllegalArgumentException esperado) {
            assertEquals(1, jugador.getRecargaDisparo(), 0.0);
        }
    }
}
