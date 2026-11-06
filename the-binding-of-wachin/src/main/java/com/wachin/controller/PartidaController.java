
package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import com.wachin.service.CombateService;
import com.wachin.service.ColisionService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.service.SalaService;
import com.wachin.view.game.AssetVisual;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.PartidaView;
import com.wachin.view.game.Proyectil;
import com.wachin.view.game.PuertaVisual;
import com.wachin.ConfiguracionPartida;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Coordina entrada, actualización del juego, combate y comunicación entre modelo y vista.
 */
public class PartidaController {

    /**
     * Constante que define limite izquierdo para este componente.
     */
    private static final double LIMITE_IZQUIERDO = 75;
    /**
     * Constante que define limite derecho para este componente.
     */
    private static final double LIMITE_DERECHO = 925;
    /**
     * Constante que define limite superior para este componente.
     */
    private static final double LIMITE_SUPERIOR = 75;
    /**
     * Constante que define limite inferior para este componente.
     */
    private static final double LIMITE_INFERIOR = 625;

    /**
     * Constante que define velocidad jugador para este componente.
     */
    private static final double VELOCIDAD_JUGADOR = 225;
    /**
     * Constante que define velocidad proyectil para este componente.
     */
    private static final double VELOCIDAD_PROYECTIL = 430;
    /**
     * Constante que define tiempo invulnerable para este componente.
     */
    private static final double TIEMPO_INVULNERABLE = 0.45;

    /**
     * Guarda vista para que la clase mantenga su estado interno.
     */
    private final PartidaView vista;
    /**
     * Guarda configuracion para que la clase mantenga su estado interno.
     */
    private final ConfiguracionPartida configuracion;
    /**
     * Constante que define alperder para este componente.
     */
    private final Consumer<ConfiguracionPartida> alPerder;

    private final Set<KeyCode> teclasPresionadas = new HashSet<>();
    private final List<Proyectil> proyectiles = new ArrayList<>();
    private final List<EnemigoVisual> enemigosVisuales = new ArrayList<>();

    private final JuegoService juegoService = new JuegoService();
    private final CombateService combateService = new CombateService();
    private final ColisionService colisionService = new ColisionService();
    private final JugadorService jugadorService = new JugadorService();
    private final SalaService salaService = new SalaService();
    private final MapaController mapaController = new MapaController();
    private final Random random = new Random();

    /**
     * Constante que define jugadorvisual para este componente.
     */
    private Node jugadorVisual;

    /**
     * Constante que define jugadorx para este componente.
     */
    private double jugadorX = 500;
    /**
     * Constante que define jugadory para este componente.
     */
    private double jugadorY = 350;
    /**
     * Constante que define jugadorxanterior para este componente.
     */
    private double jugadorXAnterior = 500;
    /**
     * Constante que define jugadoryanterior para este componente.
     */
    private double jugadorYAnterior = 350;
    /**
     * Constante que define tiempoinvulnerable para este componente.
     */
    private double tiempoInvulnerable;

    /**
     * Constante que define ultimotiempo para este componente.
     */
    private long ultimoTiempo;
    /**
     * Constante que define gameloop para este componente.
     */
    private AnimationTimer gameLoop;

    /**
     * Constante que define puertasabiertas para este componente.
     */
    private boolean puertasAbiertas;
    /**
     * Constante que define juegoterminado para este componente.
     */
    private boolean juegoTerminado;
    /**
     * Guarda pausado para que la clase mantenga su estado interno.
     */
    private boolean pausado;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public PartidaController(
        PartidaView vista,
        ConfiguracionPartida configuracion,
        Consumer<ConfiguracionPartida> alPerder) {

    this.vista = vista;
    this.configuracion = configuracion != null
            ? configuracion
            : new ConfiguracionPartida(null, null);
    this.alPerder = alPerder;
    }

    /**
     * Prepara el estado inicial y pone en marcha este componente.
     */
    public void iniciar() {
        crearJugador();
        prepararSala(mapaController.iniciar());

        iniciarGameLoop();
    }

