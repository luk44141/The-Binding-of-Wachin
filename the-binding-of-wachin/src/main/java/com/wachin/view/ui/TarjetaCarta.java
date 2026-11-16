package com.wachin.view.ui;

import com.wachin.model.CartaTipo;
import com.wachin.view.game.AssetVisual;
import com.wachin.view.game.CatalogoAssets;
import com.wachin.view.game.Dibujos;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Tarjeta visual de una carta del catálogo. */
public final class TarjetaCarta {

    private TarjetaCarta() { }

    public static ImageView icono(CartaTipo tipo, double lado) {
        ImageView vista = new ImageView(AssetVisual.imagen(CatalogoAssets.carta(tipo), "carta_" + tipo,
                () -> Dibujos.iconoCarta(tipo)));
        vista.setFitWidth(lado);
        vista.setFitHeight(lado);
        vista.setPreserveRatio(true);
        vista.setSmooth(false);
        return vista;
    }

    /** Tarjeta grande y seleccionable que se eleva al pasar el cursor. */
    public static VBox seleccionable(CartaTipo tipo, int atajo, Runnable alElegir) {
        VBox tarjeta = new VBox(12);
        tarjeta.getStyleClass().addAll("tarjeta-carta");
        if (tipo.esUnica()) tarjeta.getStyleClass().add("tarjeta-carta-unica");
        tarjeta.setAlignment(Pos.TOP_CENTER);
        tarjeta.setPrefSize(200, 290);
        tarjeta.setMinSize(200, 290);
        tarjeta.setPadding(new javafx.geometry.Insets(18, 14, 16, 14));
        Label numero = new Label(String.valueOf(atajo));
        numero.getStyleClass().add("etiqueta-tenue");
        Label nombre = new Label(tipo.getNombre().toUpperCase());
        nombre.getStyleClass().add("etiqueta-seccion");
        nombre.setStyle("-fx-font-size: 17px;");
        Label descripcion = new Label(tipo.getDescripcion());
        descripcion.getStyleClass().add("etiqueta-dato");
        descripcion.setStyle("-fx-font-size: 14px; -fx-text-fill: #cfc8df;");
        descripcion.setWrapText(true);
        descripcion.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descripcion.setAlignment(Pos.CENTER);
        Label valor = new Label(textoValor(tipo));
        valor.getStyleClass().add("etiqueta-seccion");
        valor.setStyle("-fx-text-fill: #6fdc8c; -fx-font-size: 15px;");
        valor.setWrapText(true);
        valor.setMaxWidth(172);
        valor.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        tarjeta.getChildren().addAll(numero, icono(tipo, 90), nombre, descripcion, valor);
        ScaleTransition entrar = new ScaleTransition(Duration.millis(120), tarjeta);
        entrar.setToX(1.07);
        entrar.setToY(1.07);
        ScaleTransition salir = new ScaleTransition(Duration.millis(120), tarjeta);
        salir.setToX(1);
        salir.setToY(1);
        tarjeta.setOnMouseEntered(e -> { salir.stop(); entrar.playFromStart(); });
        tarjeta.setOnMouseExited(e -> { entrar.stop(); salir.playFromStart(); });
        tarjeta.setOnMouseClicked(e -> alElegir.run());
        return tarjeta;
    }

    public static String textoValor(CartaTipo tipo) {
        return switch (tipo) {
            case DANO -> "+" + tipo.getValor() + " daño de proyectiles";
            case FUERZA -> "+" + tipo.getValor() + " daño para proyectiles e invocaciones";
            case VELOCIDAD -> "+" + tipo.getValor() + " velocidad";
            case VIDA_MAXIMA -> "+" + tipo.getValor() + " vida máxima";
            case DANO_INVOCACIONES -> "+" + tipo.getValor() + " daño de invocaciones";
            case SEGUNDO_ESPACIO_INVOCACION -> "Carta única";
        };
    }
}
