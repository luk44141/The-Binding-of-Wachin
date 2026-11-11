package com.wachin.model;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.EnergiaInsuficienteException;
import com.wachin.exception.ExperienciaInsuficienteException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;

/**
 * Especializa Entidad con recursos propios. Hereda estado común, sobrescribe el ataque (polimorfismo) y encapsula sus campos privados.
 */
public class Jugador extends Entidad {
    /**
     * Guarda experiencia para que la clase mantenga su estado interno.
     */
    private int experiencia;
    /**
     * Guarda nivel para que la clase mantenga su estado interno.
     */
    private int nivel;
    /**
     * Guarda monedas para que la clase mantenga su estado interno.
     */
    private int monedas;
    /**
     * Guarda energia para que la clase mantenga su estado interno.
     */
    private int energia;
    /**
     * Constante que define energiamaxima para este componente.
     */
    private int energiaMaxima;
    private int vidaMaximaInicial;
    private ProyectilEquipable proyectilEquipado = ProyectilEquipable.GOTA;
    private double velocidad = 225;
    private int bonoDanoProyectiles;
    private int bonoDanoInvocaciones;
    private boolean segundoEspacioInvocacion;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Jugador(String nombre, int vidaMaxima, int dano, int energiaMaxima)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(nombre, vidaMaxima, dano);
        this.vidaMaximaInicial = vidaMaxima;
        validarRecursos(0, 1, 0, energiaMaxima, energiaMaxima);
        this.experiencia = 0;
        this.nivel = 1;
        this.monedas = 0;
        this.energiaMaxima = energiaMaxima;
        this.energia = energiaMaxima;
    }

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public Jugador(int id, String nombre, int vida, int vidaMaxima, int dano,
                   int experiencia, int nivel, int monedas, int energia, int energiaMaxima)
            throws NombreInvalidoException, VidaInvalidaException, DanoInvalidoException {
        super(id, nombre, vida, vidaMaxima, dano, "JUGADOR");
        this.vidaMaximaInicial = vidaMaxima;
        validarRecursos(experiencia, nivel, monedas, energia, energiaMaxima);
        this.experiencia = experiencia;
        this.nivel = nivel;
        this.monedas = monedas;
        this.energia = energia;
        this.energiaMaxima = energiaMaxima;
    }

    /**
     * Comprueba que experiencia, nivel, monedas y energía estén dentro de sus rangos válidos.
     */
    private void validarRecursos(int exp, int nivel, int monedas, int energia, int energiaMaxima) {
        if (exp < 0 || nivel < 1 || monedas < 0 || energiaMaxima < 0
                || energia < 0 || energia > energiaMaxima) {
            throw new IllegalArgumentException("Los recursos del jugador contienen valores invalidos.");
        }
    }

    /**
     * Devuelve experiencia sin exponer directamente el atributo.
     */
    public int getExperiencia() { return experiencia; }
    /**
     * Devuelve nivel sin exponer directamente el atributo.
     */
    public int getNivel() { return nivel; }
    /**
     * Devuelve monedas sin exponer directamente el atributo.
     */
    public int getMonedas() { return monedas; }
    /**
     * Devuelve energía actual sin exponer directamente el atributo.
     */
    public int getEnergia() { return energia; }
    /**
     * Devuelve energía máxima sin exponer directamente el atributo.
     */
    public int getEnergiaMaxima() { return energiaMaxima; }
    public int getVidaMaximaInicial() { return vidaMaximaInicial; }
    public ProyectilEquipable getProyectilEquipado() { return proyectilEquipado; }
    public void equiparProyectil(ProyectilEquipable proyectil) {
        if (proyectil == null) throw new IllegalArgumentException("El proyectil equipado es obligatorio.");
        this.proyectilEquipado = proyectil;
    }
    public double getVelocidad() { return velocidad; }
    public int getBonoDanoProyectiles() { return bonoDanoProyectiles; }
    public int getBonoDanoInvocaciones() { return bonoDanoInvocaciones; }
    public boolean tieneSegundoEspacioInvocacion() { return segundoEspacioInvocacion; }
    public void aumentarVelocidad(double cantidad) {
        if (!Double.isFinite(cantidad) || cantidad <= 0) throw new IllegalArgumentException("Aumento de velocidad inválido.");
        velocidad += cantidad;
    }
    public void aumentarBonoDanoProyectiles(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Aumento de daño inválido.");
        bonoDanoProyectiles = Math.addExact(bonoDanoProyectiles, cantidad);
    }
    public void aumentarBonoDanoInvocaciones(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Aumento de daño inválido.");
        bonoDanoInvocaciones = Math.addExact(bonoDanoInvocaciones, cantidad);
    }
    public void desbloquearSegundoEspacioInvocacion() { segundoEspacioInvocacion = true; }
    public void restaurarMejorasYEquipo(int vidaInicial, double velocidad,
            int bonoDano, int bonoDanoInvocaciones, boolean segundoEspacio,
            ProyectilEquipable proyectil) {
        if (vidaInicial <= 0 || vidaInicial > getVidaMaxima() || !Double.isFinite(velocidad)
                || velocidad <= 0 || bonoDano < 0 || bonoDanoInvocaciones < 0 || proyectil == null) {
            throw new IllegalArgumentException("Mejoras o equipo restaurados inválidos.");
        }
        this.vidaMaximaInicial = vidaInicial;
        this.velocidad = velocidad;
        this.bonoDanoProyectiles = bonoDano;
        this.bonoDanoInvocaciones = bonoDanoInvocaciones;
        this.segundoEspacioInvocacion = segundoEspacio;
        this.proyectilEquipado = proyectil;
    }

    /**
     * Suma experiencia si la cantidad recibida es positiva.
     */
    public void ganarExperiencia(int cantidad) {
        if (cantidad > 0) experiencia = Math.addExact(experiencia, cantidad);
    }

    /**
     * Comprueba la experiencia, aumenta el nivel y descuenta el costo requerido.
     */
    public void subirNivel(int experienciaNecesaria) throws ExperienciaInsuficienteException {
        if (experienciaNecesaria <= 0) throw new IllegalArgumentException("La experiencia necesaria debe ser positiva.");
        if (experiencia < experienciaNecesaria) {
            throw new ExperienciaInsuficienteException("No hay suficiente experiencia para subir de nivel.");
        }
        nivel++;
        experiencia -= experienciaNecesaria;
    }

    /**
     * Valida el costo y descuenta energía disponible.
     */
    public void gastarEnergia(int cantidad) throws EnergiaInsuficienteException {
        if (cantidad < 0) throw new IllegalArgumentException("No se puede gastar energia negativa.");
        if (cantidad > energia) throw new EnergiaInsuficienteException("No hay suficiente energia para realizar esta accion.");
        energia -= cantidad;
    }

    /**
     * Recupera energía sin exceder el máximo del jugador.
     */
    public void recuperarEnergia(int cantidad) {
        if (cantidad <= 0) return;
        energia = Math.min(energiaMaxima, energia + cantidad);
    }

    @Override public void atacar(Entidad objetivo) {
        if (objetivo != null) objetivo.recibirDano(getDano());
    }
}
