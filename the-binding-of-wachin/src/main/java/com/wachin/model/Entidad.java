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
        validarVida(vidaMaxima);
        validarDano(dano);

        this.nombre = nombre;
        this.vidaMaxima = vidaMaxima;
        this.vida = vidaMaxima;
        this.dano = dano;
    }

    public Entidad(int id, String nombre, int vida, int vidaMaxima,
                   int dano, String tipo)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {

        validarNombre(nombre);
        validarVida(vidaMaxima);
        validarDano(dano);

        this.id = id;
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vidaMaxima;
        this.dano = dano;
        this.tipo = tipo;
    }

    private void validarNombre(String nombre) throws NombreInvalidoException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NombreInvalidoException("El nombre no puede estar vacío.");
        }
    }

    private void validarVida(int vida) throws VidaInvalidaException {
        if (vida <= 0) {
            throw new VidaInvalidaException("La vida debe ser mayor a 0.");
        }
    }

    private void validarDano(int dano) throws DanoInvalidoException {
        if (dano < 0) {
            throw new DanoInvalidoException("El daño no puede ser negativo.");
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) throws NombreInvalidoException {
        validarNombre(nombre);
        this.nombre = nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getDano() {
        return dano;
    }

    public void setDano(int dano) throws DanoInvalidoException {
        validarDano(dano);
        this.dano = dano;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public void recibirDano(int dano) {
        this.vida -= dano;

        if (this.vida < 0) {
            this.vida = 0;
        }
    }

    public void curar(int cantidad) throws VidaMaximaException {
        if (vida + cantidad > vidaMaxima) {
            throw new VidaMaximaException(
                "La vida no puede superar la vida máxima."
            );
        }

        vida += cantidad;
    }
}