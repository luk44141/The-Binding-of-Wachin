package com.wachin.view.fx;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Elementos de iluminación baratos: luz del jugador y destello de daño. Son imágenes pregeneradas.
 * La viñeta fija de la sala se pinta en el fondo de {@code EscenarioView}.
 */
public final class Iluminacion {

    private static final double DIAMETRO_LUZ = 340;

    private final ImageView destelloDanio;
    private final ImageView luzJugador;

    public Iluminacion(double ancho, double alto) {
        destelloDanio = pantallaCompleta(Brillo.vineta(Color.web("#ff1c3c"), 0.85, 0.35), ancho, alto);
        destelloDanio.setVisible(false);
        destelloDanio.setOpacity(0);
        luzJugador = Brillo.nodoRadial(Color.web("#ffd9a0"), 0.22, DIAMETRO_LUZ);
    }

    private static ImageView pantallaCompleta(javafx.scene.image.Image imagen, double ancho, double alto) {
        ImageView vista = new ImageView(imagen);
        vista.setFitWidth(ancho);
        vista.setFitHeight(alto);
        vista.setSmooth(true);
        vista.setMouseTransparent(true);
        return vista;
    }

    public Node getDestelloDanio() { return destelloDanio; }
    public Node getLuzJugador() { return luzJugador; }

    public void moverLuz(double x, double y) {
        luzJugador.setTranslateX(x);
        luzJugador.setTranslateY(y);
    }

    public void destellar(double intensidad) {
        destelloDanio.setVisible(true);
        destelloDanio.setOpacity(Math.max(destelloDanio.getOpacity(), Math.min(1, intensidad)));
    }

    public void actualizar(double dt) {
        if (!destelloDanio.isVisible()) return;
        double opacidad = destelloDanio.getOpacity() - dt * 1.8;
        if (opacidad <= 0) {
            destelloDanio.setOpacity(0);
            destelloDanio.setVisible(false);
        } else {
            destelloDanio.setOpacity(opacidad);
        }
    }
}
