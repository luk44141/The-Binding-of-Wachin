package com.wachin.service;

import com.wachin.model.Jugador;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombateServiceTest {
    @Test
    public void escalaDanoPorMapaYProfundidadManteniendoElValorBase() {
        CombateService combate = new CombateService();
        assertEquals(12, combate.calcularDanoRecibido(8, 1, 1));
        assertEquals(13, combate.calcularDanoRecibido(8, 2, 1));
        assertEquals(14, combate.calcularDanoRecibido(8, 1, 3));
    }

    @Test
    public void contactoEnLaDistanciaDeSeparacionFisicaDañaAlJugador() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        ColisionService colisiones = new ColisionService();
        CombateService combate = new CombateService();
        assertTrue(colisiones.colisionaJugadorEnemigo(0, 0, 0, 0,
                49, 0, 49, 0, 27, 22));
        combate.aplicarDano(combate.calcularDanoRecibido(7, 1, 1), jugador);
        assertEquals(89, jugador.getVida());
    }
}
