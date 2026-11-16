package com.wachin.service;

import com.wachin.model.Sala;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SalaServiceTest {
    @Test
    public void todosLosJefesTienenQuinientosPuntosDeVidaAdicionales() {
        SalaService servicio = new SalaService();
        assertEquals(2400, servicio.generarJefe(com.wachin.model.Jefe.Tipo.SALA_10).getVidaMaxima());
        assertEquals(3100, servicio.generarJefe(com.wachin.model.Jefe.Tipo.EMISOR_RAYOS).getVidaMaxima());
        assertEquals(2000, servicio.generarJefe(com.wachin.model.Jefe.Tipo.SALA_15).getVidaMaxima());
        assertEquals(5100, servicio.generarJefe(com.wachin.model.Jefe.Tipo.SALA_20).getVidaMaxima());
    }

    @Test
    public void dificultadUsaLaUbicacionGlobalYLosNormalesIncluyenAlTirador() {
        SalaService salas = new SalaService();
        for (int global = 1; global <= 20; global++) {
            ProgresionService.UbicacionSala ubicacion = ProgresionService.ubicacionDeSala(global);
            boolean jefe = ProgresionService.jefeDeSala(global) != null;
            Sala sala = salas.generarSala(global, ubicacion.profundidad(), jefe, ubicacion.mapa());
            assertEquals((ubicacion.mapa() - 1) * 5 + ubicacion.profundidad(), sala.getDificultad());
            if (jefe) assertTrue(sala.getEnemigos().isEmpty());
            else {
                assertFalse(sala.getEnemigos().isEmpty());
                assertTrue(sala.getEnemigos().stream().anyMatch(e -> "POLI".equals(e.getTipoEnemigo())));
                assertTrue(sala.getEnemigos().stream().anyMatch(e -> "TIRADOR".equals(e.getTipoEnemigo())));
            }
        }
    }
}
