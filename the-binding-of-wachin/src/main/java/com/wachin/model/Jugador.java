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
        validarRecursos(0, 1, 0, energiaMaxima, energiaMaxima);
        this.experiencia = 0;
        this.nivel = 1;
        this.monedas = 0;
        this.energiaMaxima = energiaMaxima;
        this.energia = energiaMaxima;
    }

    public Jugador(int id, String nombre, int vida, int vidaMaxima, int dano,
                   int experiencia, int nivel, int monedas, int energia, int energiaMaxima)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "JUGADOR");
        validarRecursos(experiencia, nivel, monedas, energia, energiaMaxima);
        this.experiencia = experiencia;
        this.nivel = nivel;
        this.monedas = monedas;
        this.energia = energia;
        this.energiaMaxima = energiaMaxima;
    }

    private void validarRecursos(int exp, int nivel, int monedas, int energia, int energiaMaxima) {
        if (exp < 0 || nivel < 1 || monedas < 0 || energiaMaxima < 0
                || energia < 0 || energia > energiaMaxima) {
            throw new IllegalArgumentException("Los recursos del jugador contienen valores invalidos.");
        }
    }

    public int getExperiencia() { return experiencia; }
    public int getNivel() { return nivel; }
    public int getMonedas() { return monedas; }
    public int getEnergia() { return energia; }
    public int getEnergiaMaxima() { return energiaMaxima; }

    public void ganarExperiencia(int cantidad) { if (cantidad > 0) experiencia += cantidad; }

    public void subirNivel(int experienciaNecesaria) throws ExperienciaInsuficienteException {
        if (experienciaNecesaria <= 0) throw new IllegalArgumentException("La experiencia necesaria debe ser positiva.");
        if (experiencia < experienciaNecesaria) {
            throw new ExperienciaInsuficienteException("No hay suficiente experiencia para subir de nivel.");
        }
        nivel++;
        experiencia -= experienciaNecesaria;
    }

    public void gastarEnergia(int cantidad) throws EnergiaInsuficienteException {
        if (cantidad < 0) throw new IllegalArgumentException("No se puede gastar energia negativa.");
        if (cantidad > energia) throw new EnergiaInsuficienteException("No hay suficiente energia para realizar esta accion.");
        energia -= cantidad;
    }

    public void recuperarEnergia(int cantidad) {
        if (cantidad <= 0) return;
        energia = Math.min(energiaMaxima, energia + cantidad);
    }

    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
