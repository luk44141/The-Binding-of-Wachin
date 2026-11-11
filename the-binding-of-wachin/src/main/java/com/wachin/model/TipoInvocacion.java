package com.wachin.model;

/** Catálogo de invocaciones con atributos iniciales fáciles de ajustar. */
public enum TipoInvocacion {
    GUSANO("Gusano", "worm", 220, 8, 145, 240, 100, 12, 6, 4),
    MURCIELAGO("Murciélago", "bat", 75, 18, 205, 250, 80, 12, 12, 0),
    ROBOT("Robot", "robot", 105, 13, 155, 390, 100, 14, 18, 0);

    private final String nombre;
    private final String carpetaAsset;
    private final int vida;
    private final int dano;
    private final double velocidad;
    private final double rangoDeteccion;
    private final int energiaMaxima;
    private final double recuperacionEnergia;
    private final int costoAtaque;
    private final int costoProteccion;

    TipoInvocacion(String nombre, String carpetaAsset, int vida, int dano, double velocidad,
            double rangoDeteccion, int energiaMaxima, double recuperacionEnergia,
            int costoAtaque, int costoProteccion) {
        this.nombre = nombre; this.carpetaAsset = carpetaAsset; this.vida = vida; this.dano = dano;
        this.velocidad = velocidad; this.rangoDeteccion = rangoDeteccion; this.energiaMaxima = energiaMaxima;
        this.recuperacionEnergia = recuperacionEnergia; this.costoAtaque = costoAtaque;
        this.costoProteccion = costoProteccion;
    }
    public String getNombre() { return nombre; }
    public String getRutaAsset() { return "assets/images/summons/" + carpetaAsset + "/" + carpetaAsset + ".png"; }
    public int getVida() { return vida; }
    public int getDano() { return dano; }
    public double getVelocidad() { return velocidad; }
    public double getRangoDeteccion() { return rangoDeteccion; }
    public int getEnergiaMaxima() { return energiaMaxima; }
    public double getRecuperacionEnergia() { return recuperacionEnergia; }
    public int getCostoAtaque() { return costoAtaque; }
    public int getCostoProteccion() { return costoProteccion; }
}