    /**
     * Detiene el ciclo de animación si se encuentra activo.
     */
    public void detener() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Ejecuta la operación teclaPresionada dentro de la responsabilidad de esta clase.
     */
    public boolean teclaPresionada(KeyCode tecla) {
    /**
     * Ejecuta la operación if dentro de la responsabilidad de esta clase.
     */
    if (tecla == KeyCode.ESCAPE || pausado || !partidaActiva()) {
        return false;
    }

    return teclasPresionadas.add(tecla);
    }

    /**
     * Ejecuta la operación teclaLiberada dentro de la responsabilidad de esta clase.
     */
    public void teclaLiberada(KeyCode tecla) {
        teclasPresionadas.remove(tecla);
    }

    /**
     * Ejecuta la operación manejarTecla dentro de la responsabilidad de esta clase.
     */
    public void manejarTecla(KeyCode tecla) {
        if (tecla == KeyCode.ESCAPE) {
            if (!juegoTerminado) {
                alternarPausa();
            }
            return;
        }

        if (!partidaActiva() || pausado) {
            return;
        }

        switch (tecla) {
            case UP:
                disparar(0, -1);
                break;
            case DOWN:
                disparar(0, 1);
                break;
            case LEFT:
                disparar(-1, 0);
                break;
            case RIGHT:
                disparar(1, 0);
                break;
            default:
                break;
        }
    }

    /**
     * Ejecuta la operación alternarPausa dentro de la responsabilidad de esta clase.
     */
    private void alternarPausa() {
        if (pausado) {
            reanudar();
            return;
        }

        pausado = true;
        teclasPresionadas.clear();
        vista.mostrarPausa(this::reanudar);
    }

    /**
     * Ejecuta la operación reanudar dentro de la responsabilidad de esta clase.
     */
    private void reanudar() {
        pausado = false;
        teclasPresionadas.clear();
        vista.ocultarPausa();
        ultimoTiempo = 0;
    }

