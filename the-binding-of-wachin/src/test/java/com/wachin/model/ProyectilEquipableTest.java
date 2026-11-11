package com.wachin.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProyectilEquipableTest {
    @Test
    public void tiposConservanDanoYRecuperanPorCodigo() {
        assertNotEquals(ProyectilEquipable.LAGRIMA.getDano(), ProyectilEquipable.GOTA.getDano());
        assertEquals(ProyectilEquipable.LAGRIMA, ProyectilEquipable.desdeCodigo("lagrima"));
    }

    @Test
    public void equipamientoDelJugadorEmpiezaEnGotaYSePuedeCambiar() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        assertEquals(ProyectilEquipable.GOTA, jugador.getProyectilEquipado());
        jugador.equiparProyectil(ProyectilEquipable.LAGRIMA);
        assertEquals(ProyectilEquipable.LAGRIMA, jugador.getProyectilEquipado());
    }
}
