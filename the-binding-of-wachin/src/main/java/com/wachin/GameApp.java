package com.wachin;

import com.wachin.controller.PartidaController;
import com.wachin.view.game.PartidaView;
import com.wachin.view.menu.MenuInicial;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApp extends Application {

    private PartidaController partidaController;

    @Override
    public void start(Stage stage) {
        stage.setTitle("The Binding of Wachin");
        stage.setResizable(false);

        Scene escenaMenu = new Scene(
                MenuInicial.crear(
                        configuracion -> iniciarPartida(stage, configuracion)
                ),
                PartidaView.ANCHO,
                PartidaView.ALTO
        );

        stage.setScene(escenaMenu);
        stage.show();
    }

    private void iniciarPartida(
            Stage stage,
            ConfiguracionPartida configuracion) {

        if (partidaController != null) {
            partidaController.detener();
        }

        PartidaView vista = new PartidaView();

        partidaController = new PartidaController(
                vista,
                configuracion
        );

        Scene escenaPartida = new Scene(
                vista.getRaiz(),
                PartidaView.ANCHO,
                PartidaView.ALTO
        );

        escenaPartida.setOnKeyPressed(event -> {
            if (partidaController.teclaPresionada(event.getCode())) {
                partidaController.manejarTecla(event.getCode());
            }
        });

        escenaPartida.setOnKeyReleased(event ->
                partidaController.teclaLiberada(event.getCode())
        );

        stage.setScene(escenaPartida);

        partidaController.iniciar();
        escenaPartida.getRoot().requestFocus();
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