package com.wachin;

/** Configuracion elegida en el menu antes de iniciar una partida. */
/**
 * Agrupa las opciones de personaje y proyectil seleccionadas para una partida.
 */
public final class ConfiguracionPartida {

    /**
     * Define una opción cerrada disponible para la configuración del juego.
     */
    public enum Personaje {
        WACHIN("Wachin", "assets/images/player/wachin.png"),
        WACHINA("Wachina", "assets/images/player/wachina.png");

        /**
         * Guarda nombre para que la clase mantenga su estado interno.
         */
        private final String nombre;
        /**
         * Guarda ruta para que la clase mantenga su estado interno.
         */
        private final String ruta;

        Personaje(String nombre, String ruta) {
            this.nombre = nombre;
            this.ruta = ruta;
        }

        public String getNombre() { return nombre; }
        public String getRuta() { return ruta; }
        @Override public String toString() { return nombre; }
    }

    /**
     * Define una opción cerrada disponible para la configuración del juego.
     */
    public enum TipoProyectil {
        LAGRIMA("Lagrima", "assets/images/projectiles/lagrima.png"),
        GOTA("Gota", "assets/images/projectiles/gota.png");

        /**
         * Guarda nombre para que la clase mantenga su estado interno.
         */
        private final String nombre;
        /**
         * Guarda ruta para que la clase mantenga su estado interno.
         */
        private final String ruta;

        TipoProyectil(String nombre, String ruta) {
            this.nombre = nombre;
            this.ruta = ruta;
        }

        public String getNombre() { return nombre; }
        public String getRuta() { return ruta; }
        @Override public String toString() { return nombre; }
    }

    /**
     * Guarda personaje para que la clase mantenga su estado interno.
     */
    private final Personaje personaje;
    /**
     * Constante que define tipoproyectil para este componente.
     */
    private final TipoProyectil tipoProyectil;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public ConfiguracionPartida(Personaje personaje, TipoProyectil tipoProyectil) {
        this.personaje = personaje == null ? Personaje.WACHIN : personaje;
        this.tipoProyectil = tipoProyectil == null ? TipoProyectil.LAGRIMA : tipoProyectil;
    }

    /**
     * Devuelve personaje seleccionado sin exponer directamente el atributo.
     */
    public Personaje getPersonaje() { return personaje; }
    /**
     * Devuelve tipo de proyectil seleccionado sin exponer directamente el atributo.
     */
    public TipoProyectil getTipoProyectil() { return tipoProyectil; }
    /**
     * Devuelve ruta del personaje sin exponer directamente el atributo.
     */
    public String getRutaPersonaje() { return personaje.getRuta(); }
    /**
     * Devuelve ruta del proyectil sin exponer directamente el atributo.
     */
    public String getRutaProyectil() { return tipoProyectil.getRuta(); }
}
