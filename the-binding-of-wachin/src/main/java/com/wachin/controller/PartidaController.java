
package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jugador;
import com.wachin.service.CombateService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.Proyectil;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    private final Pane raiz;

    private final Text vidaTexto;
    private final Text experienciaTexto;
    private final Text nivelTexto;
    private final Text enemigosTexto;
    private final Text estadoTexto;

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

    private Circle jugadorVisual;

    private double jugadorX = 500;
    private double jugadorY = 350;

    private double tiempoInvulnerable = 0;

    private long ultimoTiempo;

    private AnimationTimer gameLoop;

    public PartidaController(
            Pane raiz,
            Text vidaTexto,
            Text experienciaTexto,
            Text nivelTexto,
            Text enemigosTexto,
            Text estadoTexto) {

        this.raiz = raiz;
        this.vidaTexto = vidaTexto;
        this.experienciaTexto = experienciaTexto;
        this.nivelTexto = nivelTexto;
        this.enemigosTexto = enemigosTexto;
        this.estadoTexto = estadoTexto;
    }

    public void iniciar() {
        crearJugador();
        crearEnemigos();
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

            raiz.getChildren().add(jugadorVisual);

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudo crear a Wachin.",
                    e
            );
        }
    }

    private void crearEnemigos() {
        try {
            crearEnemigo("Slime", 200, 180);
            crearEnemigo("Murcielago", 800, 180);
            crearEnemigo("Demonio", 200, 520);
            crearEnemigo("Monstruo", 800, 520);

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudieron crear los enemigos.",
                    e
            );
        }
    }

    private void crearEnemigo(
            String nombre,
            double x,
            double y)
            throws NombreInvalidoException,
            VidaInvalidaException,
            DanoInvalidoException {

        Enemigo enemigo = new Enemigo(
                nombre,
                50,
                10,
                "NORMAL",
                1.0
        );

        juegoService.agregarEnemigo(enemigo);

        EnemigoVisual visual = new EnemigoVisual(
                enemigo,
                x,
                y
        );

        enemigosVisuales.add(visual);
        raiz.getChildren().add(visual.getVisual());
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
        raiz.getChildren().add(proyectil.getVisual());
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

                if (tiempoInvulnerable > 0) {
                    tiempoInvulnerable -= deltaTiempo;

                    if (tiempoInvulnerable < 0) {
                        tiempoInvulnerable = 0;
                    }
                }

                actualizarJugador(deltaTiempo);
                actualizarEnemigos(deltaTiempo);
                actualizarProyectiles(deltaTiempo);
                verificarEstado();
                actualizarHUD();
            }
        };

        gameLoop.start();
    }

    private boolean partidaActiva() {
        return !juegoService.isPartidaTerminada()
                && juegoService.jugadorEstaVivo();
    }

    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        double movimiento = VELOCIDAD_JUGADOR * deltaTiempo;

        if (teclasPresionadas.contains(KeyCode.W)) {
            jugadorY -= movimiento;
        }

        if (teclasPresionadas.contains(KeyCode.S)) {
            jugadorY += movimiento;
        }

        if (teclasPresionadas.contains(KeyCode.A)) {
            jugadorX -= movimiento;
        }

        if (teclasPresionadas.contains(KeyCode.D)) {
            jugadorX += movimiento;
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
                    && enemigoVisual.tocaJugador(jugadorX, jugadorY)) {

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
                raiz.getChildren().remove(proyectil.getVisual());
                iterador.remove();
                continue;
            }

            verificarImpacto(proyectil, iterador);
        }
    }

    private void verificarImpacto(
            Proyectil proyectil,
            Iterator<Proyectil> iterador) {

        for (int i = 0; i < enemigosVisuales.size(); i++) {
            EnemigoVisual enemigoVisual = enemigosVisuales.get(i);

            double distancia = calcularDistancia(
                    proyectil.getX(),
                    proyectil.getY(),
                    enemigoVisual.getX(),
                    enemigoVisual.getY()
            );

            if (distancia < RADIO_IMPACTO) {
                atacarEnemigo(enemigoVisual);

                raiz.getChildren().remove(proyectil.getVisual());
                iterador.remove();

                break;
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
                raiz.getChildren().remove(enemigoVisual.getVisual());
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
        juegoService.verificarEstado();

        if (juegoService.isPartidaGanada()) {
            estadoTexto.setText("SALA COMPLETADA");
        } else if (juegoService.isPartidaTerminada()) {
            estadoTexto.setText("GAME OVER");
        } else {
            estadoTexto.setText("");
        }
    }

    private void actualizarHUD() {
        Jugador jugador = juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        vidaTexto.setText(
                "VIDA: " + jugador.getVida()
                        + " / " + jugador.getVidaMaxima()
        );

        experienciaTexto.setText(
                "EXP: " + jugador.getExperiencia()
        );

        nivelTexto.setText(
                "NIVEL: " + jugador.getNivel()
        );

        enemigosTexto.setText(
                "ENEMIGOS: " + juegoService.getEnemigos().size()
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
