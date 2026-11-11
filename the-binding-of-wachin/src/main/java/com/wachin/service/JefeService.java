package com.wachin.service;

import com.wachin.model.Jefe;
import com.wachin.model.EstadoPartida;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Estado de combate y patrones de los jefes. No depende de JavaFX. */
public final class JefeService {
    public enum Ataque { DISPARO, RADIAL, EMBESTIDA, ZONAS, NINGUNO }
    public record Punto(double x, double y) { }
    public record Disparo(double x, double y, double vx, double vy, int dano) { }
    public record Actualizacion(double x, double y, Ataque aviso,
            double direccionX, double direccionY, List<Disparo> disparos,
            List<Punto> zonas, boolean zonasActivas) { }

    private enum Fase { PERSEGUIR, PREPARAR, EJECUTAR, RECUPERAR }
    private static final class Estado {
        Fase fase = Fase.PERSEGUIR;
        Ataque ataque = Ataque.EMBESTIDA;
        double tiempo = 2.4;
        double direccionX;
        double direccionY;
        int ultimoAtaque = -1;
        int cadena;
        boolean segundaFase;
        List<Punto> zonas = List.of();
    }

    private final Map<Jefe, Estado> estados = new IdentityHashMap<>();
    private final Random random = new Random();

    public EstadoPartida.JefeEstado guardarEstado(Jefe jefe) {
        Estado e = estados.get(jefe);
        if (e == null) return null;
        return new EstadoPartida.JefeEstado(e.fase.name(), e.ataque.name(), e.tiempo,
                e.direccionX, e.direccionY, e.ultimoAtaque, e.cadena, e.segundaFase,
                e.zonas.stream().map(Punto::x).toList(), e.zonas.stream().map(Punto::y).toList());
    }

    public void restaurarEstado(Jefe jefe, EstadoPartida.JefeEstado guardado) {
        if (jefe == null || guardado == null) return;
        Estado e = new Estado();
        e.fase = Fase.valueOf(guardado.fase());
        e.ataque = Ataque.valueOf(guardado.ataque());
        e.tiempo = guardado.tiempo();
        e.direccionX = guardado.direccionX(); e.direccionY = guardado.direccionY();
        e.ultimoAtaque = guardado.ultimoAtaque(); e.cadena = guardado.cadena();
        e.segundaFase = guardado.segundaFase();
        List<Punto> zonas = new ArrayList<>();
        for (int i = 0; i < Math.min(guardado.zonasX().size(), guardado.zonasY().size()); i++) {
            zonas.add(new Punto(guardado.zonasX().get(i), guardado.zonasY().get(i)));
        }
        e.zonas = List.copyOf(zonas);
        estados.put(jefe, e);
    }

