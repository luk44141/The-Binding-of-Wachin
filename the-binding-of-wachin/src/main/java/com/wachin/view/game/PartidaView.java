package com.wachin.view.game;

import com.wachin.ConfiguracionPartida;
import com.wachin.model.CartaTipo;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.service.AjustesService;
import com.wachin.view.fx.CapaEfectos;
import com.wachin.view.fx.Iluminacion;
import com.wachin.view.ui.PantallaModal;
import com.wachin.view.ui.Tema;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Compone las capas de la partida: escenario, entidades, efectos, iluminación, HUD y pantallas
 * modales. No calcula movimiento ni daño; recibe los datos y los presenta.
 */
public class PartidaView {

    /** Acciones del menú de pausa. */
    public enum AccionPausa { REANUDAR, CARTAS, INVOCACIONES, GUARDAR, CARGAR, AJUSTES, SALIR }

    /** Resumen de un espacio de guardado para mostrar en pantalla. */
    public record SlotView(int numero, boolean ocupado, String resumen) { }

    public static final int ANCHO = 1000;
    public static final int ALTO = 700;

    private final Pane raiz = new Pane();
    private final Group mundo = new Group();
    private final Group capaSuelo = new Group();
    private final Group capaEntidades = new Group();
    private final Group capaProyectiles = new Group();
    private final EscenarioView escenario = new EscenarioView();
    private final CapaEfectos efectos;
    private final Iluminacion iluminacion = new Iluminacion(ANCHO, ALTO);
    private final HudView hud;
    private final Rectangle fundido = new Rectangle(ANCHO, ALTO, Color.BLACK);
    private final AjustesService ajustes = AjustesService.obtener();
    private final Random azar = new Random();
    private PantallaModal pantallaActual;
    private double sacudida;
    private FadeTransition transicion;

    public PartidaView(ConfiguracionPartida configuracion) {
        raiz.setPrefSize(ANCHO, ALTO);
        raiz.setMinSize(ANCHO, ALTO);
        raiz.setMaxSize(ANCHO, ALTO);
        raiz.setFocusTraversable(true);
        raiz.setStyle("-fx-background-color: #07060b;");
        Tema.aplicar(raiz);
        Rectangle recorte = new Rectangle(ANCHO, ALTO);
        raiz.setClip(recorte);
        efectos = new CapaEfectos(ANCHO, ALTO, ajustes);
        Image retrato = AssetVisual.imagen(CatalogoAssets.personaje(configuracion.getPersonaje()),
                "jugador_" + configuracion.getPersonaje().name(),
                () -> Dibujos.jugador(configuracion.getPersonaje() == ConfiguracionPartida.Personaje.WACHINA));
        hud = new HudView(retrato);
        hud.mostrarFps(ajustes.mostrarFps());
        mundo.getChildren().addAll(escenario.getRaiz(), capaSuelo, capaEntidades, iluminacion.getLuzJugador(),
                capaProyectiles, efectos.getLienzo());
        fundido.setMouseTransparent(true);
        fundido.setOpacity(0);
        raiz.getChildren().addAll(mundo, iluminacion.getDestelloDanio(),
                hud.getRaiz(), fundido);
    }

    public Pane getRaiz() { return raiz; }
    public CapaEfectos efectos() { return efectos; }
    public HudView hud() { return hud; }

    // ───────────────────────────── escenario ─────────────────────────────

