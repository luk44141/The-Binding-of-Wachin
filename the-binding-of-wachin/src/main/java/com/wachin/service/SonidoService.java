package com.wachin.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Efectos de sonido. Si existe {@code assets/audio/<efecto>.wav} se usa ese archivo; si no, el
 * efecto se sintetiza en memoria, de modo que el juego suena sin depender de recursos externos.
 * Si el sistema no dispone de salida de audio se registra una advertencia y se desactiva.
 */
public final class SonidoService {

    /** Efectos disponibles; el nombre en minúscula es el del archivo WAV opcional. */
    public enum Efecto { DISPARO, IMPACTO, MUERTE, DANIO, RECOGER, PUERTA, JEFE, NIVEL, CLICK, CARTA, DERROTA, VICTORIA }

    private static final float FRECUENCIA = 22050f;
    private static final int MAX_SIMULTANEOS = 10;
    private static final Logger REGISTRO = Logger.getLogger(SonidoService.class.getName());
    private static SonidoService instancia;

    private final AjustesService ajustes;
    private final Map<Efecto, byte[]> muestras = new EnumMap<>(Efecto.class);
    private final Random azar = new Random(11);
    private final ExecutorService hilo = Executors.newSingleThreadExecutor(tarea -> {
        Thread t = new Thread(tarea, "sonido-wachin");
        t.setDaemon(true);
        return t;
    });
    private int activos;
    private boolean disponible = true;

    SonidoService(AjustesService ajustes) {
        this.ajustes = ajustes;
        for (Efecto efecto : Efecto.values()) muestras.put(efecto, sintetizar(efecto));
    }

    public static synchronized SonidoService obtener() {
        if (instancia == null) instancia = new SonidoService(AjustesService.obtener());
        return instancia;
    }

    /** Solicita la reproducción; el trabajo con el dispositivo de audio ocurre fuera del hilo de JavaFX. */
    public void reproducir(Efecto efecto) {
        if (!disponible || ajustes.getVolumen() == 0 || efecto == null) return;
        synchronized (this) {
            if (activos >= MAX_SIMULTANEOS) return;
            activos++;
        }
        try {
            hilo.execute(() -> iniciar(efecto));
        } catch (RejectedExecutionException e) {
            synchronized (this) { activos--; }
        }
    }

