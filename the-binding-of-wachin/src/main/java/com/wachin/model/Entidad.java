package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.exception.VidaMaximaException;
import com.wachin.interfaces.Atacable;

public abstract class Entidad implements Atacable {
    private int id;
    private String nombre;
    private int vida;
    private int vidaMaxima;
    private int dano;
    private String tipo;

    public Entidad(String nombre, int vidaMaxima, int dano)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        validarNombre(nombre);
        validarVidaMaxima(vidaMaxima);
        validarDano(dano);
        this.nombre = nombre.trim();
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.dano = dano;
    }

    public Entidad(int id, String nombre, int vida, int vidaMaxima, int dano, String tipo)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        validarNombre(nombre);
        validarVidaMaxima(vidaMaxima);
        validarDano(dano);
        if (vida < 0 || vida > vidaMaxima) {
            throw new VidaInvalidaException("La vida actual debe estar entre 0 y la vida maxima.");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.vida = vida;
        this.vidaMaxima = vidaMaxima;
        this.dano = dano;
        this.tipo = tipo;
    }

    private void validarNombre(String valor) throws NombreInvalidoException {
        if (valor == null || valor.isBlank()) throw new NombreInvalidoException("El nombre no puede estar vacio.");
    }

    private void validarVidaMaxima(int valor) throws VidaInvalidaException {
        if (valor <= 0) throw new VidaInvalidaException("La vida maxima debe ser mayor a cero.");
    }

    private void validarDano(int valor) throws DanoInvalidoException {
        if (valor < 0) throw new DanoInvalidoException("El dano no puede ser negativo.");
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getDano() { return dano; }
    public String getTipo() { return tipo; }

    public void setNombre(String nombre) throws NombreInvalidoException {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    public void setDano(int dano) throws DanoInvalidoException {
        validarDano(dano);
        this.dano = dano;
    }

    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public void recibirDano(int dano) {
        // Un dano negativo ya no puede curar accidentalmente a una entidad.
        if (dano <= 0 || vida <= 0) return;
        vida = Math.max(0, vida - dano);
    }

    public void curar(int cantidad) throws VidaMaximaException {
        if (cantidad < 0) throw new IllegalArgumentException("La curacion no puede ser negativa.");
        if (vida + cantidad > vidaMaxima) throw new VidaMaximaException("La vida no puede superar la vida maxima.");
        vida += cantidad;
    }
}
