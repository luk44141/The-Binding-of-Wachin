package com.wachin.service;

import com.wachin.model.ObjetoSuelo;
import com.wachin.model.TipoObjetoSuelo;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjetoSueloServiceTest {
    @Test
    public void objetosPersistenEnLaSalaHastaConfirmarRecogida() {
        ObjetoSueloService servicio = new ObjetoSueloService();
        ObjetoSuelo objeto = servicio.crear(TipoObjetoSuelo.PROYECTIL, "GOTA", 4, 100, 100);
        assertEquals(1, servicio.recogerCercanos(4, 120, 100, 25).size());
        assertEquals(1, servicio.listarSala(4).size());
        assertTrue(servicio.retirar(objeto.getId()));
        assertTrue(servicio.listarSala(4).isEmpty());
    }

    @Test
    public void restaurarConservaObjetosDeSalasAnteriores() {
        ObjetoSueloService servicio = new ObjetoSueloService();
        ObjetoSuelo objeto = new ObjetoSuelo(900, TipoObjetoSuelo.CARTA, "DAMAGE", 3, 50, 60);
        servicio.restaurar(List.of(objeto));
        assertEquals(1, servicio.listarSala(3).size());
        assertTrue(servicio.listarSala(4).isEmpty());
    }
}
