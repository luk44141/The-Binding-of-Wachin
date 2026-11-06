package com.wachin.interfaces;

import com.wachin.model.Entidad;

/**
 * Interfaz que abstrae las operaciones de ataque y recepción de daño exigidas a sus implementaciones.
 */
public interface Atacable {

    /**
     * Aplica al objetivo el daño definido por esta entidad.
     */
    void atacar(Entidad objetivo) throws Exception;

    /**
     * Reduce la vida sin permitir daño negativo ni valores de vida inferiores a cero.
     */
    void recibirDano(int dano) throws Exception;
}
