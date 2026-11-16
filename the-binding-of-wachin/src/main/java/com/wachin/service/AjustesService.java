package com.wachin.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Preferencias del jugador (volumen y efectos visuales), persistidas con {@link Preferences}. */
public final class AjustesService {

    /** Cantidad de partículas y efectos decorativos. */
    public enum CalidadEfectos { SIN_EFECTOS, REDUCIDOS, COMPLETOS }

    /** Límite de fotogramas por segundo. JavaFX trae un tope de 60 que hay que levantar explícitamente. */
    public enum LimiteFps {
        FPS_60("60 FPS"), FPS_144("144 FPS"), FPS_240("240 FPS"), SIN_LIMITE("SIN LÍMITE");

        private final String etiqueta;

        LimiteFps(String etiqueta) { this.etiqueta = etiqueta; }

        public String getEtiqueta() { return etiqueta; }
    }

    private static final String NODO = "/com/wachin/the-binding-of-wachin";
    private static final Logger REGISTRO = Logger.getLogger(AjustesService.class.getName());
    private static AjustesService instancia;

    private final Preferences preferencias;
    private int volumen;
    private CalidadEfectos calidad;
    private boolean sacudida;
    private boolean numerosDano;
    private LimiteFps limiteFps;
    private boolean mostrarFps;

    public AjustesService(Preferences preferencias) {
        if (preferencias == null) throw new IllegalArgumentException("Las preferencias son obligatorias.");
        this.preferencias = preferencias;
        volumen = Math.max(0, Math.min(100, preferencias.getInt("volumen", 70)));
        int indice = Math.max(0, Math.min(CalidadEfectos.values().length - 1,
                preferencias.getInt("calidad", CalidadEfectos.COMPLETOS.ordinal())));
        calidad = CalidadEfectos.values()[indice];
        sacudida = preferencias.getBoolean("sacudida", true);
        numerosDano = preferencias.getBoolean("numerosDano", true);
        int fps = Math.max(0, Math.min(LimiteFps.values().length - 1,
                preferencias.getInt("limiteFps", LimiteFps.FPS_240.ordinal())));
        limiteFps = LimiteFps.values()[fps];
        mostrarFps = preferencias.getBoolean("mostrarFps", false);
    }

    public static synchronized AjustesService obtener() {
        if (instancia == null) instancia = new AjustesService(Preferences.userRoot().node(NODO));
        return instancia;
    }

    public int getVolumen() { return volumen; }
    public CalidadEfectos getCalidad() { return calidad; }
    public boolean sacudidaActiva() { return sacudida; }
    public boolean numerosDanoActivos() { return numerosDano; }
    public LimiteFps getLimiteFps() { return limiteFps; }
    public boolean mostrarFps() { return mostrarFps; }

    /**
     * Propiedades del sistema de JavaFX que implementan el límite elegido. Deben aplicarse antes de
     * que arranque JavaFX; por eso el cambio de límite surte efecto al reiniciar el juego.
     * Con VSync activado la velocidad real no supera la frecuencia del monitor.
     */
    public Map<String, String> propiedadesDeRendimiento() {
        Map<String, String> propiedades = new LinkedHashMap<>();
        switch (limiteFps) {
            case FPS_60 -> propiedades.put("javafx.animation.pulse", "60");
            case FPS_144 -> propiedades.put("javafx.animation.pulse", "144");
            case FPS_240 -> propiedades.put("javafx.animation.pulse", "240");
            case SIN_LIMITE -> {
                propiedades.put("javafx.animation.fullspeed", "true");
                propiedades.put("prism.vsync", "false");
            }
        }
        return propiedades;
    }

    /** Los destellos de pantalla se consideran efectos y se desactivan con «sin efectos». */
    public boolean calidadHabilitaDestellos() { return calidad != CalidadEfectos.SIN_EFECTOS; }

    /** Multiplicador de partículas: 0, 0.5 o 1. */
    public double factorParticulas() {
        return switch (calidad) { case SIN_EFECTOS -> 0; case REDUCIDOS -> 0.5; case COMPLETOS -> 1; };
    }

    public void setVolumen(int valor) {
        volumen = Math.max(0, Math.min(100, valor));
        preferencias.putInt("volumen", volumen);
    }

    public void setCalidad(CalidadEfectos valor) {
        calidad = valor == null ? CalidadEfectos.COMPLETOS : valor;
        preferencias.putInt("calidad", calidad.ordinal());
    }

    public void setLimiteFps(LimiteFps valor) {
        limiteFps = valor == null ? LimiteFps.FPS_240 : valor;
        preferencias.putInt("limiteFps", limiteFps.ordinal());
    }

    public void setMostrarFps(boolean valor) {
        mostrarFps = valor;
        preferencias.putBoolean("mostrarFps", valor);
    }

    public void setSacudida(boolean valor) {
        sacudida = valor;
        preferencias.putBoolean("sacudida", valor);
    }

    public void setNumerosDano(boolean valor) {
        numerosDano = valor;
        preferencias.putBoolean("numerosDano", valor);
    }

    /** Fuerza la escritura en el almacén; un fallo se registra y no interrumpe el juego. */
    public void guardar() {
        try { preferencias.flush(); }
        catch (BackingStoreException e) { REGISTRO.log(Level.WARNING, "No se pudieron guardar los ajustes.", e); }
    }
}
