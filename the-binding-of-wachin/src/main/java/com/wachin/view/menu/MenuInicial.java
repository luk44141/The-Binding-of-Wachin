package com.wachin.view.menu;

import com.wachin.ConfiguracionPartida;
import com.wachin.model.ProyectilEquipable;
import com.wachin.service.AjustesService;
import com.wachin.view.game.AssetVisual;
import com.wachin.view.game.CatalogoAssets;
import com.wachin.view.game.Dibujos;
import com.wachin.view.game.PartidaView;
import com.wachin.view.ui.FondoAnimado;
import com.wachin.view.ui.PantallaModal;
import com.wachin.view.ui.Pantallas;
import com.wachin.view.ui.Tema;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/** Menú principal: selección de personaje y proyectil, carga de partidas, ajustes y salida. */
public final class MenuInicial {

    /** Operaciones que el menú necesita del resto de la aplicación. */
    public record Acciones(Consumer<ConfiguracionPartida> alJugar,
            Supplier<List<PartidaView.SlotView>> alListarSlots, IntConsumer alCargarSlot, IntConsumer alEliminarSlot) { }

    private final StackPane raiz = new StackPane();
    private final Acciones acciones;
    private PantallaModal abierta;

    private MenuInicial(Acciones acciones) { this.acciones = acciones; }

    public static StackPane crear(Acciones acciones, ConfiguracionPartida inicial) {
        return new MenuInicial(acciones).construir(inicial);
    }

