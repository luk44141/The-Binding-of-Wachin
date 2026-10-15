
package com.wachin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameApp extends Application {

    private static final int ANCHO = 1000;
    private static final int ALTO = 700;

    private Pane raiz;

    private Text vidaTexto;
    private Text experienciaTexto;
    private Text nivelTexto;
    private Text enemigosTexto;
    private Text estadoTexto;

    private PartidaController partidaController;

    @Override
    public void start(Stage stage) {
        raiz = new Pane();

        crearEscenario();
        crearHUD();

        Scene escena = new Scene(
                raiz,
                ANCHO,
                ALTO
        );

        partidaController = new PartidaController(
                raiz,
                vidaTexto,
                experienciaTexto,
                nivelTexto,
                enemigosTexto,
                estadoTexto
        );

        escena.setOnKeyPressed(event -> {
            KeyCode tecla = event.getCode();

            partidaController.teclaPresionada(tecla);
            partidaController.manejarTecla(tecla);
        });

        escena.setOnKeyReleased(event ->
                partidaController.teclaLiberada(event.getCode())
        );

        stage.setTitle("The Binding of Wachin");
        stage.setScene(escena);
        stage.setResizable(false);

        stage.setOnCloseRequest(event ->
                partidaController.detener()
        );

        stage.show();

        partidaController.iniciar();
    }

    private void crearEscenario() {
        Rectangle fondo = new Rectangle(
                0,
                0,
                ANCHO,
                ALTO
        );

        fondo.setFill(Color.rgb(20, 20, 20));

        Rectangle sala = new Rectangle(
                50,
                50,
                900,
                600
        );

        sala.setFill(Color.rgb(50, 50, 50));
        sala.setStroke(Color.rgb(110, 110, 110));
        sala.setStrokeWidth(4);

        raiz.getChildren().addAll(fondo, sala);
    }

    private void crearHUD() {
        Text titulo = new Text("THE BINDING OF WACHIN");

        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font(20));

        Text controles = new Text(
                "WASD: mover   FLECHAS: disparar"
        );

        controles.setX(680);
        controles.setY(30);
        controles.setFill(Color.LIGHTGRAY);

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

    private Text crearTexto(double x, double y) {
        Text texto = new Text();

        texto.setX(x);
        texto.setY(y);
        texto.setFill(Color.WHITE);
        texto.setFont(Font.font(18));

        return texto;
    }

    @Override
    public void stop() {
        if (partidaController != null) {
            partidaController.detener();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
