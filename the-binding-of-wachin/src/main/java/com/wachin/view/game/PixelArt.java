package com.wachin.view.game;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Herramientas para dar a las ilustraciones generadas por codigo una rasterizacion pixelada. */
public final class PixelArt {
    private PixelArt() { }

    /**
     * Reduce la resolucion efectiva, limita la paleta y escala sin interpolacion.
     * Cada muestra ocupa un bloque cuadrado, con transparencia limpia para conservar
     * los bordes de los sprites y evitar el suavizado propio de los dibujos vectoriales.
     */
    public static Image pixelar(Image origen, int tamanoBloque) {
        if (origen == null || origen.getPixelReader() == null || tamanoBloque <= 1) {
            return origen;
        }

        int ancho = (int) Math.ceil(origen.getWidth());
        int alto = (int) Math.ceil(origen.getHeight());
        PixelReader lector = origen.getPixelReader();
        WritableImage destino = new WritableImage(ancho, alto);
        PixelWriter escritor = destino.getPixelWriter();

        for (int y = 0; y < alto; y += tamanoBloque) {
            for (int x = 0; x < ancho; x += tamanoBloque) {
                int muestraX = Math.min(ancho - 1, x + tamanoBloque / 2);
                int muestraY = Math.min(alto - 1, y + tamanoBloque / 2);
                Color muestra = cuantizar(lector.getColor(muestraX, muestraY));

                int limiteY = Math.min(alto, y + tamanoBloque);
                int limiteX = Math.min(ancho, x + tamanoBloque);
                for (int py = y; py < limiteY; py++) {
                    for (int px = x; px < limiteX; px++) {
                        escritor.setColor(px, py, muestra);
                    }
                }
            }
        }
        return destino;
    }

    /** Reduce cada canal a 16 niveles y elimina el alfa parcial de antialiasing. */
    private static Color cuantizar(Color color) {
        if (color.getOpacity() < 0.45) {
            return Color.TRANSPARENT;
        }
        return new Color(
                Math.round(color.getRed() * 15) / 15.0,
                Math.round(color.getGreen() * 15) / 15.0,
                Math.round(color.getBlue() * 15) / 15.0,
                1.0
        );
    }
}
