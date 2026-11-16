package com.wachin.service;

import com.wachin.model.Enemigo;
import java.util.List;

/**
 * Reglas de desplazamiento y disparo de enemigos normales, independientes de JavaFX.
 *
 * <p>Los perseguidores se reparten en ángulos distintos alrededor del jugador (según
 * {@link Enemigo#getOrdenDispersion()}) y solo convergen al acercarse, de modo que lo rodean en
 * lugar de formar una fila. Los tiradores mantienen la distancia: se acercan hasta su alcance y
 * retroceden si el jugador se acerca demasiado.</p>
 */
public final class EnemigoIAService {
    private static final double ALCANCE_TIRADOR = 350;
    private static final double DISTANCIA_SEGURA_TIRADOR = 170;
    private static final double VELOCIDAD_PROYECTIL = 330;
    private static final double INTERVALO_DISPARO = 3.0;
    private static final double RADIO_FLANQUEO = 90;
    private static final double ANGULO_ENTRE_ENEMIGOS = 2.4;

    public record Disparo(double x, double y, double velocidadX, double velocidadY, int dano) { }
    public record Actualizacion(double x, double y, List<Disparo> disparos) { }

    /** Calcula movimiento y disparos de los tipos existentes. */
    public Actualizacion actualizar(Enemigo enemigo, double x, double y,
            double objetivoX, double objetivoY, double deltaTiempo) {
        if (enemigo == null || deltaTiempo <= 0) return new Actualizacion(x, y, List.of());
        if (!"TIRADOR".equals(enemigo.getTipoEnemigo())) {
            double[] posicion = calcularPersecucion(enemigo, x, y, objetivoX, objetivoY, deltaTiempo);
            return new Actualizacion(posicion[0], posicion[1], List.of());
        }

        enemigo.reducirTiempoAtaque(deltaTiempo);
        double dx = objetivoX - x;
        double dy = objetivoY - y;
        double distancia = Math.max(1, Math.hypot(dx, dy));
        double nuevaX = x;
        double nuevaY = y;
        if (distancia > ALCANCE_TIRADOR) {
            double avance = Math.min(distancia - ALCANCE_TIRADOR, enemigo.getVelocidad() * deltaTiempo);
            nuevaX += dx / distancia * avance;
            nuevaY += dy / distancia * avance;
        } else if (distancia < DISTANCIA_SEGURA_TIRADOR) {
            double retroceso = enemigo.getVelocidad() * 0.8 * deltaTiempo;
            nuevaX -= dx / distancia * retroceso;
            nuevaY -= dy / distancia * retroceso;
        }
        if (distancia > ALCANCE_TIRADOR || enemigo.getTiempoAtaque() > 0) {
            return new Actualizacion(nuevaX, nuevaY, List.of());
        }
        enemigo.iniciarTiempoAtaque(INTERVALO_DISPARO);
        return new Actualizacion(nuevaX, nuevaY, List.of(new Disparo(x, y,
                dx / distancia * VELOCIDAD_PROYECTIL,
                dy / distancia * VELOCIDAD_PROYECTIL, enemigo.getDano())));
    }

    /**
     * Calcula la siguiente posición de persecución. El enemigo de orden 0 va directo al objetivo; los
     * demás apuntan, mientras están lejos, a un punto desplazado según su orden de dispersión y
     * convergen al acercarse.
     */
    public double[] calcularPersecucion(Enemigo enemigo, double x, double y,
            double objetivoX, double objetivoY, double deltaTiempo) {
        if (enemigo == null || deltaTiempo <= 0) return new double[]{x, y};
        double distanciaReal = Math.hypot(objetivoX - x, objetivoY - y);
        double lejania = enemigo.getOrdenDispersion() == 0 ? 0 : Math.max(0, Math.min(1, (distanciaReal - 70) / 220));
        double angulo = enemigo.getOrdenDispersion() * ANGULO_ENTRE_ENEMIGOS;
        double metaX = objetivoX + Math.cos(angulo) * RADIO_FLANQUEO * lejania;
        double metaY = objetivoY + Math.sin(angulo) * RADIO_FLANQUEO * lejania;
        double dx = metaX - x;
        double dy = metaY - y;
        double distancia = Math.hypot(dx, dy);
        if (distancia <= 1) return new double[]{x, y};
        double avance = Math.min(distancia, enemigo.getVelocidad() * deltaTiempo);
        return new double[]{x + dx / distancia * avance, y + dy / distancia * avance};
    }
}
