package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.exception.ExperienciaInsuficienteException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Jugador extends Entidad {

    private int experiencia;
    private int nivel;
    private int monedas;
    private int energia;
    private int energiaMaxima;

    public Jugador(String nombre, int vidaMaxima, int dano, int energiaMaxima)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {

        super(nombre, vidaMaxima, dano);

        this.experiencia = 0;
        this.nivel = 1;
        this.monedas = 0;
        this.energiaMaxima = energiaMaxima;
        this.energia = energiaMaxima;
    }

    public Jugador(int id, String nombre, int vida, int vidaMaxima,
                   int dano, int experiencia, int nivel,
                   int monedas, int energia, int energiaMaxima)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {

        super(id, nombre, vida, vidaMaxima, dano, "JUGADOR");

        this.experiencia = experiencia;
        this.nivel = nivel;
        this.monedas = monedas;
        this.energia = energia;
        this.energiaMaxima = energiaMaxima;
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
    public void atacar(Entidad objetivo) {
        objetivo.recibirDano(getDano());
    }
}