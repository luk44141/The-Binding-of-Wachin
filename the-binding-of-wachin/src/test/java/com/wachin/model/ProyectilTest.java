package com.wachin.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProyectilTest {
    @Test
    public void modeloAvanzaYConservaLaPosicionAnteriorParaColisionContinua() {
        Proyectil proyectil = new Proyectil(10, 20, 100, -40, 12,
                true, Proyectil.Origen.ENEMIGO);
        proyectil.actualizar(0.5);
        assertEquals(60, proyectil.getX(), 0.001);
        assertEquals(0, proyectil.getY(), 0.001);
        assertEquals(10, proyectil.getXAnterior(), 0.001);
        assertEquals(20, proyectil.getYAnterior(), 0.001);
        assertTrue(proyectil.esHostil());
        assertEquals(12, proyectil.getDano());
    }
}
