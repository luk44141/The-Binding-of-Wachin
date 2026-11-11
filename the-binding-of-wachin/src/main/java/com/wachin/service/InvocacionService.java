package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.model.TipoInvocacion;
import java.util.Comparator;
import java.util.List;

/** Movimiento, protección, energía y ataques automáticos de las invocaciones. */
public final class InvocacionService {
    public record Objetivo(Enemigo enemigo, double x, double y) { }
    public record Ataque(Enemigo objetivo, int dano, boolean laser,
            double origenX, double origenY, double velocidadX, double velocidadY) { }
    public record Actualizacion(Invocacion invocacion, List<Ataque> ataques) { }

    private final MejorasService mejorasService;

    public InvocacionService() { this(new MejorasService()); }
    public InvocacionService(MejorasService mejorasService) {
        if (mejorasService == null) throw new IllegalArgumentException("El servicio de mejoras es obligatorio.");
        this.mejorasService = mejorasService;
    }

    public Invocacion crear(TipoInvocacion tipo) {
        try { return new Invocacion(tipo); }
        catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo crear la invocación.", e);
        }
    }

    public Actualizacion actualizar(Invocacion invocacion, Jugador jugador,
            double jugadorX, double jugadorY, List<Objetivo> objetivos, double dt) {
        if (invocacion == null || jugador == null || dt <= 0) {
            return new Actualizacion(invocacion, List.of());
        }
        invocacion.reducirTiempoAtaque(dt);
        invocacion.recuperarEnergia(dt);
        TipoInvocacion tipo = invocacion.getTipoInvocacion();
        Objetivo objetivo = objetivos == null ? null : objetivos.stream()
                .filter(o -> o != null && o.enemigo() != null && o.enemigo().getVida() > 0)
                .filter(o -> distancia(invocacion.getX(), invocacion.getY(), o.x(), o.y()) <= tipo.getRangoDeteccion())
                .min(Comparator.comparingDouble(o -> distancia(invocacion.getX(), invocacion.getY(), o.x(), o.y())))
                .orElse(null);

        double destinoX = jugadorX + (invocacion.getX() < jugadorX ? -48 : 48);
        double destinoY = jugadorY + 42;
        boolean proteger = false;
        if (tipo == TipoInvocacion.GUSANO && objetivo != null
                && distancia(jugadorX, jugadorY, objetivo.x(), objetivo.y()) < 210) {
            double dx = objetivo.x() - jugadorX;
            double dy = objetivo.y() - jugadorY;
            double d = Math.max(1, Math.hypot(dx, dy));
            destinoX = jugadorX + dx / d * 62;
            destinoY = jugadorY + dy / d * 62;
            proteger = true;
        } else if (tipo == TipoInvocacion.MURCIELAGO && objetivo != null) {
            destinoX = objetivo.x(); destinoY = objetivo.y();
        }
        mover(invocacion, destinoX, destinoY, dt);

        if (proteger) {
            if (!invocacion.tieneProteccionActiva()) {
                try { invocacion.gastarEnergia(tipo.getCostoProteccion()); }
                catch (EnergiaInsuficienteException e) {
                    invocacion.setEstado(Invocacion.Estado.RECUPERANDO);
                    invocacion.setProteccionActiva(false);
                    return new Actualizacion(invocacion, List.of());
                }
                invocacion.setProteccionActiva(true);
            }
            invocacion.setEstado(Invocacion.Estado.PROTEGIENDO);
        } else {
            invocacion.setProteccionActiva(false);
            invocacion.setEstado(Invocacion.Estado.SIGUIENDO);
        }

        if (objetivo == null) {
            return new Actualizacion(invocacion, List.of());
        }
        int costoAtaque = tipo.getCostoAtaque();
        if (invocacion.getEnergia() < costoAtaque) {
            invocacion.setEstado(Invocacion.Estado.RECUPERANDO);
            return new Actualizacion(invocacion, List.of());
        }
        if (invocacion.getTiempoAtaque() > 0) return new Actualizacion(invocacion, List.of());
        double distanciaObjetivo = distancia(invocacion.getX(), invocacion.getY(), objetivo.x(), objetivo.y());
        boolean laser = tipo == TipoInvocacion.ROBOT;
        double rangoAtaque = laser ? tipo.getRangoDeteccion() : 55;
        if (distanciaObjetivo > rangoAtaque) return new Actualizacion(invocacion, List.of());
        if (!invocacion.puedeUsarEnergia(tipo.getCostoAtaque())) {
            invocacion.setEstado(Invocacion.Estado.RECUPERANDO);
            return new Actualizacion(invocacion, List.of());
        }
        try { invocacion.gastarEnergia(tipo.getCostoAtaque()); }
        catch (EnergiaInsuficienteException e) {
            invocacion.setEstado(Invocacion.Estado.RECUPERANDO);
            return new Actualizacion(invocacion, List.of());
        }
        invocacion.setEstado(Invocacion.Estado.ATACANDO);
        invocacion.iniciarTiempoAtaque(tipo == TipoInvocacion.ROBOT ? 0.8 : 0.72);
        double dx = objetivo.x() - invocacion.getX();
        double dy = objetivo.y() - invocacion.getY();
        double d = Math.max(1, Math.hypot(dx, dy));
        int dano = mejorasService.calcularDanoInvocacion(jugador, tipo.getDano());
        if (laser) {
            return new Actualizacion(invocacion, List.of(new Ataque(objetivo.enemigo(), dano, true,
                    invocacion.getX(), invocacion.getY(), dx / d * 500, dy / d * 500)));
        }
        if (distanciaObjetivo <= 55) {
            return new Actualizacion(invocacion, List.of(new Ataque(objetivo.enemigo(), dano, false,
                    invocacion.getX(), invocacion.getY(), 0, 0)));
        }
        return new Actualizacion(invocacion, List.of());
    }

    private void mover(Invocacion invocacion, double destinoX, double destinoY, double dt) {
        double dx = destinoX - invocacion.getX();
        double dy = destinoY - invocacion.getY();
        double d = Math.hypot(dx, dy);
        if (d <= 1) return;
        double paso = Math.min(d, invocacion.getVelocidad() * dt);
        invocacion.moverA(invocacion.getX() + dx / d * paso, invocacion.getY() + dy / d * paso);
    }
    private double distancia(double x1, double y1, double x2, double y2) { return Math.hypot(x1 - x2, y1 - y2); }
}
