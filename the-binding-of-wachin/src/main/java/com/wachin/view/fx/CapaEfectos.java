package com.wachin.view.fx;

import com.wachin.service.AjustesService;
import com.wachin.view.ui.Tema;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Sistema de partículas, anillos y textos flotantes dibujado sobre un único {@code Canvas}.
 *
 * <p>Usar un solo lienzo evita crear y destruir nodos por cada chispa. El número de partículas
 * está acotado y se escala con la calidad elegida en los ajustes.</p>
 */
public final class CapaEfectos {

    private static final int MAX_PARTICULAS = 520;
    private static final int MAX_TEXTOS = 28;

    private static final class Particula {
        double x, y, vx, vy, vida, duracion, radio, radioFinal, friccion, gravedad;
        Color color;
        boolean brillante;
        boolean anillo;
    }

    private static final class Texto {
        double x, y, vida, duracion, tamano;
        String contenido;
        Color color;
    }

    private final Canvas lienzo;
    private final GraphicsContext g;
    private final List<Particula> particulas = new ArrayList<>();
    private final List<Texto> textos = new ArrayList<>();
    private final Random azar = new Random();
    private final java.util.Map<Integer, Font> fuentes = new java.util.HashMap<>();
    private final AjustesService ajustes;
    private boolean limpio = true;

    public CapaEfectos(double ancho, double alto, AjustesService ajustes) {
        this.ajustes = ajustes;
        lienzo = new Canvas(ancho, alto);
        lienzo.setMouseTransparent(true);
        g = lienzo.getGraphicsContext2D();
        g.setTextAlign(TextAlignment.CENTER);
    }

    public Canvas getLienzo() { return lienzo; }

    private int cantidad(int base) {
        return (int) Math.round(base * ajustes.factorParticulas());
    }

    private Particula nueva() {
        if (particulas.size() >= MAX_PARTICULAS) return null;
        Particula p = new Particula();
        particulas.add(p);
        return p;
    }

    // ───────────────────────────── efectos ─────────────────────────────

    /** Ráfaga de chispas en un punto. */
    public void chispas(double x, double y, Color color, int cantidadBase, double velocidad) {
        int n = cantidad(cantidadBase);
        for (int i = 0; i < n; i++) {
            Particula p = nueva();
            if (p == null) return;
            double angulo = azar.nextDouble() * Math.PI * 2;
            double v = velocidad * (0.35 + azar.nextDouble() * 0.9);
            p.x = x; p.y = y; p.vx = Math.cos(angulo) * v; p.vy = Math.sin(angulo) * v;
            p.duracion = p.vida = 0.25 + azar.nextDouble() * 0.35;
            p.radio = 2.6 + azar.nextDouble() * 2.4; p.radioFinal = 0.4;
            p.friccion = 3.2; p.gravedad = 0; p.color = color; p.brillante = true;
        }
    }

    public void impacto(double x, double y, Color color) {
        chispas(x, y, color, 8, 180);
        onda(x, y, color, 22, 0.18);
    }

    /** Anillo que se expande. */
    public void onda(double x, double y, Color color, double radioFinal, double duracion) {
        if (ajustes.factorParticulas() == 0) return;
        Particula p = nueva();
        if (p == null) return;
        p.x = x; p.y = y; p.vx = p.vy = 0;
        p.duracion = p.vida = duracion;
        p.radio = 4; p.radioFinal = radioFinal; p.color = color; p.anillo = true; p.brillante = true;
    }

    /** Explosión con núcleo, chispas, humo y onda de choque. */
    public void explosion(double x, double y, Color color, double escala) {
        chispas(x, y, color, (int) (16 * escala), 230 * Math.sqrt(escala));
        chispas(x, y, Color.WHITE, (int) (6 * escala), 140);
        int humo = cantidad((int) (7 * escala));
        for (int i = 0; i < humo; i++) {
            Particula p = nueva();
            if (p == null) break;
            double angulo = azar.nextDouble() * Math.PI * 2;
            p.x = x + Math.cos(angulo) * 8; p.y = y + Math.sin(angulo) * 8;
            p.vx = Math.cos(angulo) * 40; p.vy = Math.sin(angulo) * 40 - 25;
            p.duracion = p.vida = 0.55 + azar.nextDouble() * 0.4;
            p.radio = 8 * escala; p.radioFinal = 20 * escala;
            p.friccion = 2; p.color = Color.web("#2b2433"); p.brillante = false;
        }
        onda(x, y, color, 46 * escala, 0.32);
        onda(x, y, Color.WHITE, 30 * escala, 0.2);
    }

    /** Destellos ascendentes al recoger un objeto. */
    public void recoger(double x, double y, Color color) {
        int n = cantidad(14);
        for (int i = 0; i < n; i++) {
            Particula p = nueva();
            if (p == null) return;
            double angulo = -Math.PI / 2 + (azar.nextDouble() - 0.5) * 2.2;
            double v = 60 + azar.nextDouble() * 120;
            p.x = x; p.y = y; p.vx = Math.cos(angulo) * v; p.vy = Math.sin(angulo) * v;
            p.duracion = p.vida = 0.5 + azar.nextDouble() * 0.35;
            p.radio = 3; p.radioFinal = 0.5; p.friccion = 2.2; p.gravedad = 60;
            p.color = color; p.brillante = true;
        }
        onda(x, y, color, 34, 0.35);
    }

