package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.service.JefeService;
import com.wachin.view.fx.Brillo;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

/**
 * Representación de un enemigo o jefe: sprite con animación de marcha, sombra, barra de vida,
 * destello al recibir daño, aparición y avisos de los ataques del jefe.
 */
public class EnemigoVisual {

    private final Enemigo enemigo;
    private final Group nodo = new Group();
    private final Group cuerpo = new Group();
    private final Group telegrafos = new Group();
    private final ImageView sprite;
    private final Rectangle fondoVida;
    private final Rectangle barraVida;
    private final Rectangle carril = new Rectangle();
    private final Group lanzador = new Group(carril);
    private final Circle anillo = new Circle();
    private final List<Circle> zonas = new ArrayList<>();
    private final boolean esJefe;
    private final double radioColision;
    private final double tamanoVisual;
    private final ColorAdjust destello = new ColorAdjust(0, 0, 0.85, 0);
    private final double fase0 = Math.random() * 6.28;

    private double tiempo;
    private double aparicion;
    private double duracionAparicion;
    private double tiempoDestello;
    private double empujeX;
    private double empujeY;
    private double xPrevio;
    private double mirada = 1;
    private double pulso;
    private int ultimaVidaMostrada = Integer.MIN_VALUE;

    public EnemigoVisual(Enemigo enemigo, double x, double y) {
        if (enemigo == null) throw new IllegalArgumentException("El enemigo no puede ser null.");
        this.enemigo = enemigo;
        enemigo.restaurarPosicion(x, y);
        this.xPrevio = x;
        this.esJefe = enemigo instanceof Jefe;
        boolean cornudo = enemigo instanceof Jefe j && j.getTipoJefe() == Jefe.Tipo.SALA_20;
        this.radioColision = esJefe ? (cornudo ? 42 : 34) : 22;
        this.tamanoVisual = esJefe
                ? (cornudo ? 138 : ((Jefe) enemigo).getTipoJefe() == Jefe.Tipo.SALA_10
                        || ((Jefe) enemigo).getTipoJefe() == Jefe.Tipo.SALA_15 ? 148 : 112)
                : 68;

        Image imagen = esJefe ? imagenJefe((Jefe) enemigo) : imagenNormal(enemigo);
        sprite = AssetVisual.vista(imagen, tamanoVisual, tamanoVisual);
        Color aura = esJefe ? colorAura((Jefe) enemigo) : Color.TRANSPARENT;
        if (esJefe) cuerpo.getChildren().add(crearAura(aura));
        cuerpo.getChildren().add(sprite);
        nodo.getChildren().addAll(telegrafos,
                SombraVisual.crear(tamanoVisual * 0.38, tamanoVisual * 0.13, tamanoVisual * 0.4), cuerpo);

        double anchoBarra = esJefe ? 76 : 46;
        fondoVida = new Rectangle(-anchoBarra / 2, -tamanoVisual / 2 - 12, anchoBarra, esJefe ? 7 : 5);
        barraVida = new Rectangle(-anchoBarra / 2, -tamanoVisual / 2 - 12, anchoBarra, esJefe ? 7 : 5);
        for (Rectangle r : List.of(fondoVida, barraVida)) { r.setArcWidth(5); r.setArcHeight(5); }
        fondoVida.setFill(Color.web("#150a10"));
        fondoVida.setStroke(Color.BLACK);
        cuerpo.getChildren().addAll(fondoVida, barraVida);
        crearTelegrafos();
        sincronizar(0);
    }

    private static Image imagenNormal(Enemigo e) {
        boolean tirador = "TIRADOR".equals(e.getTipoEnemigo());
        return AssetVisual.imagen(CatalogoAssets.enemigo(e.getTipoEnemigo()),
                tirador ? "enemigo_tirador" : "enemigo_poli", tirador ? Dibujos::tirador : Dibujos::poli);
    }

