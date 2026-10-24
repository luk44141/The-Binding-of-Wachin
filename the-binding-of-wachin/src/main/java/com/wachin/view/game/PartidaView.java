
package com.wachin.view.game;

import com.wachin.model.Jugador;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class PartidaView {

    public static final int ANCHO = 1000;
    public static final int ALTO = 700;

    private final Pane raiz = new Pane();

    private final Text vidaTexto = crearTexto(70, 680);
    private final Text experienciaTexto = crearTexto(240, 680);
    private final Text nivelTexto = crearTexto(400, 680);
    private final Text enemigosTexto = crearTexto(780, 680);
    private final Text estadoTexto = crearTexto(350, 100);

    public PartidaView() {
        crearEscenario();
        crearHUD();
    }

    private void crearEscenario() {
        Rectangle fondo = new Rectangle(0, 0, ANCHO, ALTO);
        fondo.setFill(Color.rgb(20, 20, 20));

        Rectangle sala = new Rectangle(50, 50, 900, 600);
        sala.setFill(Color.rgb(50, 50, 50));
        sala.setStroke(Color.rgb(110, 110, 110));
        sala.setStrokeWidth(4);

        raiz.getChildren().addAll(fondo, sala);
    }

    private void crearHUD() {
        Text titulo = new Text("THE BINDING OF WACHIN");
        titulo.setX(20);
        titulo.setY(30);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font(20));

        Text controles = new Text(
                "WASD: mover   FLECHAS: disparar"
        );
        controles.setX(680);
        controles.setY(30);
        controles.setFill(Color.LIGHTGRAY);

        raiz.getChildren().addAll(
                titulo,
                controles,
                vidaTexto,
                experienciaTexto,
                nivelTexto,
                enemigosTexto,
                estadoTexto
        );
    }

    private Text crearTexto(double x, double y) {
        Text texto = new Text();
        texto.setX(x);
        texto.setY(y);
        texto.setFill(Color.WHITE);
        texto.setFont(Font.font(18));
        return texto;
    }

    public Pane getRaiz() {
        return raiz;
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

    public void actualizarHUD(Jugador jugador, int cantidadEnemigos) {
        if (jugador == null) {
            return;
        }

        vidaTexto.setText(
                "VIDA: " + jugador.getVida()
                        + " / " + jugador.getVidaMaxima()
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
    }

    public void actualizarEstado(String estado) {
        estadoTexto.setText(estado);
    }
}
