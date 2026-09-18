package com.wachin.model;

import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.exception.ExperienciaInsuficienteException;

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

    public void ganarExperiencia(int cantidad) {
        if (cantidad > 0) {
            experiencia += cantidad;
        }
    }

    public void subirNivel(int experienciaNecesaria)
            throws ExperienciaInsuficienteException {

        if (experiencia < experienciaNecesaria) {
            throw new ExperienciaInsuficienteException(
                "No hay suficiente experiencia para subir de nivel."
            );
        }

        nivel++;
        experiencia -= experienciaNecesaria;
    }

    public void gastarEnergia(int cantidad)
            throws EnergiaInsuficienteException {

        if (cantidad > energia) {
            throw new EnergiaInsuficienteException(
                "No hay suficiente energía para realizar esta acción."
            );
        }

        energia -= cantidad;
    }

    public void recuperarEnergia(int cantidad) {

        energia += cantidad;

        if (energia > energiaMaxima) {
            energia = energiaMaxima;
        }
    }

    @Override
    public void atacar(Entidad objetivo) throws Exception {
        objetivo.recibirDano(getDano());
    }
}