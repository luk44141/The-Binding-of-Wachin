package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import java.util.ArrayList;
import java.util.List;

/** Representación visual de un enemigo y sus indicadores de combate. */
public class EnemigoVisual {
    /**
     * Define una opción cerrada disponible para la configuración del juego.
     */

    /**
     * Guarda enemigo para que la clase mantenga su estado interno.
     */
    private final Enemigo enemigo;
    private final Group grupo = new Group();
    /**
     * Guarda visual para que la clase mantenga su estado interno.
     */
    private final Node visual;
    /**
     * Constante que define fondovida para este componente.
     */
    private final Rectangle fondoVida;
    /**
     * Constante que define barravida para este componente.
     */
    private final Rectangle barraVida;
    /**
     * Constante que define avisoembestida para este componente.
     */
    private final Circle avisoEmbestida;
    private final Line avisoDireccion = new Line();
    private final List<Circle> avisosZonas = new ArrayList<>();
    /**
     * Constante que define esjefe para este componente.
     */
    private final boolean esJefe;
    /**
     * Guarda velocidad para que la clase mantenga su estado interno.
     */
    /**
     * Constante que define radiocolision para este componente.
     */
    private final double radioColision;
    /**
     * Constante que define tamanovisual para este componente.
     */
    private final double tamanoVisual;

    /**
     * Constante que define tiempofase para este componente.
     */

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public EnemigoVisual(Enemigo enemigo, double x, double y) {
        if (enemigo == null) throw new IllegalArgumentException("El enemigo no puede ser null.");
        this.enemigo = enemigo;
        enemigo.restaurarPosicion(x, y);
        this.esJefe = enemigo instanceof Jefe;
        this.radioColision = esJefe ? (enemigo instanceof Jefe j && j.getTipoJefe() == Jefe.Tipo.SALA_20 ? 42 : 34) : 22;
        this.tamanoVisual = esJefe ? (enemigo instanceof Jefe j && j.getTipoJefe() == Jefe.Tipo.SALA_20 ? 138 : 112) : 68;

        // Poli es el unico sprite normal; el jefe usa el mismo asset a mayor escala.
        /**
         * Constante que define rutaasset para este componente.
         */
        String rutaAsset = enemigo instanceof Jefe j
                ? switch (j.getTipoJefe()) {
                    case SALA_10 -> "assets/images/bosses/palinni.png";
                    case EMISOR_RAYOS -> "assets/images/bosses/emisor_rayos.png";
                    case SALA_15 -> "assets/images/bosses/boss_2/boss.png";
                    case SALA_20 -> "assets/images/bosses/boss_3/boss.png";
                }
                : "TIRADOR".equals(enemigo.getTipoEnemigo())
                        ? "assets/images/enemies/ranged/tirador.png" : "assets/images/enemies/poli.png";
        Color color = enemigo instanceof Jefe j
                ? (j.getTipoJefe() == Jefe.Tipo.SALA_20 ? Color.DARKRED
                    : j.getTipoJefe() == Jefe.Tipo.SALA_15 ? Color.DARKORANGE
                    : j.getTipoJefe() == Jefe.Tipo.EMISOR_RAYOS ? Color.DEEPSKYBLUE : Color.PURPLE)
                : "TIRADOR".equals(enemigo.getTipoEnemigo()) ? Color.DODGERBLUE : Color.CRIMSON;
        this.visual = enemigo instanceof Jefe j && j.getTipoJefe() != Jefe.Tipo.SALA_10
                ? AssetVisual.crearBloque(rutaAsset, x, y, tamanoVisual, tamanoVisual,
                        color, Color.GOLD, 4)
                : "TIRADOR".equals(enemigo.getTipoEnemigo())
                        ? AssetVisual.crearBloque(rutaAsset, x, y, tamanoVisual, tamanoVisual,
                                color, Color.DARKBLUE, 2)
                : AssetVisual.crear(rutaAsset, x, y, tamanoVisual, tamanoVisual,
                        color, esJefe ? Color.GOLD : Color.BLACK, esJefe ? 5 : 2);
        grupo.getChildren().add(visual);

        /**
         * Constante que define anchobarra para este componente.
         */
        double anchoBarra = esJefe ? 76 : 48;
        /**
         * Constante que define altobarra para este componente.
         */
        double altoBarra = esJefe ? 8 : 6;
        fondoVida = new Rectangle(x - anchoBarra / 2, y - tamanoVisual / 2 - 12,
                anchoBarra, altoBarra);
        fondoVida.setArcWidth(4);
        fondoVida.setArcHeight(4);
        fondoVida.setFill(Color.rgb(45, 12, 12));
        fondoVida.setStroke(Color.BLACK);
        barraVida = new Rectangle(fondoVida.getX(), fondoVida.getY(), anchoBarra, altoBarra);
        barraVida.setArcWidth(4);
        barraVida.setArcHeight(4);
        barraVida.setFill(esJefe ? Color.GOLD : Color.LIMEGREEN);
        grupo.getChildren().addAll(fondoVida, barraVida);

        avisoEmbestida = new Circle(x, y, 57);
        avisoEmbestida.setFill(Color.TRANSPARENT);
        avisoEmbestida.setStroke(Color.ORANGERED);
        avisoEmbestida.setStrokeWidth(5);
        avisoEmbestida.setVisible(false);
        avisoDireccion.setStroke(Color.ORANGERED);
        avisoDireccion.setStrokeWidth(4);
        avisoDireccion.setVisible(false);
        grupo.getChildren().addAll(avisoEmbestida, avisoDireccion);
        for (int i = 0; i < 3; i++) {
            Circle zona = new Circle(0, 0, 48, Color.rgb(255, 50, 30, 0.25));
            zona.setStroke(Color.ORANGERED);
            zona.setStrokeWidth(3);
            zona.setVisible(false);
            avisosZonas.add(zona);
            grupo.getChildren().add(zona);
        }
        actualizarBarraVida();
    }

    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    /** Recibe la posición calculada por el servicio de IA y solo actualiza la representación. */
    public void actualizarPosicion(double nuevaX, double nuevaY) {
        enemigo.moverA(nuevaX, nuevaY);
        sincronizarVisual();
    }

