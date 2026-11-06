package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

/**
 * Especializa Entidad con costo energético y duración propios.
 */
public class Invocacion extends Entidad {
    /**
     * Constante que define costoenergia para este componente.
     */
    private final int costoEnergia;
    /**
     * Guarda duracion para que la clase mantenga su estado interno.
     */
    private final int duracion;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Invocacion(String nombre, int vidaMaxima, int dano, int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano);
        validarDatosInvocacion(costoEnergia, duracion);
        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
        setTipo("INVOCACION");
    }

    /** Constructor para reconstruir una invocacion sin restaurarla con vida completa. */
    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Invocacion(int id, String nombre, int vida, int vidaMaxima, int dano,
                      int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "INVOCACION");
        validarDatosInvocacion(costoEnergia, duracion);
        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
    }

    /**
     * Ejecuta la operación validarDatosInvocacion dentro de la responsabilidad de esta clase.
     */
    private void validarDatosInvocacion(int costo, int duracion) {
        if (costo < 0 || duracion < 0) throw new IllegalArgumentException("Costo y duracion no pueden ser negativos.");
    }

    /**
     * Devuelve costo de energía sin exponer directamente el atributo.
     */
    public int getCostoEnergia() { return costoEnergia; }
    /**
     * Devuelve duración sin exponer directamente el atributo.
     */
    public int getDuracion() { return duracion; }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
