package com.wachin.controller;

import com.wachin.model.Jugador;
import com.wachin.model.Proyectil;
import com.wachin.model.ProyectilEquipable;
import com.wachin.service.ColisionService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.JugadorVisual;
import com.wachin.view.game.ProyectilVisual;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/** Entrada del jugador, movimiento, disparo con cadencia y tiempo de invulnerabilidad. */
final class JugadorController {

    static final double VELOCIDAD_PROYECTIL = 430;
    /** Segundos entre disparos. Mantener una flecha pulsada dispara de forma continua a este ritmo. */
    static final double INTERVALO_DISPARO = 0.30;
    private static final double INTERVALO_LAGRIMA = 0.16;

    private static final Set<KeyCode> FLECHAS = EnumSet.of(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);

    private final ContextoPartida ctx;
    private final Set<KeyCode> teclas = EnumSet.noneOf(KeyCode.class);
    private final Deque<KeyCode> ordenFlechas = new ArrayDeque<>();
    private final List<ColisionService.Obstaculo> obstaculosMovimiento = new ArrayList<>();

    JugadorController(ContextoPartida ctx) { this.ctx = ctx; }

    /** Registra una tecla. Devuelve {@code false} si ya estaba pulsada (repetición del sistema). */
    boolean presionar(KeyCode tecla) {
        if (!teclas.add(tecla)) return false;
        if (FLECHAS.contains(tecla)) {
            ordenFlechas.remove(tecla);
            ordenFlechas.addLast(tecla);
        }
        return true;
    }

    void liberar(KeyCode tecla) {
        teclas.remove(tecla);
        ordenFlechas.remove(tecla);
    }

    void limpiarTeclas() {
        teclas.clear();
        ordenFlechas.clear();
    }

    void crearVisual() {
        ctx.jugadorVisual = new JugadorVisual(ctx.configuracion.getPersonaje());
        ctx.vista.agregarJugador(ctx.jugadorVisual);
        ctx.jugadorVisual.actualizar(0, ctx.x, ctx.y, 0, 0, false);
    }

    void actualizar(double dt) {
        Jugador jugador = ctx.jugador();
        if (ctx.invulnerable > 0) ctx.invulnerable = Math.max(0, ctx.invulnerable - dt);
        jugador.reducirRecargaDisparo(dt);
        if (!ctx.partidaActiva()) return;
        mover(jugador, dt);
        dispararSiCorresponde(jugador);
        ctx.jugadorVisual.actualizar(dt, ctx.x, ctx.y, ctx.direccionX, ctx.direccionY, ctx.invulnerable > 0);
    }

    private void mover(Jugador jugador, double dt) {
        ctx.xAnterior = ctx.x;
        ctx.yAnterior = ctx.y;
        double dx = 0;
        double dy = 0;
        if (teclas.contains(KeyCode.W)) dy--;
        if (teclas.contains(KeyCode.S)) dy++;
        if (teclas.contains(KeyCode.A)) dx--;
        if (teclas.contains(KeyCode.D)) dx++;
        double longitud = Math.hypot(dx, dy);
        if (longitud > 0) {
            dx /= longitud;
            dy /= longitud;
            ColisionService.Posicion posicion = ctx.colision.resolverMovimiento(ctx.x, ctx.y,
                    ctx.x + dx * jugador.getVelocidad() * dt, ctx.y + dy * jugador.getVelocidad() * dt,
                    ContextoPartida.RADIO_JUGADOR, obstaculos(), ContextoPartida.LIMITE_IZQUIERDO,
                    ContextoPartida.LIMITE_DERECHO, ContextoPartida.LIMITE_SUPERIOR, ContextoPartida.LIMITE_INFERIOR);
            ctx.x = posicion.x();
            ctx.y = posicion.y();
        }
        ctx.direccionX = dx;
        ctx.direccionY = dy;
    }

    /** Los enemigos bloquean al jugador salvo durante su invulnerabilidad; las invocaciones nunca. */
    private List<ColisionService.Obstaculo> obstaculos() {
        obstaculosMovimiento.clear();
        if (ctx.invulnerable <= 0) {
            for (EnemigoVisual enemigo : ctx.enemigos) {
                if (!enemigo.estaApareciendo()) {
                    obstaculosMovimiento.add(new ColisionService.Obstaculo(
                            enemigo.getX(), enemigo.getY(), enemigo.getRadioColision()));
                }
            }
        }
        return obstaculosMovimiento;
    }

    private void dispararSiCorresponde(Jugador jugador) {
        if (ordenFlechas.isEmpty() || !jugador.puedeDisparar()) return;
        switch (ordenFlechas.peekLast()) {
            case UP -> disparar(jugador, 0, -1);
            case DOWN -> disparar(jugador, 0, 1);
            case LEFT -> disparar(jugador, -1, 0);
            case RIGHT -> disparar(jugador, 1, 0);
            default -> { return; }
        }
        double intervalo = jugador.getProyectilEquipado() == ProyectilEquipable.LAGRIMA
                ? INTERVALO_LAGRIMA : INTERVALO_DISPARO;
        jugador.iniciarRecargaDisparo(intervalo);
    }

    private void disparar(Jugador jugador, double dx, double dy) {
        ProyectilEquipable equipado = jugador.getProyectilEquipado();
        Proyectil modelo = new Proyectil(ctx.x, ctx.y, dx * VELOCIDAD_PROYECTIL, dy * VELOCIDAD_PROYECTIL,
                ctx.mejoras.calcularDanoProyectil(jugador, equipado.getDano()), false, Proyectil.Origen.JUGADOR);
        ProyectilVisual visual = new ProyectilVisual(modelo, equipado.getRutaAsset(), colorProyectil(equipado), false);
        ctx.proyectilesJugador.add(visual);
        ctx.vista.agregarProyectil(visual);
        ctx.jugadorVisual.retroceso(dx, dy);
        ctx.efectos.disparo(ctx.x, ctx.y, dx, dy, visual.getColor());
    }

    static Color colorProyectil(ProyectilEquipable tipo) {
        return tipo == ProyectilEquipable.LAGRIMA ? Color.web("#4fd6e8") : Color.web("#ffc93a");
    }
}
