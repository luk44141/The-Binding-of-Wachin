package com.wachin.view.game;

import com.wachin.model.Invocacion;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/** Representación de una invocación con barras de vida y energía y un escudo cuando protege. */
public final class InvocacionVisual {

    private static final double TAMANO = 64;
    private final Invocacion invocacion;
    private final Group nodo = new Group();
    private final Group cuerpo = new Group();
    private final Circle escudo = new Circle(TAMANO * 0.62);
    private final Rectangle fondoVida = new Rectangle(-TAMANO * 0.48, -TAMANO / 2 - 13, TAMANO * 0.96, 6);
    private final Rectangle barraVida = new Rectangle(-TAMANO * 0.48, -TAMANO / 2 - 13, TAMANO * 0.96, 6);
    private final Rectangle fondoEnergia = new Rectangle(-TAMANO * 0.48, -TAMANO / 2 - 6, TAMANO * 0.96, 4);
    private final Rectangle barraEnergia = new Rectangle(-TAMANO * 0.48, -TAMANO / 2 - 6, TAMANO * 0.96, 4);
    private final boolean vuela;
    private final double escala;
    private double tiempo;
    private double xPrevia;
    private double mirada = 1;

    public InvocacionVisual(Invocacion invocacion) {
        if (invocacion == null) throw new IllegalArgumentException("Invocación obligatoria.");
        this.invocacion = invocacion;
        this.vuela = invocacion.getTipoInvocacion() == com.wachin.model.TipoInvocacion.MURCIELAGO;
        this.escala = invocacion.getTipoInvocacion() == com.wachin.model.TipoInvocacion.GUSANO ? 1.5 : 1.0;
        Image imagen = AssetVisual.imagen(CatalogoAssets.invocacion(invocacion.getTipoInvocacion()),
                "invocacion_" + invocacion.getTipoInvocacion(), () -> Dibujos.invocacion(invocacion.getTipoInvocacion()));
        ImageView sprite = AssetVisual.vista(imagen, TAMANO, TAMANO);
        escudo.setFill(Color.web("#4fd6e8", 0.14));
        escudo.setStroke(Color.web("#8cf2ff", 0.9));
        escudo.setStrokeWidth(2.5);
        escudo.setVisible(false);
        cuerpo.getChildren().addAll(escudo, sprite);
        fondoVida.setFill(Color.web("#2a0d14"));
        barraVida.setFill(Color.web("#ff4560"));
        fondoEnergia.setFill(Color.web("#0d1a22"));
        barraEnergia.setFill(Color.web("#4fd6e8"));
        for (Rectangle r : new Rectangle[]{fondoVida, barraVida, fondoEnergia, barraEnergia}) {
            r.setArcWidth(4);
            r.setArcHeight(4);
        }
        nodo.getChildren().addAll(SombraVisual.crear(TAMANO * 0.34, TAMANO * 0.12, vuela ? TAMANO * 0.85 : TAMANO * 0.38),
                cuerpo, fondoVida, barraVida, fondoEnergia, barraEnergia);
        nodo.setMouseTransparent(true);
        xPrevia = invocacion.getX();
        actualizar(0);
    }

    public void actualizar(double dt) {
        tiempo += dt;
        double x = invocacion.getX();
        double y = invocacion.getY();
        if (Math.abs(x - xPrevia) > 0.15) mirada = Math.signum(x - xPrevia);
        xPrevia = x;
        double vuelo = vuela ? Math.sin(tiempo * 7) * 5 - 8 : Math.abs(Math.sin(tiempo * 6)) * -3;
        cuerpo.setScaleX(mirada * escala);
        cuerpo.setScaleY(escala);
        cuerpo.setTranslateY(vuelo);
        cuerpo.setRotate(Math.sin(tiempo * 6) * 4);
        boolean protege = invocacion.tieneProteccionActiva();
        escudo.setVisible(protege);
        if (protege) escudo.setOpacity(0.65 + 0.35 * Math.sin(tiempo * 10));
        AssetVisual.moverAlCentro(nodo, x, y);
        nodo.setViewOrder(-y);
        barraVida.setWidth(TAMANO * 0.96 * Math.max(0, invocacion.getVida()) / invocacion.getVidaMaxima());
        barraEnergia.setWidth(TAMANO * 0.96 * invocacion.getEnergia() / invocacion.getEnergiaMaxima());
    }

    public Invocacion getInvocacion() { return invocacion; }
    public Node getVisual() { return nodo; }
}
