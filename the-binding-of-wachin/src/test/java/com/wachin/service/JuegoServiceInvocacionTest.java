package com.wachin.service;

import com.wachin.exception.InvocacionNoDisponibleException;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.model.TipoInvocacion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class JuegoServiceInvocacionTest {
    @Test
    public void unEspacioPermiteIntercambioYBloqueaDuplicados() throws Exception {
        JuegoService juego = new JuegoService();
        juego.iniciarPartida(new Jugador("Wachin", 100, 25, 100));
        Invocacion gusano = new Invocacion(TipoInvocacion.GUSANO);
        assertNull(juego.equiparInvocacion(gusano, 0));
        try {
            juego.equiparInvocacion(new Invocacion(TipoInvocacion.GUSANO), 0);
            throw new AssertionError("No debía aceptar una copia en el único espacio.");
        } catch (InvocacionNoDisponibleException esperado) { }
        Invocacion murcielago = new Invocacion(TipoInvocacion.MURCIELAGO);
        assertSame(gusano, juego.equiparInvocacion(murcielago, 0));
        assertEquals(1, juego.getInvocacionesActivas().size());
    }

    @Test
    public void segundoEspacioPermiteDuplicadosYReemplazoPorIndice() throws Exception {
        JuegoService juego = new JuegoService();
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        jugador.desbloquearSegundoEspacioInvocacion();
        juego.iniciarPartida(jugador);
        Invocacion primera = new Invocacion(TipoInvocacion.ROBOT);
        Invocacion segunda = new Invocacion(TipoInvocacion.ROBOT);
        juego.equiparInvocacion(primera, 0);
        juego.equiparInvocacion(segunda, 1);
        Invocacion reemplazo = new Invocacion(TipoInvocacion.GUSANO);
        assertSame(segunda, juego.equiparInvocacion(reemplazo, 1));
        assertSame(reemplazo, juego.getInvocacionesActivas().get(1));
    }
}
