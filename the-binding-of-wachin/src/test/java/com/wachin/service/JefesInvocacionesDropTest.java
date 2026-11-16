package com.wachin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wachin.model.TipoInvocacion;
import com.wachin.model.TipoObjetoSuelo;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class JefesInvocacionesDropTest {

    /** Generador que siempre devuelve el mismo valor, para fijar la tirada. */
    private static final class Tirada extends Random {
        private final double valor;
        Tirada(double valor) { this.valor = valor; }
        @Override public double nextDouble() { return valor; }
        @Override public int nextInt(int limite) { return 0; }
    }

    @Test
    public void todosLosJefesSueltanInvocacionConProbabilidadDel75PorCiento() {
        assertEquals(0.75, ObjetoSueloService.PROBABILIDAD_INVOCACION_JEFE, 0.0);
        for (int sala : new int[]{5, 10, 15, 20}) {
            var servicio = new ObjetoSueloService(new Tirada(0.74));
            var drop = servicio.crearDropInvocacionJefe(sala, 10, 10, List.of(TipoInvocacion.values()));
            assertTrue("sala " + sala, drop.isPresent());
            assertEquals(TipoObjetoSuelo.INVOCACION, drop.get().getTipo());
        }
    }

    @Test
    public void unaTiradaPorEncimaDelUmbralNoSueltaNada() {
        var servicio = new ObjetoSueloService(new Tirada(0.76));
        assertFalse(servicio.crearDropInvocacionJefe(5, 10, 10, List.of(TipoInvocacion.values())).isPresent());
    }

    @Test
    public void sinInvocacionesPermitidasNoSeCreaElObjeto() {
        var servicio = new ObjetoSueloService(new Tirada(0.0));
        assertFalse(servicio.crearDropInvocacionJefe(5, 10, 10, List.of()).isPresent());
    }
}
