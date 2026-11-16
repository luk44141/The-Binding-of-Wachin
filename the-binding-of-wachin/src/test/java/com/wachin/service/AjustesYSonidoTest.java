package com.wachin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import org.junit.Test;

public class AjustesYSonidoTest {

    /** Almacén de preferencias en memoria para no tocar el sistema real. */
    private static final class Memoria extends AbstractPreferences {
        private final Map<String, String> datos = new HashMap<>();
        Memoria() { super(null, ""); }
        @Override protected void putSpi(String clave, String valor) { datos.put(clave, valor); }
        @Override protected String getSpi(String clave) { return datos.get(clave); }
        @Override protected void removeSpi(String clave) { datos.remove(clave); }
        @Override protected void removeNodeSpi() { datos.clear(); }
        @Override protected String[] keysSpi() { return datos.keySet().toArray(new String[0]); }
        @Override protected String[] childrenNamesSpi() { return new String[0]; }
        @Override protected AbstractPreferences childSpi(String nombre) { return null; }
        @Override protected void syncSpi() { }
        @Override protected void flushSpi() { }
    }

    @Test
    public void losValoresPorDefectoSonRazonables() {
        AjustesService ajustes = new AjustesService(new Memoria());
        assertEquals(70, ajustes.getVolumen());
        assertEquals(AjustesService.CalidadEfectos.COMPLETOS, ajustes.getCalidad());
        assertTrue(ajustes.sacudidaActiva());
        assertTrue(ajustes.numerosDanoActivos());
        assertEquals(1.0, ajustes.factorParticulas(), 0.0);
    }

    @Test
    public void elVolumenSeLimitaYLosCambiosPersistenEntreInstancias() {
        Memoria almacen = new Memoria();
        AjustesService primero = new AjustesService(almacen);
        primero.setVolumen(250);
        assertEquals(100, primero.getVolumen());
        primero.setVolumen(-3);
        assertEquals(0, primero.getVolumen());
        primero.setVolumen(35);
        primero.setCalidad(AjustesService.CalidadEfectos.REDUCIDOS);
        primero.setSacudida(false);
        primero.setNumerosDano(false);

        AjustesService segundo = new AjustesService(almacen);
        assertEquals(35, segundo.getVolumen());
        assertEquals(AjustesService.CalidadEfectos.REDUCIDOS, segundo.getCalidad());
        assertFalse(segundo.sacudidaActiva());
        assertFalse(segundo.numerosDanoActivos());
        assertEquals(0.5, segundo.factorParticulas(), 0.0);
        assertTrue(segundo.calidadHabilitaDestellos());
    }

    @Test
    public void sinEfectosDesactivaParticulasYDestellos() {
        AjustesService ajustes = new AjustesService(new Memoria());
        ajustes.setCalidad(AjustesService.CalidadEfectos.SIN_EFECTOS);
        assertEquals(0.0, ajustes.factorParticulas(), 0.0);
        assertFalse(ajustes.calidadHabilitaDestellos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void exigeUnAlmacenDePreferencias() {
        new AjustesService(null);
    }

    @Test
    public void lasMuestrasSintetizadasSeGeneranYConVolumenCeroNoSeReproduceNada() {
        AjustesService ajustes = new AjustesService(new Memoria());
        ajustes.setVolumen(0);
        SonidoService sonido = new SonidoService(ajustes);
        for (SonidoService.Efecto efecto : SonidoService.Efecto.values()) sonido.reproducir(efecto);
        sonido.reproducir(null);
    }

    @Test
    public void elLimiteDeFpsPorDefectoLevantaElTopeDe60DeJavaFx() {
        AjustesService ajustes = new AjustesService(new Memoria());
        assertEquals(AjustesService.LimiteFps.FPS_240, ajustes.getLimiteFps());
        assertEquals("240", ajustes.propiedadesDeRendimiento().get("javafx.animation.pulse"));
        assertFalse(ajustes.mostrarFps());
    }

    @Test
    public void cadaLimiteGeneraLasPropiedadesDeJavaFxCorrespondientes() {
        AjustesService ajustes = new AjustesService(new Memoria());
        ajustes.setLimiteFps(AjustesService.LimiteFps.FPS_60);
        assertEquals("60", ajustes.propiedadesDeRendimiento().get("javafx.animation.pulse"));
        ajustes.setLimiteFps(AjustesService.LimiteFps.FPS_144);
        assertEquals("144", ajustes.propiedadesDeRendimiento().get("javafx.animation.pulse"));
        ajustes.setLimiteFps(AjustesService.LimiteFps.SIN_LIMITE);
        Map<String, String> sinLimite = ajustes.propiedadesDeRendimiento();
        assertEquals("true", sinLimite.get("javafx.animation.fullspeed"));
        assertEquals("false", sinLimite.get("prism.vsync"));
        assertFalse(sinLimite.containsKey("javafx.animation.pulse"));
    }

    @Test
    public void elLimiteDeFpsYElContadorPersisten() {
        Memoria almacen = new Memoria();
        AjustesService primero = new AjustesService(almacen);
        primero.setLimiteFps(AjustesService.LimiteFps.SIN_LIMITE);
        primero.setMostrarFps(true);
        AjustesService segundo = new AjustesService(almacen);
        assertEquals(AjustesService.LimiteFps.SIN_LIMITE, segundo.getLimiteFps());
        assertTrue(segundo.mostrarFps());
        segundo.setLimiteFps(null);
        assertEquals(AjustesService.LimiteFps.FPS_240, segundo.getLimiteFps());
    }
}
