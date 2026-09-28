package com.wachin;

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

import java.util.HashSet;
import java.util.Set;

public class GameApp extends Application {

    private static final int ANCHO = 1000;
    private static final int ALTO = 700;

    private double jugadorX = ANCHO / 2.0;
    private double jugadorY = ALTO / 2.0;

    private final double velocidad = 250;

    private Circle jugador;

    private final Set<KeyCode> teclasPresionadas = new HashSet<>();

    private long ultimoTiempo;

    @Override
    public void start(Stage stage) {

        Pane raiz = new Pane();

        Rectangle fondo = new Rectangle(0, 0, ANCHO, ALTO);
        fondo.setFill(Color.rgb(25, 25, 25));

        Rectangle sala = new Rectangle(50, 50, 900, 600);
        sala.setFill(Color.rgb(45, 45, 45));
        sala.setStroke(Color.rgb(100, 100, 100));
        sala.setStrokeWidth(4);

        jugador = new Circle(jugadorX, jugadorY, 25);
        jugador.setFill(Color.BEIGE);
        jugador.setStroke(Color.BLACK);
        jugador.setStrokeWidth(3);

        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font(20));

        Text vidaTexto = new Text("VIDA: 100 / 100");
        vidaTexto.setX(70);
        vidaTexto.setY(680);
        vidaTexto.setFill(Color.WHITE);
        vidaTexto.setFont(Font.font(18));

        Text controles = new Text("WASD: mover");
        controles.setX(820);
        controles.setY(30);
        controles.setFill(Color.LIGHTGRAY);
        controles.setFont(Font.font(14));

        raiz.getChildren().addAll(
                fondo,
                sala,
                jugador,
                titulo,
                vidaTexto,
                controles
        );

        Scene escena = new Scene(raiz, ANCHO, ALTO);

        escena.setOnKeyPressed(event -> {
            teclasPresionadas.add(event.getCode());
        });

        escena.setOnKeyReleased(event -> {
            teclasPresionadas.remove(event.getCode());
        });

        stage.setTitle("The Binding of Wachin");
        stage.setScene(escena);
        stage.setResizable(false);
        stage.show();

        iniciarGameLoop();
    }

    private void iniciarGameLoop() {

        AnimationTimer gameLoop = new AnimationTimer() {

            @Override
            public void handle(long ahora) {

                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double deltaTiempo = (ahora - ultimoTiempo) / 1_000_000_000.0;

                ultimoTiempo = ahora;

                actualizarJugador(deltaTiempo);
            }
        };

        gameLoop.start();
    }

    private void actualizarJugador(double deltaTiempo) {

        double movimiento = velocidad * deltaTiempo;

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

        jugador.setCenterX(jugadorX);
        jugador.setCenterY(jugadorY);
    }

    private void limitarJugador() {

        double limiteIzquierdo = 75;
        double limiteDerecho = 925;
        double limiteSuperior = 75;
        double limiteInferior = 625;

        if (jugadorX < limiteIzquierdo) {
            jugadorX = limiteIzquierdo;
        }

        if (jugadorX > limiteDerecho) {
            jugadorX = limiteDerecho;
        }

        if (jugadorY < limiteSuperior) {
            jugadorY = limiteSuperior;
        }

        if (jugadorY > limiteInferior) {
            jugadorY = limiteInferior;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}