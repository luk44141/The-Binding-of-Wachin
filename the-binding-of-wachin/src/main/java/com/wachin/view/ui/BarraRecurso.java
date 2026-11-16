package com.wachin.view.ui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Barra de recurso con relleno suavizado, estela del daño reciente y destello al recibir un golpe.
 * Se anima llamando a {@link #actualizar(double)} desde el ciclo del juego.
 */
public final class BarraRecurso extends Pane {

    private final double ancho;
    private final double alto;
    private final Rectangle estela;
    private final Rectangle relleno;
    private final Rectangle brillo;
    private final Rectangle destello;
    private double objetivo = 1;
    private double mostrado = 1;
    private double rastro = 1;
    private double pausaRastro;
    private double destelloAlfa;

    public BarraRecurso(double ancho, double alto, Color claro, Color oscuro) {
        this.ancho = ancho;
        this.alto = alto;
        setMinSize(ancho, alto);
        setPrefSize(ancho, alto);
        setMaxSize(ancho, alto);
        setMouseTransparent(true);
        setCache(true);
        setCacheHint(javafx.scene.CacheHint.SPEED);
        double arco = alto;
        Rectangle fondo = new Rectangle(ancho, alto);
        fondo.setArcWidth(arco);
        fondo.setArcHeight(arco);
        fondo.setFill(Color.web("#171717"));
        estela = new Rectangle(ancho, alto);
        estela.setFill(Color.web("#b0b0b0", 0.75));
        relleno = new Rectangle(ancho, alto);
        relleno.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, claro), new Stop(1, oscuro)));
        brillo = new Rectangle(ancho, Math.max(1, alto * 0.38));
        brillo.setFill(Color.color(1, 1, 1, 0.22));
        destello = new Rectangle(ancho, alto);
        destello.setFill(Color.WHITE);
        destello.setOpacity(0);
        Pane contenido = new Pane(estela, relleno, brillo, destello);
        Rectangle recorte = new Rectangle(ancho, alto);
        recorte.setArcWidth(arco);
        recorte.setArcHeight(arco);
        contenido.setClip(recorte);
        Rectangle borde = new Rectangle(ancho, alto);
        borde.setArcWidth(arco);
        borde.setArcHeight(arco);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(Color.web("#080808"));
        borde.setStrokeWidth(2);
        getChildren().addAll(fondo, contenido, borde);
        dibujar();
    }

    /** Agrega marcas verticales que dividen la barra en {@code partes} tramos. */
    public void setMarcas(int partes) {
        getChildren().removeIf(n -> n instanceof Line);
        for (int i = 1; i < Math.min(partes, 40); i++) {
            double x = ancho * i / partes;
            Line marca = new Line(x, 1, x, alto - 1);
            marca.setStroke(Color.color(0, 0, 0, 0.35));
            marca.setStrokeWidth(1);
            getChildren().add(marca);
        }
    }

    /** Fija el valor objetivo; una bajada inicia la estela y el destello. */
    public void establecer(double valor, double maximo) {
        double fraccion = maximo <= 0 ? 0 : Math.max(0, Math.min(1, valor / maximo));
        if (fraccion < objetivo - 0.0005) {
            pausaRastro = 0.45;
            destelloAlfa = 0.55;
        }
        objetivo = fraccion;
    }

    /** Avanza la animación. Si la barra está en reposo no toca ningún nodo, para no forzar repintados. */
    public void actualizar(double dt) {
        if (mostrado == objetivo && rastro == objetivo && pausaRastro <= 0 && destelloAlfa <= 0) return;
        double paso = 1 - Math.exp(-14 * dt);
        mostrado += (objetivo - mostrado) * paso;
        if (Math.abs(objetivo - mostrado) < 0.0005) mostrado = objetivo;
        if (pausaRastro > 0) {
            pausaRastro -= dt;
        } else {
            rastro += (objetivo - rastro) * (1 - Math.exp(-5 * dt));
            if (Math.abs(objetivo - rastro) < 0.001) rastro = objetivo;
        }
        if (rastro < mostrado) rastro = mostrado;
        if (destelloAlfa > 0) destelloAlfa = Math.max(0, destelloAlfa - dt * 2.2);
        dibujar();
    }

    private void dibujar() {
        relleno.setWidth(ancho * mostrado);
        brillo.setWidth(ancho * mostrado);
        estela.setWidth(ancho * rastro);
        destello.setWidth(ancho * Math.max(rastro, mostrado));
        destello.setOpacity(destelloAlfa);
    }
}
