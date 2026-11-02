package com.wachin;

import com.wachin.controller.PartidaController;
import com.wachin.view.game.PartidaView;
import com.wachin.view.menu.MenuInicial;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApp extends Application {

    private PartidaController partidaController;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
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
                configuracion,
                this::mostrarReintento
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

    private void mostrarReintento(ConfiguracionPartida configuracion) {
        stage.setScene(new Scene(MenuInicial.crearReintento(configuracion,
                nuevaConfiguracion -> iniciarPartida(stage, nuevaConfiguracion)),
                PartidaView.ANCHO, PartidaView.ALTO));
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
