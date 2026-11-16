package com.wachin.view.game;

import com.wachin.model.CartaTipo;
import com.wachin.model.TipoInvocacion;
import java.util.function.Consumer;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Ilustraciones generadas por código para todo elemento que no tenga sprite propio.
 *
 * <p>Siguen el estilo de los sprites existentes (contorno negro grueso y colores planos con una
 * luz) para que convivan con ellos. Cada método devuelve una {@link Image} lista para usarse con
 * un {@code ImageView}; {@link AssetVisual} las guarda en caché. Deben invocarse desde el hilo de
 * JavaFX porque se rasterizan con un {@code Canvas}.</p>
 */
public final class Dibujos {

    private static final Color NEGRO = Color.web("#0b0910");
    private static final Color BLANCO = Color.web("#f4f1fa");

    private Dibujos() { }

    // ───────────────────────────── infraestructura ─────────────────────────────

    private static Image renderizar(int ancho, int alto, Consumer<GraphicsContext> dibujo) {
        Canvas lienzo = new Canvas(ancho, alto);
        GraphicsContext g = lienzo.getGraphicsContext2D();
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
        dibujo.accept(g);
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }

    private static Color c(String hex) { return Color.web(hex); }

    private static void ovalo(GraphicsContext g, double cx, double cy, double rx, double ry,
            Color relleno, double grosor) {
        g.setFill(relleno);
        g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
        if (grosor > 0) {
            g.setStroke(NEGRO);
            g.setLineWidth(grosor);
            g.strokeOval(cx - rx, cy - ry, rx * 2, ry * 2);
        }
    }

    private static void caja(GraphicsContext g, double x, double y, double w, double h, double arco,
            Color relleno, double grosor) {
        g.setFill(relleno);
        g.fillRoundRect(x, y, w, h, arco, arco);
        if (grosor > 0) {
            g.setStroke(NEGRO);
            g.setLineWidth(grosor);
            g.strokeRoundRect(x, y, w, h, arco, arco);
        }
    }

