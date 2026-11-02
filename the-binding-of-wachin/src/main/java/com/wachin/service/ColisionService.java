package com.wachin.service;

import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.Proyectil;

/** Calcula colisiones entre entidades usando sus recorridos del fotograma. */
public final class ColisionService {
    public boolean colisionaProyectilEnemigo(Proyectil proyectil,
            EnemigoVisual enemigo) {
        return segmentosSeCruzan(proyectil.getXAnterior(), proyectil.getYAnterior(),
                proyectil.getX(), proyectil.getY(), enemigo.getXAnterior(),
                enemigo.getYAnterior(), enemigo.getX(), enemigo.getY(),
                proyectil.getRadioColision() + enemigo.getRadioColision());
    }

    public boolean colisionaJugadorEnemigo(double jugadorXAnterior,
            double jugadorYAnterior, double jugadorX, double jugadorY,
            EnemigoVisual enemigo, double radioJugador) {
        return segmentosSeCruzan(jugadorXAnterior - enemigo.getXAnterior(),
                jugadorYAnterior - enemigo.getYAnterior(),
                jugadorX - enemigo.getX(), jugadorY - enemigo.getY(),
                0, 0, 0, 0, radioJugador + enemigo.getRadioColision());
    }

    public boolean segmentosSeCruzan(double ax1, double ay1, double ax2, double ay2,
            double bx1, double by1, double bx2, double by2, double radio) {
        double dx = ax2 - ax1;
        double dy = ay2 - ay1;
        double longitudCuadrada = dx * dx + dy * dy;
        double t = longitudCuadrada == 0 ? 0
                : ((bx2 - ax1) * dx + (by2 - ay1) * dy) / longitudCuadrada;
        t = Math.max(0, Math.min(1, t));
        double cercanoX = ax1 + t * dx;
        double cercanoY = ay1 + t * dy;
        return Math.hypot(bx2 - cercanoX, by2 - cercanoY) < radio;
    }

    public double distancia(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}
