package com.wachin.interfaces;

import com.wachin.model.Entidad;

public interface Atacable {

    void atacar(Entidad objetivo) throws Exception;

    void recibirDano(int dano) throws Exception;
}