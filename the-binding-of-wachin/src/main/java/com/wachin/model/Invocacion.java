package com.wachin.model;

public class Invocacion extends Entidad {

    private int costoEnergia;
    private int duracion;

    public Invocacion(String nombre, int vidaMaxima, int dano,
                      int costoEnergia, int duracion) {

        super(nombre, vidaMaxima, dano);

        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
    }

    public int getCostoEnergia() {
        return costoEnergia;
    }

    public int getDuracion() {
        return duracion;
    }

    @Override
    public void atacar(Entidad objetivo) throws Exception {
        objetivo.recibirDano(getDano());
    }
}