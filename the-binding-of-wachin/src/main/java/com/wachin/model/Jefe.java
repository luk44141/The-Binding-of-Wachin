package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Jefe extends Enemigo {
    private final int nivelJefe;
    private final String ataqueEspecial;
    private final int danoAdicional;

    public Jefe(String nombre, int vidaMaxima, int dano, String tipoEnemigo,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano, tipoEnemigo, velocidad);
        if (nivelJefe < 1 || danoAdicional < 0) throw new IllegalArgumentException("Nivel o dano especial invalido.");
        if (ataqueEspecial == null || ataqueEspecial.isBlank()) throw new IllegalArgumentException("El ataque especial es obligatorio.");
        this.nivelJefe = nivelJefe;
        this.ataqueEspecial = ataqueEspecial;
        this.danoAdicional = danoAdicional;
        setTipo("JEFE");
    }

    /** Constructor de reconstruccion con valores persistidos. */
    public Jefe(int id, String nombre, int vida, int vidaMaxima, int dano,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "JEFE", velocidad);
        if (nivelJefe < 1 || danoAdicional < 0 || ataqueEspecial == null || ataqueEspecial.isBlank()) {
            throw new IllegalArgumentException("Los datos del jefe son invalidos.");
        }
        this.nivelJefe = nivelJefe;
        this.ataqueEspecial = ataqueEspecial;
        this.danoAdicional = danoAdicional;
        setTipo("JEFE");
    }

    public int getNivelJefe() { return nivelJefe; }
    public String getAtaqueEspecial() { return ataqueEspecial; }
    public int getDanoAdicional() { return danoAdicional; }

    public void ataqueEspecial(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano() + danoAdicional);
    }
}
