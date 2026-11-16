package com.wachin.view.game;

import com.wachin.model.CartaTipo;
import com.wachin.model.ObjetoSuelo;
import com.wachin.model.ProyectilEquipable;
import com.wachin.model.TipoInvocacion;
import com.wachin.model.TipoObjetoSuelo;
import com.wachin.service.RecompensaService;
import com.wachin.view.fx.Brillo;
import com.wachin.view.ui.EtiquetaContorno;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Objeto recogible: icono que flota sobre un halo del color de su categoría y, al acercarse el
 * jugador, el nombre. No contiene reglas de aparición ni efectos.
 */
public final class ObjetoSueloVisual {

    private final ObjetoSuelo objeto;
    private final Group visual = new Group();
    private final Group icono = new Group();
    private final ImageView halo;
    private final Circle anillo;
    private final EtiquetaContorno rotulo;
    private final Color color;
    private double tiempo = Math.random() * 6;
    private double cercania;
    private double aparicion = 0.35;

    public ObjetoSueloVisual(ObjetoSuelo objeto) {
        if (objeto == null) throw new IllegalArgumentException("El objeto no puede ser null.");
        this.objeto = objeto;
        this.color = color(objeto.getTipo());
        halo = Brillo.nodoRadial(color, 0.5, 68);
        anillo = new Circle(19);
        anillo.setFill(Color.TRANSPARENT);
        anillo.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.8));
        anillo.setStrokeWidth(1.6);
        Image imagen = AssetVisual.imagen(CatalogoAssets.objetoSuelo(objeto), "pickup_" + objeto.getTipo() + "_" + objeto.getCodigo(),
                () -> iconoPorCodigo(objeto));
        ImageView sprite = AssetVisual.vista(imagen, 34, 34);
        icono.getChildren().add(sprite);
        rotulo = new EtiquetaContorno(Font.font("Segoe UI", FontWeight.BOLD, 12), color.brighter(), 1.2);
        rotulo.setText(nombre(objeto));
        rotulo.setX(-rotulo.getAncho() / 2);
        rotulo.setY(34);
        rotulo.setOpacity(0);
        visual.getChildren().addAll(SombraVisual.crear(15, 5, 18), halo, anillo, icono, rotulo);
        visual.setMouseTransparent(true);
        AssetVisual.moverAlCentro(visual, objeto.getX(), objeto.getY());
        visual.setViewOrder(-objeto.getY() + 500);
    }

    private static Image iconoPorCodigo(ObjetoSuelo o) {
        return switch (o.getTipo()) {
            case CARTA -> Dibujos.iconoCarta(o.getCodigo());
            case RECOMPENSA -> Dibujos.iconoRecompensa();
            case INVOCACION -> {
                try { yield Dibujos.invocacion(TipoInvocacion.valueOf(o.getCodigo())); }
                catch (IllegalArgumentException e) { yield Dibujos.iconoDesconocido(); }
            }
            case PROYECTIL -> Dibujos.orbe("LAGRIMA".equals(o.getCodigo()) ? Color.web("#4fd6e8") : Color.web("#ffc93a"));
        };
    }

    /** Nombre legible para el rótulo. */
    public static String nombre(ObjetoSuelo objeto) {
        try {
            return switch (objeto.getTipo()) {
                case PROYECTIL -> ProyectilEquipable.desdeCodigo(objeto.getCodigo()).getNombre();
                case INVOCACION -> TipoInvocacion.valueOf(objeto.getCodigo()).getNombre();
                case CARTA -> "SELECCION_CARTAS".equals(objeto.getCodigo()) ? "Mazo de cartas"
                        : CartaTipo.valueOf(objeto.getCodigo()).getNombre();
                case RECOMPENSA -> RecompensaService.AUMENTO_VIDA_MAXIMA_25.equals(objeto.getCodigo())
                        ? "Vida máxima +25%" : objeto.getCodigo();
            };
        } catch (IllegalArgumentException e) {
            return objeto.getCodigo();
        }
    }

    public static Color color(TipoObjetoSuelo tipo) {
        return switch (tipo) {
            case PROYECTIL -> Color.web("#4fd6e8");
            case INVOCACION -> Color.web("#b390ff");
            case CARTA -> Color.web("#ffd873");
            case RECOMPENSA -> Color.web("#6fdc8c");
        };
    }

    /** Anima el flotar y muestra el nombre cuando el jugador está cerca. */
    public void actualizar(double dt, boolean jugadorCerca) {
        tiempo += dt;
        cercania += ((jugadorCerca ? 1 : 0) - cercania) * (1 - Math.exp(-10 * dt));
        double entrada = 1;
        if (aparicion > 0) {
            aparicion = Math.max(0, aparicion - dt);
            entrada = 1 - aparicion / 0.35;
        }
        icono.setTranslateY(-4 - Math.sin(tiempo * 3) * 4 - (1 - entrada) * 18);
        icono.setScaleX(entrada * (1 + 0.06 * cercania));
        icono.setScaleY(entrada * (1 + 0.06 * cercania));
        halo.setOpacity(0.7 + 0.3 * Math.sin(tiempo * 4));
        anillo.setScaleX(1 + 0.12 * Math.sin(tiempo * 4));
        anillo.setScaleY(anillo.getScaleX());
        rotulo.setOpacity(cercania);
    }

    public ObjetoSuelo getObjeto() { return objeto; }
    public Node getVisual() { return visual; }
    public Color getColor() { return color; }
}
