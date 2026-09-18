package com.wachin.model;

public class Jefe extends Enemigo {

    private int nivelJefe;
    private String ataqueEspecial;
    private int danoAdicional;

    public Jefe(String nombre, int vidaMaxima, int dano,
                String tipoEnemigo, double velocidad,
                int nivelJefe, String ataqueEspecial, int danoAdicional) {

        super(nombre, vidaMaxima, dano, tipoEnemigo, velocidad);

        this.nivelJefe = nivelJefe;
        this.ataqueEspecial = ataqueEspecial;
        this.danoAdicional = danoAdicional;
    }

    public int getNivelJefe() {
        return nivelJefe;
    }

    public String getAtaqueEspecial() {
        return ataqueEspecial;
    }

    public int getDanoAdicional() {
        return danoAdicional;
    }

    public void ataqueEspecial(Entidad objetivo) throws Exception {
        int danoTotal = getDano() + danoAdicional;
        objetivo.recibirDano(danoTotal);
    }
}