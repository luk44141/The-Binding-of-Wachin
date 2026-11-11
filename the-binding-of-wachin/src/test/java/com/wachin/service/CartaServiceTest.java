package com.wachin.service;

import com.wachin.model.CartaTipo;
import com.wachin.model.Jugador;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CartaServiceTest {
    @Test
    public void recompensaDelJefeOfreceTresCartasBasicasDiferentes() {
        assertEquals(3, new CartaService().elegirTresCartasDiferentes().stream().distinct().count());
        assertTrue(new CartaService().elegirTresCartasDiferentes().stream().noneMatch(CartaTipo::esUnica));
    }

    @Test
    public void cartasBasicasAcumulanYLaEspecialSoloSeObtieneUnaVez() throws Exception {
        CartaService cartas = new CartaService();
        MejorasService mejoras = new MejorasService();
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        assertTrue(cartas.registrar(CartaTipo.DANO));
        mejoras.aplicarCarta(jugador, CartaTipo.DANO);
        assertTrue(cartas.registrar(CartaTipo.DANO));
        mejoras.aplicarCarta(jugador, CartaTipo.DANO);
        assertEquals(2, cartas.cantidad(CartaTipo.DANO));
        assertEquals(35, mejoras.calcularDanoProyectil(jugador, 25));
        assertTrue(cartas.registrar(CartaTipo.SEGUNDO_ESPACIO_INVOCACION));
        mejoras.aplicarCarta(jugador, CartaTipo.SEGUNDO_ESPACIO_INVOCACION);
        assertFalse(cartas.registrar(CartaTipo.SEGUNDO_ESPACIO_INVOCACION));
        assertTrue(jugador.tieneSegundoEspacioInvocacion());
    }

    @Test
    public void efectosDeVelocidadYVidaMaximaModificanElModelo() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        MejorasService mejoras = new MejorasService();
        mejoras.aplicarCarta(jugador, CartaTipo.VELOCIDAD);
        mejoras.aplicarCarta(jugador, CartaTipo.VIDA_MAXIMA);
        assertEquals(240, jugador.getVelocidad(), 0.001);
        assertEquals(120, jugador.getVidaMaxima());
        assertNotEquals(100, jugador.getVida());
    }
}
