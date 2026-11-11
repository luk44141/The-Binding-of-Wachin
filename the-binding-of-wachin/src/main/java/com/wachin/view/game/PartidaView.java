package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import com.wachin.model.CartaTipo;
import com.wachin.model.Invocacion;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.Set;

/**
 * Construye y actualiza el escenario, el HUD, las puertas, la pausa y los avisos visuales.
 */
public class PartidaView {
    public enum AccionPausa { REANUDAR, CARTAS, INVOCACIONES, GUARDAR, CARGAR }
    public record SlotView(int numero, boolean ocupado, String resumen) { }
    /**
     * Constante que define ancho para este componente.
     */
    public static final int ANCHO = 1000;
    /**
     * Constante que define alto para este componente.
     */
    public static final int ALTO = 700;

    private final Pane raiz = new Pane();
    private final Rectangle fondo = new Rectangle(0, 0, ANCHO, ALTO);
    private final Rectangle salaVisual = new Rectangle(50, 50, 900, 600);
    private Node pisoVisual;
    private final Text vidaTexto = crearTexto(20, 682);
    private final Text experienciaTexto = crearTexto(185, 682);
    private final Text nivelTexto = crearTexto(340, 682);
    private final Text enemigosTexto = crearTexto(470, 682);
    private final Text mapaTexto = crearTexto(630, 682);
    private final Text salaTexto = crearTexto(755, 682);
    private final Text dificultadTexto = crearTexto(865, 682);
    private final Text estadoTexto = crearTexto(300, 95);
    private final Text tituloJefe = crearTexto(425, 42);
    private final Rectangle fondoVidaJefe = new Rectangle(330, 48, 340, 13);
    private final Rectangle barraVidaJefe = new Rectangle(330, 48, 340, 13);
    private final List<PuertaVisual> puertas = new ArrayList<>();
    private final List<ObjetoSueloVisual> objetosSuelo = new ArrayList<>();
    private final List<InvocacionVisual> invocacionesVisuales = new ArrayList<>();
    /**
     * Constante que define menupausa para este componente.
     */
    private StackPane menuPausa;
    private StackPane menuRecompensa;
    private StackPane menuReemplazoInvocacion;
    /**
     * Constante que define jugadorvisual para este componente.
     */
    private Node jugadorVisual;
    private final Text avisoDanio = crearTexto(430, 145);

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public PartidaView() {
        raiz.setFocusTraversable(true);
        crearEscenario();
        crearPuertas();
        crearHUD();
        avisoDanio.setText("¡DAÑO RECIBIDO!");
        avisoDanio.setFill(Color.RED);
        avisoDanio.setFont(Font.font("Arial", 25));
        avisoDanio.setVisible(false);
        raiz.getChildren().add(avisoDanio);
    }

    /**
     * Configura los colores y agrega el fondo y el área de juego a la raíz.
     */
    private void crearEscenario() {
        fondo.setFill(Color.rgb(18, 18, 22));
        pisoVisual = crearPiso(Color.rgb(52, 52, 58));
        salaVisual.setFill(Color.TRANSPARENT);
        salaVisual.setStroke(Color.rgb(135, 135, 145));
        salaVisual.setStrokeWidth(5);
        Node paredArriba = crearPared(500, 58, 900, 16);
        Node paredAbajo = crearPared(500, 642, 900, 16);
        Node paredIzquierda = crearPared(58, 350, 16, 600);
        Node paredDerecha = crearPared(942, 350, 16, 600);
        raiz.getChildren().addAll(fondo, pisoVisual, paredArriba, paredAbajo,
                paredIzquierda, paredDerecha, salaVisual);
    }

    private Node crearPiso(Color respaldo) {
        return AssetVisual.crearBloque("assets/images/environment/floors/floor.png",
                500, 350, 900, 600, respaldo, respaldo, 0);
    }

