package com.wachin.controller;

import com.wachin.model.Invocacion;
import com.wachin.model.Proyectil;
import com.wachin.service.InvocacionService;
import com.wachin.view.game.CatalogoAssets;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.InvocacionVisual;
import com.wachin.view.game.ProyectilVisual;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/** Movimiento, ataques y visuales de las invocaciones. Las invocaciones no colisionan con nada. */
final class InvocacionesController {

    private static final double ALCANCE_CUERPO_A_CUERPO = 55;
    private static final Color COLOR_LASER = Color.web("#4fd6e8");
    private final ContextoPartida ctx;
    private final List<InvocacionService.Objetivo> objetivos = new ArrayList<>();

    InvocacionesController(ContextoPartida ctx) {
        this.ctx = ctx;
    }

    void agregarVisual(Invocacion invocacion) {
        InvocacionVisual visual = new InvocacionVisual(invocacion);
        ctx.invocacionesVisuales.add(visual);
        ctx.vista.agregarInvocacion(visual);
    }

    void quitarVisual(Invocacion invocacion) {
        ctx.invocacionesVisuales.stream().filter(v -> v.getInvocacion() == invocacion).findFirst()
                .ifPresent(v -> {
                    ctx.vista.quitarInvocacion(v);
                    ctx.invocacionesVisuales.remove(v);
                });
    }

    /** Coloca las invocaciones junto al jugador al entrar en una sala. */
    void reubicarCercaDelJugador() {
        List<Invocacion> activas = ctx.juego.getInvocacionesActivas();
        for (int i = 0; i < activas.size(); i++) {
            activas.get(i).setEspacio(i);
            double x = Math.max(105, Math.min(895, ctx.x + (i == 0 ? -52 : 52)));
            double y = Math.max(105, Math.min(595, ctx.y + 44));
            activas.get(i).moverA(x, y);
        }
    }

    void actualizar(double dt, CombateController combate) {
        objetivos.clear();
        for (EnemigoVisual enemigo : ctx.enemigos) {
            if (!enemigo.estaApareciendo()) {
                objetivos.add(new InvocacionService.Objetivo(
                        enemigo.getEnemigo(), enemigo.getX(), enemigo.getY()));
            }
        }
        List<Invocacion> activas = ctx.juego.getInvocacionesActivas();
        for (int i = 0; i < activas.size(); i++) activas.get(i).setEspacio(i);
        for (Invocacion invocacion : activas) {
            invocacion.reducirRecargaBloqueoProyectil(dt);
            // Respaldo para partidas guardadas antiguas: ninguna invocación puede quedar muerta.
            if (invocacion.getVida() <= 0) {
                try { invocacion.curar(invocacion.getVidaMaxima()); }
                catch (com.wachin.exception.VidaMaximaException ignorada) { /* El máximo ya limita la curación. */ }
            }
            InvocacionService.Actualizacion accion = ctx.invocacionServicio.actualizar(invocacion, ctx.jugador(),
                    ctx.x, ctx.y, objetivos, dt);
            for (InvocacionService.Ataque ataque : accion.ataques()) ejecutarAtaque(invocacion, ataque, combate);
        }
        for (InvocacionVisual visual : ctx.invocacionesVisuales) visual.actualizar(dt);
    }

    private void ejecutarAtaque(Invocacion invocacion, InvocacionService.Ataque ataque, CombateController combate) {
        EnemigoVisual objetivo = null;
        for (EnemigoVisual enemigo : ctx.enemigos) {
            if (enemigo.getEnemigo() == ataque.objetivo()) {
                objetivo = enemigo;
                break;
            }
        }
        if (objetivo == null) return;
        if (ataque.laser()) {
            Proyectil modelo = new Proyectil(ataque.origenX(), ataque.origenY(), ataque.velocidadX(),
                    ataque.velocidadY(), ataque.dano(), false, Proyectil.Origen.INVOCACION);
            ProyectilVisual laser = new ProyectilVisual(modelo, CatalogoAssets.LASER_ROBOT, COLOR_LASER, true);
            ctx.proyectilesJugador.add(laser);
            ctx.vista.agregarProyectil(laser);
            ctx.efectos.disparo(ataque.origenX(), ataque.origenY(), ataque.velocidadX() / 500, ataque.velocidadY() / 500, COLOR_LASER);
        } else if (ctx.colision.distancia(invocacion.getX(), invocacion.getY(), objetivo.getX(), objetivo.getY())
                <= ALCANCE_CUERPO_A_CUERPO) {
            combate.atacarEnemigo(objetivo, ataque.dano(), invocacion.getX(), invocacion.getY(), Retroalimentacion.DORADO);
        }
    }
}
