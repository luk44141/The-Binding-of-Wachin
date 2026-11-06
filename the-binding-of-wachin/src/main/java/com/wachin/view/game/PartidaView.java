package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Construye y actualiza el escenario, el HUD, las puertas, la pausa y los avisos visuales.
 */
public class PartidaView {
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
    /**
     * Constante que define menupausa para este componente.
     */
    private StackPane menuPausa;
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
        salaVisual.setFill(Color.rgb(52, 52, 58));
        salaVisual.setStroke(Color.rgb(135, 135, 145));
        salaVisual.setStrokeWidth(5);
        raiz.getChildren().addAll(fondo, salaVisual);
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
        Color[] colores = {Color.rgb(52, 52, 58), Color.rgb(58, 45, 48),
                Color.rgb(42, 52, 49), Color.rgb(48, 45, 65), Color.rgb(65, 52, 38)};
        if (esSalaJefe) {
            salaVisual.setFill(Color.rgb(65, 25, 38));
            salaVisual.setStroke(Color.GOLD);
        } else {
            salaVisual.setFill(colores[(numeroMapa - 1) % colores.length]);
            salaVisual.setStroke(Color.rgb(135, 135, 145));
        }
        for (PuertaVisual puerta : puertas) {
            puerta.getVisual().setVisible(false);
            puerta.setAbierta(false);
        }
        ocultarBarraJefe();
        actualizarEstado(esSalaJefe ? "JEFE DEL MAPA " + numeroMapa : "MAPA " + numeroMapa);
    }

    /**
     * Muestra las puertas disponibles y determina si se ven abiertas.
     */
    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas, boolean abiertas) {
        for (PuertaVisual puerta : puertas) {
            boolean seleccionada = salidas.contains(puerta.getDireccion());
            puerta.getVisual().setVisible(seleccionada);
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
    public void agregarProyectil(Proyectil proyectil) { raiz.getChildren().add(proyectil.getVisual()); }
    /**
     * Retira el nodo del proyectil de la escena.
     */
    public void quitarProyectil(Proyectil proyectil) { raiz.getChildren().remove(proyectil.getVisual()); }

    /**
     * Actualiza textos de vida, experiencia, nivel, mapa, sala y dificultad.
     */
    public void actualizarHUD(Jugador jugador, int cantidadEnemigos, int numeroMapa,
                              int numeroSala, int dificultad, boolean esSalaJefe) {
        if (jugador == null) return;
        vidaTexto.setText("VIDA: " + jugador.getVida() + "/" + jugador.getVidaMaxima());
        experienciaTexto.setText("EXP: " + jugador.getExperiencia());
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
        if (menuPausa != null) return;
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
        reanudar.setOnAction(event -> alReanudar.run());
        VBox contenido = new VBox(28, titulo, reanudar, indicacion);
        contenido.setAlignment(Pos.CENTER);
        menuPausa = new StackPane(contenido);
        menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.84);");
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
}
