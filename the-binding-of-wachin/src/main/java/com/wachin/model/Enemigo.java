package com.wachin.model;

public class Enemigo extends Entidad {

    private String tipoEnemigo;
    private double velocidad;

    public Enemigo(String nombre, int vidaMaxima, int dano,
                   String tipoEnemigo, double velocidad) {

        super(nombre, vidaMaxima, dano);

        this.tipoEnemigo = tipoEnemigo;
        this.velocidad = velocidad;
    }

    public String getTipoEnemigo() {
        return tipoEnemigo;
    }

    public double getVelocidad() {
        return velocidad;
    }

    @Override
    public void atacar(Entidad objetivo) throws Exception {
        objetivo.recibirDano(getDano());
    }
}