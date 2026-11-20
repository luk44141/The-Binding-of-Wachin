package com.wachin.controller;

import com.wachin.ConfiguracionPartida;
import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.EstadoPartida;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.ProyectilEquipable;
import com.wachin.service.PartidaService;
import com.wachin.service.SonidoService;
import com.wachin.view.game.PartidaView;
import com.wachin.view.ui.Pantallas;
import java.util.function.Consumer;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

/**
 * Orquesta una partida: crea o restaura el estado, ejecuta el ciclo de juego y delega cada
 * responsabilidad en su controlador (jugador, enemigos, invocaciones, objetos, combate, salas,
 * guardado y pausa).
 */
public final class PartidaController {

    /** Salidas de la partida hacia el resto de la aplicación. */
    public record Acciones(Consumer<ConfiguracionPartida> alReintentar, Consumer<EstadoPartida> alCargar,
            Runnable alMenu) {
        public Acciones {
            if (alReintentar == null || alCargar == null || alMenu == null) {
                throw new IllegalArgumentException("Todas las acciones de salida son obligatorias.");
            }
        }
    }

    private final PartidaView vista;
    private final ConfiguracionPartida configuracion;
    private final Acciones acciones;
    private final EstadoPartida estadoInicial;
    private final ContextoPartida ctx;
    private final JugadorController jugador;
    private final InvocacionesController invocaciones;
    private final ObjetosController objetos;
    private final CombateController combate;
    private final EnemigosController enemigos;
    private final SalaController salas;
    private final GuardadoController guardado;
    private final PausaController pausa;
    private AnimationTimer cicloDeJuego;
    private long ultimoTiempo;
    private double fpsTiempo;
    private int fpsCuadros;

    public PartidaController(PartidaView vista, ConfiguracionPartida configuracion, Acciones acciones,
            EstadoPartida estadoInicial) {
        this.vista = vista;
        this.configuracion = configuracion != null ? configuracion : new ConfiguracionPartida(null, null);
        this.acciones = acciones;
        this.estadoInicial = estadoInicial;
        this.ctx = new ContextoPartida(vista, this.configuracion);
        this.jugador = new JugadorController(ctx);
        this.invocaciones = new InvocacionesController(ctx);
        this.objetos = new ObjetosController(ctx, invocaciones);
        this.combate = new CombateController(ctx, objetos);
        this.enemigos = new EnemigosController(ctx, combate);
        this.salas = new SalaController(ctx, objetos, invocaciones);
        this.guardado = new GuardadoController(ctx, jugador, salas, objetos, invocaciones);
        this.pausa = new PausaController(ctx, guardado, new PartidaService(), this::cargarPartida, this::salirAlMenu);
        ctx.limpiarEntrada = jugador::limpiarTeclas;
    }

    // ───────────────────────────── ciclo de vida ─────────────────────────────

    public void iniciar() {
        if (estadoInicial == null) {
            crearJugador();
            salas.iniciar();
        } else {
            guardado.restaurar(estadoInicial);
        }
        actualizarHud();
        vista.actualizar(0, ctx.x, ctx.y);
        iniciarCiclo();
    }

    public void detener() {
        if (cicloDeJuego != null) cicloDeJuego.stop();
    }

    private void crearJugador() {
        try {
            Jugador nuevo = new Jugador(configuracion.getPersonaje().getNombre(), 100, 25, 100);
            nuevo.equiparProyectil(configuracion.getTipoProyectil() == ConfiguracionPartida.TipoProyectil.LAGRIMA
                    ? ProyectilEquipable.LAGRIMA : ProyectilEquipable.GOTA);
            ctx.juego.iniciarPartida(nuevo);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo crear al jugador.", e);
        }
        jugador.crearVisual();
    }

    private void cargarPartida(EstadoPartida estado) {
        detener();
        acciones.alCargar().accept(estado);
    }

    private void salirAlMenu() {
        detener();
        acciones.alMenu().run();
    }

    // ───────────────────────────── entrada ─────────────────────────────

    /** Registra la pulsación de una tecla; ESC alterna la pausa. */
    public void alPresionarTecla(KeyCode tecla) {
        if (tecla == KeyCode.F3) {
            vista.alternarFps();
            return;
        }
        if (tecla == KeyCode.ESCAPE) {
            if (ctx.terminado || ctx.cartaPendiente || ctx.invocacionPendiente != null || ctx.enTransicion) return;
            pausa.alternar();
            return;
        }
        if (ctx.pausado || !ctx.partidaActiva()) return;
        jugador.presionar(tecla);
    }