    /**
     * Crea el modelo del jugador, inicia JuegoService y agrega su representación visual.
     */
    private void crearJugador() {
        try {
            String nombre = configuracion.getPersonaje().getNombre();
            String ruta = configuracion.getRutaPersonaje();

            Jugador jugador = new Jugador(nombre, 100, 25, 100);
            juegoService.iniciarPartida(jugador);

            jugadorVisual = AssetVisual.crear(
                    ruta,
                    jugadorX,
                    jugadorY,
                    90,
                    90,
                    Color.BEIGE,
                    Color.BLACK,
                    3
            );

            vista.agregarJugador(jugadorVisual);

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudo crear al jugador.",
                    e
            );
        }
    }

    /**
     * Genera los enemigos del nodo, configura la vista de sala y refresca el HUD.
     */
    private void prepararSala(MapaController.SalaNodo nodo) {
        limpiarSalaAnterior();
        if (nodo == null) return;
        puertasAbiertas = false;

        Sala sala = salaService.generarSala(
                nodo.getId(),
                nodo.getProfundidad(),
                nodo.esJefe()
        );

        vista.configurarSala(
                sala,
                nodo.getMapa(),
                nodo.esJefe()
        );

        vista.configurarSalidas(
                nodo.getDireccionesSalida(),
                false
        );

        if (nodo.esJefe()) {
            crearJefe();
        } else {
            for (Enemigo enemigo : sala.getEnemigos()) {
                juegoService.agregarEnemigo(enemigo);

                double[] posicion = generarPosicionEnemigo();

                EnemigoVisual visual = new EnemigoVisual(
                        enemigo,
                        posicion[0],
                        posicion[1]
                );

                enemigosVisuales.add(visual);
                vista.agregarEnemigo(visual);
            }
        }

        actualizarHUD();
    }

    /**
     * Genera el jefe del mapa y agrega su modelo y su nodo visual a la partida.
     */
    private void crearJefe() {
        Jefe jefe = salaService.generarJefe(
                mapaController.getActual().getId(),
                mapaController.getActual().getMapa()
        );

        juegoService.agregarEnemigo(jefe);

        EnemigoVisual visual = new EnemigoVisual(
                jefe,
                500,
                270
        );

        enemigosVisuales.add(visual);
        vista.agregarEnemigo(visual);
    }

    /**
     * Retira enemigos y disparos de la escena y limpia sus colecciones.
     */
    private void limpiarSalaAnterior() {
        for (EnemigoVisual enemigo : enemigosVisuales) {
            vista.quitarEnemigo(enemigo);
        }

        enemigosVisuales.clear();

        for (Proyectil proyectil : proyectiles) {
            vista.quitarProyectil(proyectil);
        }

        proyectiles.clear();

        juegoService.getEnemigos().clear();
        teclasPresionadas.clear();
    }

    /**
     * Ubica al jugador junto a la puerta de entrada y sincroniza sus posiciones para colisiones.
     */
    private void colocarJugadorEnEntrada(
            PuertaVisual.Direccion salidaAnterior) {

        switch (salidaAnterior) {
            case ARRIBA:
                jugadorX = 500;
                jugadorY = 580;
                break;
            case ABAJO:
                jugadorX = 500;
                jugadorY = 100;
                break;
            case IZQUIERDA:
                jugadorX = 880;
                jugadorY = 350;
                break;
            case DERECHA:
                jugadorX = 120;
                jugadorY = 350;
                break;
            default:
                break;
        }

        AssetVisual.moverAlCentro(jugadorVisual, jugadorX, jugadorY, 54, 54);
        jugadorXAnterior = jugadorX;
        jugadorYAnterior = jugadorY;
    }

    /**
     * Elige una posición dentro de la sala, intentando mantener distancia del jugador.
     */
    private double[] generarPosicionEnemigo() {
        /**
         * Guarda x para que la clase mantenga su estado interno.
         */
        double x;
        /**
         * Guarda y para que la clase mantenga su estado interno.
         */
        double y;
        /**
         * Guarda intentos para que la clase mantenga su estado interno.
         */
        int intentos = 0;

        do {
            x = 140 + random.nextDouble() * 720;
            y = 130 + random.nextDouble() * 440;
            intentos++;
        } while (
                colisionService.distancia(x, y, jugadorX, jugadorY) < 125
                        && intentos < 100
        );

        return new double[]{x, y};
    }

    /**
     * Crea un proyectil con velocidad y orientación según la dirección y lo agrega a la vista.
     */
    private void disparar(double direccionX, double direccionY) {
        Jugador jugador = juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        Proyectil proyectil = new Proyectil(
        jugadorX,
        jugadorY,
        direccionX * VELOCIDAD_PROYECTIL,
        direccionY * VELOCIDAD_PROYECTIL,
        jugador.getDano(),
        configuracion.getRutaProyectil()
        , direccionX, direccionY
        );

        proyectiles.add(proyectil);
        vista.agregarProyectil(proyectil);
    }

    /**
     * Crea el ciclo JavaFX que actualiza tiempo, movimiento, colisiones y estado.
     */
    private void iniciarGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (pausado || juegoTerminado) {
                    ultimoTiempo = 0;
                    return;
                }

                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double deltaTiempo =
                        (ahora - ultimoTiempo) / 1_000_000_000.0;

                ultimoTiempo = ahora;
                deltaTiempo = Math.min(deltaTiempo, 0.05);

                if (tiempoInvulnerable > 0) {
                    tiempoInvulnerable = Math.max(
                            0,
                            tiempoInvulnerable - deltaTiempo
                    );
                }
                vista.actualizarIndicadorDanio(tiempoInvulnerable > 0);

                actualizarJugador(deltaTiempo);

                if (detectarPuerta()) {
                    return;
                }

                actualizarEnemigos(deltaTiempo);
                actualizarProyectiles(deltaTiempo);
                verificarEstado();
                actualizarHUD();
            }
        };

        gameLoop.start();
    }

    /**
     * Ejecuta la operación partidaActiva dentro de la responsabilidad de esta clase.
     */
    private boolean partidaActiva() {
        return !juegoTerminado && juegoService.jugadorEstaVivo();
    }

    /**
     * Lee las teclas mantenidas, normaliza el movimiento, limita al jugador y mueve su sprite.
     */
    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        jugadorXAnterior = jugadorX;
        jugadorYAnterior = jugadorY;
        /**
         * Constante que define direccionx para este componente.
         */
        double direccionX = 0;
        /**
         * Constante que define direcciony para este componente.
         */
        double direccionY = 0;

        if (teclasPresionadas.contains(KeyCode.W)) direccionY--;
        if (teclasPresionadas.contains(KeyCode.S)) direccionY++;
        if (teclasPresionadas.contains(KeyCode.A)) direccionX--;
        if (teclasPresionadas.contains(KeyCode.D)) direccionX++;

        double longitud = Math.sqrt(
                direccionX * direccionX + direccionY * direccionY
        );

        if (longitud > 0) {
            direccionX /= longitud;
            direccionY /= longitud;

            jugadorX += direccionX * VELOCIDAD_JUGADOR * deltaTiempo;
            jugadorY += direccionY * VELOCIDAD_JUGADOR * deltaTiempo;
        }

        limitarJugador();

        AssetVisual.moverAlCentro(jugadorVisual, jugadorX, jugadorY, 54, 54);
    }

    /**
     * Mantiene las coordenadas del jugador dentro del área jugable.
     */
    private void limitarJugador() {
        jugadorX = Math.max(
                LIMITE_IZQUIERDO,
                Math.min(LIMITE_DERECHO, jugadorX)
        );

        jugadorY = Math.max(
                LIMITE_SUPERIOR,
                Math.min(LIMITE_INFERIOR, jugadorY)
        );
    }

    /**
     * Comprueba si el jugador alcanza una salida abierta y solicita el siguiente nodo al mapa.
     */
    private boolean detectarPuerta() {
        if (!puertasAbiertas || !partidaActiva()) {
            return false;
        }

        /**
         * Guarda direccion para que la clase mantenga su estado interno.
         */
        PuertaVisual.Direccion direccion = null;

        boolean centroX = Math.abs(jugadorX - 500) <= 55;
        boolean centroY = Math.abs(jugadorY - 350) <= 55;

        if (jugadorY <= 80 && centroX) {
            direccion = PuertaVisual.Direccion.ARRIBA;
        } else if (jugadorY >= 620 && centroX) {
            direccion = PuertaVisual.Direccion.ABAJO;
        } else if (jugadorX <= 80 && centroY) {
            direccion = PuertaVisual.Direccion.IZQUIERDA;
        } else if (jugadorX >= 920 && centroY) {
            direccion = PuertaVisual.Direccion.DERECHA;
        }

        if (direccion == null) {
            return false;
        }

        MapaController.SalaNodo destino = mapaController.entrarPor(direccion);
        if (destino == null) return false;
        colocarJugadorEnEntrada(direccion);
        prepararSala(destino);
        return true;
    }

    /**
     * Actualiza los enemigos y aplica daño al detectar contacto durante sus recorridos.
     */
    private void actualizarEnemigos(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        for (EnemigoVisual enemigoVisual : enemigosVisuales) {
            enemigoVisual.actualizar(jugadorX, jugadorY, deltaTiempo);

            if (tiempoInvulnerable <= 0
                    && colisionService.colisionaJugadorEnemigo(jugadorXAnterior,
                            jugadorYAnterior, jugadorX, jugadorY, enemigoVisual, 25)) {

                recibirDanoJugador(enemigoVisual.getDanoContacto());
                tiempoInvulnerable = TIEMPO_INVULNERABLE;
                break;
            }
        }
    }

    /**
     * Escala el daño por mapa y profundidad, lo aplica al modelo y activa el aviso visual.
     */
    private void recibirDanoJugador(int dano) {
        Jugador jugador = juegoService.getJugador();

        if (jugador != null) {
            double multiplicador = 1.50
                    + Math.min(0.90, (mapaController.getActual().getMapa() - 1) * 0.12)
                    + Math.min(0.40, (mapaController.getActual().getProfundidad() - 1) * 0.08);
            int danoAumentado = Math.max(
                    dano + 1,
                    (int) Math.ceil(dano * multiplicador)
            );
            jugador.recibirDano(danoAumentado);
            vista.mostrarDanio();
        }
    }

    /**
     * Avanza los disparos y elimina los que salen del escenario o impactan.
     */
    private void actualizarProyectiles(double deltaTiempo) {
        Iterator<Proyectil> iterador = proyectiles.iterator();

        while (iterador.hasNext()) {
            Proyectil proyectil = iterador.next();
            proyectil.actualizar(deltaTiempo);

            if (proyectil.estaFueraDeLaSala()) {
                vista.quitarProyectil(proyectil);
                iterador.remove();
                continue;
            }

            verificarImpacto(proyectil, iterador);
        }
    }

    /**
     * Busca una intersección entre trayectorias y resuelve el impacto encontrado.
     */
    private void verificarImpacto(
            Proyectil proyectil,
            Iterator<Proyectil> iterador) {

        for (EnemigoVisual enemigoVisual :
                new ArrayList<>(enemigosVisuales)) {

            if (colisionService.colisionaProyectilEnemigo(proyectil, enemigoVisual)) {
                atacarEnemigo(enemigoVisual);

                vista.quitarProyectil(proyectil);
                iterador.remove();
                return;
            }
        }
    }

    /**
     * Delega el ataque a CombateService y retira enemigos derrotados otorgando experiencia.
     */
    private void atacarEnemigo(EnemigoVisual enemigoVisual) {
        Enemigo enemigo = enemigoVisual.getEnemigo();

        try {
            combateService.atacar(juegoService.getJugador(), enemigo);

            if (combateService.estaDerrotado(enemigo)) {
                vista.quitarEnemigo(enemigoVisual);
                enemigosVisuales.remove(enemigoVisual);

                juegoService.derrotarEnemigo(enemigo, jugadorService);
            }
        } catch (DanoInvalidoException e) {
            System.err.println("Error de combate: " + e.getMessage());
        }
    }

    /**
     * Comprueba derrota y limpieza de sala; actualiza mensajes y habilita las puertas.
     */
    private void verificarEstado() {
        if (!juegoService.jugadorEstaVivo()) {
            juegoTerminado = true;
            detener();
            alPerder.accept(configuracion);
            return;
        }

        boolean salaLimpia = enemigosVisuales.isEmpty();

        if (salaLimpia && !puertasAbiertas) {
            puertasAbiertas = true;

            vista.configurarSalidas(
                    mapaController.getActual().getDireccionesSalida(),
                    true
            );
        }

        if (!salaLimpia) {
            vista.actualizarEstado(
                    mapaController.getActual().esJefe()
                            ? "DERROTÁ AL JEFE DEL MAPA " + mapaController.getActual().getMapa()
                            : "DERROTÁ A TODOS LOS ENEMIGOS"
            );
        } else {
            vista.actualizarEstado(
                    mapaController.getActual().esJefe()
                            ? "JEFE DERROTADO - ELEGÍ LA SALIDA"
                            : "SALA LIMPIA - ELEGÍ UNA PUERTA"
            );
        }
    }

    /**
     * Actualiza textos de vida, experiencia, nivel, mapa, sala y dificultad.
     */
    private void actualizarHUD() {
        if (mapaController.getActual() == null) {
            return;
        }

        vista.actualizarHUD(
                juegoService.getJugador(),
                enemigosVisuales.size(),
                mapaController.getActual().getMapa(),
                mapaController.getNumeroSala(),
                mapaController.getActual().getProfundidad(),
                mapaController.getActual().esJefe()
        );
    }

}
