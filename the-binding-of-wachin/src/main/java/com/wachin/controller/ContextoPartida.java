package com.wachin.controller;

import com.wachin.ConfiguracionPartida;
import com.wachin.model.EstadisticasPartida;
import com.wachin.model.Jugador;
import com.wachin.model.ObjetoSuelo;
import com.wachin.service.CartaService;
import com.wachin.service.ColisionService;
import com.wachin.service.CombateService;
import com.wachin.service.EnemigoIAService;
import com.wachin.service.InvocacionService;
import com.wachin.service.JefeService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.service.MejorasService;
import com.wachin.service.ObjetoSueloService;
import com.wachin.service.ProgresionService;
import com.wachin.service.RecompensaService;
import com.wachin.service.SalaService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.InvocacionVisual;
import com.wachin.view.game.JugadorVisual;
import com.wachin.view.game.ObjetoSueloVisual;
import com.wachin.view.game.PartidaView;
import com.wachin.view.game.ProyectilVisual;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Estado y servicios compartidos por los controladores de una partida. Evita que cada
 * controlador conozca a los demás solo para leer la posición del jugador o las listas de entidades.
 */
final class ContextoPartida {

    static final double LIMITE_IZQUIERDO = 75;
    static final double LIMITE_DERECHO = 925;
    static final double LIMITE_SUPERIOR = 75;
    static final double LIMITE_INFERIOR = 625;
    static final double RADIO_JUGADOR = 27;

    final PartidaView vista;
    final ConfiguracionPartida configuracion;
    final Retroalimentacion efectos;
    final EstadisticasPartida estadisticas = new EstadisticasPartida();
    final Random azar = new Random();

    final JuegoService juego = new JuegoService();
    final CombateService combate = new CombateService();
    final ColisionService colision = new ColisionService();
    final JugadorService jugadorServicio = new JugadorService();
    final EnemigoIAService enemigoIA = new EnemigoIAService();
    final JefeService jefeServicio = new JefeService();
    final SalaService salaServicio = new SalaService();
    final ProgresionService progresion = new ProgresionService();
    final RecompensaService recompensas = new RecompensaService();
    final ObjetoSueloService objetosServicio = new ObjetoSueloService();
    final CartaService cartas = new CartaService();
    final MejorasService mejoras = new MejorasService();
    final InvocacionService invocacionServicio = new InvocacionService(mejoras);
    final MapaController mapa = new MapaController(progresion);

    final List<ProyectilVisual> proyectilesJugador = new ArrayList<>();
    final List<ProyectilVisual> proyectilesHostiles = new ArrayList<>();
    final List<EnemigoVisual> enemigos = new ArrayList<>();
    final List<ObjetoSueloVisual> objetosVisuales = new ArrayList<>();
    final List<InvocacionVisual> invocacionesVisuales = new ArrayList<>();

    JugadorVisual jugadorVisual;
    double x = 500;
    double y = 350;
    double xAnterior = 500;
    double yAnterior = 350;
    double direccionX;
    double direccionY;
    double invulnerable;

    boolean puertasAbiertas;
    boolean terminado;
    boolean pausado;
    boolean enTransicion;
    boolean cartaPendiente;
    boolean modoHardcore;
    boolean debeAlejarseAntesDeRecoger;
    ObjetoSuelo invocacionPendiente;
    /** Suelta las teclas pulsadas; se invoca al abrir una pantalla modal. */
    Runnable limpiarEntrada = () -> { };

    ContextoPartida(PartidaView vista, ConfiguracionPartida configuracion) {
        this.vista = vista;
        this.configuracion = configuracion;
        this.efectos = new Retroalimentacion(vista);
    }

    Jugador jugador() { return juego.getJugador(); }

    boolean partidaActiva() { return !terminado && juego.jugadorEstaVivo(); }

    /** Verdadero mientras la lógica del mundo no debe avanzar (menús o cambio de sala). */
    boolean detenido() { return pausado || enTransicion; }
}