    public void alLiberarTecla(KeyCode tecla) {
        jugador.liberar(tecla);
    }

    // ───────────────────────────── ciclo de juego ─────────────────────────────

    private void iniciarCiclo() {
        cicloDeJuego = new AnimationTimer() {
            @Override public void handle(long ahora) {
                if (ctx.pausado) {
                    ultimoTiempo = 0;
                    return;
                }
                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }
                double transcurrido = (ahora - ultimoTiempo) / 1_000_000_000.0;
                double dt = Math.min(transcurrido, 0.05);
                ultimoTiempo = ahora;
                medirFps(transcurrido);
                avanzar(dt);
            }
        };
        cicloDeJuego.start();
    }

    /** Promedia los fotogramas reales cada medio segundo para el contador de FPS. */
    private void medirFps(double transcurrido) {
        fpsTiempo += transcurrido;
        fpsCuadros++;
        if (fpsTiempo >= 0.5) {
            vista.actualizarFps((int) Math.round(fpsCuadros / fpsTiempo));
            fpsTiempo = 0;
            fpsCuadros = 0;
        }
    }

    private void avanzar(double dt) {
        if (ctx.terminado || ctx.enTransicion) {
            vista.actualizar(dt, ctx.x, ctx.y);
            return;
        }
        ctx.estadisticas.sumarTiempo(dt);
        jugador.actualizar(dt);
        if (salas.detectarPuerta()) return;
        enemigos.actualizar(dt);
        invocaciones.actualizar(dt, combate);
        objetos.actualizar(dt);
        combate.actualizarProyectilesHostiles(dt);
        combate.actualizarProyectilesJugador(dt);
        verificarEstado();
        actualizarHud();
        vista.actualizar(dt, ctx.x, ctx.y);
    }

    private void verificarEstado() {
        if (!ctx.juego.jugadorEstaVivo()) {
            terminarPartida();
            return;
        }
        if (salas.comprobarSala() == SalaController.Resultado.VICTORIA) mostrarVictoria();
    }

    private void terminarPartida() {
        ctx.terminado = true;
        jugador.limpiarTeclas();
        ctx.vista.efectos().explosion(ctx.x, ctx.y, Retroalimentacion.ROJO, 2.2);
        ctx.vista.sacudir(14);
        ctx.efectos.sonar(SonidoService.Efecto.DERROTA);
        ctx.jugadorVisual.getVisual().setVisible(false);
        PauseTransition espera = new PauseTransition(Duration.seconds(1.1));
        espera.setOnFinished(e -> {
            detener();
            vista.mostrarPantalla(Pantallas.derrota(ctx.estadisticas, ctx.mapa.getNumeroSala(),
                    ctx.jugador().getNivel(), () -> acciones.alReintentar().accept(configuracion), this::salirAlMenu));
        });
        espera.play();
    }

    private void mostrarVictoria() {
        ctx.pausado = true;
        jugador.limpiarTeclas();
        ctx.efectos.sonar(SonidoService.Efecto.VICTORIA);
        vista.mostrarPantalla(Pantallas.victoria(ctx.estadisticas, ctx.mapa.getNumeroSala(), ctx.jugador().getNivel(),
                () -> { ctx.modoHardcore = true; ctx.pausado = false; vista.ocultarPantalla();
                    vista.mostrarAviso("MODO HARDCORE ACTIVADO: cualquier golpe te elimina",
                            com.wachin.view.game.HudView.Tono.MALO); }, this::salirAlMenu));
    }

    private void actualizarHud() {
        if (ctx.mapa.getActual() == null) return;
        MapaController.SalaNodo nodo = ctx.mapa.getActual();
        Jugador actual = ctx.jugador();
        vista.actualizarHUD(actual, ctx.enemigos.size(), nodo.getMapa(), ctx.mapa.getNumeroSala(), nodo.getProfundidad(),
                nodo.esJefe(), ctx.jugadorServicio.experienciaNecesaria(actual.getNivel()),
                ctx.juego.getInvocacionesActivas(), ctx.cartas.vista());
        Jefe jefe = ctx.juego.getJefe();
        if (jefe != null) vista.mostrarBarraJefe(jefe);
        else vista.ocultarBarraJefe();
    }
}
