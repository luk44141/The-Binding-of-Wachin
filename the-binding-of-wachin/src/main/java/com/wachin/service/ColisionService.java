package com.wachin.service;

import com.wachin.model.Proyectil;

/** Calcula colisiones entre entidades usando sus recorridos del fotograma. */
/**
 * Detecta contactos entre entidades comparando sus recorridos en cada fotograma.
 */
public final class ColisionService {
    public record Obstaculo(double x, double y, double radio) { }
    public record Posicion(double x, double y) { }
    /**
     * Compara las trayectorias del proyectil y el enemigo con sus radios combinados.
     */
    public boolean colisionaProyectilEnemigo(Proyectil proyectil,
            double enemigoXAnterior, double enemigoYAnterior,
            double enemigoX, double enemigoY, double radioEnemigo) {
        return segmentosSeCruzan(proyectil.getXAnterior(), proyectil.getYAnterior(),
                proyectil.getX(), proyectil.getY(), enemigoXAnterior,
                enemigoYAnterior, enemigoX, enemigoY,
                proyectil.getRadioColision() + radioEnemigo);
    }

    /**
     * Compara el movimiento relativo de jugador y enemigo para detectar un cruce entre fotogramas.
     */
    public boolean colisionaJugadorEnemigo(double jugadorXAnterior,
            double jugadorYAnterior, double jugadorX, double jugadorY,
            double enemigoXAnterior, double enemigoYAnterior,
            double enemigoX, double enemigoY, double radioJugador, double radioEnemigo) {
        return segmentosSeCruzan(jugadorXAnterior - enemigoXAnterior,
                jugadorYAnterior - enemigoYAnterior,
                jugadorX - enemigoX, jugadorY - enemigoY,
                0, 0, 0, 0, radioJugador + radioEnemigo + 2);
    }

    /** Detecta impactos de proyectiles enemigos contra el recorrido del jugador. */
    public boolean colisionaProyectilJugador(Proyectil proyectil,
            double jugadorXAnterior, double jugadorYAnterior,
            double jugadorX, double jugadorY, double radioJugador) {
        if (proyectil == null || !proyectil.esHostil()) return false;
        return segmentosSeCruzan(proyectil.getXAnterior(), proyectil.getYAnterior(),
                proyectil.getX(), proyectil.getY(), jugadorXAnterior, jugadorYAnterior,
                jugadorX, jugadorY, proyectil.getRadioColision() + radioJugador);
    }

    /** Resuelve movimiento por ejes para permitir deslizarse junto a una entidad. */
    public Posicion resolverMovimiento(double xActual, double yActual,
            double xDeseado, double yDeseado, double radio,
            java.util.List<Obstaculo> obstaculos,
            double minX, double maxX, double minY, double maxY) {
        double x = Math.max(minX, Math.min(maxX, xActual));
        double y = Math.max(minY, Math.min(maxY, yActual));
        double candidatoX = Math.max(minX, Math.min(maxX, xDeseado));
        double candidatoY = Math.max(minY, Math.min(maxY, yDeseado));
        if (libreMovimiento(x, y, candidatoX, y, radio, obstaculos)) x = candidatoX;
        if (libreMovimiento(x, y, x, candidatoY, radio, obstaculos)) y = candidatoY;
        return new Posicion(x, y);
    }

    private boolean libreMovimiento(double x1, double y1, double x2, double y2,
            double radio, java.util.List<Obstaculo> obstaculos) {
        if (obstaculos == null) return true;
        for (Obstaculo obstaculo : obstaculos) {
            if (obstaculo == null) continue;
            double minimo = radio + obstaculo.radio();
            double inicial = Math.hypot(x1 - obstaculo.x(), y1 - obstaculo.y());
            double finalDistancia = Math.hypot(x2 - obstaculo.x(), y2 - obstaculo.y());
            double dx = x2 - x1, dy = y2 - y1;
            double longitud2 = dx * dx + dy * dy;
            double t = longitud2 == 0 ? 0
                    : ((obstaculo.x() - x1) * dx + (obstaculo.y() - y1) * dy) / longitud2;
            t = Math.max(0, Math.min(1, t));
            double cercanoX = x1 + t * dx, cercanoY = y1 + t * dy;
            double minimoTrayecto = Math.hypot(cercanoX - obstaculo.x(), cercanoY - obstaculo.y());
            boolean yaSuperpuestoYSeAleja = inicial < minimo && finalDistancia > inicial;
            if (minimoTrayecto < minimo && !yaSuperpuestoYSeAleja) return false;
        }
        return true;
    }

    public boolean colisionaProyectilCirculo(Proyectil proyectil,
            double xAnterior, double yAnterior, double x, double y, double radio) {
        return segmentosSeCruzan(proyectil.getXAnterior(), proyectil.getYAnterior(),
                proyectil.getX(), proyectil.getY(), xAnterior, yAnterior, x, y,
                proyectil.getRadioColision() + radio);
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
        return Math.hypot(bx2 - cercanoX, by2 - cercanoY) <= radio;
    }

    /**
     * Devuelve la distancia euclidiana entre dos posiciones.
     */
    public double distancia(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }
}
