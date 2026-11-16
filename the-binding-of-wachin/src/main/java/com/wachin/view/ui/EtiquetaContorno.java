package com.wachin.view.ui;

import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Texto legible sobre cualquier fondo: una copia oscura desplazada detrás del texto.
 *
 * <p>Sustituye al contorno nativo de {@link Text} ({@code StrokeType.OUTSIDE}), que obliga a
 * trazar la silueta de cada letra y llegaba a consumir cerca de un tercio del tiempo de cada
 * fotograma. Dos textos solo con relleno son mucho más baratos y se guardan como bitmap.</p>
 */
public final class EtiquetaContorno extends Group {

    private final Text sombra = new Text();
    private final Text frente = new Text();
    private final double desplazamiento;

    public EtiquetaContorno(Font fuente, Paint color, double desplazamiento) {
        this.desplazamiento = desplazamiento;
        sombra.setFont(fuente);
        frente.setFont(fuente);
        sombra.setFill(Color.web("#000000", 0.9));
        frente.setFill(color);
        getChildren().addAll(sombra, frente);
        setMouseTransparent(true);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setX(0);
        setY(0);
    }

    public void setText(String texto) {
        sombra.setText(texto);
        frente.setText(texto);
    }

    public String getText() { return frente.getText(); }

    public void setFill(Paint color) { frente.setFill(color); }

    public void setX(double x) {
        frente.setX(x);
        sombra.setX(x + desplazamiento);
    }

    public void setY(double y) {
        frente.setY(y);
        sombra.setY(y + desplazamiento * 1.3);
    }

    /** Ancho del texto sin la sombra. */
    public double getAncho() { return frente.getLayoutBounds().getWidth(); }
}