    private void sincronizarVisual() {
        double x = enemigo.getX();
        double y = enemigo.getY();
        AssetVisual.moverAlCentro(visual, x, y, tamanoVisual, tamanoVisual);
        fondoVida.setX(x - fondoVida.getWidth() / 2);
        fondoVida.setY(y - tamanoVisual / 2 - 12);
        barraVida.setX(fondoVida.getX());
        barraVida.setY(fondoVida.getY());
        avisoEmbestida.setCenterX(x);
        avisoEmbestida.setCenterY(y);
        actualizarBarraVida();
    }

    /** Muestra avisos de ataques decididos por JefeService. */
    public void mostrarAvisoJefe(String ataque, double dx, double dy, List<double[]> zonas) {
        double x = enemigo.getX();
        double y = enemigo.getY();
        boolean visible = esJefe && ataque != null && !ataque.equals("NINGUNO");
        avisoEmbestida.setVisible(visible && ataque.equals("RADIAL"));
        avisoDireccion.setVisible(visible && (ataque.equals("EMBESTIDA")
                || ataque.equals("DISPARO") || ataque.equals("LASER")));
        avisoDireccion.setStroke(ataque.equals("LASER") ? Color.CYAN : Color.ORANGERED);
        avisoDireccion.setStrokeWidth(ataque.equals("LASER") ? 8 : 4);
        avisoDireccion.setStartX(x);
        avisoDireccion.setStartY(y);
        avisoDireccion.setEndX(x + dx * 210);
        avisoDireccion.setEndY(y + dy * 210);
        for (int i = 0; i < avisosZonas.size(); i++) {
            Circle circulo = avisosZonas.get(i);
            boolean mostrar = visible && ataque.equals("ZONAS") && zonas != null && i < zonas.size();
            circulo.setVisible(mostrar);
            if (mostrar) {
                circulo.setCenterX(zonas.get(i)[0]);
                circulo.setCenterY(zonas.get(i)[1]);
            }
        }
    }

    /**
     * Ajusta longitud y color de la barra según la vida restante.
     */
    private void actualizarBarraVida() {
        double porcentaje = Math.max(0, Math.min(1,
                (double) enemigo.getVida() / enemigo.getVidaMaxima()));
        barraVida.setWidth(fondoVida.getWidth() * porcentaje);
        barraVida.setFill(porcentaje <= 0.25 ? Color.RED : (esJefe ? Color.GOLD : Color.LIMEGREEN));
    }

    /**
     * Devuelve radio de colisión sin exponer directamente el atributo.
     */
    public double getRadioColision() { return radioColision; }

    /** Daño de contacto normal; el servicio de jefe aplica el extra durante la embestida. */
    /**
     * Devuelve danocontacto sin exponer directamente el atributo.
     */
    public int getDanoContacto() {
        return enemigo.getDano();
    }

    /**
     * Comprueba esta condición a partir del estado actual y devuelve el resultado.
     */
    public boolean estaEmbestidaActiva() { return avisoDireccion.isVisible(); }
    /**
     * Devuelve enemigo sin exponer directamente el atributo.
     */
    public Enemigo getEnemigo() { return enemigo; }
    /**
     * Devuelve nodo visual sin exponer directamente el atributo.
     */
    public Node getVisual() { return grupo; }
    /**
     * Devuelve coordenada X sin exponer directamente el atributo.
     */
    public double getX() { return enemigo.getX(); }
    /**
     * Devuelve coordenada Y sin exponer directamente el atributo.
     */
    public double getY() { return enemigo.getY(); }
    /**
     * Devuelve coordenada X anterior sin exponer directamente el atributo.
     */
    public double getXAnterior() { return enemigo.getXAnterior(); }
    /**
     * Devuelve coordenada Y anterior sin exponer directamente el atributo.
     */
    public double getYAnterior() { return enemigo.getYAnterior(); }
}
