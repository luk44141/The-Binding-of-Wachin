package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.exception.VidaMaximaException;
import com.wachin.interfaces.Atacable;

/**
 * Abstracción de los datos y reglas comunes de los actores; sus campos privados aplican encapsulamiento y las subclases especializan su conducta.
 */
public abstract class Entidad implements Atacable {
    /**
     * Guarda id para que la clase mantenga su estado interno.
     */
    private int id;
    /**
     * Guarda nombre para que la clase mantenga su estado interno.
     */
    private String nombre;
    /**
     * Guarda vida para que la clase mantenga su estado interno.
     */
    private int vida;
    /**
     * Constante que define vidamaxima para este componente.
     */
    private int vidaMaxima;
    /**
     * Guarda dano para que la clase mantenga su estado interno.
     */
    private int dano;
    /**
     * Guarda tipo para que la clase mantenga su estado interno.
     */
    private String tipo;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
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

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
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

    /**
     * Rechaza nombres nulos o vacíos antes de almacenarlos.
     */
    private void validarNombre(String valor) throws NombreInvalidoException {
        if (valor == null || valor.isBlank()) throw new NombreInvalidoException("El nombre no puede estar vacio.");
    }

    /**
     * Exige un valor de vida máxima positivo.
     */
    private void validarVidaMaxima(int valor) throws VidaInvalidaException {
        if (valor <= 0) throw new VidaInvalidaException("La vida maxima debe ser mayor a cero.");
    }

    /**
     * Impide asignar un valor de daño negativo.
     */
    private void validarDano(int valor) throws DanoInvalidoException {
        if (valor < 0) throw new DanoInvalidoException("El dano no puede ser negativo.");
    }

    /**
     * Devuelve identificador sin exponer directamente el atributo.
     */
    public int getId() { return id; }
    /**
     * Devuelve nombre sin exponer directamente el atributo.
     */
    public String getNombre() { return nombre; }
    /**
     * Devuelve vida actual sin exponer directamente el atributo.
     */
    public int getVida() { return vida; }
    /**
     * Devuelve vida máxima sin exponer directamente el atributo.
     */
    public int getVidaMaxima() { return vidaMaxima; }
    /**
     * Devuelve daño sin exponer directamente el atributo.
     */
    public int getDano() { return dano; }
    /**
     * Devuelve tipo sin exponer directamente el atributo.
     */
    public String getTipo() { return tipo; }

    /**
     * Actualiza nombre mediante el método de acceso de la clase.
     */
    public void setNombre(String nombre) throws NombreInvalidoException {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    /**
     * Actualiza dano mediante el método de acceso de la clase.
     */
    public void setDano(int dano) throws DanoInvalidoException {
        validarDano(dano);
        this.dano = dano;
    }

    /**
     * Actualiza tipo mediante el método de acceso de la clase.
     */
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    /**
     * Reduce la vida sin permitir daño negativo ni valores de vida inferiores a cero.
     */
    public void recibirDano(int dano) {
        // Un dano negativo ya no puede curar accidentalmente a una entidad.
        if (dano <= 0 || vida <= 0) return;
        vida = Math.max(0, vida - dano);
    }

    /**
     * Aumenta la vida solo si la curación no supera el máximo.
     */
    public void curar(int cantidad) throws VidaMaximaException {
        if (cantidad < 0) throw new IllegalArgumentException("La curacion no puede ser negativa.");
        if (vida + cantidad > vidaMaxima) throw new VidaMaximaException("La vida no puede superar la vida maxima.");
        vida += cantidad;
    }

    /** Aumenta el máximo y cura exactamente la cantidad añadida, sin excederlo. */
    public void aumentarVidaMaxima(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("El aumento de vida máxima debe ser positivo.");
        vidaMaxima = Math.addExact(vidaMaxima, cantidad);
        vida = Math.min(vidaMaxima, Math.addExact(vida, cantidad));
    }
}
