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
    private double tiempoAtaque;
    private double x;
    private double y;
    private double xAnterior;
    private double yAnterior;

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
    public double getTiempoAtaque() { return tiempoAtaque; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getXAnterior() { return xAnterior; }
    public double getYAnterior() { return yAnterior; }
    public void moverA(double nuevaX, double nuevaY) {
        if (!Double.isFinite(nuevaX) || !Double.isFinite(nuevaY)) {
            throw new IllegalArgumentException("La posición del enemigo debe ser finita.");
        }
        xAnterior = x;
        yAnterior = y;
        x = nuevaX;
        y = nuevaY;
    }
    public void restaurarPosicion(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("La posición restaurada del enemigo debe ser finita.");
        }
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
    }
    public void reducirTiempoAtaque(double deltaTiempo) {
        if (Double.isFinite(deltaTiempo) && deltaTiempo > 0) tiempoAtaque = Math.max(0, tiempoAtaque - deltaTiempo);
    }
    public void iniciarTiempoAtaque(double tiempo) {
        if (!Double.isFinite(tiempo) || tiempo < 0) throw new IllegalArgumentException("Tiempo de ataque inválido.");
        tiempoAtaque = tiempo;
    }
    public void restaurarTiempoAtaque(double tiempo) { iniciarTiempoAtaque(tiempo); }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
