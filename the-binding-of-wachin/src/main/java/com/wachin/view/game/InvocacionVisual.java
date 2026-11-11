package com.wachin.view.game;

import com.wachin.model.Invocacion;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/** Representación provisional de una invocación con barras de vida y energía. */
public final class InvocacionVisual {
    private final Invocacion invocacion;
    private final Group grupo = new Group();
    private final Node figura;
    private final Rectangle fondoVida = new Rectangle(0, 0, 44, 5);
    private final Rectangle barraVida = new Rectangle(0, 0, 44, 5);
    private final Rectangle fondoEnergia = new Rectangle(0, 0, 44, 4);
    private final Rectangle barraEnergia = new Rectangle(0, 0, 44, 4);
    private final Text etiqueta = new Text();

    public InvocacionVisual(Invocacion invocacion) {
        if (invocacion == null) throw new IllegalArgumentException("Invocación obligatoria.");
        this.invocacion = invocacion;
        Color color = switch (invocacion.getTipoInvocacion()) {
            case GUSANO -> Color.SADDLEBROWN;
            case MURCIELAGO -> Color.DARKMAGENTA;
            case ROBOT -> Color.DODGERBLUE;
        };
        figura = AssetVisual.crearBloque(invocacion.getTipoInvocacion().getRutaAsset(),
                invocacion.getX(), invocacion.getY(), 42, 42, color, Color.BLACK, 2);
        fondoVida.setFill(Color.DARKRED); barraVida.setFill(Color.LIMEGREEN);
        fondoEnergia.setFill(Color.rgb(20, 20, 50)); barraEnergia.setFill(Color.CYAN);
        etiqueta.setFill(Color.WHITE); etiqueta.setStroke(Color.BLACK); etiqueta.setFont(Font.font("Arial", 9));
        grupo.getChildren().addAll(figura, fondoVida, barraVida, fondoEnergia, barraEnergia, etiqueta);
        actualizar();
    }

    public void actualizar() {
        AssetVisual.moverAlCentro(figura, invocacion.getX(), invocacion.getY(), 42, 42);
        double izquierda = invocacion.getX() - 22;
        fondoVida.setX(izquierda); fondoVida.setY(invocacion.getY() - 29);
        barraVida.setX(izquierda); barraVida.setY(invocacion.getY() - 29);
        barraVida.setWidth(44.0 * invocacion.getVida() / invocacion.getVidaMaxima());
        fondoEnergia.setX(izquierda); fondoEnergia.setY(invocacion.getY() - 22);
        barraEnergia.setX(izquierda); barraEnergia.setY(invocacion.getY() - 22);
        barraEnergia.setWidth(44.0 * invocacion.getEnergia() / invocacion.getEnergiaMaxima());
        etiqueta.setText(invocacion.getTipoInvocacion().getNombre() + " " + invocacion.getEstado());
        etiqueta.setX(invocacion.getX() - etiqueta.getLayoutBounds().getWidth() / 2);
        etiqueta.setY(invocacion.getY() + 31);
    }
    public Invocacion getInvocacion() { return invocacion; }
    public Node getVisual() { return grupo; }
}
