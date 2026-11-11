package com.wachin.service;

import com.wachin.model.Proyectil;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ColisionServiceTest {
    @Test
    public void movimientoNoPuedeAtravesarUnObstaculo() {
        ColisionService servicio = new ColisionService();
        var resultado = servicio.resolverMovimiento(0, 0, 100, 0, 10,
                List.of(new ColisionService.Obstaculo(50, 0, 10)), -100, 100, -100, 100);
        assertEquals(0, resultado.x(), 0.001);
        assertEquals(0, resultado.y(), 0.001);
    }

    @Test
    public void movimientoPuedeDeslizarsePorUnEjeLibre() {
        ColisionService servicio = new ColisionService();
        var resultado = servicio.resolverMovimiento(0, 0, 100, 30, 10,
                List.of(new ColisionService.Obstaculo(50, 0, 10)), -100, 100, -100, 100);
        assertEquals(0, resultado.x(), 0.001);
        assertEquals(30, resultado.y(), 0.001);
    }

    @Test
    public void obstaculoYaSuperpuestoPermiteSepararse() {
        ColisionService servicio = new ColisionService();
        var resultado = servicio.resolverMovimiento(0, 0, -10, 0, 10,
                List.of(new ColisionService.Obstaculo(15, 0, 10)), -100, 100, -100, 100);
        assertEquals(-10, resultado.x(), 0.001);
    }

    @Test
    public void contactoEnElUmbralFisicoCuentaComoImpacto() {
        ColisionService servicio = new ColisionService();
        assertTrue(servicio.segmentosSeCruzan(0, 0, 0, 0,
                51, 0, 51, 0, 51));
    }

    @Test
    public void proyectilDetectaImpactoContraEnemigoYJugadorDuranteElRecorrido() {
        ColisionService servicio = new ColisionService();
        Proyectil hostil = new Proyectil(0, 100, 100, 0, 8, true, Proyectil.Origen.ENEMIGO);
        hostil.actualizar(0.5);
        assertTrue(servicio.colisionaProyectilJugador(hostil, 45, 100, 45, 100, 27));

        Proyectil amigo = new Proyectil(0, 200, 100, 0, 12, false, Proyectil.Origen.JUGADOR);
        amigo.actualizar(0.5);
        assertTrue(servicio.colisionaProyectilEnemigo(amigo,
                42, 200, 42, 200, 22));
    }
}
