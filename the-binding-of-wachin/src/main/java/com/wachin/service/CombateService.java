package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.model.Entidad;

/**
 * Valida y coordina ataques entre entidades.
 */
public class CombateService {

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
