package com.wachin.service;

import com.wachin.model.Enemigo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemigoIAServiceTest {

    @Test
    public void persecucionAvanzaSegunVelocidadYDelta() throws Exception {
        Enemigo enemigo = new Enemigo("Poli", 10, 1, "POLI", 100);
        double[] siguiente = new EnemigoIAService().calcularPersecucion(
                enemigo, 0, 0, 300, 400, 0.5);

        assertEquals(30, siguiente[0], 0.0001);
        assertEquals(40, siguiente[1], 0.0001);
    }

    @Test
    public void persecucionNoSobrepasaAlObjetivo() throws Exception {
        Enemigo enemigo = new Enemigo("Poli", 10, 1, "POLI", 500);
        double[] siguiente = new EnemigoIAService().calcularPersecucion(
                enemigo, 0, 0, 3, 4, 1);

        assertEquals(3, siguiente[0], 0.0001);
        assertEquals(4, siguiente[1], 0.0001);
    }

    @Test
    public void tiradorSeMantieneEnRangoDisparaYRespetaSuRecarga() throws Exception {
        Enemigo enemigo = new Enemigo("Tirador", 50, 8, "TIRADOR", 100);
        EnemigoIAService ia = new EnemigoIAService();
        var disparo = ia.actualizar(enemigo, 0, 0, 200, 0, 0.1);
        assertEquals(0, disparo.x(), 0.001);
        assertEquals(1, disparo.disparos().size());
        assertTrue(disparo.disparos().get(0).velocidadX() > 0);
        assertTrue(ia.actualizar(enemigo, 0, 0, 200, 0, 0.1).disparos().isEmpty());
        assertEquals(1, ia.actualizar(enemigo, 0, 0, 200, 0, 3.1).disparos().size());
    }

    @Test
    public void elTiradorRetrocedeSiElJugadorSeAcercaDemasiado() throws Exception {
        Enemigo enemigo = new Enemigo("Tirador", 50, 8, "TIRADOR", 100);
        var accion = new EnemigoIAService().actualizar(enemigo, 300, 100, 350, 100, 0.1);
        assertTrue("Debe alejarse del jugador", accion.x() < 300);
    }

    @Test
    public void losPerseguidoresSeReparten_enAngulosDistintosCuandoEstanLejos() throws Exception {
        EnemigoIAService ia = new EnemigoIAService();
        Enemigo directo = new Enemigo("Poli", 10, 1, "POLI", 100);
        Enemigo flanco = new Enemigo("Poli", 10, 1, "POLI", 100);
        flanco.setOrdenDispersion(1);
        double[] a = ia.calcularPersecucion(directo, 100, 100, 700, 100, 0.1);
        double[] b = ia.calcularPersecucion(flanco, 100, 100, 700, 100, 0.1);
        assertEquals(100, a[1], 1e-9);
        assertTrue("El segundo enemigo debe desviarse del eje directo", Math.abs(b[1] - 100) > 0.5);
    }

    @Test
    public void alLlegarCercaTodosVanDirectosAlJugador() throws Exception {
        Enemigo flanco = new Enemigo("Poli", 10, 1, "POLI", 100);
        flanco.setOrdenDispersion(3);
        double[] p = new EnemigoIAService().calcularPersecucion(flanco, 100, 100, 150, 100, 0.1);
        assertEquals(110, p[0], 1e-6);
        assertEquals(100, p[1], 1e-6);
    }
}
