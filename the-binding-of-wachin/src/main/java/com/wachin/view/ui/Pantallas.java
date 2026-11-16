package com.wachin.view.ui;

import com.wachin.model.Carta;
import com.wachin.model.CartaTipo;
import com.wachin.model.EstadisticasPartida;
import com.wachin.model.Invocacion;
import com.wachin.model.TipoInvocacion;
import com.wachin.service.AjustesService;
import com.wachin.service.SonidoService;
import com.wachin.view.game.AssetVisual;
import com.wachin.view.game.CatalogoAssets;
import com.wachin.view.game.Dibujos;
import com.wachin.view.game.PartidaView;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Fábrica de las pantallas modales del juego. Cada método devuelve la pantalla ya construida. */
public final class Pantallas {

    private static final double W = PartidaView.ANCHO;
    private static final double H = PartidaView.ALTO;

    private Pantallas() { }

    public static PantallaModal pausa(Consumer<PartidaView.AccionPausa> alElegir) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo("PAUSA"),
                PantallaModal.subtitulo("El juego está detenido. Presioná ESC para volver."),
                PantallaModal.boton("REANUDAR", "btn-primario", () -> alElegir.accept(PartidaView.AccionPausa.REANUDAR)),
                PantallaModal.boton("CARTAS OBTENIDAS", null, () -> alElegir.accept(PartidaView.AccionPausa.CARTAS)),
                PantallaModal.boton("INVOCACIONES", null, () -> alElegir.accept(PartidaView.AccionPausa.INVOCACIONES)),
                PantallaModal.boton("GUARDAR PARTIDA", null, () -> alElegir.accept(PartidaView.AccionPausa.GUARDAR)),
                PantallaModal.boton("CARGAR PARTIDA", null, () -> alElegir.accept(PartidaView.AccionPausa.CARGAR)),
                PantallaModal.boton("AJUSTES", null, () -> alElegir.accept(PartidaView.AccionPausa.AJUSTES)),
                PantallaModal.boton("SALIR AL MENÚ", "btn-peligro", () -> alElegir.accept(PartidaView.AccionPausa.SALIR)));
        pantalla.getPanel().setSpacing(11);
        return pantalla;
    }

    /** Mensaje corto con una lista de líneas y un botón para volver. */
    public static PantallaModal mensaje(String titulo, List<String> filas, boolean error, Runnable alVolver) {
        PantallaModal pantalla = new PantallaModal(W, H, error ? "panel-modal-peligro" : null);
        pantalla.agregar(PantallaModal.titulo(titulo));
        for (String fila : filas) pantalla.agregar(PantallaModal.etiqueta(fila, error ? "etiqueta-error" : "etiqueta-dato"));
        pantalla.agregar(PantallaModal.boton("VOLVER", "btn-primario", alVolver));
        return pantalla;
    }

    public static PantallaModal cartasObtenidas(List<Carta> cartas, Runnable alVolver) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo("CARTAS OBTENIDAS"));
        if (cartas.isEmpty()) {
            pantalla.agregar(PantallaModal.subtitulo("Todavía no obtuviste ninguna carta. Aparecen cada dos salas "
                    + "limpias y como recompensa de jefes."));
        } else {
            VBox lista = new VBox(8);
            for (Carta carta : cartas) {
                HBox fila = new HBox(14);
                fila.getStyleClass().add("fila-slot");
                fila.setAlignment(Pos.CENTER_LEFT);
                VBox texto = new VBox(2,
                        PantallaModal.etiqueta(carta.getNombre().toUpperCase() + "   x" + carta.getAcumulaciones(), "etiqueta-seccion"),
                        PantallaModal.etiqueta(carta.getDescripcion(), "etiqueta-dato"));
                fila.getChildren().addAll(TarjetaCarta.icono(carta.getTipo(), 44), texto);
                lista.getChildren().add(fila);
            }
            lista.setMinWidth(520);
            pantalla.agregar(lista);
        }
        pantalla.agregar(PantallaModal.boton("VOLVER", "btn-primario", alVolver));
        return pantalla;
    }

    public static PantallaModal invocaciones(List<Invocacion> invocaciones, Runnable alVolver) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo("INVOCACIONES"));
        if (invocaciones.isEmpty()) {
            pantalla.agregar(PantallaModal.subtitulo("No tenés invocaciones equipadas. Los jefes y algunos "
                    + "enemigos pueden soltarlas."));
        }
        for (Invocacion i : invocaciones) {
            String ataque = switch (i.getTipoInvocacion()) {
                case GUSANO -> "Protección y cuerpo a cuerpo";
                case MURCIELAGO -> "Cuerpo a cuerpo";
                case ROBOT -> "Disparo láser";
            };
            String coste = i.getTipoInvocacion() == TipoInvocacion.GUSANO
                    ? "ataque " + i.getCostoEnergia() + " · protección " + i.getTipoInvocacion().getCostoProteccion()
                    : "ataque " + i.getCostoEnergia();
            ImageView icono = new ImageView(AssetVisual.imagen(CatalogoAssets.invocacion(i.getTipoInvocacion()),
                    "invocacion_" + i.getTipoInvocacion(), () -> Dibujos.invocacion(i.getTipoInvocacion())));
            icono.setFitWidth(56);
            icono.setFitHeight(56);
            icono.setPreserveRatio(true);
            icono.setSmooth(false);
            BarraRecurso vida = new BarraRecurso(200, 10, Color.web("#ff5a72"), Color.web("#a8162b"));
            vida.establecer(i.getVida(), i.getVidaMaxima());
            vida.actualizar(1);
            BarraRecurso energia = new BarraRecurso(200, 8, Color.web("#8cf2ff"), Color.web("#2a9fb4"));
            energia.establecer(i.getEnergia(), i.getEnergiaMaxima());
            energia.actualizar(1);
            VBox datos = new VBox(3,
                    PantallaModal.etiqueta(i.getTipoInvocacion().getNombre().toUpperCase(), "etiqueta-seccion"),
                    PantallaModal.etiqueta("Vida " + i.getVida() + "/" + i.getVidaMaxima() + " · Daño " + i.getDano()
                            + " · Velocidad " + (int) i.getVelocidad() + " · Rango " + (int) i.getRangoDeteccion(), "etiqueta-dato"),
                    PantallaModal.etiqueta(ataque + " · coste de energía: " + coste + " · recupera "
                            + (int) i.getRecuperacionEnergia() + "/s", "etiqueta-tenue"));
            VBox barras = new VBox(5, vida, energia);
            barras.setAlignment(Pos.CENTER);
            datos.setMinWidth(430);
            HBox.setHgrow(datos, Priority.ALWAYS);
            HBox fila = new HBox(16, icono, datos, barras);
            fila.getStyleClass().add("fila-invocacion");
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setMinWidth(680);
            pantalla.agregar(fila);
        }
        pantalla.agregar(PantallaModal.boton("VOLVER", "btn-primario", alVolver));
        return pantalla;
    }

    public static PantallaModal slots(List<PartidaView.SlotView> slots, boolean guardar,
            IntConsumer alSeleccionar, IntConsumer alEliminar, Runnable alVolver) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo(guardar ? "GUARDAR PARTIDA" : "CARGAR PARTIDA"));
        for (PartidaView.SlotView slot : slots) {
            HBox fila = new HBox(12);
            fila.getStyleClass().add("fila-slot");
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setMinWidth(640);
            VBox texto = new VBox(2, PantallaModal.etiqueta("ESPACIO " + slot.numero(), "etiqueta-seccion"),
                    PantallaModal.etiqueta(slot.resumen(), slot.ocupado() ? "etiqueta-dato" : "etiqueta-tenue"));
            Region relleno = new Region();
            HBox.setHgrow(relleno, Priority.ALWAYS);
            Button usar = PantallaModal.boton(guardar ? (slot.ocupado() ? "SOBRESCRIBIR" : "GUARDAR") : "CARGAR",
                    "btn-primario", () -> alSeleccionar.accept(slot.numero()));
            usar.setMinWidth(150);
            usar.setStyle("-fx-font-size: 14px; -fx-padding: 8 14 8 14;");
            usar.setDisable(!guardar && !slot.ocupado());
            fila.getChildren().addAll(texto, relleno, usar);
            if (slot.ocupado()) {
                Button borrar = PantallaModal.boton("ELIMINAR", "btn-peligro", () -> alEliminar.accept(slot.numero()));
                borrar.setMinWidth(110);
                borrar.setStyle("-fx-font-size: 14px; -fx-padding: 8 14 8 14;");
                fila.getChildren().add(borrar);
            }
            pantalla.agregar(fila);
        }
        pantalla.agregar(PantallaModal.boton("VOLVER", null, alVolver));
        return pantalla;
    }

    public static PantallaModal ajustes(AjustesService ajustes, Runnable alVolver) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo("AJUSTES"));
        Slider volumen = new Slider(0, 100, ajustes.getVolumen());
        volumen.getStyleClass().add("slider-wachin");
        volumen.setPrefWidth(300);
        Label valorVolumen = PantallaModal.etiqueta(ajustes.getVolumen() + "%", "etiqueta-dato");
        valorVolumen.setMinWidth(48);
        volumen.valueProperty().addListener((o, antes, ahora) -> {
            ajustes.setVolumen(ahora.intValue());
            valorVolumen.setText(ahora.intValue() + "%");
        });
        volumen.setOnMouseReleased(e -> SonidoService.obtener().reproducir(SonidoService.Efecto.CARTA));
        pantalla.agregar(filaAjuste("VOLUMEN", new HBox(10, volumen, valorVolumen)));

        ToggleGroup calidad = new ToggleGroup();
        HBox opciones = new HBox(8);
        String[] nombres = {"SIN EFECTOS", "REDUCIDOS", "COMPLETOS"};
        for (AjustesService.CalidadEfectos c : AjustesService.CalidadEfectos.values()) {
            ToggleButton boton = new ToggleButton(nombres[c.ordinal()]);
            boton.getStyleClass().add("toggle-wachin");
            boton.setToggleGroup(calidad);
            boton.setSelected(c == ajustes.getCalidad());
            boton.setOnAction(e -> { if (boton.isSelected()) ajustes.setCalidad(c); else boton.setSelected(true); });
            opciones.getChildren().add(boton);
        }
        pantalla.agregar(filaAjuste("PARTÍCULAS Y EFECTOS", opciones));
        ToggleGroup limite = new ToggleGroup();
        HBox opcionesFps = new HBox(6);
        for (AjustesService.LimiteFps l : AjustesService.LimiteFps.values()) {
            ToggleButton boton = new ToggleButton(l.getEtiqueta());
            boton.getStyleClass().add("toggle-wachin");
            boton.setStyle("-fx-padding: 8 11 8 11;");
            boton.setToggleGroup(limite);
            boton.setSelected(l == ajustes.getLimiteFps());
            boton.setOnAction(e -> { if (boton.isSelected()) ajustes.setLimiteFps(l); else boton.setSelected(true); });
            opcionesFps.getChildren().add(boton);
        }
        pantalla.agregar(filaAjuste("LÍMITE DE FPS", opcionesFps));
        pantalla.agregar(PantallaModal.etiqueta("Se aplica al reiniciar el juego. Con VSync no se supera la frecuencia del monitor; "
                + "«Sin límite» la desactiva.", "etiqueta-tenue"));
        pantalla.agregar(filaAjuste("MOSTRAR FPS (F3)", interruptor(ajustes.mostrarFps(), ajustes::setMostrarFps)));
        pantalla.agregar(filaAjuste("SACUDIDA DE PANTALLA", interruptor(ajustes.sacudidaActiva(), ajustes::setSacudida)));
        pantalla.agregar(filaAjuste("NÚMEROS DE DAÑO", interruptor(ajustes.numerosDanoActivos(), ajustes::setNumerosDano)));
        pantalla.agregar(PantallaModal.subtitulo("F11 alterna la pantalla completa. Los ajustes se guardan automáticamente."));
        pantalla.agregar(PantallaModal.boton("VOLVER", "btn-primario", () -> { ajustes.guardar(); alVolver.run(); }));
        return pantalla;
    }

    private static HBox interruptor(boolean activo, java.util.function.Consumer<Boolean> alCambiar) {
        ToggleGroup grupo = new ToggleGroup();
        ToggleButton si = new ToggleButton("SÍ");
        ToggleButton no = new ToggleButton("NO");
        for (ToggleButton b : List.of(si, no)) { b.getStyleClass().add("toggle-wachin"); b.setToggleGroup(grupo); }
        si.setSelected(activo);
        no.setSelected(!activo);
        si.setOnAction(e -> { si.setSelected(true); alCambiar.accept(true); });
        no.setOnAction(e -> { no.setSelected(true); alCambiar.accept(false); });
        return new HBox(8, si, no);
    }

    private static HBox filaAjuste(String nombre, javafx.scene.Node control) {
        Label etiqueta = PantallaModal.etiqueta(nombre, "etiqueta-seccion");
        etiqueta.setMinWidth(230);
        HBox fila = new HBox(16, etiqueta, control);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setMinWidth(620);
        return fila;
    }

    // ───────────────────────────── fin de partida ─────────────────────────────

    public static PantallaModal derrota(EstadisticasPartida estadisticas, int sala, int nivel,
            Runnable alReintentar, Runnable alMenu) {
        PantallaModal pantalla = new PantallaModal(W, H, "panel-modal-peligro");
        Label titulo = PantallaModal.titulo("HAS CAÍDO");
        titulo.setStyle("-fx-text-fill: #ff4560; -fx-font-size: 52px; -fx-effect: dropshadow(gaussian, rgba(255,69,96,0.7), 24, 0.3, 0, 0);");
        pantalla.agregar(titulo, PantallaModal.subtitulo("Wachin no logró escapar... esta vez."),
                resumen(estadisticas, sala, nivel),
                PantallaModal.boton("VOLVER A INTENTAR", "btn-primario", alReintentar),
                PantallaModal.boton("MENÚ PRINCIPAL", null, alMenu));
        return pantalla;
    }

    public static PantallaModal victoria(EstadisticasPartida estadisticas, int sala, int nivel,
            Runnable alSeguir, Runnable alMenu) {
        PantallaModal pantalla = new PantallaModal(W, H, "panel-modal-oro");
        Label titulo = PantallaModal.titulo("¡VICTORIA!");
        titulo.setStyle("-fx-text-fill: #ffd873; -fx-font-size: 54px; -fx-effect: dropshadow(gaussian, rgba(255,216,115,0.8), 26, 0.3, 0, 0);");
        pantalla.agregar(titulo, PantallaModal.subtitulo("Derrotaste al jefe de la sala 20. La profundidad todavía "
                + "guarda más desafíos si querés seguir."), resumen(estadisticas, sala, nivel),
                PantallaModal.boton("SEGUIR EXPLORANDO", "btn-primario", alSeguir),
                PantallaModal.boton("VOLVER AL MENÚ", null, alMenu));
        return pantalla;
    }

    private static HBox resumen(EstadisticasPartida e, int sala, int nivel) {
        HBox caja = new HBox(30);
        caja.setAlignment(Pos.CENTER);
        caja.setPadding(new Insets(8, 0, 10, 0));
        caja.getChildren().addAll(dato("SALA", String.valueOf(sala)), dato("NIVEL", String.valueOf(nivel)),
                dato("ENEMIGOS", String.valueOf(e.getEnemigosDerrotados())),
                dato("JEFES", String.valueOf(e.getJefesDerrotados())), dato("DAÑO RECIBIDO", String.valueOf(e.getDanoRecibido())),
                dato("TIEMPO", e.tiempoFormateado()));
        return caja;
    }

    private static VBox dato(String nombre, String valor) {
        Label v = new Label(valor);
        v.setStyle("-fx-text-fill: #f4efff; -fx-font-size: 28px; -fx-font-weight: bold;");
        VBox caja = new VBox(2, v, PantallaModal.etiqueta(nombre, "etiqueta-tenue"));
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    // ───────────────────────────── recompensas ─────────────────────────────

    public static PantallaModal seleccionCartas(List<CartaTipo> opciones, Consumer<CartaTipo> alElegir) {
        PantallaModal pantalla = new PantallaModal(W, H, "panel-modal-oro");
        Label titulo = PantallaModal.titulo("ELEGÍ UNA CARTA");
        titulo.setStyle("-fx-text-fill: #ffd873;");
        HBox fila = new HBox(22);
        fila.setAlignment(Pos.CENTER);
        for (int i = 0; i < opciones.size(); i++) {
            CartaTipo carta = opciones.get(i);
            fila.getChildren().add(TarjetaCarta.seleccionable(carta, i + 1, () -> alElegir.accept(carta)));
        }
        pantalla.agregar(titulo, PantallaModal.subtitulo("Hacé clic en una carta o presioná 1, 2 o 3."), fila);
        pantalla.setAlTeclear(tecla -> {
            int indice = tecla == KeyCode.DIGIT1 || tecla == KeyCode.NUMPAD1 ? 0
                    : tecla == KeyCode.DIGIT2 || tecla == KeyCode.NUMPAD2 ? 1
                    : tecla == KeyCode.DIGIT3 || tecla == KeyCode.NUMPAD3 ? 2 : -1;
            if (indice >= 0 && indice < opciones.size()) alElegir.accept(opciones.get(indice));
        });
        return pantalla;
    }

    public static PantallaModal reemplazoInvocacion(List<Invocacion> invocaciones, Consumer<Integer> alElegir) {
        PantallaModal pantalla = new PantallaModal(W, H, null);
        pantalla.agregar(PantallaModal.titulo("¿QUÉ INVOCACIÓN REEMPLAZAR?"),
                PantallaModal.subtitulo("Ambas ranuras están ocupadas. La invocación reemplazada queda en el suelo."));
        for (int i = 0; i < invocaciones.size(); i++) {
            final int espacio = i;
            Invocacion inv = invocaciones.get(i);
            ImageView icono = new ImageView(AssetVisual.imagen(CatalogoAssets.invocacion(inv.getTipoInvocacion()),
                    "invocacion_" + inv.getTipoInvocacion(), () -> Dibujos.invocacion(inv.getTipoInvocacion())));
            icono.setFitWidth(48);
            icono.setFitHeight(48);
            icono.setPreserveRatio(true);
            icono.setSmooth(false);
            Button boton = PantallaModal.boton((i + 1) + ".  " + inv.getTipoInvocacion().getNombre().toUpperCase()
                    + "   vida " + inv.getVida() + "/" + inv.getVidaMaxima() + "   daño " + inv.getDano(),
                    null, () -> alElegir.accept(espacio));
            boton.setGraphic(icono);
            boton.setMinWidth(520);
            pantalla.agregar(boton);
        }
        pantalla.setAlTeclear(tecla -> {
            if (tecla == KeyCode.DIGIT1 && !invocaciones.isEmpty()) alElegir.accept(0);
            if (tecla == KeyCode.DIGIT2 && invocaciones.size() > 1) alElegir.accept(1);
        });
        return pantalla;
    }
}
