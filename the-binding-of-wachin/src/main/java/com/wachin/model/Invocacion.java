package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

public class Invocacion extends Entidad {
    private final int costoEnergia;
    private final int duracion;

    public Invocacion(String nombre, int vidaMaxima, int dano, int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano);
        validarDatosInvocacion(costoEnergia, duracion);
        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
        setTipo("INVOCACION");
    }

    /** Constructor para reconstruir una invocacion sin restaurarla con vida completa. */
    public Invocacion(int id, String nombre, int vida, int vidaMaxima, int dano,
                      int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "INVOCACION");
        validarDatosInvocacion(costoEnergia, duracion);
        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
    }

    private void validarDatosInvocacion(int costo, int duracion) {
        if (costo < 0 || duracion < 0) throw new IllegalArgumentException("Costo y duracion no pueden ser negativos.");
    }

    public int getCostoEnergia() { return costoEnergia; }
    public int getDuracion() { return duracion; }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