    private static Image imagenJefe(Jefe j) {
        return AssetVisual.imagen(CatalogoAssets.jefe(j.getTipoJefe()), "jefe_" + j.getTipoJefe(), () -> switch (j.getTipoJefe()) {
            case SALA_10 -> Dibujos.jefeArtillero();
            case EMISOR_RAYOS -> Dibujos.jefeEmisor();
            case SALA_15 -> Dibujos.jefePalinni();
            case SALA_20 -> Dibujos.jefeCornudo();
        });
    }

    private static Color colorAura(Jefe j) {
        return switch (j.getTipoJefe()) {
            case SALA_10 -> Color.web("#9b6dff");
            case EMISOR_RAYOS -> Color.web("#4fd6e8");
            case SALA_15 -> Color.web("#ff8a1e");
            case SALA_20 -> Color.web("#ff2d4d");
        };
    }

    private Node crearAura(Color color) {
        return Brillo.nodoRadial(color, 0.32, tamanoVisual * 1.7);
    }

    private void crearTelegrafos() {
        carril.setHeight(radioColision * 1.5);
        carril.setY(-radioColision * 0.75);
        carril.setWidth(260);
        lanzador.setVisible(false);
        anillo.setFill(Color.color(1, 0.3, 0.2, 0.12));
        anillo.setStroke(Color.ORANGERED);
        anillo.setStrokeWidth(4);
        anillo.setVisible(false);
        telegrafos.getChildren().addAll(lanzador, anillo);
        for (int i = 0; i < 3; i++) {
            Circle zona = new Circle(48, Color.color(1, 0.2, 0.12, 0.22));
            zona.setStroke(Color.ORANGERED);
            zona.setStrokeWidth(3);
            zona.getStrokeDashArray().addAll(10.0, 7.0);
            zona.setVisible(false);
            zonas.add(zona);
            telegrafos.getChildren().add(zona);
        }
        telegrafos.setMouseTransparent(true);
        telegrafos.setViewOrder(1000);
    }

    /** Inicia la animación de entrada (escala con rebote) y mantiene al enemigo inactivo visualmente. */
    public void aparecer(double segundos) {
        duracionAparicion = Math.max(0.01, segundos);
        aparicion = duracionAparicion;
    }

    /** Destello blanco y pequeño retroceso desde el punto de impacto. */
    public void golpeado(double origenX, double origenY) {
        tiempoDestello = 0.1;
        sprite.setEffect(destello);
        double dx = enemigo.getX() - origenX;
        double dy = enemigo.getY() - origenY;
        double d = Math.max(1, Math.hypot(dx, dy));
        double fuerza = esJefe ? 2.5 : 7;
        empujeX = dx / d * fuerza;
        empujeY = dy / d * fuerza;
    }

    /** Recibe la posición calculada por la lógica y actualiza el modelo. */
    public void actualizarPosicion(double nuevaX, double nuevaY) {
        enemigo.moverA(nuevaX, nuevaY);
    }

    /** Anima el sprite y las barras a partir del estado actual del modelo. */
    public void actualizar(double dt) {
        tiempo += dt;
        sincronizar(dt);
    }

