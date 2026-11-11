package com.wachin.model;

import java.util.concurrent.atomic.AtomicLong;

/** Identidad, categoría y ubicación lógica de un objeto recogible. */
public final class ObjetoSuelo {
    private static final AtomicLong SIGUIENTE_ID = new AtomicLong(1);
    private final long id;
    private final TipoObjetoSuelo tipo;
    private final String codigo;
    private final int salaGlobal;
    private final double x;
    private final double y;

    public ObjetoSuelo(TipoObjetoSuelo tipo, String codigo, int salaGlobal, double x, double y) {
        this(SIGUIENTE_ID.getAndIncrement(), tipo, codigo, salaGlobal, x, y);
    }

    public ObjetoSuelo(long id, TipoObjetoSuelo tipo, String codigo,
            int salaGlobal, double x, double y) {
        if (id <= 0 || tipo == null || codigo == null || codigo.isBlank()
                || salaGlobal < 1 || !Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Datos de objeto del suelo inválidos.");
        }
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.salaGlobal = salaGlobal;
        this.x = x;
        this.y = y;
        SIGUIENTE_ID.accumulateAndGet(id + 1, Math::max);
    }

    public long getId() { return id; }
    public TipoObjetoSuelo getTipo() { return tipo; }
    public String getCodigo() { return codigo; }
    public int getSalaGlobal() { return salaGlobal; }
    public double getX() { return x; }
    public double getY() { return y; }
}
