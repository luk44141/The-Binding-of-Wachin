
package com.wachin.view.menu;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuInicial {

    public static StackPane crear(Runnable alJugar) {
        StackPane raiz = new StackPane();
        raiz.setStyle("-fx-background-color: #141414;");

        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 34));

        Button jugar = new Button("JUGAR");
        jugar.setFont(Font.font("Arial", 20));
        jugar.setPrefWidth(200);
        jugar.setPrefHeight(50);
        jugar.setStyle(
                "-fx-background-color: #8b0000;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
        );

        jugar.setOnAction(event -> alJugar.run());

        VBox contenido = new VBox(35, titulo, jugar);
        contenido.setAlignment(Pos.CENTER);

        raiz.getChildren().add(contenido);

        return raiz;
    }
}
