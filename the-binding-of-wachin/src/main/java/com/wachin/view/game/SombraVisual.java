package com.wachin.view.game;

import com.wachin.view.fx.Brillo;
import javafx.scene.image.ImageView;

/** Sombra elíptica suave que ancla a las entidades al suelo (imagen compartida, no un degradado por entidad). */
final class SombraVisual {

    private SombraVisual() { }

    static ImageView crear(double radioX, double radioY, double desplazamientoY) {
        ImageView sombra = new ImageView(Brillo.sombra());
        sombra.setFitWidth(radioX * 2);
        sombra.setFitHeight(radioY * 2);
        sombra.setX(-radioX);
        sombra.setY(desplazamientoY - radioY);
        sombra.setMouseTransparent(true);
        return sombra;
    }
}
