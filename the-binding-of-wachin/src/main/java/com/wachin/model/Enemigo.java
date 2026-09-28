package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Enemigo extends Entidad {

    private String tipoEnemigo;
    private double velocidad;

    public Enemigo(String nombre, int vidaMaxima, int dano,
                   String tipoEnemigo, double velocidad)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {

        super(nombre, vidaMaxima, dano);

        this.tipoEnemigo = tipoEnemigo;
        this.velocidad = velocidad;
        setTipo("ENEMIGO");
    }

    public String getTipoEnemigo() {
        return tipoEnemigo;
    }

    public double getVelocidad() {
        return velocidad;
    }

    @Override
    public void atacar(Entidad objetivo) {
        objetivo.recibirDano(getDano());
    }
}