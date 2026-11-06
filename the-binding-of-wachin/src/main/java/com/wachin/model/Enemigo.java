package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

/**
 * Especializa Entidad con tipo y velocidad; puede redefinir operaciones heredadas mediante polimorfismo.
 */
public class Enemigo extends Entidad {
    /**
     * Constante que define tipoenemigo para este componente.
     */
    private final String tipoEnemigo;
    /**
     * Guarda velocidad para que la clase mantenga su estado interno.
     */
    private final double velocidad;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Enemigo(String nombre, int vidaMaxima, int dano, String tipoEnemigo, double velocidad)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano);
        if (tipoEnemigo == null || tipoEnemigo.isBlank()) throw new IllegalArgumentException("El tipo de enemigo es obligatorio.");
        if (!Double.isFinite(velocidad) || velocidad < 0) throw new IllegalArgumentException("La velocidad debe ser finita y no negativa.");
        this.tipoEnemigo = tipoEnemigo;
        this.velocidad = velocidad;
        setTipo("ENEMIGO");
    }

    /** Constructor usado al reconstruir enemigos desde la base de datos. */
    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Enemigo(int id, String nombre, int vida, int vidaMaxima, int dano,
                   String tipoEnemigo, double velocidad)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "ENEMIGO");
        if (tipoEnemigo == null || tipoEnemigo.isBlank()) throw new IllegalArgumentException("El tipo de enemigo es obligatorio.");
        if (!Double.isFinite(velocidad) || velocidad < 0) throw new IllegalArgumentException("La velocidad debe ser finita y no negativa.");
        this.tipoEnemigo = tipoEnemigo;
        this.velocidad = velocidad;
    }

    /**
     * Devuelve tipo de enemigo sin exponer directamente el atributo.
     */
    public String getTipoEnemigo() { return tipoEnemigo; }
    /**
     * Devuelve velocidad sin exponer directamente el atributo.
     */
    public double getVelocidad() { return velocidad; }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
