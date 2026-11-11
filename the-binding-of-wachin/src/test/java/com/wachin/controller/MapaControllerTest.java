package com.wachin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.wachin.service.ProgresionService;
import com.wachin.view.game.PuertaVisual;
import java.util.Set;
import org.junit.Test;

public class MapaControllerTest {

    @Test
    public void mantieneCincoProfundidadesYJefesEnSalasGlobalesAcordadas() {
        MapaController mapa = new MapaController(new ProgresionService());
        MapaController.SalaNodo actual = mapa.iniciar();

        for (int salaGlobal = 1; salaGlobal <= 20; salaGlobal++) {
            ProgresionService.UbicacionSala ubicacion = ProgresionService.ubicacionDeSala(salaGlobal);
            assertEquals(salaGlobal, mapa.getNumeroSala());
            assertEquals(salaGlobal, actual.getId());
            assertEquals(ubicacion.mapa(), actual.getMapa());
            assertEquals(ubicacion.profundidad(), actual.getProfundidad());
            assertEquals(salaGlobal % 5 == 0, actual.esJefe());
            if (actual.esJefe()) {
                assertEquals(1, actual.getDireccionesSalida().size());
                if (salaGlobal > 1) {
                    assertNotNull(actual.getDireccionEntrada());
                    assertTrue(!actual.getDireccionesSalida().contains(actual.getDireccionEntrada()));
                }
            }
            if (salaGlobal == 5 || salaGlobal == 10 || salaGlobal == 15 || salaGlobal == 20) {
                assertTrue(actual.getProfundidad() == 5);
            }

            if (salaGlobal < 20) {
                PuertaVisual.Direccion salida = actual.getDireccionesSalida().iterator().next();
                actual = mapa.entrarPor(salida);
                assertNotNull(actual);
            }
        }
    }

    @Test
    public void restaurarSalaReanudaElAvanceDeMapaDesdeLaUbicacionGlobal() {
        MapaController mapa = new MapaController(new ProgresionService());
        MapaController.SalaNodo sala = mapa.restaurarSala(15, 3, 5,
                Set.of(PuertaVisual.Direccion.DERECHA));

        assertTrue(sala.esJefe());
        assertEquals(15, mapa.getNumeroSala());
        assertEquals(4, mapa.getDestino(PuertaVisual.Direccion.DERECHA).getMapa());
        assertEquals(1, mapa.getDestino(PuertaVisual.Direccion.DERECHA).getProfundidad());
    }
}
