package com.wachin.service;

import com.wachin.model.Sala;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SalaServiceTest {
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
