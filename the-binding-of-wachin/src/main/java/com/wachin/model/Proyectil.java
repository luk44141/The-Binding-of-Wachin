package com.wachin.model;

/** Estado y movimiento de un proyectil, independiente de su representación gráfica. */
public final class Proyectil {
    public enum Origen { JUGADOR, ENEMIGO, JEFE, INVOCACION }

    private double x;
    private double y;
    private double xAnterior;
    private double yAnterior;
    private final double velocidadX;
    private final double velocidadY;
    private final int dano;
    private final boolean hostil;
    private final Origen origen;
    private final double radioColision = 11;

    public Proyectil(double x, double y, double velocidadX, double velocidadY,
            int dano, boolean hostil, Origen origen) {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(velocidadX) || !Double.isFinite(velocidadY)
                || dano < 0 || origen == null) {
            throw new IllegalArgumentException("Datos de proyectil inválidos.");
        }
        if (hostil != (origen == Origen.ENEMIGO || origen == Origen.JEFE)) {
            throw new IllegalArgumentException("El origen y la hostilidad del proyectil no coinciden.");
        }
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
        this.velocidadX = velocidadX;
        this.velocidadY = velocidadY;
        this.dano = dano;
        this.hostil = hostil;
        this.origen = origen;
    }

    public void actualizar(double deltaTiempo) {
        if (!Double.isFinite(deltaTiempo) || deltaTiempo < 0) {
            throw new IllegalArgumentException("El tiempo de actualización no puede ser negativo.");
        }
        xAnterior = x;
        yAnterior = y;
        x += velocidadX * deltaTiempo;
        y += velocidadY * deltaTiempo;
    }

    public boolean estaFueraDeLaSala() { return x < 50 || x > 950 || y < 50 || y > 650; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getXAnterior() { return xAnterior; }
    public double getYAnterior() { return yAnterior; }
    public double getVelocidadX() { return velocidadX; }
    public double getVelocidadY() { return velocidadY; }
    public int getDano() { return dano; }
    public boolean esHostil() { return hostil; }
    public Origen getOrigen() { return origen; }
    public double getRadioColision() { return radioColision; }
}
