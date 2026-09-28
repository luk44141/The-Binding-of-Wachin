package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Invocacion extends Entidad {

    private int costoEnergia;
    private int duracion;

    public Invocacion(String nombre, int vidaMaxima, int dano,
                      int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {

        super(nombre, vidaMaxima, dano);

        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
        setTipo("INVOCACION");
    }

    public int getCostoEnergia() {
        return costoEnergia;
    }

    public int getDuracion() {
        return duracion;
    }

    @Override
    public void atacar(Entidad objetivo) {
        objetivo.recibirDano(getDano());
    }
}