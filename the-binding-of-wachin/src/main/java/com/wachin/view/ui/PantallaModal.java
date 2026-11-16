package com.wachin.view.ui;

import com.wachin.service.SonidoService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * Pantalla emergente a pantalla completa: velo oscuro y panel central con animación de entrada.
 * Es la base de pausa, ajustes, recompensas, derrota y victoria.
 */
public final class PantallaModal extends StackPane {

    private final VBox panel = new VBox(14);
    private Consumer<KeyCode> alTeclear = tecla -> { };

    public PantallaModal(double ancho, double alto, String claseBorde) {
        setPrefSize(ancho, alto);
        setMinSize(ancho, alto);
        setMaxSize(ancho, alto);
        Region velo = new Region();
        velo.getStyleClass().add("velo");
        panel.getStyleClass().addAll("panel-modal");
        if (claseBorde != null && !claseBorde.isBlank()) panel.getStyleClass().add(claseBorde);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(28, 40, 28, 40));
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        getChildren().addAll(velo, panel);
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, evento -> alTeclear.accept(evento.getCode()));
    }

    /** Registra un manejador de teclas del panel (por ejemplo, 1/2/3 en la elección de cartas). */
    public void setAlTeclear(Consumer<KeyCode> manejador) { alTeclear = manejador; }

    public VBox getPanel() { return panel; }

    public PantallaModal agregar(Node... nodos) {
        panel.getChildren().addAll(nodos);
        return this;
    }

    /** Reproduce la entrada animada y toma el foco para que el teclado funcione de inmediato. */
    public void mostrar() {
        setOpacity(0);
        panel.setScaleX(0.93);
        panel.setScaleY(0.93);
        FadeTransition fundido = new FadeTransition(Duration.millis(180), this);
        fundido.setToValue(1);
        ScaleTransition escala = new ScaleTransition(Duration.millis(220), panel);
        escala.setToX(1);
        escala.setToY(1);
        new ParallelTransition(fundido, escala).play();
        requestFocus();
    }

    // ───────────────────────────── fábricas de controles ─────────────────────────────

    public static Label titulo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-titulo");
        return etiqueta;
    }

    public static Label subtitulo(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-subtitulo");
        etiqueta.setWrapText(true);
        etiqueta.setMaxWidth(520);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        return etiqueta;
    }

    public static Label etiqueta(String texto, String clase) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add(clase);
        return etiqueta;
    }

    public static Button boton(String texto, String claseExtra, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("btn-wachin");
        if (claseExtra != null && !claseExtra.isBlank()) boton.getStyleClass().add(claseExtra);
        boton.setMinWidth(250);
        boton.setCache(true);
        boton.setCacheHint(javafx.scene.CacheHint.SPEED);
        boton.setOnAction(evento -> {
            SonidoService.obtener().reproducir(SonidoService.Efecto.CLICK);
            accion.run();
        });
        return boton;
    }
}
