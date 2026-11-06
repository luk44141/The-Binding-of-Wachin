package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/** Vista y comportamiento de persecucion/embestida de una entidad enemiga. */
/**
 * Representa un enemigo en pantalla y actualiza su movimiento, IA de jefe y barra de vida.
 */
public class EnemigoVisual {
    /**
     * Define una opción cerrada disponible para la configuración del juego.
     */
    private enum FaseJefe { PERSEGUIR, PREPARAR, EMBESTIR, RECUPERAR }

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
    /**
     * Constante que define esjefe para este componente.
     */
    private final boolean esJefe;
    /**
     * Guarda velocidad para que la clase mantenga su estado interno.
     */
    private final double velocidad;
    /**
     * Constante que define radiocolision para este componente.
     */
    private final double radioColision;
    /**
     * Constante que define tamanovisual para este componente.
     */
    private final double tamanoVisual;

    /**
     * Guarda x para que la clase mantenga su estado interno.
     */
    private double x;
    /**
     * Guarda y para que la clase mantenga su estado interno.
     */
    private double y;
    /**
     * Constante que define xanterior para este componente.
     */
    private double xAnterior;
    /**
     * Constante que define yanterior para este componente.
     */
    private double yAnterior;
    /**
     * Constante que define tiempofase para este componente.
     */
    private double tiempoFase;
    /**
     * Constante que define direcciondashx para este componente.
     */
    private double direccionDashX;
    /**
     * Constante que define direcciondashy para este componente.
     */
    private double direccionDashY;
    /**
     * Constante que define fasejefe para este componente.
     */
    private FaseJefe faseJefe = FaseJefe.PERSEGUIR;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public EnemigoVisual(Enemigo enemigo, double x, double y) {
        if (enemigo == null) throw new IllegalArgumentException("El enemigo no puede ser null.");
        this.enemigo = enemigo;
        this.x = x;
        this.y = y;
        this.xAnterior = x;
        this.yAnterior = y;
        this.esJefe = enemigo instanceof Jefe;
        this.velocidad = enemigo.getVelocidad();
        this.radioColision = esJefe ? 34 : 22;
        this.tamanoVisual = esJefe ? 112 : 68;
        // El jefe espera antes de su primera embestida para que el jugador pueda posicionarse.
        this.tiempoFase = esJefe ? 2.4 : 0;

        // Poli es el unico sprite normal; el jefe usa el mismo asset a mayor escala.
        /**
         * Constante que define rutaasset para este componente.
         */
        String rutaAsset = esJefe
                ? "assets/images/bosses/palinni.png"
                : "assets/images/enemies/poli.png";
        this.visual = AssetVisual.crear(rutaAsset, x, y,
                tamanoVisual, tamanoVisual,
                esJefe ? Color.PURPLE : Color.CRIMSON,
                esJefe ? Color.GOLD : Color.BLACK,
                esJefe ? 5 : 2);
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
        grupo.getChildren().add(avisoEmbestida);
        actualizarBarraVida();
    }

    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    public void actualizar(double jugadorX, double jugadorY, double deltaTiempo) {
        if (deltaTiempo <= 0) return;
        xAnterior = x;
        yAnterior = y;
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

    /**
     * Avanza las fases de persecución, preparación, embestida y recuperación.
     */
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

    /**
     * Normaliza la dirección al objetivo y avanza según velocidad y tiempo transcurrido.
     */
    private void moverHacia(double objetivoX, double objetivoY, double velocidadActual, double dt) {
        /**
         * Guarda dx para que la clase mantenga su estado interno.
         */
        double dx = objetivoX - x;
        /**
         * Guarda dy para que la clase mantenga su estado interno.
         */
        double dy = objetivoY - y;
        double distancia = Math.hypot(dx, dy);
        if (distancia > 1) {
            x += (dx / distancia) * velocidadActual * dt;
            y += (dy / distancia) * velocidadActual * dt;
        }
    }

    /**
     * Restringe la posición del enemigo a los límites del escenario.
     */
    private void limitarMovimiento() {
        x = Math.max(75, Math.min(925, x));
        y = Math.max(75, Math.min(625, y));
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

    /** El contacto durante el dash suma el dano especial del jefe. */
    /**
     * Devuelve danocontacto sin exponer directamente el atributo.
     */
    public int getDanoContacto() {
        if (esJefe && faseJefe == FaseJefe.EMBESTIR) {
            return enemigo.getDano() + ((Jefe) enemigo).getDanoAdicional();
        }
        return enemigo.getDano();
    }

    /**
     * Comprueba esta condición a partir del estado actual y devuelve el resultado.
     */
    public boolean estaEmbestidaActiva() { return esJefe && faseJefe == FaseJefe.EMBESTIR; }
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
    public double getX() { return x; }
    /**
     * Devuelve coordenada Y sin exponer directamente el atributo.
     */
    public double getY() { return y; }
    /**
     * Devuelve coordenada X anterior sin exponer directamente el atributo.
     */
    public double getXAnterior() { return xAnterior; }
    /**
     * Devuelve coordenada Y anterior sin exponer directamente el atributo.
     */
    public double getYAnterior() { return yAnterior; }
}
