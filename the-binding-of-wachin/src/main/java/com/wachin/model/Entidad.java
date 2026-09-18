package com.wachin.model;

import com.wachin.interfaces.Atacable;

public abstract class Entidad implements Atacable {

    private String nombre;
    private int vida;
    private int vidaMaxima;
    private int dano;

    public Entidad(String nombre, int vidaMaxima, int dano) {
        this.nombre = nombre;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.dano = dano;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getDano() {
        return dano;
    }

    public void setDano(int dano) {
        this.dano = dano;
    }

    @Override
    public void recibirDano(int dano) {
        this.vida -= dano;

        if (this.vida < 0) {
            this.vida = 0;
        }
    }
}