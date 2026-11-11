package com.wachin.model;

/** Catálogo de cartas. Las cartas básicas acumulan el valor indicado por copia. */
public enum CartaTipo {
    DANO("Fuerza", "Aumenta el daño de todos los proyectiles", 5, false),
    VELOCIDAD("Agilidad", "Aumenta la velocidad de movimiento", 15, false),
    VIDA_MAXIMA("Vitalidad", "Aumenta la vida máxima y cura el aumento", 20, false),
    DANO_INVOCACIONES("Vínculo", "Aumenta el daño de las invocaciones", 5, false),
    SEGUNDO_ESPACIO_INVOCACION("Pacto", "Desbloquea un segundo espacio de invocación", 1, true);

    private final String nombre;
    private final String descripcion;
    private final int valor;
    private final boolean unica;
    CartaTipo(String nombre, String descripcion, int valor, boolean unica) {
        this.nombre = nombre; this.descripcion = descripcion; this.valor = valor; this.unica = unica;
    }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getValor() { return valor; }
    public boolean esUnica() { return unica; }
}
