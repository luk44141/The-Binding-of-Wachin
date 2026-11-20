package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.Proyectil;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.HudView.Tono;
import com.wachin.view.game.ProyectilVisual;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

/** Proyectiles, daño y muertes: resuelve quién golpea a quién y qué ocurre después. */
final class CombateController {

    private static final Logger REGISTRO = Logger.getLogger(CombateController.class.getName());
    static final double TIEMPO_INVULNERABLE = 1.15;
    private static final double RADIO_PROYECTIL_JUGADOR_CONTRA_INVOCACION = 21;
    private static final double RADIO_IMPACTO_JUGADOR = 25;

    private final ContextoPartida ctx;
    private final ObjetosController objetos;

    CombateController(ContextoPartida ctx, ObjetosController objetos) {
        this.ctx = ctx;
        this.objetos = objetos;
    }

    // ───────────────────────────── proyectiles del jugador y las invocaciones ─────────────────────────────

    void actualizarProyectilesJugador(double dt) {
        Iterator<ProyectilVisual> iterador = ctx.proyectilesJugador.iterator();
        while (iterador.hasNext()) {
            ProyectilVisual visual = iterador.next();
            Proyectil proyectil = visual.getModelo();
            visual.actualizar(dt);
            ctx.vista.efectos().estela(proyectil.getX(), proyectil.getY(), visual.getColor());
            if (proyectil.estaFueraDeLaSala()) {
                ctx.efectos.impactoPared(Math.max(50, Math.min(950, proyectil.getX())),
                        Math.max(50, Math.min(650, proyectil.getY())), visual.getColor());
                ctx.vista.quitarProyectil(visual);
                iterador.remove();
            } else if (impactarEnemigo(visual)) {
                ctx.vista.quitarProyectil(visual);
                iterador.remove();
            }
        }
    }

    private boolean impactarEnemigo(ProyectilVisual visual) {
        Proyectil proyectil = visual.getModelo();
        // La simulacion corre en el hilo JavaFX; si el impacto elimina al enemigo, retornamos enseguida.
        for (EnemigoVisual enemigo : ctx.enemigos) {
            if (enemigo.estaApareciendo()) continue;
            if (ctx.colision.colisionaProyectilEnemigo(proyectil, enemigo.getXAnterior(), enemigo.getYAnterior(),
                    enemigo.getX(), enemigo.getY(), enemigo.getRadioColision())) {
                atacarEnemigo(enemigo, proyectil.getDano(), proyectil.getX(), proyectil.getY(), visual.getColor());
                return true;
            }
        }
        return false;
    }

    // ───────────────────────────── proyectiles hostiles ─────────────────────────────

    void actualizarProyectilesHostiles(double dt) {
        Iterator<ProyectilVisual> iterador = ctx.proyectilesHostiles.iterator();
        while (iterador.hasNext()) {
            ProyectilVisual visual = iterador.next();
            Proyectil proyectil = visual.getModelo();
            visual.actualizar(dt);
            ctx.vista.efectos().estela(proyectil.getX(), proyectil.getY(), visual.getColor());
            boolean eliminar = false;
            if (proyectil.estaFueraDeLaSala()) {
                ctx.efectos.impactoPared(Math.max(50, Math.min(950, proyectil.getX())),
                        Math.max(50, Math.min(650, proyectil.getY())), visual.getColor());
                eliminar = true;
            } else if (impactarInvocacion(visual)) {
                eliminar = true;
            } else if (ctx.invulnerable <= 0 && ctx.colision.colisionaProyectilJugador(proyectil,
                    ctx.xAnterior, ctx.yAnterior, ctx.x, ctx.y, RADIO_IMPACTO_JUGADOR)) {
                recibirDanoJugador(proyectil.getDano());
                eliminar = true;
            }
            if (eliminar) {
                ctx.vista.quitarProyectil(visual);
                iterador.remove();
            }
        }
    }

