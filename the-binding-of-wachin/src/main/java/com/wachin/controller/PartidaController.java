package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import com.wachin.service.CombateService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.service.SalaService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.PartidaView;
import com.wachin.view.game.Proyectil;
import com.wachin.view.game.PuertaVisual;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PartidaController {

    private static final double LIMITE_IZQUIERDO = 75;
    private static final double LIMITE_DERECHO = 925;
    private static final double LIMITE_SUPERIOR = 75;
    private static final double LIMITE_INFERIOR = 625;

    private static final double VELOCIDAD_JUGADOR = 250;
    private static final double VELOCIDAD_PROYECTIL = 500;

    private static final double RADIO_IMPACTO = 28;
    private static final double TIEMPO_INVULNERABLE = 0.8;

    private final PartidaView vista;

    private final Set<KeyCode> teclasPresionadas =
            new HashSet<>();

    private final List<Proyectil> proyectiles =
            new ArrayList<>();

    private final List<EnemigoVisual> enemigosVisuales =
            new ArrayList<>();

    private final JuegoService juegoService =
            new JuegoService();

    private final CombateService combateService =
            new CombateService();

    private final JugadorService jugadorService =
            new JugadorService();

    private final SalaService salaService =
            new SalaService();

    private final Random random = new Random();

    private Circle jugadorVisual;

    private Sala salaActual;

    private double jugadorX = 500;
    private double jugadorY = 350;

    private double tiempoInvulnerable = 0;

    private long ultimoTiempo;

    private AnimationTimer gameLoop;

    private boolean puertasAbiertas;
    private boolean juegoTerminado;

    public PartidaController(PartidaView vista) {
        this.vista = vista;
    }

    public void iniciar() {
        crearJugador();
        generarSala(1, null);
        actualizarHUD();
        iniciarGameLoop();
    }

    public void detener() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void teclaPresionada(KeyCode tecla) {
        teclasPresionadas.add(tecla);
    }

    public void teclaLiberada(KeyCode tecla) {
        teclasPresionadas.remove(tecla);
    }

    public void manejarTecla(KeyCode tecla) {
        if (!partidaActiva()) {
            return;
        }

        if (tecla == KeyCode.UP) {
            disparar(0, -1);
        } else if (tecla == KeyCode.DOWN) {
            disparar(0, 1);
        } else if (tecla == KeyCode.LEFT) {
            disparar(-1, 0);
        } else if (tecla == KeyCode.RIGHT) {
            disparar(1, 0);
        }
    }

    private void crearJugador() {
        try {
            Jugador jugador = new Jugador(
                    "Wachin",
                    100,
                    25,
                    100
            );

            juegoService.iniciarPartida(jugador);

            jugadorVisual = new Circle(
                    jugadorX,
                    jugadorY,
                    25
            );

            jugadorVisual.setFill(Color.BEIGE);
            jugadorVisual.setStroke(Color.BLACK);
            jugadorVisual.setStrokeWidth(3);

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

    private void generarSala(
            int numeroSala,
            PuertaVisual.Direccion salidaAnterior) {

        limpiarSalaAnterior();

        if (salidaAnterior != null) {
            colocarJugadorEnEntrada(salidaAnterior);
        }

        salaActual = salaService.generarSala(numeroSala);
        puertasAbiertas = false;

        vista.configurarSala(salaActual);

        for (Enemigo enemigo : salaActual.getEnemigos()) {
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

        actualizarHUD();
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
        }

        jugadorVisual.setCenterX(jugadorX);
        jugadorVisual.setCenterY(jugadorY);
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
                calcularDistancia(
                        x,
                        y,
                        jugadorX,
                        jugadorY
                ) < 180
                && intentos < 100
        );

        return new double[]{x, y};
    }

    private void disparar(
            double direccionX,
            double direccionY) {

        Jugador jugador = juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        Proyectil proyectil = new Proyectil(
                jugadorX,
                jugadorY,
                direccionX * VELOCIDAD_PROYECTIL,
                direccionY * VELOCIDAD_PROYECTIL,
                jugador.getDano()
        );

        proyectiles.add(proyectil);
        vista.agregarProyectil(proyectil);
    }

    private void iniciarGameLoop() {
        gameLoop = new AnimationTimer() {

            @Override
            public void handle(long ahora) {
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double deltaTiempo =
                        (ahora - ultimoTiempo) / 1_000_000_000.0;

                ultimoTiempo = ahora;

                // Evita saltos grandes si el juego se pausa brevemente.
                deltaTiempo = Math.min(deltaTiempo, 0.05);

                if (tiempoInvulnerable > 0) {
                    tiempoInvulnerable = Math.max(
                            0,
                            tiempoInvulnerable - deltaTiempo
                    );
                }

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
        return !juegoTerminado
                && juegoService.jugadorEstaVivo();
    }

    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        double direccionX = 0;
        double direccionY = 0;

        if (teclasPresionadas.contains(KeyCode.W)) {
            direccionY--;
        }

        if (teclasPresionadas.contains(KeyCode.S)) {
            direccionY++;
        }

        if (teclasPresionadas.contains(KeyCode.A)) {
            direccionX--;
        }

        if (teclasPresionadas.contains(KeyCode.D)) {
            direccionX++;
        }

        double longitud = Math.sqrt(
                direccionX * direccionX
                        + direccionY * direccionY
        );

        if (longitud > 0) {
            direccionX /= longitud;
            direccionY /= longitud;

            jugadorX += direccionX
                    * VELOCIDAD_JUGADOR
                    * deltaTiempo;

            jugadorY += direccionY
                    * VELOCIDAD_JUGADOR
                    * deltaTiempo;
        }

        limitarJugador();

        jugadorVisual.setCenterX(jugadorX);
        jugadorVisual.setCenterY(jugadorY);
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

        PuertaVisual.Direccion salida = null;

        boolean cercaDelCentroHorizontal =
                Math.abs(jugadorX - 500) <= 55;

        boolean cercaDelCentroVertical =
                Math.abs(jugadorY - 350) <= 55;

        if (jugadorY <= 80 && cercaDelCentroHorizontal) {
            salida = PuertaVisual.Direccion.ARRIBA;

        } else if (
                jugadorY >= 620
                        && cercaDelCentroHorizontal) {

            salida = PuertaVisual.Direccion.ABAJO;

        } else if (
                jugadorX <= 80
                        && cercaDelCentroVertical) {

            salida = PuertaVisual.Direccion.IZQUIERDA;

        } else if (
                jugadorX >= 920
                        && cercaDelCentroVertical) {

            salida = PuertaVisual.Direccion.DERECHA;
        }

        if (salida == null) {
            return false;
        }

        generarSala(salaActual.getNumero() + 1, salida);
        return true;
    }

    private void actualizarEnemigos(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        for (EnemigoVisual enemigoVisual : enemigosVisuales) {
            enemigoVisual.actualizar(
                    jugadorX,
                    jugadorY,
                    deltaTiempo
            );

            if (tiempoInvulnerable <= 0
                    && enemigoVisual.tocaJugador(
                            jugadorX,
                            jugadorY
                    )) {

                recibirDanoJugador(
                        enemigoVisual.getEnemigo().getDano()
                );

                tiempoInvulnerable = TIEMPO_INVULNERABLE;
                break;
            }
        }
    }

    private void recibirDanoJugador(int dano) {
        Jugador jugador = juegoService.getJugador();

        if (jugador != null) {
            jugador.recibirDano(dano);
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

        for (EnemigoVisual enemigoVisual
                : new ArrayList<>(enemigosVisuales)) {

            double distancia = calcularDistancia(
                    proyectil.getX(),
                    proyectil.getY(),
                    enemigoVisual.getX(),
                    enemigoVisual.getY()
            );

            if (distancia < RADIO_IMPACTO) {
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
            combateService.atacar(
                    juegoService.getJugador(),
                    enemigo
            );

            if (combateService.estaDerrotado(enemigo)) {
                vista.quitarEnemigo(enemigoVisual);
                enemigosVisuales.remove(enemigoVisual);

                juegoService.derrotarEnemigo(
                        enemigo,
                        jugadorService
                );
            }

        } catch (DanoInvalidoException e) {
            System.err.println(
                    "Error de combate: " + e.getMessage()
            );
        }
    }

    private void verificarEstado() {
        if (!juegoService.jugadorEstaVivo()) {
            if (!juegoTerminado) {
                juegoTerminado = true;

                vista.actualizarEstado(
                        "GAME OVER - SALAS: "
                                + salaActual.getNumero()
                );

                detener();
            }

            return;
        }

        boolean salaLimpia = enemigosVisuales.isEmpty();

        if (salaLimpia && !puertasAbiertas) {
            puertasAbiertas = true;
            vista.actualizarPuertas(true);
        }

        if (salaLimpia) {
            vista.actualizarEstado(
                    "SALA LIMPIA - ELEGÍ UNA PUERTA"
            );
        } else {
            vista.actualizarEstado(
                    "DERROTÁ A TODOS LOS ENEMIGOS"
            );
        }
    }

    private void actualizarHUD() {
        if (salaActual == null) {
            return;
        }

        vista.actualizarHUD(
                juegoService.getJugador(),
                enemigosVisuales.size(),
                salaActual.getNumero(),
                salaActual.getDificultad()
        );
    }

    private double calcularDistancia(
            double x1,
            double y1,
            double x2,
            double y2) {

        double diferenciaX = x1 - x2;
        double diferenciaY = y1 - y2;

        return Math.sqrt(
                diferenciaX * diferenciaX
                        + diferenciaY * diferenciaY
        );
    }
}