    public Actualizacion actualizar(Jefe jefe, double x, double y,
            double jugadorX, double jugadorY, double dt) {
        if (jefe == null || dt <= 0) return resultado(x, y, Ataque.NINGUNO, 0, 0, List.of(), List.of(), false);
        Estado e = estados.computeIfAbsent(jefe, ignorado -> new Estado());
        if (jefe.getTipoJefe() == Jefe.Tipo.SALA_20 && !e.segundaFase
                && jefe.getVida() * 2 <= jefe.getVidaMaxima()) {
            e.segundaFase = true;
        }
        double faseRapida = e.segundaFase ? 1.35 : 1.0;
        List<Disparo> disparos = new ArrayList<>();
        List<Punto> zonas = new ArrayList<>();
        double distancia = Math.max(1, Math.hypot(jugadorX - x, jugadorY - y));
        double dx = (jugadorX - x) / distancia;
        double dy = (jugadorY - y) / distancia;
        Ataque aviso = Ataque.NINGUNO;
        boolean zonasActivas = false;

        if (jefe.getTipoJefe() == Jefe.Tipo.SALA_10) {
            switch (e.fase) {
                case PERSEGUIR -> {
                    double[] p = avanzar(x, y, jugadorX, jugadorY, jefe.getVelocidad() * 1.22, dt);
                    x = p[0]; y = p[1]; e.tiempo -= dt;
                    if (e.tiempo <= 0) {
                        e.direccionX = dx; e.direccionY = dy;
                        e.fase = Fase.PREPARAR; e.tiempo = 0.595; aviso = Ataque.EMBESTIDA;
                    }
                }
                case PREPARAR -> {
                    e.tiempo -= dt; aviso = Ataque.EMBESTIDA;
                    if (e.tiempo <= 0) { e.fase = Fase.EJECUTAR; e.tiempo = 0.562; }
                }
                case EJECUTAR -> {
                    x += e.direccionX * 615 * dt; y += e.direccionY * 615 * dt;
                    e.tiempo -= dt;
                    if (e.tiempo <= 0 || fuera(x, y)) { e.fase = Fase.RECUPERAR; e.tiempo = 0.745; }
                }
                case RECUPERAR -> {
                    e.tiempo -= dt;
                    if (e.tiempo <= 0) { e.fase = Fase.PERSEGUIR; e.tiempo = 2.76; }
                }
            }
            return resultado(x, y, aviso, e.direccionX, e.direccionY, disparos, zonas, false);
        }

        // Los jefes 15 y 20 se aproximan entre ataques y conservan ventana de recuperación.
        if (e.fase == Fase.PERSEGUIR) {
            double velocidad = jefe.getVelocidad() * faseRapida;
            if (jefe.getTipoJefe() == Jefe.Tipo.SALA_15 && distancia < 245) {
                x -= dx * velocidad * 0.65 * dt; y -= dy * velocidad * 0.65 * dt;
            } else {
                double[] p = avanzar(x, y, jugadorX, jugadorY, velocidad, dt); x = p[0]; y = p[1];
            }
            e.tiempo -= dt;
            if (e.tiempo <= 0) {
                e.ataque = elegirAtaque(jefe, e);
                if (e.ataque == Ataque.ZONAS) e.zonas = crearZonas(jugadorX, jugadorY, x, y);
                e.direccionX = dx; e.direccionY = dy;
                e.fase = Fase.PREPARAR;
                e.tiempo = (jefe.getTipoJefe() == Jefe.Tipo.SALA_20 ? 0.55 : 0.8) / faseRapida;
            }
        } else if (e.fase == Fase.PREPARAR) {
            aviso = e.ataque;
            e.tiempo -= dt;
            if (jefe.getTipoJefe() == Jefe.Tipo.SALA_20 && e.ataque == Ataque.ZONAS) {
                if (e.zonas.isEmpty()) e.zonas = crearZonas(jugadorX, jugadorY, x, y);
                zonas.addAll(e.zonas);
            }
            if (e.tiempo <= 0) {
                e.fase = Fase.EJECUTAR;
                ejecutar(jefe, e, x, y, jugadorX, jugadorY, faseRapida, disparos, zonas);
                if (e.ataque == Ataque.ZONAS) { zonasActivas = true; e.zonas = List.of(); }
                e.tiempo = e.ataque == Ataque.EMBESTIDA ? 0.5 / faseRapida : 0.16;
            }
        } else if (e.fase == Fase.EJECUTAR) {
            if (e.ataque == Ataque.EMBESTIDA) {
                x += e.direccionX * 670 * faseRapida * dt;
                y += e.direccionY * 670 * faseRapida * dt;
            }
            e.tiempo -= dt;
            if (e.tiempo <= 0 || fuera(x, y)) {
                if (jefe.getTipoJefe() == Jefe.Tipo.SALA_20 && e.ataque == Ataque.EMBESTIDA
                        && e.cadena == 0) {
                    e.cadena = 1; e.ataque = Ataque.RADIAL; e.fase = Fase.PREPARAR; e.tiempo = 0.32 / faseRapida;
                } else {
                    e.cadena = 0; e.fase = Fase.RECUPERAR;
                    e.tiempo = (jefe.getTipoJefe() == Jefe.Tipo.SALA_20 ? 0.52 : 0.9) / faseRapida;
                }
            }
        } else {
            e.tiempo -= dt;
            if (e.tiempo <= 0) { e.fase = Fase.PERSEGUIR; e.tiempo = 0.45 / faseRapida; }
        }
        return resultado(x, y, aviso, e.direccionX, e.direccionY, disparos, zonas, zonasActivas);
    }

