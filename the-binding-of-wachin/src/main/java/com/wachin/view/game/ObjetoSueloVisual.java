package com.wachin.view.game;

import com.wachin.model.ObjetoSuelo;
import com.wachin.model.TipoObjetoSuelo;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/** Representación de un objeto recogible; no contiene reglas de aparición ni efectos. */
public final class ObjetoSueloVisual {
    private final ObjetoSuelo objeto;
    private final Group visual = new Group();

    public ObjetoSueloVisual(ObjetoSuelo objeto) {
        if (objeto == null) throw new IllegalArgumentException("El objeto no puede ser null.");
        this.objeto = objeto;
        String nombre = objeto.getCodigo().toLowerCase();
        String ruta = "assets/images/pickups/" + carpeta(objeto.getTipo()) + "/" + nombre + ".png";
        Node figura = AssetVisual.crearBloque(ruta, objeto.getX(), objeto.getY(),
                30, 30, color(objeto.getTipo()), Color.WHITE, 2);
        Text rotulo = new Text(objeto.getCodigo());
        rotulo.setFill(Color.WHITE);
        rotulo.setStroke(Color.BLACK);
        rotulo.setFont(Font.font("Arial", 10));
        rotulo.setX(objeto.getX() - rotulo.getLayoutBounds().getWidth() / 2);
        rotulo.setY(objeto.getY() + 25);
        visual.getChildren().addAll(figura, rotulo);
        visual.setMouseTransparent(true);
    }

    private String carpeta(TipoObjetoSuelo tipo) {
        return switch (tipo) {
            case PROYECTIL -> "projectiles";
            case INVOCACION -> "summons";
            case CARTA -> "cards";
            case RECOMPENSA -> "rewards";
        };
    }

    private Color color(TipoObjetoSuelo tipo) {
        return switch (tipo) {
            case PROYECTIL -> Color.DEEPSKYBLUE;
            case INVOCACION -> Color.MEDIUMPURPLE;
            case CARTA -> Color.GOLD;
            case RECOMPENSA -> Color.LIMEGREEN;
        };
    }

    public ObjetoSuelo getObjeto() { return objeto; }
    public Node getVisual() { return visual; }
}
