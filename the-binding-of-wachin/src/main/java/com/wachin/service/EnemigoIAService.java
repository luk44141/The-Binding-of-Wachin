package com.wachin.service;

import com.wachin.model.Enemigo;
import java.util.List;

/** Reglas de desplazamiento de enemigos normales, independientes de JavaFX. */
public final class EnemigoIAService {
    private static final double ALCANCE_TIRADOR = 350;
    private static final double VELOCIDAD_PROYECTIL = 330;
    private static final double INTERVALO_DISPARO = 3.0;
    public record Disparo(double x, double y, double velocidadX, double velocidadY, int dano) { }
    public record Actualizacion(double x, double y, List<Disparo> disparos) { }

    /** Calcula movimiento y disparos de los tipos existentes sin depender de JavaFX. */
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
        }
        if (distancia > ALCANCE_TIRADOR || enemigo.getTiempoAtaque() > 0) {
            return new Actualizacion(nuevaX, nuevaY, List.of());
        }
        enemigo.iniciarTiempoAtaque(INTERVALO_DISPARO);
        return new Actualizacion(nuevaX, nuevaY, List.of(new Disparo(x, y,
                dx / distancia * VELOCIDAD_PROYECTIL,
                dy / distancia * VELOCIDAD_PROYECTIL, enemigo.getDano())));
    }

    /** Calcula la siguiente posición de persecución respetando velocidad y delta de tiempo. */
    public double[] calcularPersecucion(Enemigo enemigo, double x, double y,
            double objetivoX, double objetivoY, double deltaTiempo) {
        if (enemigo == null || deltaTiempo <= 0) return new double[]{x, y};
        double dx = objetivoX - x;
        double dy = objetivoY - y;
        double distancia = Math.hypot(dx, dy);
        if (distancia <= 1) return new double[]{x, y};
        double avance = Math.min(distancia, enemigo.getVelocidad() * deltaTiempo);
        return new double[]{x + dx / distancia * avance, y + dy / distancia * avance};
    }
}
