package com.wachin.view.game;

import com.wachin.model.Jugador;
import com.wachin.model.Sala;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PartidaView {

    public static final int ANCHO = 1000;
    public static final int ALTO = 700;

    private final Pane raiz = new Pane();

    private final Rectangle fondo =
            new Rectangle(0, 0, ANCHO, ALTO);

    private final Rectangle salaVisual =
            new Rectangle(50, 50, 900, 600);

    private final Text vidaTexto = crearTexto(20, 682);
    private final Text experienciaTexto = crearTexto(200, 682);
    private final Text nivelTexto = crearTexto(365, 682);
    private final Text enemigosTexto = crearTexto(500, 682);
    private final Text salaTexto = crearTexto(690, 682);
    private final Text dificultadTexto = crearTexto(845, 682);
    private final Text estadoTexto = crearTexto(320, 95);

    private final List<PuertaVisual> puertas =
            new ArrayList<>();

    public PartidaView() {
        crearEscenario();
        crearPuertas();
        crearHUD();
    }

    private void crearEscenario() {
        fondo.setFill(Color.rgb(18, 18, 22));

        salaVisual.setFill(Color.rgb(52, 52, 58));
        salaVisual.setStroke(Color.rgb(135, 135, 145));
        salaVisual.setStrokeWidth(5);

        raiz.getChildren().addAll(fondo, salaVisual);
    }

    private void crearPuertas() {
        for (PuertaVisual.Direccion direccion
                : PuertaVisual.Direccion.values()) {

            PuertaVisual puerta = new PuertaVisual(direccion);

            puertas.add(puerta);
            raiz.getChildren().add(puerta.getVisual());
        }
    }

    private void crearHUD() {
        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", 20));

        Text controles = new Text(
                "WASD: mover | FLECHAS: disparar"
        );
        controles.setX(650);
        controles.setY(30);
        controles.setFill(Color.LIGHTGRAY);
        controles.setFont(Font.font("Arial", 14));

        for (Text texto : List.of(
                vidaTexto,
                experienciaTexto,
                nivelTexto,
                enemigosTexto,
                salaTexto,
                dificultadTexto
        )) {
            texto.setFont(Font.font("Arial", 15));
        }

        raiz.getChildren().addAll(
                titulo,
                controles,
                vidaTexto,
                experienciaTexto,
                nivelTexto,
                enemigosTexto,
                salaTexto,
                dificultadTexto,
                estadoTexto
        );

        actualizarEstado("");
    }

    private Text crearTexto(double x, double y) {
        Text texto = new Text();
        texto.setX(x);
        texto.setY(y);
        texto.setFill(Color.WHITE);
        return texto;
    }

    public Pane getRaiz() {
        return raiz;
    }

    public void configurarSala(Sala sala) {
        Color[] colores = {
                Color.rgb(52, 52, 58),
                Color.rgb(58, 45, 48),
                Color.rgb(42, 52, 49),
                Color.rgb(48, 45, 65),
                Color.rgb(65, 52, 38)
        };

        int indiceColor = (sala.getNumero() - 1) % colores.length;

        salaVisual.setFill(colores[indiceColor]);

        actualizarPuertas(false);
        actualizarEstado("SALA " + sala.getNumero());
    }

    public void actualizarPuertas(boolean abiertas) {
        for (PuertaVisual puerta : puertas) {
            puerta.setAbierta(abiertas);
        }
    }

    public void agregarJugador(Circle jugador) {
        raiz.getChildren().add(jugador);
    }

    public void agregarEnemigo(EnemigoVisual enemigo) {
        raiz.getChildren().add(enemigo.getVisual());
    }

    public void quitarEnemigo(EnemigoVisual enemigo) {
        raiz.getChildren().remove(enemigo.getVisual());
    }

    public void agregarProyectil(Proyectil proyectil) {
        raiz.getChildren().add(proyectil.getVisual());
    }

    public void quitarProyectil(Proyectil proyectil) {
        raiz.getChildren().remove(proyectil.getVisual());
    }

    public void actualizarHUD(
            Jugador jugador,
            int cantidadEnemigos,
            int numeroSala,
            int dificultad) {

        if (jugador == null) {
            return;
        }

        vidaTexto.setText(
                "VIDA: " + jugador.getVida()
                        + "/" + jugador.getVidaMaxima()
        );

        experienciaTexto.setText(
                "EXP: " + jugador.getExperiencia()
        );

        nivelTexto.setText(
                "NIVEL: " + jugador.getNivel()
        );

        enemigosTexto.setText(
                "ENEMIGOS: " + cantidadEnemigos
        );

        salaTexto.setText(
                "SALA: " + numeroSala
        );

        dificultadTexto.setText(
                "DIFICULTAD: " + dificultad
        );
    }

    public void actualizarEstado(String estado) {
        estadoTexto.setText(estado);
        estadoTexto.setFill(Color.WHITE);
        estadoTexto.setFont(Font.font("Arial", 20));
    }
}