    private Ataque elegirAtaque(Jefe jefe, Estado e) {
        Ataque[] opciones = jefe.getTipoJefe() == Jefe.Tipo.SALA_15
                ? new Ataque[]{Ataque.DISPARO, Ataque.RADIAL, Ataque.EMBESTIDA}
                : new Ataque[]{Ataque.DISPARO, Ataque.RADIAL, Ataque.EMBESTIDA, Ataque.ZONAS};
        int elegido;
        do { elegido = random.nextInt(opciones.length); } while (elegido == e.ultimoAtaque);
        e.ultimoAtaque = elegido;
        return opciones[elegido];
    }

    private void ejecutar(Jefe jefe, Estado e, double x, double y,
            double jugadorX, double jugadorY, double rapidez,
            List<Disparo> disparos, List<Punto> zonas) {
        int dano = jefe.getDano();
        switch (e.ataque) {
            case DISPARO -> {
                double d = Math.max(1, Math.hypot(jugadorX - x, jugadorY - y));
                int cantidad = jefe.getTipoJefe() == Jefe.Tipo.SALA_20 && e.segundaFase ? 5 : 3;
                for (int i = 0; i < cantidad; i++) {
                    double angulo = Math.atan2(jugadorY - y, jugadorX - x) + (i - (cantidad - 1) / 2.0) * 0.13;
                    disparos.add(new Disparo(x, y, Math.cos(angulo) * 270 * rapidez,
                            Math.sin(angulo) * 270 * rapidez, dano));
                }
            }
            case RADIAL -> {
                int cantidad = jefe.getTipoJefe() == Jefe.Tipo.SALA_20 && e.segundaFase ? 16 : 12;
                for (int i = 0; i < cantidad; i++) {
                    double a = Math.PI * 2 * i / cantidad;
                    disparos.add(new Disparo(x, y, Math.cos(a) * 230 * rapidez,
                            Math.sin(a) * 230 * rapidez, dano));
                }
            }
            case ZONAS -> { /* Las zonas anunciadas se activan desde la actualización del estado. */ }
            default -> { }
        }
    }

    private List<Punto> crearZonas(double jugadorX, double jugadorY, double x, double y) {
        List<Punto> puntos = new ArrayList<>();
        puntos.add(new Punto(jugadorX, jugadorY));
        puntos.add(new Punto(Math.max(95, Math.min(905, x + 130)), Math.max(95, Math.min(605, y))));
        puntos.add(new Punto(Math.max(95, Math.min(905, x - 130)), Math.max(95, Math.min(605, y))));
        return puntos;
    }

    private double[] avanzar(double x, double y, double tx, double ty, double velocidad, double dt) {
        double d = Math.hypot(tx - x, ty - y);
        if (d <= 1) return new double[]{x, y};
        double paso = Math.min(d, velocidad * dt);
        return new double[]{x + (tx - x) / d * paso, y + (ty - y) / d * paso};
    }
    private boolean fuera(double x, double y) { return x < 78 || x > 922 || y < 78 || y > 622; }
    private Actualizacion resultado(double x, double y, Ataque a, double dx, double dy,
            List<Disparo> d, List<Punto> z, boolean activas) {
        return new Actualizacion(x, y, a, dx, dy, List.copyOf(d), List.copyOf(z), activas);
    }
    public int obtenerDanoContacto(Jefe jefe) {
        Estado e = estados.get(jefe);
        return jefe.getDano() + (e != null && e.fase == Fase.EJECUTAR && e.ataque == Ataque.EMBESTIDA
                ? jefe.getDanoAdicional() : 0);
    }
}
