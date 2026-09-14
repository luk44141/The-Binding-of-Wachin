package com.wachin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class GameApp extends Application {

    @Override
    public void start(Stage stage) {
        Label texto = new Label("The Binding of Wachin");

        Scene escena = new Scene(texto, 800, 600);

        stage.setTitle("The Binding of Wachin");
        stage.setScene(escena);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}