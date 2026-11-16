package com.wachin.model;

/** Contadores de la partida en curso que se muestran en las pantallas de derrota y victoria. */
public final class EstadisticasPartida {

    private int enemigosDerrotados;
    private int jefesDerrotados;
    private int danoRecibido;
    private double segundosJugados;

    public void registrarEnemigoDerrotado(boolean jefe) {
        enemigosDerrotados++;
        if (jefe) jefesDerrotados++;
    }

    public void registrarDanoRecibido(int dano) {
        if (dano > 0) danoRecibido = Math.addExact(danoRecibido, dano);
    }

    public void sumarTiempo(double segundos) {
        if (Double.isFinite(segundos) && segundos > 0) segundosJugados += segundos;
    }

    public int getEnemigosDerrotados() { return enemigosDerrotados; }
    public int getJefesDerrotados() { return jefesDerrotados; }
    public int getDanoRecibido() { return danoRecibido; }

    /** Tiempo con formato {@code m:ss}. */
    public String tiempoFormateado() {
        int total = (int) segundosJugados;
        return (total / 60) + ":" + String.format("%02d", total % 60);
    }
}
