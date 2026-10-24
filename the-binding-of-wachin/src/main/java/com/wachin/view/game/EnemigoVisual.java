package com.wachin.view.game;

import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class EnemigoVisual {

    private final Enemigo enemigo;
    private final Node visual;
    private final double velocidad;
    private final double radio;
    private final double tamanoVisual;
    private double x;
    private double y;

    public EnemigoVisual(Enemigo enemigo, double x, double y) {
        this.enemigo = enemigo;
        this.x = x;
        this.y = y;
        this.velocidad = enemigo.getVelocidad();

        boolean esJefe = enemigo instanceof Jefe;
        this.radio = esJefe ? 38 : 22;
        this.tamanoVisual = esJefe ? 82 : 48;

        String asset;
        if (esJefe) {
            asset = "assets/images/bosses/jefe.png";
        } else {
            String especifico = obtenerAssetEnemigo(enemigo);
            asset = AssetVisual.existe(especifico)
                    ? especifico
                    : "assets/images/enemies/enemigo.png";
        }

        this.visual = AssetVisual.crear(
                asset, x, y, tamanoVisual, tamanoVisual,
                obtenerColor(enemigo), esJefe ? Color.GOLD : Color.BLACK,
                esJefe ? 5 : 2
        );
    }

    private String obtenerAssetEnemigo(Enemigo enemigo) {
        String nombre = enemigo.getNombre().toLowerCase(java.util.Locale.ROOT);
        switch (nombre) {
            case "slime": return "assets/images/enemies/slime.png";
            case "murcielago": return "assets/images/enemies/murcielago.png";
            case "demonio": return "assets/images/enemies/demonio.png";
            case "monstruo": return "assets/images/enemies/monstruo.png";
            case "esqueleto": return "assets/images/enemies/esqueleto.png";
            case "fantasma": return "assets/images/enemies/fantasma.png";
            default: return "assets/images/enemies/enemigo.png";
        }
    }

    private Color obtenerColor(Enemigo enemigo) {
        if (enemigo instanceof Jefe) return Color.PURPLE;
        switch (enemigo.getNombre()) {
            case "Slime": return Color.LIMEGREEN;
            case "Murcielago": return Color.DARKSLATEBLUE;
            case "Demonio": return Color.CRIMSON;
            case "Monstruo": return Color.DARKORANGE;
            case "Esqueleto": return Color.LIGHTGRAY;
            case "Fantasma": return Color.LIGHTSKYBLUE;
            default: return Color.CRIMSON;
        }
    }

    public void actualizar(double jugadorX, double jugadorY, double deltaTiempo) {
        double diferenciaX = jugadorX - x;
        double diferenciaY = jugadorY - y;
        double distancia = Math.sqrt(diferenciaX * diferenciaX + diferenciaY * diferenciaY);

        if (distancia > 1) {
            x += (diferenciaX / distancia) * velocidad * deltaTiempo;
            y += (diferenciaY / distancia) * velocidad * deltaTiempo;
            limitarMovimiento();
            AssetVisual.moverAlCentro(visual, x, y, tamanoVisual, tamanoVisual);
        }
    }

    private void limitarMovimiento() {
        x = Math.max(75, Math.min(925, x));
        y = Math.max(75, Math.min(625, y));
    }

    public boolean tocaJugador(double jugadorX, double jugadorY) {
        double dx = jugadorX - x;
        double dy = jugadorY - y;
        return Math.sqrt(dx * dx + dy * dy) < radio + 23;
    }

    public Enemigo getEnemigo() { return enemigo; }
    public Node getVisual() { return visual; }
    public double getX() { return x; }
    public double getY() { return y; }
}