    private boolean impactarInvocacion(ProyectilVisual visual) {
        Proyectil proyectil = visual.getModelo();
        for (Invocacion invocacion : ctx.juego.getInvocacionesActivas()) {
            // Murcielago y Robot no bloquean: los proyectiles enemigos los atraviesan.
            if (!invocacion.puedeBloquearProyectil()) continue;
            if (ctx.colision.colisionaProyectilCirculo(proyectil, invocacion.getX(), invocacion.getY(),
                    invocacion.getX(), invocacion.getY(), RADIO_PROYECTIL_JUGADOR_CONTRA_INVOCACION)) {
                invocacion.bloquearProyectil();
                ctx.vista.efectos().impacto(invocacion.getX(), invocacion.getY(), visual.getColor());
                return true;
            }
        }
        return false;
    }

    // ───────────────────────────── daño ─────────────────────────────

    void recibirDanoJugador(int danoBase) {
        Jugador jugador = ctx.jugador();
        if (jugador == null) return;
        int dano = ctx.modoHardcore ? Math.max(jugador.getVida(), jugador.getVidaMaxima())
                : ctx.combate.calcularDanoRecibido(danoBase, ctx.mapa.getActual().getMapa(),
                        ctx.mapa.getActual().getProfundidad());
        try {
            ctx.combate.aplicarDano(dano, jugador);
        } catch (DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo aplicar daño al jugador.", e);
        }
        ctx.invulnerable = TIEMPO_INVULNERABLE;
        ctx.estadisticas.registrarDanoRecibido(dano);
        ctx.jugadorVisual.destellar();
        ctx.efectos.jugadorHerido(ctx.x, ctx.y, dano);
    }

    /** Aplica daño a un enemigo; si muere, concede experiencia, suelta objetos y lanza los efectos. */
    void atacarEnemigo(EnemigoVisual visual, int dano, double origenX, double origenY, Color color) {
        Enemigo enemigo = visual.getEnemigo();
        try {
            ctx.combate.aplicarDano(dano, enemigo);
        } catch (DanoInvalidoException e) {
            REGISTRO.log(Level.WARNING, "Daño inválido ignorado: " + dano, e);
            return;
        }
        visual.golpeado(origenX, origenY);
        ctx.efectos.impacto(visual.getX(), visual.getY() - 10, dano, color);
        if (ctx.combate.estaDerrotado(enemigo)) derrotar(visual);
    }

    private void derrotar(EnemigoVisual visual) {
        Enemigo enemigo = visual.getEnemigo();
        boolean esJefe = enemigo instanceof Jefe;
        ctx.vista.quitarEnemigo(visual);
        ctx.enemigos.remove(visual);
        ctx.efectos.muerteEnemigo(visual.getX(), visual.getY(), esJefe);
        ctx.estadisticas.registrarEnemigoDerrotado(esJefe);
        int niveles = ctx.juego.derrotarEnemigo(enemigo, ctx.jugadorServicio);
        if (niveles > 0) {
            ctx.efectos.nivelSubido(ctx.x, ctx.y, ctx.jugador().getNivel());
            ctx.vista.mostrarAviso("¡Nivel " + ctx.jugador().getNivel() + "! Vida máxima +" + (niveles * 10)
                    + " y vida recuperada", Tono.ESPECIAL);
        }
        if (esJefe) objetos.soltarRecompensaJefe(ctx.mapa.getNumeroSala(), visual.getX(), visual.getY());
        else objetos.soltarDropEnemigo(visual.getX(), visual.getY());
    }

    /** Crea un proyectil hostil y lo agrega a la escena. */
    void lanzarProyectilHostil(double x, double y, double vx, double vy, int dano, Proyectil.Origen origen,
            String ruta, Color color) {
        Proyectil modelo = new Proyectil(x, y, vx, vy, dano, true, origen);
        ProyectilVisual visual = new ProyectilVisual(modelo, ruta, color, false);
        ctx.proyectilesHostiles.add(visual);
        ctx.vista.agregarProyectil(visual);
    }
}
