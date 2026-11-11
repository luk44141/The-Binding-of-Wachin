package com.wachin.model;

/** Registro de una carta adquirida y la cantidad de copias acumuladas. */
public final class Carta {
    private final CartaTipo tipo;
    private final int acumulaciones;
    public Carta(CartaTipo tipo, int acumulaciones) {
        if (tipo == null || acumulaciones < 1 || (tipo.esUnica() && acumulaciones > 1)) {
            throw new IllegalArgumentException("Estado de carta inválido.");
        }
        this.tipo = tipo;
        this.acumulaciones = acumulaciones;
    }
    public CartaTipo getTipo() { return tipo; }
    public String getNombre() { return tipo.getNombre(); }
    public String getDescripcion() { return tipo.getDescripcion(); }
    public int getAcumulaciones() { return acumulaciones; }
}
