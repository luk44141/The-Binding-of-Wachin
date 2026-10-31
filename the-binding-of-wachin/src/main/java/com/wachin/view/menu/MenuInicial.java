package com.wachin.view.menu;

import com.wachin.ConfiguracionPartida;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.function.Consumer;

public final class MenuInicial {
    private MenuInicial() { }

    public static StackPane crear(Consumer<ConfiguracionPartida> alJugar) {
        StackPane raiz = new StackPane();
        raiz.setStyle("-fx-background-color: #141414;");

        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 34));

        Text etiquetaPersonaje = etiqueta("PERSONAJE");
        ComboBox<ConfiguracionPartida.Personaje> personajes = new ComboBox<>();
        personajes.getItems().setAll(ConfiguracionPartida.Personaje.values());
        personajes.setValue(ConfiguracionPartida.Personaje.WACHIN);
        personajes.setPrefWidth(250);

        Text etiquetaProyectil = etiqueta("TIPO DE PROYECTIL");
        ComboBox<ConfiguracionPartida.TipoProyectil> proyectiles = new ComboBox<>();
        proyectiles.getItems().setAll(ConfiguracionPartida.TipoProyectil.values());
        proyectiles.setValue(ConfiguracionPartida.TipoProyectil.LAGRIMA);
        proyectiles.setPrefWidth(250);

        Button jugar = new Button("JUGAR");
        jugar.setFont(Font.font("Arial", 20));
        jugar.setPrefWidth(250);
        jugar.setPrefHeight(52);
        jugar.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white; -fx-cursor: hand;");
        jugar.setOnAction(event -> alJugar.accept(new ConfiguracionPartida(
                personajes.getValue(), proyectiles.getValue()
        )));

        VBox contenido = new VBox(15, titulo, etiquetaPersonaje, personajes,
                etiquetaProyectil, proyectiles, jugar);
        contenido.setAlignment(Pos.CENTER);
        raiz.getChildren().add(contenido);
        return raiz;
    }

    /** Mantiene compatibilidad con codigo que aun use el menu sin opciones. */
    public static StackPane crear(Runnable alJugar) {
        return crear(configuracion -> alJugar.run());
    }

    private static Text etiqueta(String valor) {
        Text texto = new Text(valor);
        texto.setFill(Color.LIGHTGRAY);
        texto.setFont(Font.font("Arial", 15));
        return texto;
    }
}
