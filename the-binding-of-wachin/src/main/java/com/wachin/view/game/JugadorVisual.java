package com.wachin.view.game;

import com.wachin.ConfiguracionPartida;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Representación del jugador: sprite, sombra y animaciones procedurales de caminar y disparar. */
public final class JugadorVisual {

    public static final double TAMANO = 64;

    private final Group nodo = new Group();
    private final Group cuerpo = new Group();
    private final ImageView sprite;
    private final ColorAdjust destello = new ColorAdjust(0, 0.2, 0.6, 0);
    private double fase;
    private double retrocesoX;
    private double retrocesoY;
    private double tiempoDestello;
    private double mirada = 1;
    private double intensidadMovimiento;

    public JugadorVisual(ConfiguracionPartida.Personaje personaje) {
        Image imagen = AssetVisual.imagen(CatalogoAssets.personaje(personaje), "jugador_" + personaje.name(),
                () -> Dibujos.jugador(personaje == ConfiguracionPartida.Personaje.WACHINA));
        sprite = AssetVisual.vista(imagen, TAMANO, TAMANO);
        sprite.setY(sprite.getY() - 4);
        cuerpo.getChildren().add(sprite);
        nodo.getChildren().addAll(SombraVisual.crear(26, 9, TAMANO / 2 - 6), cuerpo);
        nodo.setMouseTransparent(true);
    }

    /**
     * Sincroniza posición y animación.
     *
     * @param velocidadX componente horizontal normalizada del movimiento (−1…1)
     * @param velocidadY componente vertical normalizada del movimiento (−1…1)
     */
    public void actualizar(double dt, double x, double y, double velocidadX, double velocidadY, boolean invulnerable) {
        double objetivo = Math.min(1, Math.hypot(velocidadX, velocidadY));
        intensidadMovimiento += (objetivo - intensidadMovimiento) * (1 - Math.exp(-12 * dt));
        fase += dt * (6 + 9 * intensidadMovimiento);
        double rebote = Math.abs(Math.sin(fase)) * 4 * intensidadMovimiento;
        if (Math.abs(velocidadX) > 0.1) mirada = Math.signum(velocidadX);
        retrocesoX *= Math.exp(-14 * dt);
        retrocesoY *= Math.exp(-14 * dt);
        double respiracion = 1 + Math.sin(fase * 0.45) * 0.015 * (1 - intensidadMovimiento);
        cuerpo.setScaleX(mirada * (1 + 0.04 * Math.sin(fase * 2) * intensidadMovimiento) * respiracion);
        cuerpo.setScaleY((1 - 0.04 * Math.sin(fase * 2) * intensidadMovimiento) * respiracion);
        cuerpo.setRotate(Math.sin(fase) * 5 * intensidadMovimiento);
        cuerpo.setTranslateX(retrocesoX);
        cuerpo.setTranslateY(-rebote + retrocesoY);
        AssetVisual.moverAlCentro(nodo, x, y);
        nodo.setViewOrder(-y);
        if (tiempoDestello > 0) {
            tiempoDestello -= dt;
            sprite.setEffect(tiempoDestello > 0 ? destello : null);
        }
        nodo.setOpacity(invulnerable ? (Math.sin(fase * 9) > 0 ? 1 : 0.38) : 1);
    }

    /** Pequeño empujón hacia atrás al disparar. */
    public void retroceso(double direccionX, double direccionY) {
        retrocesoX = -direccionX * 4;
        retrocesoY = -direccionY * 4;
        if (direccionX != 0) mirada = Math.signum(direccionX);
    }

    public void destellar() { tiempoDestello = 0.14; sprite.setEffect(destello); }

    public Node getVisual() { return nodo; }
}
