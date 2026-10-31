package com.wachin.service;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.model.Entidad;

public class CombateService {

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

    public boolean estaVivo(Entidad entidad) {
        return entidad != null && entidad.getVida() > 0;
    }

    public boolean estaDerrotado(Entidad entidad) {
        return entidad == null || entidad.getVida() <= 0;
    }
}