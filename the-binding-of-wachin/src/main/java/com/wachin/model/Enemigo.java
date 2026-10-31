package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Enemigo extends Entidad {
    private final String tipoEnemigo;
    private final double velocidad;

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
    public Enemigo(int id, String nombre, int vida, int vidaMaxima, int dano,
                   String tipoEnemigo, double velocidad)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "ENEMIGO");
        if (tipoEnemigo == null || tipoEnemigo.isBlank()) throw new IllegalArgumentException("El tipo de enemigo es obligatorio.");
        if (!Double.isFinite(velocidad) || velocidad < 0) throw new IllegalArgumentException("La velocidad debe ser finita y no negativa.");
        this.tipoEnemigo = tipoEnemigo;
        this.velocidad = velocidad;
    }

    public String getTipoEnemigo() { return tipoEnemigo; }
    public double getVelocidad() { return velocidad; }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
