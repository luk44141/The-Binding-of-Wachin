package com.wachin.service;

import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.Proyectil;

/** Calcula colisiones entre entidades usando sus recorridos del fotograma. */
/**
 * Detecta contactos entre entidades comparando sus recorridos en cada fotograma.
 */
public final class ColisionService {
    /**
     * Compara las trayectorias del proyectil y el enemigo con sus radios combinados.
     */
    public boolean colisionaProyectilEnemigo(Proyectil proyectil,
            EnemigoVisual enemigo) {
        return segmentosSeCruzan(proyectil.getXAnterior(), proyectil.getYAnterior(),
                proyectil.getX(), proyectil.getY(), enemigo.getXAnterior(),
                enemigo.getYAnterior(), enemigo.getX(), enemigo.getY(),
                proyectil.getRadioColision() + enemigo.getRadioColision());
    }

    /**
     * Compara el movimiento relativo de jugador y enemigo para detectar un cruce entre fotogramas.
     */
    public boolean colisionaJugadorEnemigo(double jugadorXAnterior,
            double jugadorYAnterior, double jugadorX, double jugadorY,
            EnemigoVisual enemigo, double radioJugador) {
        return segmentosSeCruzan(jugadorXAnterior - enemigo.getXAnterior(),
                jugadorYAnterior - enemigo.getYAnterior(),
                jugadorX - enemigo.getX(), jugadorY - enemigo.getY(),
                0, 0, 0, 0, radioJugador + enemigo.getRadioColision());
    }

    /**
     * Calcula el punto más cercano entre segmentos y lo compara con el radio de colisión.
     */
    public boolean segmentosSeCruzan(double ax1, double ay1, double ax2, double ay2,
            double bx1, double by1, double bx2, double by2, double radio) {
        /**
         * Guarda dx para que la clase mantenga su estado interno.
         */
        double dx = ax2 - ax1;
        /**
         * Guarda dy para que la clase mantenga su estado interno.
         */
        double dy = ay2 - ay1;
        /**
         * Constante que define longitudcuadrada para este componente.
         */
        double longitudCuadrada = dx * dx + dy * dy;
        /**
         * Guarda t para que la clase mantenga su estado interno.
         */
        double t = longitudCuadrada == 0 ? 0
                : ((bx2 - ax1) * dx + (by2 - ay1) * dy) / longitudCuadrada;
        t = Math.max(0, Math.min(1, t));
        /**
         * Constante que define cercanox para este componente.
         */
        double cercanoX = ax1 + t * dx;
        /**
         * Constante que define cercanoy para este componente.
         */
        double cercanoY = ay1 + t * dy;
        return Math.hypot(bx2 - cercanoX, by2 - cercanoY) < radio;
    }

    /**
     * Devuelve la distancia euclidiana entre dos posiciones.
     */
    public double distancia(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}
