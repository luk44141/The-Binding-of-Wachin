package com.wachin;

/** Configuracion elegida en el menu antes de iniciar una partida. */
public final class ConfiguracionPartida {

    public enum Personaje {
        WACHIN("Wachin", "assets/images/player/wachin.png"),
        WACHINA("Wachina", "assets/images/player/wachina.png");

        private final String nombre;
        private final String ruta;

        Personaje(String nombre, String ruta) {
            this.nombre = nombre;
            this.ruta = ruta;
        }

        public String getNombre() { return nombre; }
        public String getRuta() { return ruta; }
        @Override public String toString() { return nombre; }
    }

    public enum TipoProyectil {
        LAGRIMA("Lagrima", "assets/images/projectiles/lagrima.png"),
        GOTA("Gota", "assets/images/projectiles/gota.png");

        private final String nombre;
        private final String ruta;

        TipoProyectil(String nombre, String ruta) {
            this.nombre = nombre;
            this.ruta = ruta;
        }

        public String getNombre() { return nombre; }
        public String getRuta() { return ruta; }
        @Override public String toString() { return nombre; }
    }

    private final Personaje personaje;
    private final TipoProyectil tipoProyectil;

    public ConfiguracionPartida(Personaje personaje, TipoProyectil tipoProyectil) {
        this.personaje = personaje == null ? Personaje.WACHIN : personaje;
        this.tipoProyectil = tipoProyectil == null ? TipoProyectil.LAGRIMA : tipoProyectil;
    }

    public Personaje getPersonaje() { return personaje; }
    public TipoProyectil getTipoProyectil() { return tipoProyectil; }
    public String getRutaPersonaje() { return personaje.getRuta(); }
    public String getRutaProyectil() { return tipoProyectil.getRuta(); }
}