    /** Fogonazo de un disparo en la dirección indicada. */
    public void disparo(double x, double y, double dx, double dy, Color color) {
        int n = cantidad(5);
        for (int i = 0; i < n; i++) {
            Particula p = nueva();
            if (p == null) return;
            double angulo = Math.atan2(dy, dx) + (azar.nextDouble() - 0.5) * 0.9;
            double v = 120 + azar.nextDouble() * 110;
            p.x = x + dx * 16; p.y = y + dy * 16;
            p.vx = Math.cos(angulo) * v; p.vy = Math.sin(angulo) * v;
            p.duracion = p.vida = 0.14 + azar.nextDouble() * 0.1;
            p.radio = 3.4; p.radioFinal = 0.5; p.friccion = 4; p.color = color; p.brillante = true;
        }
    }

    /** Partícula tenue que deja un proyectil a su paso. */
    public void estela(double x, double y, Color color) {
        if (ajustes.factorParticulas() < 1 && azar.nextBoolean()) return;
        if (ajustes.factorParticulas() == 0) return;
        Particula p = nueva();
        if (p == null) return;
        p.x = x + (azar.nextDouble() - 0.5) * 4; p.y = y + (azar.nextDouble() - 0.5) * 4;
        p.vx = (azar.nextDouble() - 0.5) * 16; p.vy = (azar.nextDouble() - 0.5) * 16;
        p.duracion = p.vida = 0.22; p.radio = 4; p.radioFinal = 0.5;
        p.friccion = 2; p.color = color; p.brillante = true;
    }

    /** Texto que asciende y se desvanece (daño, mensajes breves). */
    public void texto(double x, double y, String contenido, Color color, double tamano) {
        if (textos.size() >= MAX_TEXTOS) textos.remove(0);
        Texto t = new Texto();
        t.x = x + (azar.nextDouble() - 0.5) * 16; t.y = y;
        t.contenido = contenido; t.color = color; t.tamano = tamano;
        t.duracion = t.vida = 0.8;
        textos.add(t);
    }

    public void limpiar() {
        particulas.clear();
        textos.clear();
        g.clearRect(0, 0, lienzo.getWidth(), lienzo.getHeight());
        limpio = true;
    }

    // ───────────────────────────── ciclo ─────────────────────────────

    public void actualizar(double dt) {
        if (particulas.isEmpty() && textos.isEmpty()) {
            if (!limpio) { g.clearRect(0, 0, lienzo.getWidth(), lienzo.getHeight()); limpio = true; }
            return;
        }
        limpio = false;
        g.clearRect(0, 0, lienzo.getWidth(), lienzo.getHeight());
        for (int i = particulas.size() - 1; i >= 0; i--) {
            Particula p = particulas.get(i);
            p.vida -= dt;
            if (p.vida <= 0) {
                particulas.set(i, particulas.get(particulas.size() - 1));
                particulas.remove(particulas.size() - 1);
                continue;
            }
            double amortiguacion = Math.exp(-p.friccion * dt);
            p.vx *= amortiguacion; p.vy = p.vy * amortiguacion + p.gravedad * dt;
            p.x += p.vx * dt; p.y += p.vy * dt;
            dibujar(p);
        }
        g.setGlobalBlendMode(BlendMode.SRC_OVER);
        g.setGlobalAlpha(1);
        for (int i = textos.size() - 1; i >= 0; i--) {
            Texto t = textos.get(i);
            t.vida -= dt;
            if (t.vida <= 0) { textos.remove(i); continue; }
            t.y -= 46 * dt;
            double progreso = 1 - t.vida / t.duracion;
            double escala = progreso < 0.15 ? 0.6 + progreso / 0.15 * 0.6 : 1.2 - Math.min(0.2, (progreso - 0.15) * 0.4);
            g.setGlobalAlpha(Math.min(1, t.vida / t.duracion * 2.2));
            g.setFont(fuente(t.tamano * escala));
            g.setFill(Color.BLACK);
            g.fillText(t.contenido, t.x + 1.5, t.y + 1.5);
            g.setFill(t.color);
            g.fillText(t.contenido, t.x, t.y);
        }
        g.setGlobalAlpha(1);
    }

    /** Las fuentes se reutilizan por tamaño entero; crear una por fotograma y texto es innecesariamente caro. */
    private Font fuente(double tamano) {
        return fuentes.computeIfAbsent((int) Math.round(tamano), t -> Font.font("Segoe UI", FontWeight.BOLD, t));
    }

    private void dibujar(Particula p) {
        double fraccion = p.vida / p.duracion;
        double radio = p.radioFinal + (p.radio - p.radioFinal) * (p.anillo ? 1 - fraccion : fraccion);
        if (p.anillo) radio = p.radio + (p.radioFinal - p.radio) * (1 - fraccion * fraccion);
        g.setGlobalBlendMode(p.brillante ? BlendMode.ADD : BlendMode.SRC_OVER);
        g.setGlobalAlpha(Math.max(0, Math.min(1, p.anillo ? fraccion : fraccion * (p.brillante ? 1 : 0.55))));
        if (p.anillo) {
            g.setStroke(p.color);
            g.setLineWidth(1 + 3 * fraccion);
            g.strokeOval(p.x - radio, p.y - radio, radio * 2, radio * 2);
        } else {
            g.setFill(p.color);
            g.fillOval(p.x - radio, p.y - radio, radio * 2, radio * 2);
        }
    }

    /** Color de texto por defecto para daño. */
    public static Color colorDano() { return Tema.ORO_CLARO; }
}
