package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

/**
 * Especializa Enemigo con nivel y daño adicional; hereda los datos comunes y añade comportamiento de jefe.
 */
public class Jefe extends Enemigo {
    public enum Tipo { SALA_10, SALA_15, SALA_20 }

    /**
     * Constante que define niveljefe para este componente.
     */
    private final int nivelJefe;
    /**
     * Constante que define ataqueespecial para este componente.
     */
    private final String ataqueEspecial;
    /**
     * Constante que define danoadicional para este componente.
     */
    private final int danoAdicional;
    private final Tipo tipoJefe;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Jefe(String nombre, int vidaMaxima, int dano, String tipoEnemigo,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        this(nombre, vidaMaxima, dano, tipoEnemigo, velocidad, nivelJefe,
                ataqueEspecial, danoAdicional, Tipo.SALA_10);
    }

    public Jefe(String nombre, int vidaMaxima, int dano, String tipoEnemigo,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional,
                Tipo tipoJefe)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano, tipoEnemigo, velocidad);
        if (nivelJefe < 1 || danoAdicional < 0) throw new IllegalArgumentException("Nivel o dano especial invalido.");
        if (ataqueEspecial == null || ataqueEspecial.isBlank()) throw new IllegalArgumentException("El ataque especial es obligatorio.");
        this.nivelJefe = nivelJefe;
        this.ataqueEspecial = ataqueEspecial;
        this.danoAdicional = danoAdicional;
        this.tipoJefe = tipoJefe == null ? Tipo.SALA_10 : tipoJefe;
        setTipo("JEFE");
    }

    /** Constructor de reconstruccion con valores persistidos. */
    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Jefe(int id, String nombre, int vida, int vidaMaxima, int dano,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        this(id, nombre, vida, vidaMaxima, dano, velocidad, nivelJefe,
                ataqueEspecial, danoAdicional, Tipo.SALA_10);
    }

    public Jefe(int id, String nombre, int vida, int vidaMaxima, int dano,
                double velocidad, int nivelJefe, String ataqueEspecial, int danoAdicional,
                Tipo tipoJefe)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "JEFE", velocidad);
        if (nivelJefe < 1 || danoAdicional < 0 || ataqueEspecial == null || ataqueEspecial.isBlank()) {
            throw new IllegalArgumentException("Los datos del jefe son invalidos.");
        }
        this.nivelJefe = nivelJefe;
        this.ataqueEspecial = ataqueEspecial;
        this.danoAdicional = danoAdicional;
        this.tipoJefe = tipoJefe == null ? Tipo.SALA_10 : tipoJefe;
        setTipo("JEFE");
    }

    /**
     * Devuelve nivel del jefe sin exponer directamente el atributo.
     */
    public int getNivelJefe() { return nivelJefe; }
    /**
     * Devuelve ataqueespecial sin exponer directamente el atributo.
     */
    public String getAtaqueEspecial() { return ataqueEspecial; }
    /**
     * Devuelve daño adicional sin exponer directamente el atributo.
     */
    public int getDanoAdicional() { return danoAdicional; }
    public Tipo getTipoJefe() { return tipoJefe; }

    /**
     * Ejecuta la operación ataqueEspecial dentro de la responsabilidad de esta clase.
     */
    public void ataqueEspecial(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano() + danoAdicional);
    }
}
