
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

public class PartidaController {

    private static final double LIMITE_IZQUIERDO = 75;
    private static final double LIMITE_DERECHO = 925;
    private static final double LIMITE_SUPERIOR = 75;
    private static final double LIMITE_INFERIOR = 625;

    private static final double VELOCIDAD_JUGADOR = 225;
    private static final double VELOCIDAD_PROYECTIL = 430;
    private static final double TIEMPO_INVULNERABLE = 0.45;

    private final PartidaView vista;
    private final ConfiguracionPartida configuracion;
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

    private Node jugadorVisual;

    private double jugadorX = 500;
    private double jugadorY = 350;
    private double jugadorXAnterior = 500;
    private double jugadorYAnterior = 350;
    private double tiempoInvulnerable;

    private long ultimoTiempo;
    private AnimationTimer gameLoop;

    private boolean puertasAbiertas;
    private boolean juegoTerminado;
    private boolean pausado;

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

    public void iniciar() {
        crearJugador();
        prepararSala(mapaController.iniciar());

        iniciarGameLoop();
    }

    public void detener() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public boolean teclaPresionada(KeyCode tecla) {
    if (tecla == KeyCode.ESCAPE || pausado || !partidaActiva()) {
        return false;
    }

    return teclasPresionadas.add(tecla);
    }

    public void teclaLiberada(KeyCode tecla) {
        teclasPresionadas.remove(tecla);
    }

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

    private void alternarPausa() {
        if (pausado) {
            reanudar();
            return;
        }

        pausado = true;
        teclasPresionadas.clear();
        vista.mostrarPausa(this::reanudar);
    }

    private void reanudar() {
        pausado = false;
        teclasPresionadas.clear();
        vista.ocultarPausa();
        ultimoTiempo = 0;
    }

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

    private double[] generarPosicionEnemigo() {
        double x;
        double y;
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

    private boolean partidaActiva() {
        return !juegoTerminado && juegoService.jugadorEstaVivo();
    }

    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        jugadorXAnterior = jugadorX;
        jugadorYAnterior = jugadorY;
        double direccionX = 0;
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

    private boolean detectarPuerta() {
        if (!puertasAbiertas || !partidaActiva()) {
            return false;
        }

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
