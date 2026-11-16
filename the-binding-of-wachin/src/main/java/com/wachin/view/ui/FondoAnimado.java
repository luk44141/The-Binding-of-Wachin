package com.wachin.view.ui;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Fondo de los menús: niebla violeta y viñeta (una imagen fija pregenerada) con brasas ascendentes
 * dibujadas encima. Solo se anima mientras está dentro de una ventana visible.
 */
public final class FondoAnimado extends Pane {

    private static final int BRASAS = 60;
    private final double[] x = new double[BRASAS];
    private final double[] y = new double[BRASAS];
    private final double[] v = new double[BRASAS];
    private final double[] r = new double[BRASAS];
    private final double[] fase = new double[BRASAS];
    private final Random azar = new Random(7);
    private final Canvas brasas;
    private double tiempo;
    private final AnimationTimer reloj = new AnimationTimer() {
        private long anterior;
        @Override public void handle(long ahora) {
            if (anterior == 0) anterior = ahora;
            double dt = Math.min(0.05, (ahora - anterior) / 1e9);
            anterior = ahora;
            dibujarBrasas(dt);
        }
        @Override public void stop() { super.stop(); anterior = 0; }
    };

    public FondoAnimado(double ancho, double alto, Color tinte) {
        setPrefSize(ancho, alto);
        setMinSize(ancho, alto);
        setMaxSize(ancho, alto);
        setMouseTransparent(true);
        ImageView fijo = new ImageView(renderizarFondo(ancho, alto, tinte));
        fijo.setFitWidth(ancho);
        fijo.setFitHeight(alto);
        brasas = new Canvas(ancho, alto);
        getChildren().addAll(fijo, brasas);
        for (int i = 0; i < BRASAS; i++) reiniciar(i, true);
        sceneProperty().addListener((o, antes, ahora) -> {
            if (ahora == null) { reloj.stop(); return; }
            ahora.windowProperty().addListener((w, anterior, ventana) -> { if (ventana == null) reloj.stop(); else reloj.start(); });
            if (ahora.getWindow() != null) reloj.start();
        });
    }

    /** Niebla y viñeta: se rasterizan una sola vez. */
    private static Image renderizarFondo(double w, double h, Color tinte) {
        Canvas lienzo = new Canvas(w, h);
        GraphicsContext g = lienzo.getGraphicsContext2D();
        g.setFill(Color.web("#08070c"));
        g.fillRect(0, 0, w, h);
        double[][] niebla = {{0.25, 0.5}, {0.5, 0.62}, {0.75, 0.5}};
        for (double[] n : niebla) {
            g.setFill(new RadialGradient(0, 0, w * n[0], h * n[1], 380, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(tinte.getRed(), tinte.getGreen(), tinte.getBlue(), 0.2)),
                    new Stop(1, Color.TRANSPARENT)));
            g.fillRect(0, 0, w, h);
        }
        g.setFill(new RadialGradient(0, 0, w / 2, h / 2, Math.max(w, h) * 0.75, false, CycleMethod.NO_CYCLE,
                new Stop(0.45, Color.TRANSPARENT), new Stop(1, Color.color(0, 0, 0, 0.85))));
        g.fillRect(0, 0, w, h);
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }

    private void reiniciar(int i, boolean inicial) {
        x[i] = azar.nextDouble() * brasas.getWidth();
        y[i] = inicial ? azar.nextDouble() * brasas.getHeight() : brasas.getHeight() + 10;
        v[i] = 14 + azar.nextDouble() * 36;
        r[i] = 1 + azar.nextDouble() * 2.4;
        fase[i] = azar.nextDouble() * Math.PI * 2;
    }

    private void dibujarBrasas(double dt) {
        tiempo += dt;
        GraphicsContext g = brasas.getGraphicsContext2D();
        g.clearRect(0, 0, brasas.getWidth(), brasas.getHeight());
        g.setGlobalBlendMode(BlendMode.ADD);
        for (int i = 0; i < BRASAS; i++) {
            y[i] -= v[i] * dt;
            x[i] += Math.sin(tiempo * 0.9 + fase[i]) * 10 * dt;
            if (y[i] < -10) reiniciar(i, false);
            double alfa = 0.25 + 0.5 * Math.abs(Math.sin(tiempo * 1.7 + fase[i]));
            g.setGlobalAlpha(alfa * Math.min(1, y[i] / 160.0 + 0.2));
            g.setFill(i % 5 == 0 ? Tema.ORO_CLARO : Tema.ROJO_VIVO);
            g.fillOval(x[i] - r[i], y[i] - r[i], r[i] * 2, r[i] * 2);
        }
        g.setGlobalAlpha(1);
        g.setGlobalBlendMode(BlendMode.SRC_OVER);
    }
}
