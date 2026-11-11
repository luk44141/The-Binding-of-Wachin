package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.controller.MapaController;
import com.wachin.view.game.PuertaVisual;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProgresionServiceTest {
    @Test
    public void jefesSeDeterminanPorSalaGlobal() {
        assertEquals(Jefe.Tipo.SALA_10, ProgresionService.jefeDeSala(5));
        assertEquals(Jefe.Tipo.SALA_10, ProgresionService.jefeDeSala(10));
        assertEquals(Jefe.Tipo.SALA_15, ProgresionService.jefeDeSala(15));
        assertEquals(Jefe.Tipo.SALA_20, ProgresionService.jefeDeSala(20));
        assertNull(ProgresionService.recompensaDeSala(5));
    }

    @Test
    public void recompensaDeSala10AumentaMaximoYCuraElAumento() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        jugador.recibirDano(60);
        int aumento = new RecompensaService().otorgarAumentoVidaMaxima(jugador, 0.25);
        assertEquals(25, aumento);
        assertEquals(125, jugador.getVidaMaxima());
        assertEquals(65, jugador.getVida());
    }

    @Test
    public void recompensaSoloSeMarcaUnaVez() {
        ProgresionService progresion = new ProgresionService();
        assertTrue(progresion.marcarRecompensaEntregada(10));
        assertFalse(progresion.marcarRecompensaEntregada(10));
        assertNotNull(ProgresionService.recompensaDeSala(15));
    }

    @Test
    public void objetoDeRecompensaAplicaAumentoDefinidoYConservaCodigoDesconocido() throws Exception {
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        jugador.recibirDano(50);
        RecompensaService servicio = new RecompensaService();
        assertTrue(servicio.aplicarObjetoSuelo(jugador, RecompensaService.AUMENTO_VIDA_MAXIMA_25));
        assertEquals(125, jugador.getVidaMaxima());
        assertEquals(75, jugador.getVida());
        assertEquals(false, servicio.aplicarObjetoSuelo(jugador, "DESCONOCIDA"));
    }

    @Test
    public void mapaCuentaSalasGlobalesIndependientementeDeLaProfundidad() {
        ProgresionService progresion = new ProgresionService();
        MapaController mapa = new MapaController(progresion);
        MapaController.SalaNodo nodo = mapa.iniciar();
        for (int sala = 1; sala <= 20; sala++) {
            assertEquals(sala, mapa.getNumeroSala());
            assertEquals(sala % 5 == 0, nodo.esJefe());
            if (sala < 20) {
                PuertaVisual.Direccion salida = nodo.getDireccionesSalida().iterator().next();
                nodo = mapa.entrarPor(salida);
                assertNotNull(nodo);
            }
        }
    }
}
