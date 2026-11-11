package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.exception.EnergiaInsuficienteException;

/**
 * Especializa Entidad con costo energético y duración propios.
 */
public class Invocacion extends Entidad {
    public enum Estado { SIGUIENDO, PROTEGIENDO, ATACANDO, RECUPERANDO }
    /**
     * Constante que define costoenergia para este componente.
     */
    private final int costoEnergia;
    /**
     * Guarda duracion para que la clase mantenga su estado interno.
     */
    private final int duracion;
    private final TipoInvocacion tipoInvocacion;
    private final double velocidad;
    private final double rangoDeteccion;
    private final double energiaMaxima;
    private final double recuperacionEnergia;
    private double energia;
    private double x;
    private double y;
    private double tiempoAtaque;
    private Estado estado = Estado.SIGUIENDO;
    private boolean proteccionActiva;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Invocacion(String nombre, int vidaMaxima, int dano, int costoEnergia, int duracion)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano);
        validarDatosInvocacion(costoEnergia, duracion);
        this.costoEnergia = costoEnergia;
        this.duracion = duracion;
        this.tipoInvocacion = TipoInvocacion.GUSANO;
        this.velocidad = TipoInvocacion.GUSANO.getVelocidad();
        this.rangoDeteccion = TipoInvocacion.GUSANO.getRangoDeteccion();
        this.energiaMaxima = TipoInvocacion.GUSANO.getEnergiaMaxima();
        this.recuperacionEnergia = TipoInvocacion.GUSANO.getRecuperacionEnergia();
        this.energia = energiaMaxima;
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
        this.tipoInvocacion = TipoInvocacion.GUSANO;
        this.velocidad = TipoInvocacion.GUSANO.getVelocidad();
        this.rangoDeteccion = TipoInvocacion.GUSANO.getRangoDeteccion();
        this.energiaMaxima = TipoInvocacion.GUSANO.getEnergiaMaxima();
        this.recuperacionEnergia = TipoInvocacion.GUSANO.getRecuperacionEnergia();
        this.energia = energiaMaxima;
    }

    public Invocacion(TipoInvocacion tipo) throws NombreInvalidoException,
            VidaInvalidaException, DanoInvalidoException {
        super(tipo.getNombre(), tipo.getVida(), tipo.getDano());
        this.tipoInvocacion = tipo;
        this.costoEnergia = tipo.getCostoAtaque();
        this.duracion = 0;
        this.velocidad = tipo.getVelocidad();
        this.rangoDeteccion = tipo.getRangoDeteccion();
        this.energiaMaxima = tipo.getEnergiaMaxima();
        this.recuperacionEnergia = tipo.getRecuperacionEnergia();
        this.energia = energiaMaxima;
        setTipo("INVOCACION");
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
    public TipoInvocacion getTipoInvocacion() { return tipoInvocacion; }
    public double getVelocidad() { return velocidad; }
    public double getRangoDeteccion() { return rangoDeteccion; }
    public double getEnergia() { return energia; }
    public double getEnergiaMaxima() { return energiaMaxima; }
    public double getRecuperacionEnergia() { return recuperacionEnergia; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getTiempoAtaque() { return tiempoAtaque; }
    public Estado getEstado() { return estado; }
    public boolean tieneProteccionActiva() { return proteccionActiva; }
    public void moverA(double x, double y) { this.x = x; this.y = y; }
    public void setEstado(Estado estado) { this.estado = estado == null ? Estado.SIGUIENDO : estado; }
    public void setProteccionActiva(boolean activa) { proteccionActiva = activa; }
    public void reducirTiempoAtaque(double dt) { tiempoAtaque = Math.max(0, tiempoAtaque - Math.max(0, dt)); }
    public void iniciarTiempoAtaque(double tiempo) {
        if (!Double.isFinite(tiempo) || tiempo < 0) throw new IllegalArgumentException("Tiempo de ataque inválido.");
        tiempoAtaque = tiempo;
    }
    public boolean puedeUsarEnergia(double costo) { return costo >= 0 && energia >= costo; }
    public void gastarEnergia(double costo) throws EnergiaInsuficienteException {
        if (!Double.isFinite(costo) || costo < 0) throw new IllegalArgumentException("Costo energético inválido.");
        if (energia < costo) throw new EnergiaInsuficienteException("La invocación necesita recuperar energía.");
        energia -= costo;
    }
    public void recuperarEnergia(double deltaTiempo) {
        if (deltaTiempo > 0 && Double.isFinite(deltaTiempo)) {
            energia = Math.min(energiaMaxima, energia + recuperacionEnergia * deltaTiempo);
        }
    }
    public void restaurarEstado(double energia, double x, double y, double tiempoAtaque,
            Estado estado, boolean proteccionActiva) {
        if (!Double.isFinite(energia) || energia < 0 || energia > energiaMaxima
                || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(tiempoAtaque)
                || tiempoAtaque < 0) throw new IllegalArgumentException("Estado de invocación inválido.");
        this.energia = energia; this.x = x; this.y = y; this.tiempoAtaque = tiempoAtaque;
        this.estado = estado == null ? Estado.SIGUIENDO : estado;
        this.proteccionActiva = proteccionActiva;
    }
    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
