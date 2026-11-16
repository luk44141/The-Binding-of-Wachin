package com.wachin.view.game;

import com.wachin.view.fx.Brillo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;

/**
 * Dibuja la sala: piso, paredes, decoración, antorchas y puertas. El fondo se compone una vez por
 * combinación de mapa, tipo de sala y variante y se guarda en caché; durante la partida solo se
 * animan las llamas y las puertas.
 */
public final class EscenarioView {

    public static final double ANCHO = 1000;
    public static final double ALTO = 700;
    private static final double GROSOR = 50;
    private static final Color[] TINTES = {
        Color.web("#a9bde0"), Color.web("#e0bb90"), Color.web("#a4d6ba"), Color.web("#c3a9e8"), Color.web("#e6c585")};
    private static final double[][] ANTORCHAS = {
        {270, 30}, {730, 30}, {270, 670}, {730, 670}, {30, 190}, {30, 510}, {970, 190}, {970, 510}};

    private final Group raiz = new Group();
    private final Group capaFondo = new Group();
    private final ImageView fondo = new ImageView();
    private final Group llamas = new Group();
    private final List<PuertaVisual> puertas = new ArrayList<>();
    private final List<Group> grupoLlamas = new ArrayList<>();
    private final Map<String, Image> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) { return size() > 8; }
    };
    private double tiempo;
    private boolean jefe;

    public EscenarioView() {
        fondo.setMouseTransparent(true);
        capaFondo.getChildren().add(fondo);
        raiz.getChildren().add(capaFondo);
        for (PuertaVisual.Direccion d : PuertaVisual.Direccion.values()) {
            PuertaVisual puerta = new PuertaVisual(d);
            puertas.add(puerta);
            raiz.getChildren().add(puerta.getVisual());
        }
        for (double[] p : ANTORCHAS) {
            Group llama = crearLlama(p[0], p[1]);
            grupoLlamas.add(llama);
            llamas.getChildren().add(llama);
        }
        llamas.setMouseTransparent(true);
        raiz.getChildren().add(llamas);
    }

    public Group getRaiz() { return raiz; }

    private Group crearLlama(double x, double y) {
        double haciaX = x < 100 ? 14 : x > 900 ? -14 : 0;
        double haciaY = y < 100 ? 14 : y > 600 ? -14 : 0;
        ImageView brillo = Brillo.nodoRadial(Color.web("#ffb050"), 0.5, 104);
        Ellipse exterior = new Ellipse(0, -2, 5, 9);
        exterior.setFill(Color.web("#ff8a1e"));
        Ellipse interior = new Ellipse(0, 1, 2.6, 5);
        interior.setFill(Color.web("#fff0a0"));
        Group g = new Group(brillo, exterior, interior);
        g.setTranslateX(x + haciaX);
        g.setTranslateY(y + haciaY - 4);
        g.setUserData(new Object[]{brillo, exterior, interior, Math.random() * 6.28});
        return g;
    }

    /** Compone (o recupera de la caché) el fondo de la sala y ajusta colores de antorchas y puertas. */
    public void configurar(int mapa, boolean esJefe, int variante) {
        this.jefe = esJefe;
        String clave = mapa + ":" + esJefe + ":" + Math.floorMod(variante, 4);
        fondo.setImage(cache.computeIfAbsent(clave, k -> renderizar(mapa, esJefe, Math.floorMod(variante, 4))));
        for (Group g : grupoLlamas) {
            Object[] partes = (Object[]) g.getUserData();
            Color luz = esJefe ? Color.web("#ff3050") : Color.web("#ffb050");
            ((ImageView) partes[0]).setImage(Brillo.radial(luz, 0.5));
            ((Ellipse) partes[1]).setFill(esJefe ? Color.web("#e0203c") : Color.web("#ff8a1e"));
        }
        for (PuertaVisual puerta : puertas) puerta.setJefe(esJefe);
    }

    public void configurarSalidas(Set<PuertaVisual.Direccion> salidas, PuertaVisual.Direccion entrada, boolean abiertas) {
        for (PuertaVisual puerta : puertas) {
            boolean seleccionada = salidas.contains(puerta.getDireccion());
            boolean esEntrada = entrada == puerta.getDireccion();
            puerta.getVisual().setVisible(seleccionada || esEntrada);
            puerta.setAbierta(seleccionada && abiertas);
        }
    }

    public boolean esSalaJefe() { return jefe; }

    public void actualizar(double dt) {
        tiempo += dt;
        for (Group g : grupoLlamas) {
            Object[] partes = (Object[]) g.getUserData();
            double f = (double) partes[3];
            double parpadeo = Math.sin(tiempo * 11 + f) * 0.5 + Math.sin(tiempo * 17 + f * 2) * 0.5;
            ((ImageView) partes[0]).setOpacity(0.78 + 0.22 * parpadeo);
            ((Ellipse) partes[1]).setScaleY(1 + 0.2 * parpadeo);
            ((Ellipse) partes[2]).setScaleY(1 + 0.3 * parpadeo);
        }
        for (PuertaVisual puerta : puertas) puerta.actualizar(dt);
    }

    // ───────────────────────────── composición del fondo ─────────────────────────────

    private Image renderizar(int mapa, boolean esJefe, int variante) {
        Random azar = new Random(mapa * 7919L + variante * 104729L + (esJefe ? 13 : 0));
        Canvas lienzo = new Canvas(ANCHO, ALTO);
        GraphicsContext g = lienzo.getGraphicsContext2D();
        g.setFill(Color.web("#07060b"));
        g.fillRect(0, 0, ANCHO, ALTO);
        pintarPiso(g, mapa, esJefe, azar);
        pintarParedes(g, azar);
        pintarAntorchas(g, esJefe);
        pintarVineta(g);
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }

    /** Oscurece los bordes de la sala. Se pinta en el fondo en caché para no cubrir la pantalla con un degradado cada fotograma. */
    private void pintarVineta(GraphicsContext g) {
        g.setFill(new RadialGradient(0, 0, ANCHO / 2, ALTO / 2, Math.max(ANCHO, ALTO) * 0.72, false, CycleMethod.NO_CYCLE,
                new Stop(0.55, Color.TRANSPARENT), new Stop(1, Color.color(0.02, 0.01, 0.04, 0.82))));
        g.fillRect(0, 0, ANCHO, ALTO);
    }

    private void pintarPiso(GraphicsContext g, int mapa, boolean esJefe, Random azar) {
        double x0 = GROSOR, y0 = GROSOR, w = ANCHO - GROSOR * 2, h = ALTO - GROSOR * 2;
        g.save();
        g.beginPath();
        g.rect(x0, y0, w, h);
        g.clip();
        List<String> preferidas = mapa % 2 == 0 ? CatalogoAssets.conHistoricos(CatalogoAssets.PISO_ALTERNATIVO)
                : CatalogoAssets.conHistoricos(CatalogoAssets.PISO);
        Optional<Image> textura = AssetVisual.buscar(preferidas);
        if (textura.isEmpty()) textura = AssetVisual.buscar(CatalogoAssets.conHistoricos(CatalogoAssets.PISO));
        if (textura.isPresent()) {
            int lado = 128;
            for (double y = y0; y < y0 + h; y += lado) {
                for (double x = x0; x < x0 + w; x += lado) g.drawImage(textura.get(), x, y, lado, lado);
            }
        } else {
            pintarLosas(g, x0, y0, w, h, azar);
        }
        boolean usaAlternativa = mapa % 2 == 0 && AssetVisual.existe(CatalogoAssets.conHistoricos(CatalogoAssets.PISO_ALTERNATIVO));
        g.setGlobalBlendMode(BlendMode.MULTIPLY);
        g.setFill(esJefe ? Color.web("#e07080") : usaAlternativa ? Color.web("#d8d0c8") : TINTES[(mapa - 1) % TINTES.length]);
        g.fillRect(x0, y0, w, h);
        g.setFill(Color.web("#10101c", 0.38));
        g.fillRect(x0, y0, w, h);
        g.setGlobalBlendMode(BlendMode.SRC_OVER);
        decorarPiso(g, esJefe, azar);
        sombrearBordes(g, x0, y0, w, h);
        g.restore();
    }

    private void pintarLosas(GraphicsContext g, double x0, double y0, double w, double h, Random azar) {
        int lado = 75;
        for (double y = y0; y < y0 + h; y += lado) {
            for (double x = x0; x < x0 + w; x += lado) {
                double tono = 0.36 + azar.nextDouble() * 0.1;
                g.setFill(Color.color(tono, tono, tono + 0.03));
                g.fillRect(x, y, lado, lado);
                g.setFill(Color.color(1, 1, 1, 0.07));
                g.fillRect(x, y, lado, 3);
                g.setStroke(Color.color(0, 0, 0, 0.55));
                g.setLineWidth(2.5);
                g.strokeRect(x, y, lado, lado);
            }
        }
    }

    private void decorarPiso(GraphicsContext g, boolean esJefe, Random azar) {
        for (int i = 0; i < 4; i++) {
            double x = 110 + azar.nextDouble() * 780, y = 110 + azar.nextDouble() * 480, r = 28 + azar.nextDouble() * 40;
            Color mancha = azar.nextInt(3) == 0 ? Color.web("#3a6a40") : Color.web("#4a0c18");
            g.setFill(new RadialGradient(0, 0, x, y, r, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(mancha.getRed(), mancha.getGreen(), mancha.getBlue(), 0.28)),
                    new Stop(1, Color.color(mancha.getRed(), mancha.getGreen(), mancha.getBlue(), 0))));
            g.fillOval(x - r, y - r, r * 2, r * 2);
        }
        g.setLineWidth(2);
        for (int i = 0; i < 6; i++) {
            double x = 90 + azar.nextDouble() * 820, y = 90 + azar.nextDouble() * 520;
            g.beginPath();
            g.moveTo(x, y);
            for (int s = 0; s < 5; s++) { x += (azar.nextDouble() - 0.4) * 34; y += (azar.nextDouble() - 0.5) * 30; g.lineTo(x, y); }
            g.setStroke(Color.color(0, 0, 0, 0.4));
            g.stroke();
        }
        if (esJefe) {
            g.setGlobalBlendMode(BlendMode.ADD);
            g.setStroke(Color.web("#ff2d4d", 0.4));
            g.setLineWidth(3);
            g.strokeOval(500 - 150, 350 - 150, 300, 300);
            g.strokeOval(500 - 118, 350 - 118, 236, 236);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * i / 4;
                g.strokeLine(500 + Math.cos(a) * 118, 350 + Math.sin(a) * 118, 500 + Math.cos(a) * 150, 350 + Math.sin(a) * 150);
                g.strokeLine(500 + Math.cos(a) * 118, 350 + Math.sin(a) * 118,
                        500 + Math.cos(a + Math.PI / 4) * 118, 350 + Math.sin(a + Math.PI / 4) * 118);
            }
            g.setFill(new RadialGradient(0, 0, 500, 350, 130, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#ff2d4d", 0.16)), new Stop(1, Color.TRANSPARENT)));
            g.fillOval(370, 220, 260, 260);
            g.setGlobalBlendMode(BlendMode.SRC_OVER);
        }
    }

    private void sombrearBordes(GraphicsContext g, double x, double y, double w, double h) {
        double d = 90;
        Color oscuro = Color.color(0, 0, 0, 0.62);
        g.setFill(new LinearGradient(0, y, 0, y + d, false, CycleMethod.NO_CYCLE, new Stop(0, oscuro), new Stop(1, Color.TRANSPARENT)));
        g.fillRect(x, y, w, d);
        g.setFill(new LinearGradient(0, y + h, 0, y + h - d, false, CycleMethod.NO_CYCLE, new Stop(0, oscuro), new Stop(1, Color.TRANSPARENT)));
        g.fillRect(x, y + h - d, w, d);
        g.setFill(new LinearGradient(x, 0, x + d, 0, false, CycleMethod.NO_CYCLE, new Stop(0, oscuro), new Stop(1, Color.TRANSPARENT)));
        g.fillRect(x, y, d, h);
        g.setFill(new LinearGradient(x + w, 0, x + w - d, 0, false, CycleMethod.NO_CYCLE, new Stop(0, oscuro), new Stop(1, Color.TRANSPARENT)));
        g.fillRect(x + w - d, y, d, h);
    }

    private void pintarParedes(GraphicsContext g, Random azar) {
        double[][] bandas = {{0, 0, ANCHO, GROSOR}, {0, ALTO - GROSOR, ANCHO, GROSOR},
            {0, GROSOR, GROSOR, ALTO - GROSOR * 2}, {ANCHO - GROSOR, GROSOR, GROSOR, ALTO - GROSOR * 2}};
        Optional<Image> textura = AssetVisual.buscar(CatalogoAssets.conHistoricos(CatalogoAssets.PARED));
        for (double[] b : bandas) {
            g.save();
            g.beginPath();
            g.rect(b[0], b[1], b[2], b[3]);
            g.clip();
            if (textura.isPresent()) {
                int lado = 100;
                for (double y = b[1]; y < b[1] + b[3]; y += lado) {
                    for (double x = b[0]; x < b[0] + b[2]; x += lado) g.drawImage(textura.get(), x, y, lado, lado);
                }
                g.setGlobalBlendMode(BlendMode.MULTIPLY);
                g.setFill(Color.web("#7f7a99"));
                g.fillRect(b[0], b[1], b[2], b[3]);
                g.setGlobalBlendMode(BlendMode.SRC_OVER);
            } else {
                pintarLadrillos(g, b, azar);
            }
            g.restore();
        }
        // sombreado hacia el interior y filo luminoso
        g.setFill(new LinearGradient(0, 0, 0, GROSOR, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0, 0, 0, 0.55)), new Stop(1, Color.color(0, 0, 0, 0.05))));
        g.fillRect(0, 0, ANCHO, GROSOR);
        g.setFill(new LinearGradient(0, ALTO - GROSOR, 0, ALTO, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0, 0, 0, 0.05)), new Stop(1, Color.color(0, 0, 0, 0.6))));
        g.fillRect(0, ALTO - GROSOR, ANCHO, GROSOR);
        g.setFill(new LinearGradient(0, 0, GROSOR, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0, 0, 0, 0.55)), new Stop(1, Color.color(0, 0, 0, 0.05))));
        g.fillRect(0, GROSOR, GROSOR, ALTO - GROSOR * 2);
        g.setFill(new LinearGradient(ANCHO - GROSOR, 0, ANCHO, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0, 0, 0, 0.05)), new Stop(1, Color.color(0, 0, 0, 0.55))));
        g.fillRect(ANCHO - GROSOR, GROSOR, GROSOR, ALTO - GROSOR * 2);
        g.setStroke(Color.web("#8f84b0", 0.55));
        g.setLineWidth(2);
        g.strokeRect(GROSOR, GROSOR, ANCHO - GROSOR * 2, ALTO - GROSOR * 2);
        g.setStroke(Color.web("#000000", 0.7));
        g.setLineWidth(3);
        g.strokeRect(GROSOR - 2.5, GROSOR - 2.5, ANCHO - GROSOR * 2 + 5, ALTO - GROSOR * 2 + 5);
        for (double[] esquina : new double[][]{{0, 0}, {ANCHO - 64, 0}, {0, ALTO - 64}, {ANCHO - 64, ALTO - 64}}) {
            g.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#5b5170")), new Stop(1, Color.web("#2a2438"))));
            g.fillRoundRect(esquina[0], esquina[1], 64, 64, 10, 10);
            g.setStroke(Color.web("#050409"));
            g.setLineWidth(3);
            g.strokeRoundRect(esquina[0], esquina[1], 64, 64, 10, 10);
            g.setFill(Color.web("#ffffff", 0.12));
            g.fillRoundRect(esquina[0] + 6, esquina[1] + 6, 26, 8, 4, 4);
        }
    }

    private void pintarLadrillos(GraphicsContext g, double[] b, Random azar) {
        int alto = 25;
        int ancho = 50;
        for (double y = b[1]; y < b[1] + b[3]; y += alto) {
            int fila = (int) ((y - b[1]) / alto);
            for (double x = b[0] - (fila % 2) * ancho / 2.0; x < b[0] + b[2]; x += ancho) {
                double tono = 0.28 + azar.nextDouble() * 0.08;
                g.setFill(Color.color(tono, tono * 0.97, tono * 1.1));
                g.fillRect(x, y, ancho, alto);
                g.setFill(Color.color(1, 1, 1, 0.06));
                g.fillRect(x, y, ancho, 2);
                g.setStroke(Color.color(0, 0, 0, 0.6));
                g.setLineWidth(2);
                g.strokeRect(x, y, ancho, alto);
            }
        }
    }

    private void pintarAntorchas(GraphicsContext g, boolean esJefe) {
        Color luz = esJefe ? Color.web("#ff2d4d") : Color.web("#ffb050");
        for (double[] p : ANTORCHAS) {
            double haciaX = p[0] < 100 ? 1 : p[0] > 900 ? -1 : 0;
            double haciaY = p[1] < 100 ? 1 : p[1] > 600 ? -1 : 0;
            g.setGlobalBlendMode(BlendMode.ADD);
            double cx = p[0] + haciaX * 70, cy = p[1] + haciaY * 70;
            g.setFill(new RadialGradient(0, 0, cx, cy, 150, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(luz.getRed(), luz.getGreen(), luz.getBlue(), 0.16)),
                    new Stop(1, Color.color(luz.getRed(), luz.getGreen(), luz.getBlue(), 0))));
            g.fillOval(cx - 150, cy - 150, 300, 300);
            g.setGlobalBlendMode(BlendMode.SRC_OVER);
            g.setFill(Color.web("#17131f"));
            g.fillRoundRect(p[0] - 7 + haciaX * 4, p[1] - 5 + haciaY * 4, 14, 14, 4, 4);
            g.setStroke(Color.BLACK);
            g.setLineWidth(2);
            g.strokeRoundRect(p[0] - 7 + haciaX * 4, p[1] - 5 + haciaY * 4, 14, 14, 4, 4);
        }
    }
}
