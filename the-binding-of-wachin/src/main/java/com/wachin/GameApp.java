package com.wachin;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.game.EnemigoVisual;
import com.wachin.game.Proyectil;
import com.wachin.model.Enemigo;
import com.wachin.model.Jugador;
import com.wachin.service.CombateService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GameApp extends Application {

    private static final int ANCHO = 1000;
    private static final int ALTO = 700;

    private static final double LIMITE_IZQUIERDO = 75;
    private static final double LIMITE_DERECHO = 925;
    private static final double LIMITE_SUPERIOR = 75;
    private static final double LIMITE_INFERIOR = 625;

    private static final double VELOCIDAD_JUGADOR = 250;
    private static final double VELOCIDAD_PROYECTIL = 500;

    private double jugadorX = 500;
    private double jugadorY = 350;

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

    private Pane raiz;

    private Circle jugadorVisual;

    private Text vidaTexto;
    private Text experienciaTexto;
    private Text nivelTexto;
    private Text enemigosTexto;
    private Text estadoTexto;

    private long ultimoTiempo;

    @Override
    public void start(Stage stage) {

        raiz = new Pane();

        crearEscenario();
        crearJugador();
        crearPartida();
        crearHUD();

        Scene escena = new Scene(
                raiz,
                ANCHO,
                ALTO
        );

        escena.setOnKeyPressed(event -> {

            teclasPresionadas.add(
                    event.getCode()
            );

            manejarTecla(event.getCode());
        });

        escena.setOnKeyReleased(event -> {

            teclasPresionadas.remove(
                    event.getCode()
            );
        });

        stage.setTitle(
                "The Binding of Wachin"
        );

        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();

        iniciarGameLoop();
    }

    private void crearEscenario() {

        Rectangle fondo =
                new Rectangle(
                        0,
                        0,
                        ANCHO,
                        ALTO
                );

        fondo.setFill(
                Color.rgb(20, 20, 20)
        );

        Rectangle sala =
                new Rectangle(
                        50,
                        50,
                        900,
                        600
                );

        sala.setFill(
                Color.rgb(50, 50, 50)
        );

        sala.setStroke(
                Color.rgb(110, 110, 110)
        );

        sala.setStrokeWidth(4);

        raiz.getChildren().addAll(
                fondo,
                sala
        );
    }

    private void crearJugador() {

        try {

            Jugador jugador =
                    new Jugador(
                            "Wachin",
                            100,
                            25,
                            100
                    );

            juegoService.iniciarPartida(
                    jugador
            );

            jugadorVisual =
                    new Circle(
                            jugadorX,
                            jugadorY,
                            25
                    );

            jugadorVisual.setFill(
                    Color.BEIGE
            );

            jugadorVisual.setStroke(
                    Color.BLACK
            );

            jugadorVisual.setStrokeWidth(3);

            raiz.getChildren().add(
                    jugadorVisual
            );

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new RuntimeException(
                    "No se pudo crear a Wachin.",
                    e
            );
        }
    }

    private void crearPartida() {

        try {

            crearEnemigo(
                    "Slime",
                    200,
                    180
            );

            crearEnemigo(
                    "Murcielago",
                    800,
                    180
            );

            crearEnemigo(
                    "Demonio",
                    200,
                    520
            );

            crearEnemigo(
                    "Monstruo",
                    800,
                    520
            );

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new RuntimeException(
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

        Enemigo enemigo =
                new Enemigo(
                        nombre,
                        50,
                        10,
                        "NORMAL",
                        1.0
                );

        juegoService.agregarEnemigo(
                enemigo
        );

        EnemigoVisual visual =
                new EnemigoVisual(
                        enemigo,
                        x,
                        y
                );

        enemigosVisuales.add(
                visual
        );

        raiz.getChildren().add(
                visual.getVisual()
        );
    }

    private void crearHUD() {

        Text titulo =
                new Text(
                        "THE BINDING OF WACHIN"
                );

        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(
                Font.font(20)
        );

        Text controles =
                new Text(
                        "WASD: mover   FLECHAS: disparar"
                );

        controles.setX(680);
        controles.setY(30);
        controles.setFill(
                Color.LIGHTGRAY
        );

        vidaTexto = crearTexto(70, 680);
        experienciaTexto = crearTexto(240, 680);
        nivelTexto = crearTexto(400, 680);
        enemigosTexto = crearTexto(780, 680);

        estadoTexto = crearTexto(350, 100);

        raiz.getChildren().addAll(
                titulo,
                controles,
                vidaTexto,
                experienciaTexto,
                nivelTexto,
                enemigosTexto,
                estadoTexto
        );
    }

    private Text crearTexto(
            double x,
            double y) {

        Text texto = new Text();

        texto.setX(x);
        texto.setY(y);

        texto.setFill(
                Color.WHITE
        );

        texto.setFont(
                Font.font(18)
        );

        return texto;
    }

    private void manejarTecla(
            KeyCode tecla) {

        if (tecla == KeyCode.UP) {
            disparar(0, -1);
        }

        if (tecla == KeyCode.DOWN) {
            disparar(0, 1);
        }

        if (tecla == KeyCode.LEFT) {
            disparar(-1, 0);
        }

        if (tecla == KeyCode.RIGHT) {
            disparar(1, 0);
        }
    }

    private void disparar(
            double direccionX,
            double direccionY) {

        if (!juegoService.jugadorEstaVivo()) {
            return;
        }

        Jugador jugador =
                juegoService.getJugador();

        Proyectil proyectil =
                new Proyectil(
                        jugadorX,
                        jugadorY,
                        direccionX *
                                VELOCIDAD_PROYECTIL,
                        direccionY *
                                VELOCIDAD_PROYECTIL,
                        jugador.getDano()
                );

        proyectiles.add(
                proyectil
        );

        raiz.getChildren().add(
                proyectil.getVisual()
        );
    }

    private void iniciarGameLoop() {

        AnimationTimer gameLoop =
                new AnimationTimer() {

                    @Override
                    public void handle(
                            long ahora) {

                        if (ultimoTiempo == 0) {

                            ultimoTiempo =
                                    ahora;

                            return;
                        }

                        double deltaTiempo =
                                (
                                        ahora -
                                        ultimoTiempo
                                ) /
                                1_000_000_000.0;

                        ultimoTiempo =
                                ahora;

                        actualizarJugador(
                                deltaTiempo
                        );

                        actualizarEnemigos(
                                deltaTiempo
                        );

                        actualizarProyectiles(
                                deltaTiempo
                        );

                        verificarEstado();

                        actualizarHUD();
                    }
                };

        gameLoop.start();
    }

    private void actualizarJugador(
            double deltaTiempo) {

        if (!juegoService.jugadorEstaVivo()) {
            return;
        }

        double movimiento =
                VELOCIDAD_JUGADOR *
                deltaTiempo;

        if (teclasPresionadas.contains(
                KeyCode.W)) {

            jugadorY -= movimiento;
        }

        if (teclasPresionadas.contains(
                KeyCode.S)) {

            jugadorY += movimiento;
        }

        if (teclasPresionadas.contains(
                KeyCode.A)) {

            jugadorX -= movimiento;
        }

        if (teclasPresionadas.contains(
                KeyCode.D)) {

            jugadorX += movimiento;
        }

        limitarJugador();

        jugadorVisual.setCenterX(
                jugadorX
        );

        jugadorVisual.setCenterY(
                jugadorY
        );
    }

    private void actualizarEnemigos(
            double deltaTiempo) {

        if (!juegoService.jugadorEstaVivo()) {
            return;
        }

        for (EnemigoVisual enemigoVisual :
                enemigosVisuales) {

            enemigoVisual.actualizar(
                    jugadorX,
                    jugadorY,
                    deltaTiempo
            );

            if (enemigoVisual.tocaJugador(
                    jugadorX,
                    jugadorY)) {

                recibirDanoJugador(
                        enemigoVisual
                                .getEnemigo()
                                .getDano()
                );
            }
        }
    }

    private void recibirDanoJugador(
            int dano) {

        Jugador jugador =
                juegoService.getJugador();

        jugador.recibirDano(
                dano
        );
    }

    private void actualizarProyectiles(
            double deltaTiempo) {

        Iterator<Proyectil> proyectilIterator =
                proyectiles.iterator();

        while (proyectilIterator.hasNext()) {

            Proyectil proyectil =
                    proyectilIterator.next();

            proyectil.actualizar(
                    deltaTiempo
            );

            if (proyectil.estaFueraDeLaSala()) {

                raiz.getChildren().remove(
                        proyectil.getVisual()
                );

                proyectilIterator.remove();

                continue;
            }

            verificarImpacto(
                    proyectil,
                    proyectilIterator
            );
        }
    }

    private void verificarImpacto(
            Proyectil proyectil,
            Iterator<Proyectil> proyectilIterator) {

        for (int i = 0;
             i < enemigosVisuales.size();
             i++) {

            EnemigoVisual enemigoVisual =
                    enemigosVisuales.get(i);

            double distancia =
                    calcularDistancia(
                            proyectil.getX(),
                            proyectil.getY(),
                            enemigoVisual.getX(),
                            enemigoVisual.getY()
                    );

            if (distancia < 28) {

                atacarEnemigo(
                        enemigoVisual
                );

                raiz.getChildren().remove(
                        proyectil.getVisual()
                );

                proyectilIterator.remove();

                break;
            }
        }
    }

    private void atacarEnemigo(
            EnemigoVisual enemigoVisual) {

        Enemigo enemigo =
                enemigoVisual.getEnemigo();

        try {

            combateService.atacar(
                    juegoService.getJugador(),
                    enemigo
            );

            if (!combateService.estaVivo(
                    enemigo)) {

                raiz.getChildren().remove(
                        enemigoVisual.getVisual()
                );

                enemigosVisuales.remove(
                        enemigoVisual
                );

                juegoService.derrotarEnemigo(
                        enemigo,
                        jugadorService
                );
            }

        } catch (DanoInvalidoException e) {

            System.out.println(
                    "Error de combate: "
                            + e.getMessage()
            );
        }
    }

    private void verificarEstado() {

        juegoService.verificarEstado();

        if (juegoService.isPartidaGanada()) {

            estadoTexto.setText(
                    "SALA COMPLETADA"
            );

        } else if (
                juegoService.isPartidaTerminada()) {

            estadoTexto.setText(
                    "GAME OVER"
            );
        } else {

            estadoTexto.setText("");
        }
    }

    private void actualizarHUD() {

        Jugador jugador =
                juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        vidaTexto.setText(
                "VIDA: "
                        + jugador.getVida()
                        + " / "
                        + jugador.getVidaMaxima()
        );

        experienciaTexto.setText(
                "EXP: "
                        + jugador.getExperiencia()
        );

        nivelTexto.setText(
                "NIVEL: "
                        + jugador.getNivel()
        );

        enemigosTexto.setText(
                "ENEMIGOS: "
                        + juegoService
                        .getEnemigos()
                        .size()
        );
    }

    private void limitarJugador() {

        if (jugadorX <
                LIMITE_IZQUIERDO) {

            jugadorX =
                    LIMITE_IZQUIERDO;
        }

        if (jugadorX >
                LIMITE_DERECHO) {

            jugadorX =
                    LIMITE_DERECHO;
        }

        if (jugadorY <
                LIMITE_SUPERIOR) {

            jugadorY =
                    LIMITE_SUPERIOR;
        }

        if (jugadorY >
                LIMITE_INFERIOR) {

            jugadorY =
                    LIMITE_INFERIOR;
        }
    }

    private double calcularDistancia(
            double x1,
            double y1,
            double x2,
            double y2) {

        double diferenciaX =
                x1 - x2;

        double diferenciaY =
                y1 - y2;

        return Math.sqrt(
                diferenciaX * diferenciaX +
                diferenciaY * diferenciaY
        );
    }

    public static void main(
            String[] args) {

        launch();
    }
}