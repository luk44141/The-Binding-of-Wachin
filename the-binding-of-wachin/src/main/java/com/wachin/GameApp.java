package com.wachin;

import com.wachin.controller.PartidaController;
import com.wachin.view.game.PartidaView;
import com.wachin.view.menu.MenuInicial;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Arranca la aplicación JavaFX, muestra el menú y conecta sus opciones con el ciclo de vida de la partida.
 */
public class GameApp extends Application {

    /**
     * Constante que define partidacontroller para este componente.
     */
    private PartidaController partidaController;
    /**
     * Guarda stage para que la clase mantenga su estado interno.
     */
    private Stage stage;

    @Override
    /**
     * Configura la ventana JavaFX, construye la escena inicial y muestra el menú.
     */
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

    /**
     * Crea una vista y un controlador nuevos, enlaza los controles y arranca la partida elegida.
     */
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

    /**
     * Muestra el menú de derrota y conecta la configuración elegida con una nueva partida.
     */
    private void mostrarReintento(ConfiguracionPartida configuracion) {
        stage.setScene(new Scene(MenuInicial.crearReintento(configuracion,
                nuevaConfiguracion -> iniciarPartida(stage, nuevaConfiguracion)),
                PartidaView.ANCHO, PartidaView.ALTO));
    }

    @Override
    /**
     * Detiene la partida activa antes de cerrar la aplicación.
     */
    public void stop() {
        if (partidaController != null) {
            partidaController.detener();
        }
    }

    /**
     * Inicia el programa auxiliar, prueba la conexión a la base de datos e informa el resultado.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