    private StackPane construir(ConfiguracionPartida inicial) {
        raiz.setPrefSize(PartidaView.ANCHO, PartidaView.ALTO);
        raiz.setMinSize(PartidaView.ANCHO, PartidaView.ALTO);
        raiz.setMaxSize(PartidaView.ANCHO, PartidaView.ALTO);
        Tema.aplicar(raiz);
        FondoAnimado fondo = new FondoAnimado(PartidaView.ANCHO, PartidaView.ALTO, Color.web("#5a2a8a"));

        ConfiguracionPartida.Personaje[] personaje = {inicial == null ? ConfiguracionPartida.Personaje.WACHIN : inicial.getPersonaje()};
        ConfiguracionPartida.TipoProyectil[] proyectil = {inicial == null ? ConfiguracionPartida.TipoProyectil.GOTA : inicial.getTipoProyectil()};

        Text antetitulo = Tema.texto("T H E   B I N D I N G   O F", 17, Tema.ORO, true);
        Text titulo = Tema.tituloDorado("WACHIN", 84);
        titulo.setCache(true);
        titulo.setCacheHint(javafx.scene.CacheHint.SPEED);
        antetitulo.setCache(true);
        Label lema = PantallaModal.etiqueta("Un roguelike oscuro de salas, jefes e invocaciones", "etiqueta-subtitulo");

        HBox personajes = new HBox(22);
        personajes.setAlignment(Pos.CENTER);
        List<VBox> tarjetasPersonaje = new ArrayList<>();
        for (ConfiguracionPartida.Personaje p : ConfiguracionPartida.Personaje.values()) {
            VBox tarjeta = tarjetaPersonaje(p);
            tarjetasPersonaje.add(tarjeta);
            activar(tarjeta, () -> {
                personaje[0] = p;
                marcar(tarjetasPersonaje, ConfiguracionPartida.Personaje.values(), p);
            });
            personajes.getChildren().add(tarjeta);
        }
        marcar(tarjetasPersonaje, ConfiguracionPartida.Personaje.values(), personaje[0]);

        HBox proyectiles = new HBox(16);
        proyectiles.setAlignment(Pos.CENTER);
        List<HBox> tarjetasProyectil = new ArrayList<>();
        for (ConfiguracionPartida.TipoProyectil t : ConfiguracionPartida.TipoProyectil.values()) {
            HBox tarjeta = tarjetaProyectil(t);
            tarjetasProyectil.add(tarjeta);
            activar(tarjeta, () -> {
                proyectil[0] = t;
                marcar(tarjetasProyectil, ConfiguracionPartida.TipoProyectil.values(), t);
            });
            proyectiles.getChildren().add(tarjeta);
        }
        marcar(tarjetasProyectil, ConfiguracionPartida.TipoProyectil.values(), proyectil[0]);

        Button jugar = PantallaModal.boton("JUGAR", "btn-primario",
                () -> acciones.alJugar().accept(new ConfiguracionPartida(personaje[0], proyectil[0])));
        jugar.setMinWidth(330);
        Button cargar = PantallaModal.boton("CARGAR", null, this::abrirSlots);
        Button ajustes = PantallaModal.boton("AJUSTES", null,
                () -> abrir(Pantallas.ajustes(AjustesService.obtener(), this::cerrar)));
        Button salir = PantallaModal.boton("SALIR", "btn-sutil", Platform::exit);
        for (Button b : List.of(cargar, ajustes, salir)) b.setMinWidth(104);
        HBox secundarios = new HBox(8, cargar, ajustes, salir);
        secundarios.setAlignment(Pos.CENTER);
        VBox botones = new VBox(10, jugar, secundarios);
        botones.setAlignment(Pos.CENTER);
        Label ayuda = PantallaModal.etiqueta("WASD mover  ·  FLECHAS disparar  ·  ESC pausa  ·  F11 pantalla completa",
                "etiqueta-tenue");

        VBox contenido = new VBox(5, antetitulo, titulo, lema, espacio(12), etiquetaSeccion("ELEGÍ A TU PERSONAJE"),
                personajes, espacio(4), etiquetaSeccion("PROYECTIL INICIAL"), proyectiles, espacio(10), botones,
                espacio(2), ayuda);
        contenido.setAlignment(Pos.CENTER);
        raiz.getChildren().addAll(fondo, contenido);
        raiz.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) cerrar();
        });
        return raiz;
    }

    private void abrir(PantallaModal pantalla) {
        cerrar();
        abierta = pantalla;
        raiz.getChildren().add(pantalla);
        pantalla.mostrar();
    }

    private void cerrar() {
        if (abierta != null) {
            raiz.getChildren().remove(abierta);
            abierta = null;
            raiz.requestFocus();
        }
    }

    private void abrirSlots() {
        try {
            List<PartidaView.SlotView> slots = acciones.alListarSlots().get();
            abrir(Pantallas.slots(slots, false, acciones.alCargarSlot()::accept, slot -> {
                acciones.alEliminarSlot().accept(slot);
                abrirSlots();
            }, this::cerrar));
        } catch (RuntimeException e) {
            Logger.getLogger(MenuInicial.class.getName()).log(Level.WARNING,
                    "No se pudieron consultar los espacios de guardado.", e);
            abrir(Pantallas.mensaje("NO SE PUDO CARGAR", List.of("No se pudieron consultar las partidas guardadas.",
                    String.valueOf(e.getMessage())), true, this::cerrar));
        }
    }

    private static Text etiquetaSeccion(String texto) {
        return Tema.texto(texto, 13, Tema.TEXTO_TENUE, true);
    }

    private static javafx.scene.layout.Region espacio(double alto) {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        r.setMinHeight(alto);
        return r;
    }

    private static void activar(javafx.scene.Node nodo, Runnable accion) {
        nodo.setOnMouseClicked(e -> accion.run());
        nodo.setFocusTraversable(true);
        nodo.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) { accion.run(); e.consume(); }
        });
    }

    private static <T> void marcar(List<? extends javafx.scene.Node> tarjetas, T[] valores, T elegido) {
        for (int i = 0; i < tarjetas.size(); i++) {
            tarjetas.get(i).getStyleClass().remove("tarjeta-opcion-activa");
            if (valores[i] == elegido) tarjetas.get(i).getStyleClass().add("tarjeta-opcion-activa");
        }
    }

    private static VBox tarjetaPersonaje(ConfiguracionPartida.Personaje personaje) {
        Image imagen = AssetVisual.imagen(CatalogoAssets.personaje(personaje), "jugador_" + personaje.name(),
                () -> Dibujos.jugador(personaje == ConfiguracionPartida.Personaje.WACHINA));
        ImageView sprite = AssetVisual.vista(imagen, 88, 88);
        sprite.setX(-sprite.getFitWidth() / 2);
        StackPane marco = new StackPane(sprite);
        marco.setPrefSize(110, 96);
        marco.setMaxSize(110, 96);
        TranslateTransition flotar = new TranslateTransition(Duration.seconds(1.3), sprite);
        flotar.setFromY(-4);
        flotar.setToY(4);
        flotar.setAutoReverse(true);
        flotar.setCycleCount(Animation.INDEFINITE);
        flotar.setInterpolator(Interpolator.EASE_BOTH);
        flotar.play();
        String descripcion = personaje == ConfiguracionPartida.Personaje.WACHIN ? "El clásico de siempre" : "Con moño y mucha actitud";
        VBox tarjeta = new VBox(6, marco, PantallaModal.etiqueta(personaje.getNombre().toUpperCase(), "etiqueta-seccion"),
                PantallaModal.etiqueta(descripcion, "etiqueta-tenue"));
        tarjeta.getStyleClass().add("tarjeta-opcion");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPrefSize(200, 166);
        return tarjeta;
    }

    private static HBox tarjetaProyectil(ConfiguracionPartida.TipoProyectil tipo) {
        ProyectilEquipable equipable = ProyectilEquipable.valueOf(tipo.name());
        Image imagen = AssetVisual.imagen(CatalogoAssets.proyectil(tipo), "proyectil_hud_" + equipable,
                () -> Dibujos.orbe(tipo == ConfiguracionPartida.TipoProyectil.LAGRIMA ? Color.web("#4fd6e8") : Color.web("#ffc93a")));
        ImageView icono = AssetVisual.vista(imagen, 40, 40);
        icono.setX(-20);
        icono.setY(-20);
        StackPane marco = new StackPane(icono);
        marco.setPrefSize(48, 48);
        VBox datos = new VBox(2, PantallaModal.etiqueta(equipable.getNombre().toUpperCase(), "etiqueta-seccion"),
                PantallaModal.etiqueta("Daño " + equipable.getDano(), "etiqueta-tenue"));
        HBox tarjeta = new HBox(12, marco, datos);
        tarjeta.getStyleClass().add("tarjeta-opcion");
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPrefSize(190, 64);
        tarjeta.setCache(true);
        tarjeta.setCacheHint(javafx.scene.CacheHint.SPEED);
        tarjeta.setStyle("-fx-padding: 8 14 8 14;");
        return tarjeta;
    }
}