    private void sincronizar(double dt) {
        double x = enemigo.getX();
        double y = enemigo.getY();
        double velocidadX = dt > 0 ? (x - xPrevio) / dt : 0;
        xPrevio = x;
        if (Math.abs(velocidadX) > 8) mirada = Math.signum(velocidadX);
        double marcha = Math.min(1, Math.abs(velocidadX) / 60 + 0.25);
        double onda = Math.sin(tiempo * (esJefe ? 5 : 11) + fase0);
        double escala = 1;
        if (aparicion > 0) {
            aparicion = Math.max(0, aparicion - dt);
            double p = 1 - aparicion / duracionAparicion;
            escala = p < 0.7 ? p / 0.7 * 1.12 : 1.12 - (p - 0.7) / 0.3 * 0.12;
            cuerpo.setOpacity(Math.min(1, p * 1.8));
        } else {
            cuerpo.setOpacity(1);
        }
        pulso += dt;
        double latido = esJefe ? 1 + Math.sin(pulso * 3) * 0.02 : 1;
        cuerpo.setScaleX(mirada * escala * latido * (1 + onda * 0.05 * marcha));
        cuerpo.setScaleY(escala * latido * (1 - onda * 0.05 * marcha));
        cuerpo.setRotate(onda * (esJefe ? 2 : 6) * marcha);
        empujeX *= Math.exp(-12 * dt);
        empujeY *= Math.exp(-12 * dt);
        cuerpo.setTranslateX(x + empujeX);
        cuerpo.setTranslateY(y + empujeY - Math.abs(onda) * (esJefe ? 1 : 3) * marcha);
        nodo.setViewOrder(-y);
        if (tiempoDestello > 0) {
            tiempoDestello -= dt;
            if (tiempoDestello <= 0) sprite.setEffect(null);
        }
        int vidaActual = enemigo.getVida();
        if (vidaActual != ultimaVidaMostrada) {
            ultimaVidaMostrada = vidaActual;
            double fraccion = Math.max(0, Math.min(1, (double) vidaActual / enemigo.getVidaMaxima()));
            barraVida.setWidth(fondoVida.getWidth() * fraccion);
            barraVida.setFill(fraccion <= 0.25 ? Color.web("#ff3a50")
                    : esJefe ? Color.web("#ffc93a") : Color.web("#7ee06a"));
            boolean mostrarBarra = !esJefe && fraccion < 0.999;
            fondoVida.setVisible(mostrarBarra);
            barraVida.setVisible(mostrarBarra);
        }
        if (lanzador.isVisible()) {
            double parpadeo = 0.55 + 0.45 * Math.sin(tiempo * 24);
            lanzador.setOpacity(parpadeo);
            anillo.setOpacity(parpadeo);
        }
        anillo.setCenterX(x);
        anillo.setCenterY(y);
        lanzador.setTranslateX(x);
        lanzador.setTranslateY(y);
    }

    /** Muestra los avisos de ataque que decide {@code JefeService}. */
    public void mostrarAvisoJefe(JefeService.Ataque ataque, double dx, double dy, List<JefeService.Punto> zonasAviso) {
        boolean visible = esJefe && ataque != null && ataque != JefeService.Ataque.NINGUNO;
        anillo.setVisible(visible && ataque == JefeService.Ataque.RADIAL);
        if (anillo.isVisible()) anillo.setRadius(radioColision + 14 + 10 * Math.abs(Math.sin(tiempo * 12)));
        boolean linea = visible && (ataque == JefeService.Ataque.EMBESTIDA || ataque == JefeService.Ataque.DISPARO
                || ataque == JefeService.Ataque.LASER);
        lanzador.setVisible(linea);
        if (linea) {
            Color color = ataque == JefeService.Ataque.LASER ? Color.web("#4fd6e8") : Color.web("#ff4a30");
            carril.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.55)),
                    new Stop(1, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0))));
            carril.setHeight(ataque == JefeService.Ataque.LASER ? 18 : radioColision * 1.5);
            carril.setY(-carril.getHeight() / 2);
            double angulo = Math.toDegrees(Math.atan2(dy, dx));
            lanzador.getTransforms().setAll(new Rotate(angulo, 0, 0));
        }
        for (int i = 0; i < zonas.size(); i++) {
            Circle circulo = zonas.get(i);
            boolean mostrar = visible && ataque == JefeService.Ataque.ZONAS && zonasAviso != null && i < zonasAviso.size();
            circulo.setVisible(mostrar);
            if (mostrar) {
                circulo.setCenterX(zonasAviso.get(i).x());
                circulo.setCenterY(zonasAviso.get(i).y());
                circulo.setRadius(46 + 4 * Math.sin(tiempo * 14));
            }
        }
    }

    public boolean estaApareciendo() { return aparicion > 0; }
    public double getRadioColision() { return radioColision; }
    public int getDanoContacto() { return enemigo.getDano(); }
    public Enemigo getEnemigo() { return enemigo; }
    public Node getVisual() { return nodo; }
    public double getX() { return enemigo.getX(); }
    public double getY() { return enemigo.getY(); }
    public double getXAnterior() { return enemigo.getXAnterior(); }
    public double getYAnterior() { return enemigo.getYAnterior(); }
    public boolean esJefe() { return esJefe; }
}