    private Node crearPared(double x, double y, double ancho, double alto) {
        return AssetVisual.crearBloque("assets/images/environment/walls/wall.png",
                x, y, ancho, alto, Color.rgb(58, 58, 65), Color.rgb(135, 135, 145), 2);
    }

    /**
     * Crea una puerta visual por dirección y la agrega al escenario.
     */
    private void crearPuertas() {
        for (PuertaVisual.Direccion direccion : PuertaVisual.Direccion.values()) {
            PuertaVisual puerta = new PuertaVisual(direccion);
            puertas.add(puerta);
            raiz.getChildren().add(puerta.getVisual());
        }
    }

    /**
     * Construye títulos, controles, indicadores y barra de vida del jefe.
     */
    private void crearHUD() {
        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 20));
        Text controles = new Text("WASD: mover | FLECHAS: disparar");
        controles.setX(540);
        controles.setY(30);
        controles.setFill(Color.LIGHTGRAY);
        controles.setFont(Font.font("Arial", 14));
        for (Text texto : List.of(vidaTexto, experienciaTexto, nivelTexto, enemigosTexto,
                mapaTexto, salaTexto, dificultadTexto)) texto.setFont(Font.font("Arial", 14));

        tituloJefe.setFill(Color.GOLD);
        tituloJefe.setFont(Font.font("Arial", 13));
        fondoVidaJefe.setFill(Color.rgb(45, 12, 12));
        fondoVidaJefe.setStroke(Color.BLACK);
        barraVidaJefe.setFill(Color.GOLD);
        tituloJefe.setVisible(false);
        fondoVidaJefe.setVisible(false);
        barraVidaJefe.setVisible(false);

        raiz.getChildren().addAll(titulo, controles, vidaTexto, experienciaTexto, nivelTexto,
                enemigosTexto, mapaTexto, salaTexto, dificultadTexto, estadoTexto,
                tituloJefe, fondoVidaJefe, barraVidaJefe);
        actualizarEstado("");
    }

    /**
     * Construye un nodo Text en las coordenadas indicadas.
     */
    private Text crearTexto(double x, double y) {
        Text texto = new Text();
        texto.setX(x);
        texto.setY(y);
        texto.setFill(Color.WHITE);
        return texto;
    }

    /**
     * Devuelve el nodo raíz para incorporarlo a una escena JavaFX.
     */
    public Pane getRaiz() { return raiz; }

    /**
     * Actualiza el aspecto de la sala y reinicia puertas y elementos visuales previos.
     */
    public void configurarSala(Sala sala, int numeroMapa, boolean esSalaJefe) {
        limpiarObjetosSuelo();
        Color[] colores = {Color.rgb(52, 52, 58), Color.rgb(58, 45, 48),
                Color.rgb(42, 52, 49), Color.rgb(48, 45, 65), Color.rgb(65, 52, 38)};
        Color colorPiso = esSalaJefe ? Color.rgb(65, 25, 38)
                : colores[(numeroMapa - 1) % colores.length];
        raiz.getChildren().remove(pisoVisual);
        pisoVisual = crearPiso(colorPiso);
        raiz.getChildren().add(1, pisoVisual);
        salaVisual.setFill(Color.TRANSPARENT);
        salaVisual.setStroke(esSalaJefe ? Color.GOLD : Color.rgb(135, 135, 145));
        for (PuertaVisual puerta : puertas) {
            puerta.getVisual().setVisible(false);
            puerta.setAbierta(false);
        }
        ocultarBarraJefe();
        actualizarEstado(esSalaJefe ? "JEFE - SALA " + sala.getNumero() : "MAPA " + numeroMapa);
    }

    /**
     * Muestra las puertas disponibles y determina si se ven abiertas.
     */
    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas, boolean abiertas) {
        configurarSalidas(salidas, null, abiertas);
    }

    /** Dibuja por separado la puerta de entrada cerrada y las salidas conectadas. */
    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas,
            PuertaVisual.Direccion entrada, boolean abiertas) {
        for (PuertaVisual puerta : puertas) {
            boolean seleccionada = salidas.contains(puerta.getDireccion());
            boolean esEntrada = entrada == puerta.getDireccion();
            puerta.getVisual().setVisible(seleccionada || esEntrada);
            puerta.setAbierta(seleccionada && abiertas);
        }
    }

    /**
     * Muestra nombre y barra del jefe y sincroniza su vida.
     */
    public void mostrarBarraJefe(Enemigo jefe) {
        if (jefe == null) return;
        tituloJefe.setText(jefe.getNombre() + "  " + jefe.getVida() + "/" + jefe.getVidaMaxima());
        tituloJefe.setVisible(true);
        fondoVidaJefe.setVisible(true);
        barraVidaJefe.setVisible(true);
        actualizarBarraJefe(jefe);
    }

    /**
     * Calcula la proporción de vida y actualiza ancho, color y texto.
     */
    public void actualizarBarraJefe(Enemigo jefe) {
        if (jefe == null) return;
        double porcentaje = Math.max(0, Math.min(1,
                (double) jefe.getVida() / jefe.getVidaMaxima()));
        barraVidaJefe.setWidth(fondoVidaJefe.getWidth() * porcentaje);
        barraVidaJefe.setFill(porcentaje <= 0.25 ? Color.RED : Color.GOLD);
        tituloJefe.setText(jefe.getNombre() + "  " + jefe.getVida() + "/" + jefe.getVidaMaxima());
    }

    /**
     * Oculta el nombre y las figuras de la barra del jefe.
     */
    public void ocultarBarraJefe() {
        tituloJefe.setVisible(false);
        fondoVidaJefe.setVisible(false);
        barraVidaJefe.setVisible(false);
    }

    /**
     * Guarda la referencia visual del jugador y agrega su nodo a la escena.
     */
    public void agregarJugador(Node jugador) { jugadorVisual = jugador; raiz.getChildren().add(jugador); }
    /**
     * Muestra el aviso de daño y aplica un destello al sprite del jugador.
     */
    public void mostrarDanio() {
        avisoDanio.setVisible(true);
        if (jugadorVisual != null) {
            ColorAdjust efecto = new ColorAdjust();
            efecto.setHue(-0.05);
            efecto.setSaturation(1);
            efecto.setBrightness(0.25);
            jugadorVisual.setEffect(efecto);
        }
    }
    /**
     * Mantiene el aviso durante la invulnerabilidad y retira el efecto al terminar.
     */
    public void actualizarIndicadorDanio(boolean invulnerable) {
        if (jugadorVisual != null && !invulnerable) jugadorVisual.setEffect(null);
        avisoDanio.setVisible(invulnerable);
    }
    /**
     * Agrega el grupo visual del enemigo al escenario.
     */
    public void agregarEnemigo(EnemigoVisual enemigo) { raiz.getChildren().add(enemigo.getVisual()); }
    /**
     * Retira el grupo visual del enemigo del escenario.
     */
    public void quitarEnemigo(EnemigoVisual enemigo) { raiz.getChildren().remove(enemigo.getVisual()); }
    /**
     * Agrega el nodo del proyectil a la escena.
     */
    public void agregarProyectil(ProyectilVisual proyectil) { raiz.getChildren().add(proyectil.getVisual()); }
    /**
     * Retira el nodo del proyectil de la escena.
     */
    public void quitarProyectil(ProyectilVisual proyectil) { raiz.getChildren().remove(proyectil.getVisual()); }

    public void agregarObjetoSuelo(ObjetoSueloVisual objeto) {
        objetosSuelo.add(objeto);
        raiz.getChildren().add(objeto.getVisual());
    }
    public void quitarObjetoSuelo(ObjetoSueloVisual objeto) {
        objetosSuelo.remove(objeto);
        raiz.getChildren().remove(objeto.getVisual());
    }
    public void limpiarObjetosSuelo() {
        for (ObjetoSueloVisual objeto : objetosSuelo) raiz.getChildren().remove(objeto.getVisual());
        objetosSuelo.clear();
    }
    public void agregarInvocacion(InvocacionVisual invocacion) {
        invocacionesVisuales.add(invocacion);
        raiz.getChildren().add(invocacion.getVisual());
    }
    public void quitarInvocacion(InvocacionVisual invocacion) {
        invocacionesVisuales.remove(invocacion);
        raiz.getChildren().remove(invocacion.getVisual());
    }

    /** Presenta las tres opciones de recompensa obligatoria del jefe de sala 15. */
    public void mostrarSeleccionCartas(List<CartaTipo> opciones, Consumer<CartaTipo> alElegir) {
        if (menuRecompensa != null) return;
        Text titulo = new Text("ELEGÍ UNA CARTA");
        titulo.setFill(Color.GOLD);
        titulo.setFont(Font.font("Arial", 30));
        VBox contenido = new VBox(16);
        contenido.setAlignment(Pos.CENTER);
        contenido.getChildren().add(titulo);
        for (CartaTipo carta : opciones) {
            Button boton = new Button(carta.getNombre() + " — " + carta.getDescripcion());
            boton.setGraphic(AssetVisual.crearIcono("assets/images/cards/"
                    + carta.name().toLowerCase(java.util.Locale.ROOT) + ".png", 32, 32, Color.GOLD));
            boton.setPrefWidth(620);
            boton.setPrefHeight(48);
            boton.setWrapText(true);
            boton.setStyle("-fx-background-color: #633; -fx-text-fill: white; -fx-cursor: hand;");
            boton.setOnAction(event -> alElegir.accept(carta));
            contenido.getChildren().add(boton);
        }
        menuRecompensa = new StackPane(contenido);
        menuRecompensa.setPrefSize(ANCHO, ALTO);
        menuRecompensa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.92);");
        raiz.getChildren().add(menuRecompensa);
    }

    public void ocultarSeleccionCartas() {
        if (menuRecompensa != null) {
            raiz.getChildren().remove(menuRecompensa);
            menuRecompensa = null;
        }
    }

    public void mostrarReemplazoInvocacion(List<Invocacion> invocaciones,
            Consumer<Integer> alElegir) {
        if (menuReemplazoInvocacion != null) return;
        VBox contenido = new VBox(18);
        contenido.setAlignment(Pos.CENTER);
        Text titulo = new Text("ELEGÍ QUÉ INVOCACIÓN REEMPLAZAR");
        titulo.setFill(Color.GOLD); titulo.setFont(Font.font("Arial", 26));
        contenido.getChildren().add(titulo);
        for (int i = 0; i < invocaciones.size(); i++) {
            final int espacio = i;
            Invocacion invocacion = invocaciones.get(i);
            Button boton = new Button(invocacion.getTipoInvocacion().getNombre()
                    + " — VIDA " + invocacion.getVida() + "/" + invocacion.getVidaMaxima()
                    + " — DAÑO " + invocacion.getDano());
            boton.setPrefWidth(500); boton.setPrefHeight(48);
            boton.setStyle("-fx-background-color: #49336b; -fx-text-fill: white;");
            boton.setOnAction(event -> alElegir.accept(espacio));
            contenido.getChildren().add(boton);
        }
        menuReemplazoInvocacion = new StackPane(contenido);
        menuReemplazoInvocacion.setPrefSize(ANCHO, ALTO);
        menuReemplazoInvocacion.setStyle("-fx-background-color: rgba(0, 0, 0, 0.92);");
        raiz.getChildren().add(menuReemplazoInvocacion);
    }

    public void ocultarReemplazoInvocacion() {
        if (menuReemplazoInvocacion != null) {
            raiz.getChildren().remove(menuReemplazoInvocacion);
            menuReemplazoInvocacion = null;
        }
    }

    /**
     * Actualiza textos de vida, experiencia, nivel, mapa, sala y dificultad.
     */
    public void actualizarHUD(Jugador jugador, int cantidadEnemigos, int numeroMapa,
                              int numeroSala, int dificultad, boolean esSalaJefe,
                              int experienciaSiguienteNivel) {
        if (jugador == null) return;
        vidaTexto.setText("VIDA: " + jugador.getVida() + "/" + jugador.getVidaMaxima());
        experienciaTexto.setText("EXP: " + jugador.getExperiencia() + "/" + experienciaSiguienteNivel);
        nivelTexto.setText("NIVEL: " + jugador.getNivel());
        enemigosTexto.setText((esSalaJefe ? "JEFE: " : "ENEMIGOS: ") + cantidadEnemigos);
        mapaTexto.setText("MAPA: " + numeroMapa);
        salaTexto.setText("SALA: " + numeroSala);
        dificultadTexto.setText("DIF: " + dificultad);
    }

    /**
     * Cambia el mensaje de estado mostrado en pantalla.
     */
    public void actualizarEstado(String estado) {
        estadoTexto.setText(estado);
        estadoTexto.setFill(Color.WHITE);
        estadoTexto.setFont(Font.font("Arial", 20));
    }

    /**
     * Construye el panel de pausa y conecta el botón con la acción de reanudar.
     */
    public void mostrarPausa(Runnable alReanudar) {
        mostrarPausa(accion -> { if (accion == AccionPausa.REANUDAR) alReanudar.run(); });
    }

    public void mostrarPausa(Consumer<AccionPausa> alSeleccionar) {
        // La vista también se usa para subpantallas de pausa; al volver hay que sustituirlas.
        ocultarPausa();
        Text titulo = new Text("JUEGO EN PAUSA");
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 36));
        Text indicacion = new Text("Presiona ESC para volver al juego");
        indicacion.setFill(Color.LIGHTGRAY);
        indicacion.setFont(Font.font("Arial", 16));
        Button reanudar = new Button("REANUDAR");
        reanudar.setPrefWidth(240);
        reanudar.setPrefHeight(50);
        reanudar.setFont(Font.font("Arial", 20));
        reanudar.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white; -fx-cursor: hand;");
        reanudar.setOnAction(event -> alSeleccionar.accept(AccionPausa.REANUDAR));
        Button cartas = new Button("CARTAS OBTENIDAS");
        Button invocaciones = new Button("INVOCACIONES");
        Button guardar = new Button("GUARDAR PARTIDA");
        Button cargar = new Button("CARGAR PARTIDA");
        for (Button boton : List.of(cartas, invocaciones, guardar, cargar)) {
            boton.setPrefWidth(240); boton.setPrefHeight(45);
            boton.setStyle("-fx-background-color: #49336b; -fx-text-fill: white; -fx-cursor: hand;");
        }
        cartas.setOnAction(event -> alSeleccionar.accept(AccionPausa.CARTAS));
        invocaciones.setOnAction(event -> alSeleccionar.accept(AccionPausa.INVOCACIONES));
        guardar.setOnAction(event -> alSeleccionar.accept(AccionPausa.GUARDAR));
        cargar.setOnAction(event -> alSeleccionar.accept(AccionPausa.CARGAR));
        VBox contenido = new VBox(13, titulo, reanudar, cartas, invocaciones, guardar, cargar, indicacion);
        contenido.setAlignment(Pos.CENTER);
        menuPausa = new StackPane(contenido);
        menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.84);");
        raiz.getChildren().add(menuPausa);
    }

    public void mostrarConsultaPausa(String tituloTexto, List<String> filas, Runnable alVolver) {
        ocultarPausa();
        Text titulo = new Text(tituloTexto);
        titulo.setFill(Color.GOLD); titulo.setFont(Font.font("Arial", 28));
        VBox contenido = new VBox(14);
        contenido.setAlignment(Pos.CENTER);
        contenido.getChildren().add(titulo);
        if (filas.isEmpty()) {
            Text vacio = new Text("Todavía no hay elementos para mostrar.");
            vacio.setFill(Color.LIGHTGRAY); contenido.getChildren().add(vacio);
        } else {
            for (String fila : filas) {
                Text texto = new Text(fila);
                texto.setFill(Color.WHITE); texto.setFont(Font.font("Arial", 16));
                contenido.getChildren().add(texto);
            }
        }
        Button volver = new Button("VOLVER");
        volver.setPrefWidth(220); volver.setPrefHeight(44);
        volver.setOnAction(event -> alVolver.run());
        contenido.getChildren().add(volver);
        menuPausa = new StackPane(contenido);
        menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        raiz.getChildren().add(menuPausa);
    }

    /** Presenta el cierre de la progresión después de derrotar al jefe final. */
    public void mostrarVictoria(Runnable alVolverAlMenu) {
        ocultarPausa();
        Text titulo = new Text("PROGRESIÓN COMPLETADA");
        titulo.setFill(Color.GOLD);
        titulo.setFont(Font.font("Arial", 34));
        Text detalle = new Text("Derrotaste al jefe de la sala 20.");
        detalle.setFill(Color.WHITE);
        detalle.setFont(Font.font("Arial", 18));
        Button volver = new Button("VOLVER AL MENÚ");
        volver.setPrefWidth(240);
        volver.setPrefHeight(48);
        volver.setOnAction(event -> alVolverAlMenu.run());
        VBox contenido = new VBox(20, titulo, detalle, volver);
        contenido.setAlignment(Pos.CENTER);
        menuPausa = new StackPane(contenido);
        menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.92);");
        raiz.getChildren().add(menuPausa);
    }

    /**
     * Retira el panel de pausa de la raíz de la vista.
     */
    public void ocultarPausa() {
        if (menuPausa != null) {
            raiz.getChildren().remove(menuPausa);
            menuPausa = null;
        }
    }

    public void mostrarSlots(List<SlotView> slots, boolean modoGuardar,
            IntConsumer alSeleccionar, IntConsumer alEliminar, Runnable alVolver) {
        ocultarPausa();
        Text titulo = new Text(modoGuardar ? "GUARDAR EN UN ESPACIO" : "CARGAR PARTIDA");
        titulo.setFill(Color.GOLD); titulo.setFont(Font.font("Arial", 28));
        VBox contenido = new VBox(14); contenido.setAlignment(Pos.CENTER);
        contenido.getChildren().add(titulo);
        for (SlotView slot : slots) {
            HBox fila = new HBox(10); fila.setAlignment(Pos.CENTER);
            Text resumen = new Text("ESPACIO " + slot.numero() + " — " + slot.resumen());
            resumen.setFill(Color.WHITE); resumen.setFont(Font.font("Arial", 15));
            fila.getChildren().add(resumen);
            Button usar = new Button(modoGuardar
                    ? (slot.ocupado() ? "SOBRESCRIBIR" : "GUARDAR") : "CARGAR");
            usar.setDisable(!modoGuardar && !slot.ocupado());
            usar.setOnAction(event -> alSeleccionar.accept(slot.numero()));
            fila.getChildren().add(usar);
            if (slot.ocupado()) {
                Button eliminar = new Button("ELIMINAR");
                eliminar.setOnAction(event -> alEliminar.accept(slot.numero()));
                fila.getChildren().add(eliminar);
            }
            contenido.getChildren().add(fila);
        }
        Button volver = new Button("VOLVER");
        volver.setOnAction(event -> alVolver.run());
        contenido.getChildren().add(volver);
        menuPausa = new StackPane(contenido);
        menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.92);");
        raiz.getChildren().add(menuPausa);
    }
}
