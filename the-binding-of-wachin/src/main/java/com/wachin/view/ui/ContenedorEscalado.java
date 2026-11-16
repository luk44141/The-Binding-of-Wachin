package com.wachin.view.ui;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;

/**
 * Escala un contenido de tamaño fijo para ocupar la ventana manteniendo la proporción y centrándolo
 * con bandas negras. Permite redimensionar la ventana y usar pantalla completa sin deformar el juego.
 */
public final class ContenedorEscalado extends Region {

    private final Pane contenido;
    private final double anchoBase;
    private final double altoBase;

    public ContenedorEscalado(Pane contenido, double anchoBase, double altoBase) {
        this.contenido = contenido;
        this.anchoBase = anchoBase;
        this.altoBase = altoBase;
        setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        getChildren().add(contenido);
        setMinSize(0, 0);
        setPrefSize(anchoBase, altoBase);
    }

    @Override
    protected void layoutChildren() {
        double escala = Math.max(0.1, Math.min(getWidth() / anchoBase, getHeight() / altoBase));
        contenido.resize(anchoBase, altoBase);
        contenido.setScaleX(escala);
        contenido.setScaleY(escala);
        contenido.relocate((getWidth() - anchoBase) / 2, (getHeight() - altoBase) / 2);
    }
}
