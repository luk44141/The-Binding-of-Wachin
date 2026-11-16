package com.wachin;

import com.wachin.controller.PartidaController;
import com.wachin.model.EstadoPartida;
import com.wachin.service.AjustesService;
import com.wachin.service.PartidaService;
import com.wachin.view.game.PartidaView;
import com.wachin.view.menu.MenuInicial;
import com.wachin.view.ui.ContenedorEscalado;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX. Administra la ventana (redimensionable y con pantalla
 * completa), alterna entre el menú y la partida y conecta las acciones entre ambos.
 */
public class GameApp extends Application {

    private static final Logger REGISTRO = Logger.getLogger(GameApp.class.getName());

    static {
        aplicarRendimiento();
    }

    /**
     * JavaFX limita la animación a 60 fps salvo que se le indique otra cosa antes de arrancar.
     * Se aplican las propiedades del límite elegido en Ajustes, sin pisar las que el usuario haya
     * pasado por línea de comandos con {@code -D}.
     */
    private static void aplicarRendimiento() {
        AjustesService.obtener().propiedadesDeRendimiento().forEach((clave, valor) -> {
            if (System.getProperty(clave) == null) System.setProperty(clave, valor);
        });
    }

    private PartidaController partidaController;
    private PartidaService partidaService;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("The Binding of Wachin");
        stage.setMinWidth(800);
        stage.setMinHeight(580);
        stage.setFullScreenExitHint("F11 para salir de la pantalla completa");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        mostrarMenuInicial(null);
        stage.show();
    }

    private Scene crearEscena(Pane contenido) {
        Scene escena = new Scene(new ContenedorEscalado(contenido, PartidaView.ANCHO, PartidaView.ALTO),
                stage.getScene() == null ? PartidaView.ANCHO : stage.getScene().getWidth(),
                stage.getScene() == null ? PartidaView.ALTO : stage.getScene().getHeight());
        escena.addEventFilter(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                evento.consume();
            }
        });
        return escena;
    }

    private PartidaService servicioDePartidas() {
        if (partidaService == null) partidaService = new PartidaService();
        return partidaService;
    }

    private void mostrarMenuInicial(ConfiguracionPartida anterior) {
        detenerPartida();
        MenuInicial.Acciones acciones = new MenuInicial.Acciones(
                configuracion -> iniciarPartida(configuracion, null),
                () -> servicioDePartidas().listarSlots().stream()
                        .map(s -> new PartidaView.SlotView(s.numero(), s.estaOcupado(),
                                s.estaOcupado() ? s.nombre() + " — " + s.fechaGuardado() : "VACÍO"))
                        .toList(),
                this::cargarDesdeMenu,
                slot -> servicioDePartidas().eliminar(slot));
        stage.setScene(crearEscena(MenuInicial.crear(acciones, anterior)));
    }

    private void cargarDesdeMenu(int slot) {
        try {
            EstadoPartida estado = servicioDePartidas().cargar(slot).orElse(null);
            if (estado != null) iniciarPartida(configuracionDe(estado), estado);
        } catch (RuntimeException e) {
            REGISTRO.log(Level.WARNING, "No se pudo cargar el espacio " + slot, e);
        }
    }

    private static ConfiguracionPartida configuracionDe(EstadoPartida estado) {
        return new ConfiguracionPartida(ConfiguracionPartida.Personaje.valueOf(estado.personaje()));
    }

    private void iniciarPartida(ConfiguracionPartida configuracion, EstadoPartida estadoInicial) {
        detenerPartida();
        PartidaView vista = new PartidaView(configuracion);
        PartidaController.Acciones acciones = new PartidaController.Acciones(
                nueva -> iniciarPartida(nueva, null),
                estado -> iniciarPartida(configuracionDe(estado), estado),
                () -> mostrarMenuInicial(configuracion));
        partidaController = new PartidaController(vista, configuracion, acciones, estadoInicial);
        Scene escena = crearEscena(vista.getRaiz());
        escena.setOnKeyPressed(evento -> partidaController.alPresionarTecla(evento.getCode()));
        escena.setOnKeyReleased(evento -> partidaController.alLiberarTecla(evento.getCode()));
        stage.setScene(escena);
        partidaController.iniciar();
        vista.getRaiz().requestFocus();
    }

    private void detenerPartida() {
        if (partidaController != null) {
            partidaController.detener();
            partidaController = null;
        }
    }

    @Override
    public void stop() {
        detenerPartida();
        AjustesService.obtener().guardar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