    private void iniciar(Efecto efecto) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(abrir(efecto));
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl ganancia = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                double lineal = Math.max(0.0001, ajustes.getVolumen() / 100.0 * 0.6);
                float decibelios = (float) (20 * Math.log10(lineal));
                ganancia.setValue(Math.max(ganancia.getMinimum(), Math.min(ganancia.getMaximum(), decibelios)));
            }
            clip.addLineListener(evento -> {
                if (evento.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    synchronized (this) { activos--; }
                }
            });
            clip.start();
        } catch (LineUnavailableException | IOException | IllegalArgumentException | IllegalStateException | SecurityException e) {
            synchronized (this) { activos--; }
            desactivar(e);
        }
    }

    private AudioInputStream abrir(Efecto efecto) {
        String ruta = "/assets/audio/" + efecto.name().toLowerCase(Locale.ROOT) + ".wav";
        InputStream recurso = SonidoService.class.getResourceAsStream(ruta);
        if (recurso != null) {
            try (InputStream entrada = new BufferedInputStream(recurso)) {
                byte[] datos = entrada.readAllBytes();
                return AudioSystem.getAudioInputStream(new ByteArrayInputStream(datos));
            } catch (IOException | UnsupportedAudioFileException e) {
                REGISTRO.log(Level.WARNING, "WAV no válido (" + ruta + "); se usa el efecto sintetizado.", e);
            }
        }
        byte[] datos = muestras.get(efecto);
        AudioFormat formato = new AudioFormat(FRECUENCIA, 16, 1, true, false);
        return new AudioInputStream(new ByteArrayInputStream(datos), formato, datos.length / 2);
    }

    private synchronized void desactivar(Exception causa) {
        if (disponible) {
            disponible = false;
            REGISTRO.log(Level.WARNING, "Audio desactivado: no hay salida disponible ({0}).", causa.getMessage());
        }
    }

    // ───────────────────────────── síntesis ─────────────────────────────

    private byte[] sintetizar(Efecto efecto) {
        return switch (efecto) {
            case DISPARO -> tono(900, 260, 0.11, Onda.CUADRADA, 0.35, 0);
            case IMPACTO -> mezclar(tono(240, 90, 0.09, Onda.TRIANGULO, 0.5, 0), tono(0, 0, 0.07, Onda.RUIDO, 0.3, 0));
            case MUERTE -> mezclar(tono(320, 50, 0.34, Onda.SIERRA, 0.4, 0), tono(0, 0, 0.3, Onda.RUIDO, 0.35, 0));
            case DANIO -> mezclar(tono(180, 70, 0.28, Onda.CUADRADA, 0.45, 0), tono(0, 0, 0.18, Onda.RUIDO, 0.3, 0));
            case RECOGER -> secuencia(new double[]{660, 880, 1320}, 0.07, Onda.TRIANGULO, 0.4);
            case PUERTA -> mezclar(tono(110, 60, 0.4, Onda.SIERRA, 0.35, 0), tono(0, 0, 0.35, Onda.RUIDO, 0.18, 0));
            case JEFE -> mezclar(tono(90, 45, 0.9, Onda.SIERRA, 0.5, 6), tono(60, 40, 0.9, Onda.CUADRADA, 0.3, 0));
            case NIVEL -> secuencia(new double[]{523, 659, 784, 1047}, 0.09, Onda.CUADRADA, 0.3);
            case CLICK -> tono(520, 420, 0.04, Onda.CUADRADA, 0.25, 0);
            case CARTA -> secuencia(new double[]{440, 554, 659}, 0.06, Onda.TRIANGULO, 0.4);
            case DERROTA -> secuencia(new double[]{392, 330, 262, 196}, 0.22, Onda.SIERRA, 0.4);
            case VICTORIA -> secuencia(new double[]{523, 659, 784, 1047, 1318}, 0.14, Onda.CUADRADA, 0.3);
        };
    }

    private enum Onda { CUADRADA, TRIANGULO, SIERRA, RUIDO }

    /** Genera un tono que barre de {@code f0} a {@code f1} con decaimiento exponencial. */
    private byte[] tono(double f0, double f1, double segundos, Onda onda, double amplitud, double vibrato) {
        int n = (int) (segundos * FRECUENCIA);
        byte[] salida = new byte[n * 2];
        double fase = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) n;
            double f = f0 + (f1 - f0) * t + (vibrato > 0 ? Math.sin(t * 40) * vibrato : 0);
            fase += 2 * Math.PI * f / FRECUENCIA;
            double muestra = switch (onda) {
                case CUADRADA -> Math.sin(fase) >= 0 ? 1 : -1;
                case TRIANGULO -> 2 / Math.PI * Math.asin(Math.sin(fase));
                case SIERRA -> 2 * ((fase / (2 * Math.PI)) % 1) - 1;
                case RUIDO -> azar.nextDouble() * 2 - 1;
            };
            double envolvente = Math.pow(1 - t, 2) * Math.min(1, i / 60.0);
            short valor = (short) (muestra * envolvente * amplitud * Short.MAX_VALUE);
            salida[i * 2] = (byte) (valor & 0xff);
            salida[i * 2 + 1] = (byte) ((valor >> 8) & 0xff);
        }
        return salida;
    }

    private byte[] secuencia(double[] notas, double duracionNota, Onda onda, double amplitud) {
        byte[][] partes = new byte[notas.length][];
        int total = 0;
        for (int i = 0; i < notas.length; i++) {
            partes[i] = tono(notas[i], notas[i], duracionNota, onda, amplitud, 0);
            total += partes[i].length;
        }
        byte[] salida = new byte[total];
        int posicion = 0;
        for (byte[] parte : partes) {
            System.arraycopy(parte, 0, salida, posicion, parte.length);
            posicion += parte.length;
        }
        return salida;
    }

    private byte[] mezclar(byte[] a, byte[] b) {
        byte[] largo = a.length >= b.length ? a : b;
        byte[] corto = largo == a ? b : a;
        byte[] salida = largo.clone();
        for (int i = 0; i + 1 < corto.length; i += 2) {
            int suma = (short) ((largo[i] & 0xff) | (largo[i + 1] << 8))
                    + (short) ((corto[i] & 0xff) | (corto[i + 1] << 8));
            short limitado = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, suma));
            salida[i] = (byte) (limitado & 0xff);
            salida[i + 1] = (byte) ((limitado >> 8) & 0xff);
        }
        return salida;
    }
}
