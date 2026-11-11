package com.wachin.model;

/** Proyectiles que pueden equiparse; cada tipo conserva sus datos aunque se intercambie. */
public enum ProyectilEquipable {
    LAGRIMA("Lágrima", "assets/images/projectiles/lagrima.png", 30),
    GOTA("Gota", "assets/images/projectiles/gota.png", 25);

    private final String nombre;
    private final String rutaAsset;
    private final int dano;

    ProyectilEquipable(String nombre, String rutaAsset, int dano) {
        this.nombre = nombre;
        this.rutaAsset = rutaAsset;
        this.dano = dano;
    }
    public String getNombre() { return nombre; }
    public String getRutaAsset() { return rutaAsset; }
    public int getDano() { return dano; }
    public static ProyectilEquipable desdeCodigo(String codigo) {
        return valueOf(codigo.trim().toUpperCase());
    }
}
