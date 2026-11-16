package com.wachin.controller;

import com.wachin.service.AjustesService;
import com.wachin.service.SonidoService;
import com.wachin.service.SonidoService.Efecto;
import com.wachin.view.fx.CapaEfectos;
import com.wachin.view.game.PartidaView;
import javafx.scene.paint.Color;

/**
 * Punto único para la respuesta sensorial a los eventos del juego: partículas, textos flotantes,
 * sacudida de cámara y sonido. Así los controladores describen qué ocurrió sin repetir cómo se muestra.
 */
final class Retroalimentacion {

    static final Color DORADO = Color.web("#ffd873");
    static final Color ROJO = Color.web("#ff4560");
    static final Color CIAN = Color.web("#4fd6e8");

    private final PartidaView vista;
    private final SonidoService sonido = SonidoService.obtener();
    private final AjustesService ajustes = AjustesService.obtener();

    Retroalimentacion(PartidaView vista) { this.vista = vista; }

    void sonar(Efecto efecto) { sonido.reproducir(efecto); }

    void disparo(double x, double y, double dx, double dy, Color color) {
        vista.efectos().disparo(x, y, dx, dy, color);
        sonido.reproducir(Efecto.DISPARO);
    }

    void impacto(double x, double y, int dano, Color color) {
        CapaEfectos efectos = vista.efectos();
        efectos.impacto(x, y, color);
        if (ajustes.numerosDanoActivos()) efectos.texto(x, y - 24, String.valueOf(dano), CapaEfectos.colorDano(), 17);
        sonido.reproducir(Efecto.IMPACTO);
    }

    void impactoPared(double x, double y, Color color) {
        vista.efectos().chispas(x, y, color, 5, 120);
    }

    void muerteEnemigo(double x, double y, boolean jefe) {
        vista.efectos().explosion(x, y, jefe ? Color.web("#ffb050") : ROJO, jefe ? 2.6 : 1.0);
        sonido.reproducir(Efecto.MUERTE);
        if (jefe) vista.sacudir(14);
    }

    void jugadorHerido(double x, double y, int dano) {
        vista.efectos().chispas(x, y, ROJO, 12, 220);
        if (ajustes.numerosDanoActivos()) vista.efectos().texto(x, y - 34, "-" + dano, ROJO, 22);
        vista.sacudir(8);
        vista.destellarDanio(0.75);
        sonido.reproducir(Efecto.DANIO);
    }

    void recoger(double x, double y, Color color) {
        vista.efectos().recoger(x, y, color);
        sonido.reproducir(Efecto.RECOGER);
    }

    void nivelSubido(double x, double y, int nivel) {
        vista.efectos().onda(x, y, DORADO, 70, 0.5);
        vista.efectos().recoger(x, y, DORADO);
        vista.efectos().texto(x, y - 40, "NIVEL " + nivel, DORADO, 22);
        sonido.reproducir(Efecto.NIVEL);
    }

    void aparicionEnemigo(double x, double y) {
        vista.efectos().onda(x, y, Color.web("#9b6dff"), 30, 0.35);
        vista.efectos().chispas(x, y, Color.web("#9b6dff"), 6, 90);
    }

    void aparicionJefe(double x, double y, String nombre, int sala) {
        vista.efectos().onda(x, y, ROJO, 160, 0.9);
        vista.efectos().onda(x, y, DORADO, 100, 0.6);
        vista.efectos().chispas(x, y, ROJO, 30, 260);
        vista.sacudir(12);
        vista.mostrarCartel(nombre.toUpperCase(), "JEFE DE LA SALA " + sala, 2.4);
        sonido.reproducir(Efecto.JEFE);
    }

    void puertasAbiertas() { sonido.reproducir(Efecto.PUERTA); }

    void ataqueJefe(double x, double y) {
        vista.sacudir(4);
        vista.efectos().onda(x, y, ROJO, 50, 0.25);
    }
}