    private static void poligono(GraphicsContext g, Color relleno, double grosor, double... puntos) {
        int n = puntos.length / 2;
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) { xs[i] = puntos[i * 2]; ys[i] = puntos[i * 2 + 1]; }
        g.setFill(relleno);
        g.fillPolygon(xs, ys, n);
        if (grosor > 0) {
            g.setStroke(NEGRO);
            g.setLineWidth(grosor);
            g.strokePolygon(xs, ys, n);
        }
    }

    /** Resplandor radial suave centrado en un punto. */
    private static void resplandor(GraphicsContext g, double cx, double cy, double radio, Color color, double alfa) {
        g.setFill(new RadialGradient(0, 0, cx, cy, radio, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(color.getRed(), color.getGreen(), color.getBlue(), alfa)),
                new Stop(1, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0))));
        g.fillOval(cx - radio, cy - radio, radio * 2, radio * 2);
    }

    private static void ojo(GraphicsContext g, double cx, double cy, double r, double mirarX, double mirarY) {
        ovalo(g, cx, cy, r, r * 1.1, BLANCO, 3);
        ovalo(g, cx + mirarX, cy + mirarY, r * 0.5, r * 0.55, NEGRO, 0);
    }

    // ───────────────────────────── personajes ─────────────────────────────

    /** Respaldo del jugador: criatura rosada y redonda con orejas. */
    public static Image jugador(boolean moño) {
        return renderizar(128, 128, g -> {
            ovalo(g, 36, 40, 14, 16, c("#e7a9b7"), 5);
            ovalo(g, 92, 40, 14, 16, c("#e7a9b7"), 5);
            ovalo(g, 64, 78, 40, 36, c("#f0b7c4"), 6);
            g.setFill(c("#ffd9e0"));
            g.fillOval(40, 54, 26, 14);
            ojo(g, 50, 70, 8, 1, 1);
            ojo(g, 78, 70, 8, -1, 1);
            ovalo(g, 64, 88, 7, 5, NEGRO, 0);
            if (moño) {
                poligono(g, c("#ff5c9d"), 4, 84, 30, 104, 22, 104, 44);
                poligono(g, c("#ff5c9d"), 4, 84, 30, 64, 22, 64, 44);
                ovalo(g, 84, 33, 6, 6, c("#ff8cc0"), 3);
            }
        });
    }

    /** Respaldo del enemigo cuerpo a cuerpo: policía con gorra azul. */
    public static Image poli() {
        return renderizar(128, 128, g -> {
            ovalo(g, 64, 86, 38, 28, c("#34456b"), 6);
            ovalo(g, 64, 62, 30, 26, c("#e8b98f"), 6);
            g.setFill(c("#3b5f9e"));
            g.fillArc(30, 20, 68, 56, 0, 180, ArcType.ROUND);
            g.setStroke(NEGRO); g.setLineWidth(6); g.strokeArc(30, 20, 68, 56, 0, 180, ArcType.ROUND);
            caja(g, 28, 46, 72, 11, 8, c("#1f2c47"), 5);
            caja(g, 57, 30, 14, 12, 3, c("#e6b84a"), 3);
            ojo(g, 52, 66, 5, 1, 1);
            ojo(g, 76, 66, 5, -1, 1);
        });
    }

    /** Enemigo a distancia: guardia con gafas oscuras y pistola. */
    public static Image tirador() {
        return renderizar(128, 128, g -> {
            ovalo(g, 40, 112, 12, 7, c("#1d2a40"), 4);
            ovalo(g, 88, 112, 12, 7, c("#1d2a40"), 4);
            ovalo(g, 64, 88, 34, 30, c("#2f4d7a"), 6);
            g.setFill(c("#4b73ad"));
            g.fillOval(44, 68, 22, 14);
            // brazo y pistola
            caja(g, 86, 78, 28, 12, 6, c("#e8b98f"), 4);
            caja(g, 104, 66, 22, 14, 3, c("#4a4d57"), 4);
            caja(g, 116, 62, 8, 8, 2, c("#2c2e35"), 3);
            // cabeza
            ovalo(g, 64, 54, 26, 24, c("#e8b98f"), 6);
            g.setFill(c("#2b4366"));
            g.fillArc(34, 22, 60, 50, 0, 180, ArcType.ROUND);
            g.setStroke(NEGRO); g.setLineWidth(6); g.strokeArc(34, 22, 60, 50, 0, 180, ArcType.ROUND);
            caja(g, 30, 44, 68, 9, 6, c("#16233a"), 5);
            caja(g, 58, 28, 12, 10, 3, c("#e6b84a"), 3);
            caja(g, 42, 54, 44, 10, 4, NEGRO, 0);
            g.setFill(Color.color(1, 1, 1, 0.35));
            g.fillRect(46, 56, 10, 3);
            g.fillRect(68, 56, 10, 3);
        });
    }

    /** Jefe del mapa 15: golem artillero con cañones en los hombros. */
    public static Image jefeArtillero() {
        return renderizar(160, 160, g -> {
            resplandor(g, 80, 90, 78, c("#ff7a2a"), 0.28);
            caja(g, 6, 38, 38, 30, 8, c("#4a4d5a"), 6);
            caja(g, 116, 38, 38, 30, 8, c("#4a4d5a"), 6);
            ovalo(g, 12, 53, 7, 11, NEGRO, 0);
            ovalo(g, 148, 53, 7, 11, NEGRO, 0);
            caja(g, 28, 56, 104, 90, 26, c("#8a5230"), 7);
            caja(g, 28, 56, 104, 26, 20, c("#aa6a3e"), 0);
            g.setStroke(NEGRO); g.setLineWidth(6); g.strokeRoundRect(28, 56, 104, 90, 26, 26);
            for (int i = 0; i < 4; i++) {
                ovalo(g, 44 + i * 24, 70, 4, 4, c("#e0b080"), 2);
            }
            caja(g, 52, 30, 56, 38, 18, c("#6a4226"), 6);
            caja(g, 62, 42, 36, 9, 4, c("#ffcf5a"), 3);
            resplandor(g, 80, 108, 24, c("#ffcf5a"), 0.9);
            ovalo(g, 80, 108, 15, 15, c("#ff8a1e"), 5);
            ovalo(g, 76, 103, 5, 5, c("#fff1b0"), 0);
            caja(g, 36, 138, 30, 14, 6, c("#3a2415"), 5);
            caja(g, 94, 138, 30, 14, 6, c("#3a2415"), 5);
        });
    }

    /** Jefe del mapa 20: demonio con cuernos. */
    public static Image jefeCornudo() {
        return renderizar(176, 176, g -> {
            resplandor(g, 88, 96, 86, c("#ff2d4d"), 0.3);
            Color hueso = c("#eadcbc");
            g.setFill(hueso);
            g.beginPath();
            g.moveTo(52, 70); g.bezierCurveTo(14, 70, 6, 38, 20, 8); g.bezierCurveTo(34, 38, 52, 46, 70, 52);
            g.closePath(); g.fill();
            g.setStroke(NEGRO); g.setLineWidth(6); g.stroke();
            g.setFill(hueso);
            g.beginPath();
            g.moveTo(124, 70); g.bezierCurveTo(162, 70, 170, 38, 156, 8); g.bezierCurveTo(142, 38, 124, 46, 106, 52);
            g.closePath(); g.fill(); g.stroke();
            ovalo(g, 88, 100, 58, 54, c("#8a1c26"), 7);
            g.setFill(c("#b3293a"));
            g.fillOval(50, 66, 46, 24);
            poligono(g, NEGRO, 0, 50, 70, 82, 86, 50, 90);
            poligono(g, NEGRO, 0, 126, 70, 94, 86, 126, 90);
            poligono(g, c("#ffd23a"), 4, 52, 84, 80, 94, 60, 104);
            poligono(g, c("#ffd23a"), 4, 124, 84, 96, 94, 116, 104);
            ovalo(g, 66, 96, 3.5, 5, NEGRO, 0);
            ovalo(g, 110, 96, 3.5, 5, NEGRO, 0);
            caja(g, 56, 120, 64, 26, 12, c("#2a0610"), 5);
            g.setFill(BLANCO);
            for (int i = 0; i < 5; i++) {
                double x = 62 + i * 11;
                g.fillPolygon(new double[]{x, x + 10, x + 5}, new double[]{121, 121, 134}, 3);
            }
        });
    }

    /** Respaldo del jefe del mapa 5 (planta con cara). */
    public static Image jefePalinni() {
        return renderizar(160, 160, g -> {
            resplandor(g, 80, 90, 74, c("#9b6dff"), 0.25);
            caja(g, 70, 10, 20, 70, 8, c("#a8703f"), 6);
            ovalo(g, 80, 14, 40, 12, c("#c48a52"), 6);
            ovalo(g, 80, 108, 52, 40, c("#8e95a6"), 7);
            g.setFill(c("#b9c0d0"));
            g.fillOval(48, 86, 36, 18);
            ojo(g, 62, 104, 8, 1, 1);
            ojo(g, 98, 104, 8, -1, 1);
            g.setStroke(NEGRO); g.setLineWidth(5);
            g.strokeArc(62, 108, 36, 22, 200, 140, ArcType.OPEN);
        });
    }

    /** Respaldo del jefe del mapa 10 (criatura marrón furiosa). */
    public static Image jefeEmisor() {
        return renderizar(160, 160, g -> {
            resplandor(g, 80, 92, 78, c("#4fd6e8"), 0.25);
            ovalo(g, 80, 100, 62, 50, c("#6b4128"), 7);
            ovalo(g, 80, 60, 38, 34, c("#7c4c2f"), 7);
            ojo(g, 62, 86, 11, 2, 2);
            ojo(g, 98, 86, 11, -2, 2);
            g.setStroke(NEGRO); g.setLineWidth(6);
            g.strokeLine(48, 70, 70, 80); g.strokeLine(112, 70, 90, 80);
            caja(g, 58, 108, 44, 24, 10, c("#2a0610"), 5);
        });
    }

    // ───────────────────────────── invocaciones ─────────────────────────────

    public static Image invocacion(TipoInvocacion tipo) {
        return switch (tipo) {
            case GUSANO -> gusano();
            case MURCIELAGO -> murcielago();
            case ROBOT -> robot();
        };
    }

    private static Image gusano() {
        return renderizar(128, 128, g -> {
            double[][] segmentos = {{20, 92, 11}, {38, 98, 14}, {60, 98, 17}, {84, 92, 20}};
            Color[] tonos = {c("#a95f52"), c("#b86b5c"), c("#c97b6b"), c("#d98b78")};
            for (int i = 0; i < segmentos.length; i++) {
                ovalo(g, segmentos[i][0], segmentos[i][1], segmentos[i][2], segmentos[i][2], tonos[i], 5);
                g.setStroke(c("#7c3d33")); g.setLineWidth(3);
                g.strokeArc(segmentos[i][0] - segmentos[i][2] * 0.6, segmentos[i][1] - segmentos[i][2] * 0.8,
                        segmentos[i][2] * 1.2, segmentos[i][2] * 1.6, 100, 160, ArcType.OPEN);
            }
            ovalo(g, 100, 74, 22, 21, c("#e29a86"), 6);
            g.setFill(c("#f4b8a6"));
            g.fillOval(88, 62, 16, 9);
            ojo(g, 92, 72, 6, 1, 1);
            ojo(g, 110, 72, 6, 1, 1);
            g.setStroke(NEGRO); g.setLineWidth(4);
            g.strokeArc(92, 78, 18, 12, 200, 140, ArcType.OPEN);
        });
    }

    private static Image murcielago() {
        return renderizar(128, 128, g -> {
            Color ala = c("#5b3a85");
            for (int lado = -1; lado <= 1; lado += 2) {
                double s = lado;
                g.setFill(ala);
                g.beginPath();
                g.moveTo(64 + s * 12, 62);
                g.bezierCurveTo(64 + s * 38, 28, 64 + s * 60, 34, 64 + s * 62, 50);
                g.lineTo(64 + s * 52, 56);
                g.lineTo(64 + s * 50, 70);
                g.lineTo(64 + s * 38, 64);
                g.lineTo(64 + s * 32, 80);
                g.lineTo(64 + s * 18, 74);
                g.closePath();
                g.fill();
                g.setStroke(NEGRO); g.setLineWidth(5); g.stroke();
                poligono(g, c("#3b2459"), 4, 64 + s * 14, 46, 64 + s * 22, 22, 64 + s * 28, 46);
            }
            ovalo(g, 64, 70, 22, 24, c("#3f2766"), 6);
            g.setFill(c("#6a4a99"));
            g.fillOval(52, 56, 14, 10);
            resplandor(g, 55, 68, 9, c("#ff4a5a"), 0.9);
            resplandor(g, 73, 68, 9, c("#ff4a5a"), 0.9);
            ovalo(g, 55, 68, 4, 5, c("#ff3a4a"), 2);
            ovalo(g, 73, 68, 4, 5, c("#ff3a4a"), 2);
            poligono(g, BLANCO, 2, 56, 80, 60, 90, 62, 80);
            poligono(g, BLANCO, 2, 66, 80, 68, 90, 72, 80);
        });
    }

    private static Image robot() {
        return renderizar(128, 128, g -> {
            g.setStroke(NEGRO); g.setLineWidth(5); g.strokeLine(64, 24, 64, 12);
            ovalo(g, 64, 10, 6, 6, c("#ff4a5a"), 3);
            caja(g, 34, 100, 22, 16, 5, c("#3b4358"), 5);
            caja(g, 72, 100, 22, 16, 5, c("#3b4358"), 5);
            caja(g, 38, 66, 52, 40, 10, c("#56627c"), 6);
            g.setFill(Color.color(1, 1, 1, 0.18)); g.fillRoundRect(42, 70, 44, 9, 6, 6);
            ovalo(g, 64, 90, 7, 7, c("#4fd6e8"), 3);
            caja(g, 90, 70, 28, 18, 6, c("#2f3648"), 5);
            ovalo(g, 120, 79, 5, 7, c("#4fd6e8"), 3);
            caja(g, 36, 24, 56, 44, 12, c("#7d8aa6"), 6);
            caja(g, 44, 38, 40, 16, 7, c("#102a33"), 4);
            resplandor(g, 64, 46, 22, c("#4fd6e8"), 0.7);
            ovalo(g, 54, 46, 5, 5, c("#8cf2ff"), 0);
            ovalo(g, 74, 46, 5, 5, c("#8cf2ff"), 0);
        });
    }

    // ───────────────────────────── proyectiles ─────────────────────────────

    /** Orbe hostil; el color distingue a cada tirador o jefe. */
    public static Image orbe(Color color) {
        return renderizar(64, 64, g -> {
            resplandor(g, 32, 32, 30, color, 0.55);
            ovalo(g, 32, 32, 15, 15, color, 4);
            g.setFill(Color.color(1, 1, 1, 0.8));
            g.fillOval(24, 22, 9, 7);
        });
    }

    /** Rayo alargado; apunta hacia arriba como el resto de proyectiles. */
    public static Image rayo(Color color) {
        return renderizar(64, 64, g -> {
            resplandor(g, 32, 32, 30, color, 0.5);
            caja(g, 25, 4, 14, 56, 14, color, 4);
            caja(g, 29, 10, 6, 44, 6, Color.color(1, 1, 1, 0.9), 0);
        });
    }

    // ───────────────────────────── iconos ─────────────────────────────

    public static Image iconoCarta(String codigo) {
        return renderizar(96, 96, g -> {
            switch (codigo) {
                case "DANO" -> {
                    resplandor(g, 48, 48, 44, c("#ff4560"), 0.45);
                    g.setStroke(NEGRO); g.setLineWidth(15); g.strokeLine(20, 76, 70, 18);
                    g.setStroke(c("#e8edf5")); g.setLineWidth(8); g.strokeLine(20, 76, 70, 18);
                    caja(g, 14, 62, 30, 9, 3, c("#c0283a"), 4);
                    ovalo(g, 24, 78, 7, 7, c("#a8703f"), 4);
                    poligono(g, c("#ffd873"), 4, 66, 14, 80, 12, 74, 28);
                }
                case "VELOCIDAD" -> {
                    resplandor(g, 48, 48, 44, c("#4fd6e8"), 0.45);
                    poligono(g, c("#8cf2ff"), 5, 56, 8, 24, 54, 44, 54, 36, 88, 72, 38, 52, 38);
                }
                case "VIDA_MAXIMA" -> {
                    resplandor(g, 48, 48, 44, c("#ff4560"), 0.4);
                    g.setFill(c("#e63a54"));
                    g.beginPath();
                    g.moveTo(48, 82);
                    g.bezierCurveTo(10, 56, 10, 22, 32, 20);
                    g.bezierCurveTo(42, 20, 48, 28, 48, 34);
                    g.bezierCurveTo(48, 28, 54, 20, 64, 20);
                    g.bezierCurveTo(86, 22, 86, 56, 48, 82);
                    g.closePath(); g.fill();
                    g.setStroke(NEGRO); g.setLineWidth(5); g.stroke();
                    g.setFill(Color.color(1, 1, 1, 0.5)); g.fillOval(24, 28, 14, 10);
                    caja(g, 42, 40, 12, 28, 3, BLANCO, 3);
                    caja(g, 34, 48, 28, 12, 3, BLANCO, 3);
                }
                case "DANO_INVOCACIONES" -> {
                    resplandor(g, 48, 48, 44, c("#9b6dff"), 0.45);
                    ovalo(g, 48, 62, 20, 16, c("#b390ff"), 5);
                    ovalo(g, 24, 38, 8, 11, c("#b390ff"), 4);
                    ovalo(g, 40, 26, 8, 11, c("#b390ff"), 4);
                    ovalo(g, 56, 26, 8, 11, c("#b390ff"), 4);
                    ovalo(g, 72, 38, 8, 11, c("#b390ff"), 4);
                }
                case "SEGUNDO_ESPACIO_INVOCACION" -> {
                    resplandor(g, 48, 48, 44, c("#ffd873"), 0.45);
                    g.setStroke(NEGRO); g.setLineWidth(13);
                    g.strokeOval(10, 28, 44, 44); g.strokeOval(42, 28, 44, 44);
                    g.setStroke(c("#ffd873")); g.setLineWidth(7);
                    g.strokeOval(10, 28, 44, 44); g.strokeOval(42, 28, 44, 44);
                }
                default -> { // SELECCION_CARTAS: mazo
                    resplandor(g, 48, 48, 44, c("#ffd873"), 0.5);
                    caja(g, 16, 22, 44, 58, 7, c("#7a4fd6"), 5);
                    caja(g, 28, 16, 44, 58, 7, c("#9b6dff"), 5);
                    caja(g, 40, 12, 44, 58, 7, c("#ffd873"), 5);
                    poligono(g, c("#8a1c26"), 3, 62, 26, 66, 38, 78, 38, 68, 46, 72, 58, 62, 50, 52, 58, 56, 46, 46, 38, 58, 38);
                }
            }
        });
    }

    public static Image iconoCarta(CartaTipo tipo) { return iconoCarta(tipo.name()); }

    /** Corazón dorado de la recompensa de vida máxima. */
    public static Image iconoRecompensa() {
        return renderizar(96, 96, g -> {
            resplandor(g, 48, 50, 46, c("#ffd873"), 0.6);
            g.setFill(c("#ffc93a"));
            g.beginPath();
            g.moveTo(48, 84);
            g.bezierCurveTo(8, 56, 10, 20, 32, 18);
            g.bezierCurveTo(42, 18, 48, 26, 48, 32);
            g.bezierCurveTo(48, 26, 54, 18, 64, 18);
            g.bezierCurveTo(86, 20, 88, 56, 48, 84);
            g.closePath(); g.fill();
            g.setStroke(NEGRO); g.setLineWidth(5); g.stroke();
            g.setFill(Color.color(1, 1, 1, 0.6)); g.fillOval(24, 26, 14, 10);
        });
    }

    /** Pieza genérica de objeto cuyo código no tiene icono propio. */
    public static Image iconoDesconocido() {
        return renderizar(96, 96, g -> {
            resplandor(g, 48, 48, 44, c("#9b93ad"), 0.4);
            ovalo(g, 48, 48, 28, 28, c("#6c6580"), 5);
            g.setFill(BLANCO);
            g.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 34));
            g.fillText("?", 40, 60);
        });
    }
}
