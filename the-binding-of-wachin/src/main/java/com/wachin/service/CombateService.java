package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.model.Entidad;

/**
 * Valida y coordina ataques entre entidades.
 */
public class CombateService {

    /** Aplica el escalado ya usado por la partida según el mapa y la profundidad. */
    public int calcularDanoRecibido(int danoBase, int mapa, int profundidad) {
        if (danoBase < 0 || mapa < 1 || profundidad < 1) {
            throw new IllegalArgumentException("Daño, mapa o profundidad inválidos.");
        }
        if (danoBase == 0) return 0;
        double multiplicador = 1.50
                + Math.min(0.90, (mapa - 1) * 0.12)
                + Math.min(0.40, (profundidad - 1) * 0.08);
        return Math.max(Math.addExact(danoBase, 1), (int) Math.ceil(danoBase * multiplicador));
    }

    /**
     * Aplica al objetivo el daño definido por esta entidad.
     */
    public void atacar(Entidad atacante, Entidad objetivo)
            throws DanoInvalidoException {

        if (atacante == null || objetivo == null) {
            return;
        }

        if (atacante.getDano() < 0) {
            throw new DanoInvalidoException(
                    "El daño del atacante no puede ser negativo."
            );
        }

        try {
            atacante.atacar(objetivo);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo ejecutar el ataque.",
                    e
            );
        }
    }

    public void aplicarDano(int dano, Entidad objetivo) throws DanoInvalidoException {
        if (objetivo == null) return;
        if (dano < 0) throw new DanoInvalidoException("El daño no puede ser negativo.");
        objetivo.recibirDano(dano);
    }

    /**
     * Indica si la entidad tiene vida mayor que cero.
     */
    public boolean estaVivo(Entidad entidad) {
        return entidad != null && entidad.getVida() > 0;
    }

    /**
     * Indica si la entidad no existe o su vida llegó a cero.
     */
    public boolean estaDerrotado(Entidad entidad) {
        return entidad == null || entidad.getVida() <= 0;
    }
}
