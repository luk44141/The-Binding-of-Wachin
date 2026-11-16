package com.wachin.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EstadisticasPartidaTest {

    @Test
    public void cuentaEnemigosJefesYDano() {
        EstadisticasPartida e = new EstadisticasPartida();
        e.registrarEnemigoDerrotado(false);
        e.registrarEnemigoDerrotado(true);
        e.registrarDanoRecibido(12);
        e.registrarDanoRecibido(-4);
        assertEquals(2, e.getEnemigosDerrotados());
        assertEquals(1, e.getJefesDerrotados());
        assertEquals(12, e.getDanoRecibido());
    }

    @Test
    public void formateaElTiempoJugado() {
        EstadisticasPartida e = new EstadisticasPartida();
        e.sumarTiempo(65.9);
        e.sumarTiempo(-10);
        e.sumarTiempo(Double.NaN);
        assertEquals("1:05", e.tiempoFormateado());
    }
}
