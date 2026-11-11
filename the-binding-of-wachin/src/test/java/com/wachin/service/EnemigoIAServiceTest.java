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
        assertEquals(1, ia.actualizar(enemigo, 0, 0, 200, 0, 2).disparos().size());
    }
}
