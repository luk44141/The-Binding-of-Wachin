package com.wachin.view.game;

import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PartidaView {
    public static final int ANCHO = 1000;
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
    private final List<PuertaVisual> puertas = new ArrayList<>();
    private StackPane menuPausa;

    public PartidaView() { crearEscenario(); crearPuertas(); crearHUD(); }

    private void crearEscenario() {
        fondo.setFill(Color.rgb(18, 18, 22));
        salaVisual.setFill(Color.rgb(52, 52, 58));
        salaVisual.setStroke(Color.rgb(135, 135, 145));
        salaVisual.setStrokeWidth(5);
        raiz.getChildren().addAll(fondo, salaVisual);
    }

    private void crearPuertas() {
        for (PuertaVisual.Direccion direccion : PuertaVisual.Direccion.values()) {
            PuertaVisual puerta = new PuertaVisual(direccion);
            puertas.add(puerta);
            raiz.getChildren().add(puerta.getVisual());
        }
    }

    private void crearHUD() {
        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setX(20); titulo.setY(30); titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 20));
        Text controles = new Text("WASD: mover | FLECHAS: disparar | ESC: pausa");
        controles.setX(540); controles.setY(30); controles.setFill(Color.LIGHTGRAY);
        controles.setFont(Font.font("Arial", 14));
        for (Text texto : List.of(vidaTexto, experienciaTexto, nivelTexto, enemigosTexto, mapaTexto, salaTexto, dificultadTexto)) {
            texto.setFont(Font.font("Arial", 14));
        }
        raiz.getChildren().addAll(titulo, controles, vidaTexto, experienciaTexto, nivelTexto,
                enemigosTexto, mapaTexto, salaTexto, dificultadTexto, estadoTexto);
        actualizarEstado("");
    }

    private Text crearTexto(double x, double y) {
        Text texto = new Text(); texto.setX(x); texto.setY(y); texto.setFill(Color.WHITE); return texto;
    }

    public Pane getRaiz() { return raiz; }

    public void configurarSala(Sala sala, int numeroMapa, boolean esSalaJefe) {
        Color[] colores = {Color.rgb(52,52,58), Color.rgb(58,45,48), Color.rgb(42,52,49), Color.rgb(48,45,65), Color.rgb(65,52,38)};
        if (esSalaJefe) {
            salaVisual.setFill(Color.rgb(65, 25, 38)); salaVisual.setStroke(Color.GOLD);
        } else {
            salaVisual.setFill(colores[(numeroMapa - 1) % colores.length]);
            salaVisual.setStroke(Color.rgb(135, 135, 145));
        }
        for (PuertaVisual puerta : puertas) { puerta.getVisual().setVisible(false); puerta.setAbierta(false); }
        actualizarEstado(esSalaJefe ? "JEFE DEL MAPA " + numeroMapa : "MAPA " + numeroMapa);
    }

    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas, boolean abiertas) {
        for (PuertaVisual puerta : puertas) {
            boolean seleccionada = salidas.contains(puerta.getDireccion());
            puerta.getVisual().setVisible(seleccionada);
            puerta.setAbierta(seleccionada && abiertas);
        }
    }

    public void actualizarPuertas(boolean abiertas) {
        for (PuertaVisual puerta : puertas) if (puerta.getVisual().isVisible()) puerta.setAbierta(abiertas);
    }

    public void agregarJugador(Node jugador) { raiz.getChildren().add(jugador); }
    public void agregarEnemigo(EnemigoVisual enemigo) { raiz.getChildren().add(enemigo.getVisual()); }
    public void quitarEnemigo(EnemigoVisual enemigo) { raiz.getChildren().remove(enemigo.getVisual()); }
    public void agregarProyectil(Proyectil proyectil) { raiz.getChildren().add(proyectil.getVisual()); }
    public void quitarProyectil(Proyectil proyectil) { raiz.getChildren().remove(proyectil.getVisual()); }

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

    public void actualizarEstado(String estado) {
        estadoTexto.setText(estado); estadoTexto.setFill(Color.WHITE); estadoTexto.setFont(Font.font("Arial", 20));
    }

    public void mostrarPausa(Runnable alReanudar) {
        if (menuPausa != null) return;
        Text titulo = new Text("JUEGO EN PAUSA"); titulo.setFill(Color.WHITE); titulo.setFont(Font.font("Arial", 36));
        Text indicacion = new Text("Presioná ESC para volver al juego"); indicacion.setFill(Color.LIGHTGRAY); indicacion.setFont(Font.font("Arial", 16));
        Button reanudar = new Button("REANUDAR"); reanudar.setPrefWidth(240); reanudar.setPrefHeight(50); reanudar.setFont(Font.font("Arial", 20));
        reanudar.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white; -fx-cursor: hand;");
        reanudar.setOnAction(event -> alReanudar.run());
        VBox contenido = new VBox(28, titulo, reanudar, indicacion); contenido.setAlignment(Pos.CENTER);
        menuPausa = new StackPane(contenido); menuPausa.setPrefSize(ANCHO, ALTO);
        menuPausa.setStyle("-fx-background-color: rgba(0, 0, 0, 0.84);"); menuPausa.setLayoutX(0); menuPausa.setLayoutY(0);
        raiz.getChildren().add(menuPausa);
    }

    public void ocultarPausa() {
        if (menuPausa != null) { raiz.getChildren().remove(menuPausa); menuPausa = null; }
    }
}
