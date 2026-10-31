package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/** Vista y comportamiento de persecucion/embestida de una entidad enemiga. */
public class EnemigoVisual {
    private enum FaseJefe { PERSEGUIR, PREPARAR, EMBESTIR, RECUPERAR }

    private final Enemigo enemigo;
    private final Group grupo = new Group();
    private final Node visual;
    private final Rectangle fondoVida;
    private final Rectangle barraVida;
    private final Circle avisoEmbestida;
    private final boolean esJefe;
    private final double velocidad;
    private final double radio;
    private final double tamanoVisual;

    private double x;
    private double y;
    private double tiempoFase;
    private double direccionDashX;
    private double direccionDashY;
    private FaseJefe faseJefe = FaseJefe.PERSEGUIR;

    public EnemigoVisual(Enemigo enemigo, double x, double y) {
        if (enemigo == null) throw new IllegalArgumentException("El enemigo no puede ser null.");
        this.enemigo = enemigo;
        this.x = x;
        this.y = y;
        this.esJefe = enemigo instanceof Jefe;
        this.velocidad = enemigo.getVelocidad();
        this.radio = esJefe ? 43 : 29;
        this.tamanoVisual = esJefe ? 112 : 68;
        // El jefe espera antes de su primera embestida para que el jugador pueda posicionarse.
        this.tiempoFase = esJefe ? 2.4 : 0;

        // Poli es el unico sprite normal; el jefe usa el mismo asset a mayor escala.
        this.visual = AssetVisual.crear("assets/images/enemies/poli.png", x, y,
                tamanoVisual, tamanoVisual,
                esJefe ? Color.PURPLE : Color.CRIMSON,
                esJefe ? Color.GOLD : Color.BLACK,
                esJefe ? 5 : 2);
        grupo.getChildren().add(visual);

        double anchoBarra = esJefe ? 76 : 48;
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
        grupo.getChildren().add(avisoEmbestida);
        actualizarBarraVida();
    }

    public void actualizar(double jugadorX, double jugadorY, double deltaTiempo) {
        if (deltaTiempo <= 0) return;
        if (esJefe) actualizarJefe(jugadorX, jugadorY, deltaTiempo);
        else moverHacia(jugadorX, jugadorY, velocidad, deltaTiempo);
        limitarMovimiento();
        AssetVisual.moverAlCentro(visual, x, y, tamanoVisual, tamanoVisual);
        fondoVida.setX(x - fondoVida.getWidth() / 2);
        fondoVida.setY(y - tamanoVisual / 2 - 12);
        barraVida.setX(fondoVida.getX());
        barraVida.setY(fondoVida.getY());
        avisoEmbestida.setCenterX(x);
        avisoEmbestida.setCenterY(y);
        actualizarBarraVida();
    }

    private void actualizarJefe(double jugadorX, double jugadorY, double dt) {
        Jefe jefe = (Jefe) enemigo;
        double nivel = jefe.getNivelJefe();
        switch (faseJefe) {
            case PERSEGUIR:
                moverHacia(jugadorX, jugadorY, velocidad * 1.22, dt);
                tiempoFase -= dt;
                if (tiempoFase <= 0) {
                    double dx = jugadorX - x;
                    double dy = jugadorY - y;
                    double distancia = Math.max(1, Math.hypot(dx, dy));
                    direccionDashX = dx / distancia;
                    direccionDashY = dy / distancia;
                    faseJefe = FaseJefe.PREPARAR;
                    tiempoFase = Math.max(0.30, 0.62 - nivel * 0.025);
                    avisoEmbestida.setVisible(true);
                }
                break;
            case PREPARAR:
                // El jefe marca la embestida y fija la direccion para dar una ventana de reaccion.
                tiempoFase -= dt;
                if (tiempoFase <= 0) {
                    faseJefe = FaseJefe.EMBESTIR;
                    tiempoFase = Math.max(0.30, 0.58 - nivel * 0.018);
                    avisoEmbestida.setVisible(false);
                }
                break;
            case EMBESTIR:
                double velocidadDash = Math.min(560 + nivel * 55, 1050);
                x += direccionDashX * velocidadDash * dt;
                y += direccionDashY * velocidadDash * dt;
                tiempoFase -= dt;
                if (tiempoFase <= 0 || x <= 78 || x >= 922 || y <= 78 || y >= 622) {
                    faseJefe = FaseJefe.RECUPERAR;
                    tiempoFase = Math.max(0.32, 0.78 - nivel * 0.035);
                }
                break;
            case RECUPERAR:
                tiempoFase -= dt;
                if (tiempoFase <= 0) {
                    faseJefe = FaseJefe.PERSEGUIR;
                    // El cooldown baja con el nivel para que los mapas superiores sean mucho mas agresivos.
                    tiempoFase = Math.max(1.05, 3.0 - nivel * 0.24);
                }
                break;
            default:
                break;
        }
    }

    private void moverHacia(double objetivoX, double objetivoY, double velocidadActual, double dt) {
        double dx = objetivoX - x;
        double dy = objetivoY - y;
        double distancia = Math.hypot(dx, dy);
        if (distancia > 1) {
            x += (dx / distancia) * velocidadActual * dt;
            y += (dy / distancia) * velocidadActual * dt;
        }
    }

    private void limitarMovimiento() {
        x = Math.max(75, Math.min(925, x));
        y = Math.max(75, Math.min(625, y));
    }

    private void actualizarBarraVida() {
        double porcentaje = Math.max(0, Math.min(1,
                (double) enemigo.getVida() / enemigo.getVidaMaxima()));
        barraVida.setWidth(fondoVida.getWidth() * porcentaje);
        barraVida.setFill(porcentaje <= 0.25 ? Color.RED : (esJefe ? Color.GOLD : Color.LIMEGREEN));
    }

    public boolean tocaJugador(double jugadorX, double jugadorY) {
        return Math.hypot(jugadorX - x, jugadorY - y) < radio + 40;
    }

    /** El contacto durante el dash suma el dano especial del jefe. */
    public int getDanoContacto() {
        if (esJefe && faseJefe == FaseJefe.EMBESTIR) {
            return enemigo.getDano() + ((Jefe) enemigo).getDanoAdicional();
        }
        return enemigo.getDano();
    }

    public boolean estaEmbestidaActiva() { return esJefe && faseJefe == FaseJefe.EMBESTIR; }
    public Enemigo getEnemigo() { return enemigo; }
    public Node getVisual() { return grupo; }
    public double getX() { return x; }
    public double getY() { return y; }
}
