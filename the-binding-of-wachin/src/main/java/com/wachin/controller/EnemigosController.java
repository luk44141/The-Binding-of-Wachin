package com.wachin.controller;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.model.Proyectil;
import com.wachin.service.ColisionService;
import com.wachin.service.EnemigoIAService;
import com.wachin.service.JefeService;
import com.wachin.view.game.CatalogoAssets;
import com.wachin.view.game.EnemigoVisual;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/**
 * Comportamiento de enemigos y jefes: persecución con dispersión, ataques, avisos y daño por contacto.
 * Los enemigos se empujan entre sí y se bloquean mutuamente, de modo que rodean al jugador en lugar
 * de apilarse o atravesarse.
 */
final class EnemigosController {

    private static final double VELOCIDAD_SEPARACION = 110;
    private static final double RADIO_ZONA = 73;
    private static final Color COLOR_TIRADOR = Color.web("#ff7a2a");

    private final ContextoPartida ctx;
    private final CombateController combate;
    private boolean zonasActivasPrevias;

    EnemigosController(ContextoPartida ctx, CombateController combate) {
        this.ctx = ctx;
        this.combate = combate;
    }

    void actualizar(double dt) {
        List<EnemigoVisual> actuales = new ArrayList<>(ctx.enemigos);
        boolean activa = ctx.partidaActiva();
        boolean jefePresente = false;
        for (EnemigoVisual visual : actuales) {
            visual.actualizar(dt);
            if (!activa || visual.estaApareciendo()) continue;
            double deseadoX;
            double deseadoY;
            if (visual.getEnemigo() instanceof Jefe jefe) {
                jefePresente = true;
                double[] destino = actualizarJefe(visual, jefe, dt);
                deseadoX = destino[0];
                deseadoY = destino[1];
            } else {
                EnemigoIAService.Actualizacion accion = ctx.enemigoIA.actualizar(visual.getEnemigo(),
                        visual.getX(), visual.getY(), ctx.x, ctx.y, dt);
                for (EnemigoIAService.Disparo disparo : accion.disparos()) {
                    combate.lanzarProyectilHostil(disparo.x(), disparo.y(), disparo.velocidadX(), disparo.velocidadY(),
                            disparo.dano(), Proyectil.Origen.ENEMIGO, CatalogoAssets.PROYECTIL_TIRADOR, COLOR_TIRADOR);
                    ctx.efectos.disparo(disparo.x(), disparo.y(), disparo.velocidadX() / 330, disparo.velocidadY() / 330, COLOR_TIRADOR);
                }
                ColisionService.Obstaculo[] otros = obstaculosDeOtros(visual, actuales);
                ColisionService.Posicion empuje = ctx.colision.empujeSeparacion(visual.getX(), visual.getY(),
                        visual.getRadioColision(), List.of(otros), 1.0);
                deseadoX = accion.x() + empuje.x() * VELOCIDAD_SEPARACION * dt;
                deseadoY = accion.y() + empuje.y() * VELOCIDAD_SEPARACION * dt;
            }
            List<ColisionService.Obstaculo> bloqueos = new ArrayList<>();
            if (ctx.invulnerable <= 0) bloqueos.add(new ColisionService.Obstaculo(ctx.x, ctx.y, ContextoPartida.RADIO_JUGADOR));
            if (!(visual.getEnemigo() instanceof Jefe)) bloqueos.addAll(List.of(obstaculosDeOtros(visual, actuales)));
            ColisionService.Posicion resuelta = ctx.colision.resolverMovimiento(visual.getX(), visual.getY(),
                    deseadoX, deseadoY, visual.getRadioColision(), bloqueos, 75, 925, 75, 625);
            visual.actualizarPosicion(resuelta.x(), resuelta.y());
            if (comprobarContacto(visual)) break;
        }
        if (!jefePresente) zonasActivasPrevias = false;
    }

    private ColisionService.Obstaculo[] obstaculosDeOtros(EnemigoVisual propio, List<EnemigoVisual> todos) {
        return todos.stream().filter(o -> o != propio && !o.estaApareciendo())
                .map(o -> new ColisionService.Obstaculo(o.getX(), o.getY(), o.getRadioColision()))
                .toArray(ColisionService.Obstaculo[]::new);
    }

    private double[] actualizarJefe(EnemigoVisual visual, Jefe jefe, double dt) {
        JefeService.Actualizacion accion = ctx.jefeServicio.actualizar(jefe, visual.getX(), visual.getY(),
                ctx.x, ctx.y, dt);
        List<double[]> zonas = accion.zonas().stream().map(p -> new double[]{p.x(), p.y()}).toList();
        visual.mostrarAvisoJefe(accion.aviso(), accion.direccionX(), accion.direccionY(), zonas);
        Color color = colorProyectilJefe(jefe.getTipoJefe());
        for (JefeService.Disparo disparo : accion.disparos()) {
            combate.lanzarProyectilHostil(disparo.x(), disparo.y(), disparo.vx(), disparo.vy(), disparo.dano(),
                    Proyectil.Origen.JEFE, CatalogoAssets.PROYECTIL_JEFE, color);
        }
        if (!accion.disparos().isEmpty()) ctx.efectos.ataqueJefe(visual.getX(), visual.getY());
        if (accion.zonasActivas() && !zonasActivasPrevias) {
            for (JefeService.Punto zona : accion.zonas()) {
                ctx.vista.efectos().explosion(zona.x(), zona.y(), Retroalimentacion.ROJO, 1.4);
            }
            ctx.vista.sacudir(7);
        }
        zonasActivasPrevias = accion.zonasActivas();
        if (accion.zonasActivas() && ctx.invulnerable <= 0 && accion.zonas().stream()
                .anyMatch(p -> ctx.colision.distancia(ctx.x, ctx.y, p.x(), p.y()) < RADIO_ZONA)) {
            combate.recibirDanoJugador(jefe.getDano() + jefe.getDanoAdicional());
        }
        return new double[]{accion.x(), accion.y()};
    }

    /** Aplica el daño por contacto; devuelve {@code true} si hirió al jugador en este fotograma. */
    private boolean comprobarContacto(EnemigoVisual visual) {
        if (ctx.invulnerable > 0 || !ctx.colision.colisionaJugadorEnemigo(ctx.xAnterior, ctx.yAnterior, ctx.x, ctx.y,
                visual.getXAnterior(), visual.getYAnterior(), visual.getX(), visual.getY(),
                ContextoPartida.RADIO_JUGADOR, visual.getRadioColision())) {
            return false;
        }
        Enemigo enemigo = visual.getEnemigo();
        int dano = enemigo instanceof Jefe jefe ? ctx.jefeServicio.obtenerDanoContacto(jefe) : visual.getDanoContacto();
        combate.recibirDanoJugador(dano);
        return true;
    }

    static Color colorProyectilJefe(Jefe.Tipo tipo) {
        return switch (tipo) {
            case SALA_10 -> Color.web("#b07bff");
            case EMISOR_RAYOS -> Color.web("#4fd6e8");
            case SALA_15 -> Color.web("#ff8a1e");
            case SALA_20 -> Color.web("#ff3a55");
        };
    }
}
