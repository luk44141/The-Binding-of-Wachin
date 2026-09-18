package com.wachin.model;

public class Jugador extends Entidad {

    private int experiencia;
    private int nivel;
    private int monedas;
    private int energia;
    private int energiaMaxima;

    public Jugador(String nombre, int vidaMaxima, int dano, int energiaMaxima) {
        super(nombre, vidaMaxima, dano);

        this.experiencia = 0;
        this.nivel = 1;
        this.monedas = 0;
        this.energiaMaxima = energiaMaxima;
        this.energia = energiaMaxima;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public int getNivel() {
        return nivel;
    }

    public int getMonedas() {
        return monedas;
    }

    public int getEnergia() {
        return energia;
    }

    public int getEnergiaMaxima() {
        return energiaMaxima;
    }

    @Override
    public void atacar(Entidad objetivo) throws Exception {
        objetivo.recibirDano(getDano());
    }
}