    public void configurarSala(int mapa, boolean esSalaJefe, int variante) {
        limpiarObjetosSuelo();
        escenario.configurar(mapa, esSalaJefe, variante);
        efectos.limpiar();
        ocultarBarraJefe();
    }

    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas, PuertaVisual.Direccion entrada, boolean abiertas) {
        escenario.configurarSalidas(salidas, entrada, abiertas);
    }

    // ───────────────────────────── entidades ─────────────────────────────

    public void agregarJugador(JugadorVisual jugador) { capaEntidades.getChildren().add(jugador.getVisual()); }
    public void agregarEnemigo(EnemigoVisual enemigo) { capaEntidades.getChildren().add(enemigo.getVisual()); }
    public void quitarEnemigo(EnemigoVisual enemigo) { capaEntidades.getChildren().remove(enemigo.getVisual()); }
    public void agregarProyectil(ProyectilVisual proyectil) { capaProyectiles.getChildren().add(proyectil.getVisual()); }
    public void quitarProyectil(ProyectilVisual proyectil) { capaProyectiles.getChildren().remove(proyectil.getVisual()); }
    public void agregarObjetoSuelo(ObjetoSueloVisual objeto) { capaSuelo.getChildren().add(objeto.getVisual()); }
    public void quitarObjetoSuelo(ObjetoSueloVisual objeto) { capaSuelo.getChildren().remove(objeto.getVisual()); }
    public void limpiarObjetosSuelo() { capaSuelo.getChildren().clear(); }
    public void agregarInvocacion(InvocacionVisual invocacion) { capaEntidades.getChildren().add(invocacion.getVisual()); }
    public void quitarInvocacion(InvocacionVisual invocacion) { capaEntidades.getChildren().remove(invocacion.getVisual()); }

    // ───────────────────────────── efectos de cámara ─────────────────────────────

    /** Sacude el mundo; la intensidad se mide en píxeles y decae sola. */
    public void sacudir(double intensidad) {
        if (ajustes.sacudidaActiva()) sacudida = Math.max(sacudida, intensidad);
    }

    public void destellarDanio(double intensidad) {
        if (ajustes.calidadHabilitaDestellos()) iluminacion.destellar(intensidad);
    }

    /**
     * Oscurece la pantalla, ejecuta {@code alCubrir} cuando está totalmente negra y vuelve a
     * mostrarla; {@code alTerminar} se ejecuta al finalizar el fundido de entrada.
     */
    public void transicionar(Runnable alCubrir, Runnable alTerminar) {
        if (transicion != null) transicion.stop();
        FadeTransition salida = new FadeTransition(Duration.millis(170), fundido);
        salida.setFromValue(fundido.getOpacity());
        salida.setToValue(1);
        salida.setOnFinished(e -> {
            alCubrir.run();
            FadeTransition entrada = new FadeTransition(Duration.millis(300), fundido);
            entrada.setToValue(0);
            entrada.setOnFinished(f -> alTerminar.run());
            transicion = entrada;
            entrada.play();
        });
        transicion = salida;
        salida.play();
    }

    // ───────────────────────────── ciclo ─────────────────────────────

    /** Avanza animaciones del escenario, efectos, iluminación y HUD. */
    public void actualizar(double dt, double jugadorX, double jugadorY) {
        escenario.actualizar(dt);
        efectos.actualizar(dt);
        iluminacion.actualizar(dt);
        iluminacion.moverLuz(jugadorX, jugadorY);
        hud.animar(dt);
        if (sacudida > 0.15) {
            mundo.setTranslateX((azar.nextDouble() - 0.5) * 2 * sacudida);
            mundo.setTranslateY((azar.nextDouble() - 0.5) * 2 * sacudida);
            sacudida *= Math.exp(-9 * dt);
        } else if (sacudida > 0) {
            sacudida = 0;
            mundo.setTranslateX(0);
            mundo.setTranslateY(0);
        }
    }

    // ───────────────────────────── HUD ─────────────────────────────

    public void actualizarHUD(Jugador jugador, int enemigos, int mapa, int sala, int profundidad, boolean esSalaJefe,
            int xpSiguiente, List<Invocacion> invocaciones, Map<CartaTipo, Integer> cartas) {
        hud.actualizar(jugador, enemigos, mapa, sala, profundidad, esSalaJefe, xpSiguiente, invocaciones, cartas);
    }

    /** Muestra u oculta el contador de FPS y devuelve el nuevo estado. */
    public boolean alternarFps() {
        boolean visible = !ajustes.mostrarFps();
        ajustes.setMostrarFps(visible);
        hud.mostrarFps(visible);
        return visible;
    }

    public void actualizarFps(int fps) { hud.actualizarFps(fps); }

    public void mostrarBarraJefe(Enemigo jefe) { hud.mostrarBarraJefe(jefe); }
    public void ocultarBarraJefe() { hud.ocultarBarraJefe(); }
    public void fijarObjetivo(String texto) { hud.fijarObjetivo(texto); }
    public void mostrarAviso(String mensaje, HudView.Tono tono) { hud.mostrarAviso(mensaje, tono); }
    public void mostrarCartel(String titulo, String subtitulo, double segundos) { hud.mostrarCartel(titulo, subtitulo, segundos); }

    // ───────────────────────────── pantallas modales ─────────────────────────────

    /** Muestra una pantalla modal reemplazando la que hubiera. */
    public void mostrarPantalla(PantallaModal pantalla) {
        ocultarPantalla();
        pantallaActual = pantalla;
        raiz.getChildren().add(pantalla);
        pantalla.mostrar();
    }

    public void ocultarPantalla() {
        if (pantallaActual != null) {
            raiz.getChildren().remove(pantallaActual);
            pantallaActual = null;
            raiz.requestFocus();
        }
    }
}
