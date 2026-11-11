
package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import com.wachin.model.ObjetoSuelo;
import com.wachin.model.ProyectilEquipable;
import com.wachin.model.TipoObjetoSuelo;
import com.wachin.model.CartaTipo;
import com.wachin.model.Invocacion;
import com.wachin.model.TipoInvocacion;
import com.wachin.model.EstadoPartida;
import com.wachin.model.Proyectil;
import com.wachin.service.CombateService;
import com.wachin.service.ColisionService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.service.EnemigoIAService;
import com.wachin.service.JefeService;
import com.wachin.service.ProgresionService;
import com.wachin.service.ObjetoSueloService;
import com.wachin.service.RecompensaService;
import com.wachin.service.CartaService;
import com.wachin.service.MejorasService;
import com.wachin.service.InvocacionService;
import com.wachin.service.SalaService;
import com.wachin.service.PartidaService;
import com.wachin.view.game.AssetVisual;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.PartidaView;
import com.wachin.view.game.ProyectilVisual;
import com.wachin.view.game.ObjetoSueloVisual;
import com.wachin.view.game.InvocacionVisual;
import com.wachin.view.game.PuertaVisual;
import com.wachin.ConfiguracionPartida;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Coordina entrada, actualización del juego, combate y comunicación entre modelo y vista.
 */
public class PartidaController {

    /**
     * Constante que define limite izquierdo para este componente.
     */
    private static final double LIMITE_IZQUIERDO = 75;
    /**
     * Constante que define limite derecho para este componente.
     */
    private static final double LIMITE_DERECHO = 925;
    /**
     * Constante que define limite superior para este componente.
     */
    private static final double LIMITE_SUPERIOR = 75;
    /**
     * Constante que define limite inferior para este componente.
     */
    private static final double LIMITE_INFERIOR = 625;

    /**
     * Constante que define velocidad jugador para este componente.
     */
    private static final double VELOCIDAD_JUGADOR = 225;
    /**
     * Constante que define velocidad proyectil para este componente.
     */
    private static final double VELOCIDAD_PROYECTIL = 430;
    /**
     * Constante que define tiempo invulnerable para este componente.
     */
    private static final double TIEMPO_INVULNERABLE = 1.15;
    private static final double TAMANO_JUGADOR = 64;

    /**
     * Guarda vista para que la clase mantenga su estado interno.
     */
    private final PartidaView vista;
    /**
     * Guarda configuracion para que la clase mantenga su estado interno.
     */
    private final ConfiguracionPartida configuracion;
    /**
     * Constante que define alperder para este componente.
     */
    private final Consumer<ConfiguracionPartida> alPerder;
    private final Consumer<EstadoPartida> alCargar;
    private final EstadoPartida estadoInicial;
    private final Runnable alGanar;

    private final Set<KeyCode> teclasPresionadas = new HashSet<>();
    private final List<ProyectilVisual> proyectiles = new ArrayList<>();
    private final List<ProyectilVisual> proyectilesHostiles = new ArrayList<>();
    private final List<EnemigoVisual> enemigosVisuales = new ArrayList<>();
    private final List<ObjetoSueloVisual> objetosVisualesSala = new ArrayList<>();
    private final List<InvocacionVisual> invocacionesVisuales = new ArrayList<>();

    private final JuegoService juegoService = new JuegoService();
    private final CombateService combateService = new CombateService();
    private final ColisionService colisionService = new ColisionService();
    private final JugadorService jugadorService = new JugadorService();
    private final EnemigoIAService enemigoIAService = new EnemigoIAService();
    private final JefeService jefeService = new JefeService();
    private final SalaService salaService = new SalaService();
    private final ProgresionService progresionService = new ProgresionService();
    private final RecompensaService recompensaService = new RecompensaService();
    private final ObjetoSueloService objetoSueloService = new ObjetoSueloService();
    private final CartaService cartaService = new CartaService();
    private final MejorasService mejorasService = new MejorasService();
    private final InvocacionService invocacionService = new InvocacionService(mejorasService);
    private final PartidaService partidaService = new PartidaService();
    private final MapaController mapaController = new MapaController(progresionService);
    private final Random random = new Random();

    /**
     * Constante que define jugadorvisual para este componente.
     */
    private Node jugadorVisual;

    /**
     * Constante que define jugadorx para este componente.
     */
    private double jugadorX = 500;
    /**
     * Constante que define jugadory para este componente.
     */
    private double jugadorY = 350;
    /**
     * Constante que define jugadorxanterior para este componente.
     */
    private double jugadorXAnterior = 500;
    /**
     * Constante que define jugadoryanterior para este componente.
     */
    private double jugadorYAnterior = 350;
    /**
     * Constante que define tiempoinvulnerable para este componente.
     */
    private double tiempoInvulnerable;

    /**
     * Constante que define ultimotiempo para este componente.
     */
    private long ultimoTiempo;
    /**
     * Constante que define gameloop para este componente.
     */
    private AnimationTimer gameLoop;

    /**
     * Constante que define puertasabiertas para este componente.
     */
    private boolean puertasAbiertas;
    /**
     * Constante que define juegoterminado para este componente.
     */
    private boolean juegoTerminado;
    /**
     * Guarda pausado para que la clase mantenga su estado interno.
     */
    private boolean pausado;
    private boolean recompensaCartaPendiente;
    private boolean debeAlejarseAntesDeRecoger;
    private ObjetoSuelo objetoInvocacionPendiente;

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public PartidaController(
        PartidaView vista,
        ConfiguracionPartida configuracion,
        Consumer<ConfiguracionPartida> alPerder) {
        this(vista, configuracion, alPerder, estado -> { }, null, () -> { });
    }

    public PartidaController(PartidaView vista, ConfiguracionPartida configuracion,
            Consumer<ConfiguracionPartida> alPerder, Consumer<EstadoPartida> alCargar,
            EstadoPartida estadoInicial) {
        this(vista, configuracion, alPerder, alCargar, estadoInicial, () -> { });
    }

    public PartidaController(PartidaView vista, ConfiguracionPartida configuracion,
            Consumer<ConfiguracionPartida> alPerder, Consumer<EstadoPartida> alCargar,
            EstadoPartida estadoInicial, Runnable alGanar) {

        this.vista = vista;
        this.configuracion = configuracion != null
                ? configuracion : new ConfiguracionPartida(null, null);
        this.alPerder = alPerder;
        this.alCargar = alCargar == null ? estado -> { } : alCargar;
        this.estadoInicial = estadoInicial;
        this.alGanar = alGanar == null ? () -> { } : alGanar;
    }

    /**
     * Prepara el estado inicial y pone en marcha este componente.
     */
    public void iniciar() {
        if (estadoInicial == null) {
            crearJugador();
            prepararSala(mapaController.iniciar());
        } else {
            restaurarPartida(estadoInicial);
        }

        iniciarGameLoop();
    }

    private EstadoPartida crearEstadoPartida() {
        Jugador j = juegoService.getJugador();
        EstadoPartida.JugadorEstado jugador = new EstadoPartida.JugadorEstado(
                j.getNombre(), j.getVida(), j.getVidaMaxima(), j.getVidaMaximaInicial(),
                j.getDano(), j.getExperiencia(), j.getNivel(), j.getMonedas(),
                j.getEnergia(), j.getEnergiaMaxima(), jugadorX, jugadorY,
                j.getProyectilEquipado().name(), j.getVelocidad(),
                j.getBonoDanoProyectiles(), j.getBonoDanoInvocaciones(),
                j.tieneSegundoEspacioInvocacion());
        List<EstadoPartida.EnemigoEstado> enemigos = enemigosVisuales.stream().map(v -> {
            Enemigo e = v.getEnemigo();
            if (e instanceof Jefe jefe) return new EstadoPartida.EnemigoEstado(e.getNombre(), "JEFE",
                    e.getVida(), e.getVidaMaxima(), e.getDano(), jefe.getVelocidad(), v.getX(), v.getY(),
                    jefe.getTipoJefe().name(), jefe.getNivelJefe(), jefe.getAtaqueEspecial(), jefe.getDanoAdicional(),
                    jefe.getTiempoAtaque());
            return new EstadoPartida.EnemigoEstado(e.getNombre(), e.getTipoEnemigo(),
                    e.getVida(), e.getVidaMaxima(), e.getDano(), e.getVelocidad(), v.getX(), v.getY(),
                    null, 0, null, 0, e.getTiempoAtaque());
        }).toList();
        List<EstadoPartida.InvocacionEstado> invocaciones = juegoService.getInvocacionesActivas().stream()
                .map(i -> new EstadoPartida.InvocacionEstado(i.getTipoInvocacion().name(), i.getVida(),
                        i.getVidaMaxima(), i.getDano(), i.getEnergia(), i.getEnergiaMaxima(), i.getX(), i.getY(),
                        i.getTiempoAtaque(), i.getEstado().name(), i.tieneProteccionActiva())).toList();
        java.util.Map<String, Integer> cartas = new java.util.HashMap<>();
        cartaService.instantanea().forEach((tipo, cantidad) -> cartas.put(tipo.name(), cantidad));
        List<EstadoPartida.ObjetoEstado> objetos = objetoSueloService.obtenerTodos().stream()
                .map(o -> new EstadoPartida.ObjetoEstado(o.getId(), o.getTipo().name(), o.getCodigo(),
                        o.getSalaGlobal(), o.getX(), o.getY())).toList();
        List<EstadoPartida.ProyectilEstado> disparos = new ArrayList<>();
        for (ProyectilVisual p : proyectiles) disparos.add(guardarProyectil(p));
        for (ProyectilVisual p : proyectilesHostiles) disparos.add(guardarProyectil(p));
        Jefe jefeActual = juegoService.getJefe();
        List<String> salidas = mapaController.getActual().getDireccionesSalida().stream()
                .map(Enum::name).toList();
        String entrada = mapaController.getActual().getDireccionEntrada() == null ? null
                : mapaController.getActual().getDireccionEntrada().name();
        return new EstadoPartida(configuracion.getPersonaje().name(), jugador,
                mapaController.getNumeroSala(), mapaController.getActual().getMapa(),
                mapaController.getActual().getProfundidad(), salidas, entrada, puertasAbiertas,
                progresionService.getRecompensasEntregadas(), enemigos, invocaciones,
                cartas, objetos, disparos, jefeActual == null ? null : jefeService.guardarEstado(jefeActual));
    }

    private EstadoPartida.ProyectilEstado guardarProyectil(ProyectilVisual visual) {
        Proyectil p = visual.getModelo();
        return new EstadoPartida.ProyectilEstado(p.getX(), p.getY(), p.getVelocidadX(),
                p.getVelocidadY(), p.getDano(), visual.getRutaAsset(), p.getOrigen().name());
    }

    private void restaurarPartida(EstadoPartida estado) {
        limpiarSalaAnterior();
        EstadoPartida.JugadorEstado j = estado.jugador();
        try {
            Jugador jugador = new Jugador(0, j.nombre(), j.vida(), j.vidaMaxima(), j.dano(),
                    j.experiencia(), j.nivel(), j.monedas(), j.energia(), j.energiaMaxima());
            jugador.restaurarMejorasYEquipo(j.vidaInicial(), j.velocidad(), j.bonoDano(),
                    j.bonoDanoInvocaciones(), j.segundoEspacio(), ProyectilEquipable.valueOf(j.proyectil()));
            juegoService.iniciarPartida(jugador);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo restaurar al jugador.", e);
        }
        jugadorX = j.x(); jugadorY = j.y();
        jugadorXAnterior = jugadorX; jugadorYAnterior = jugadorY;
        progresionService.restaurarRecompensasEntregadas(estado.recompensasEntregadas());
        Set<PuertaVisual.Direccion> direcciones = estado.salidas().stream()
                .map(PuertaVisual.Direccion::valueOf).collect(java.util.stream.Collectors.toSet());
        PuertaVisual.Direccion entrada = estado.entrada() == null ? null
                : PuertaVisual.Direccion.valueOf(estado.entrada());
        MapaController.SalaNodo nodo = mapaController.restaurarSala(estado.salaGlobal(),
                estado.mapa(), estado.profundidad(), direcciones, entrada);
        puertasAbiertas = estado.puertasAbiertas();

        jugadorVisual = AssetVisual.crear(configuracion.getRutaPersonaje(), jugadorX, jugadorY,
                TAMANO_JUGADOR, TAMANO_JUGADOR, Color.BEIGE, Color.BLACK, 3);
        vista.agregarJugador(jugadorVisual);
        Sala salaVisual = salaService.generarSala(nodo.getId(), nodo.getProfundidad(),
                nodo.esJefe(), nodo.getMapa());
        vista.configurarSala(salaVisual, nodo.getMapa(), nodo.esJefe());
        vista.configurarSalidas(direcciones, entrada, puertasAbiertas);

        for (EstadoPartida.EnemigoEstado e : estado.enemigos()) {
            try {
                Enemigo modelo;
                if ("JEFE".equals(e.tipo())) {
                    modelo = new Jefe(0, e.nombre(), e.vida(), e.vidaMaxima(), e.dano(), e.velocidad(),
                            e.nivelJefe(), e.ataqueEspecial(), e.danoAdicional(), Jefe.Tipo.valueOf(e.tipoJefe()));
                } else {
                    modelo = new Enemigo(0, e.nombre(), e.vida(), e.vidaMaxima(), e.dano(), e.tipo(), e.velocidad());
                }
                modelo.restaurarTiempoAtaque(e.tiempoAtaque());
                juegoService.agregarEnemigo(modelo);
                EnemigoVisual visual = new EnemigoVisual(modelo, e.x(), e.y());
                enemigosVisuales.add(visual); vista.agregarEnemigo(visual);
                if (modelo instanceof Jefe jefe) jefeService.restaurarEstado(jefe, estado.estadoJefe());
            } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException ex) {
                throw new IllegalStateException("No se pudo restaurar un enemigo.", ex);
            }
        }
        for (int i = 0; i < estado.invocaciones().size(); i++) {
            EstadoPartida.InvocacionEstado iEstado = estado.invocaciones().get(i);
            Invocacion invocacion = invocacionService.crear(TipoInvocacion.valueOf(iEstado.tipo()));
            invocacion.recibirDano(invocacion.getVidaMaxima() - iEstado.vida());
            invocacion.restaurarEstado(iEstado.energia(), iEstado.x(), iEstado.y(), iEstado.tiempoAtaque(),
                    Invocacion.Estado.valueOf(iEstado.estado()), iEstado.proteccionActiva());
            try { juegoService.equiparInvocacion(invocacion, i); }
            catch (com.wachin.exception.InvocacionNoDisponibleException e) {
                throw new IllegalStateException("No se pudo restaurar el equipamiento de invocaciones.", e);
            }
            agregarVisualInvocacion(invocacion);
        }
        reubicarInvocacionesCercaDelJugador();
        java.util.Map<CartaTipo, Integer> cartas = new java.util.EnumMap<>(CartaTipo.class);
        estado.cartas().forEach((tipo, cantidad) -> cartas.put(CartaTipo.valueOf(tipo), cantidad));
        cartaService.restaurar(cartas);
        List<ObjetoSuelo> objetos = estado.objetos().stream().map(o -> new ObjetoSuelo(o.id(),
                TipoObjetoSuelo.valueOf(o.tipo()), o.codigo(), o.sala(), o.x(), o.y())).toList();
        objetoSueloService.restaurar(objetos);
        for (ObjetoSuelo objeto : objetoSueloService.listarSala(estado.salaGlobal())) agregarObjetoSueloVisual(objeto);
        for (EstadoPartida.ProyectilEstado p : estado.proyectiles()) {
            Proyectil.Origen origen = Proyectil.Origen.valueOf(p.origen());
            boolean hostil = origen == Proyectil.Origen.JEFE || origen == Proyectil.Origen.ENEMIGO;
            Proyectil disparo = new Proyectil(p.x(), p.y(), p.velocidadX(), p.velocidadY(),
                    p.dano(), hostil, origen);
            ProyectilVisual visual = new ProyectilVisual(disparo, p.ruta());
            if (hostil) proyectilesHostiles.add(visual); else proyectiles.add(visual);
            vista.agregarProyectil(visual);
        }
        actualizarHUD();
    }

    /**
     * Detiene el ciclo de animación si se encuentra activo.
     */
    public void detener() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Ejecuta la operación teclaPresionada dentro de la responsabilidad de esta clase.
     */
    public boolean teclaPresionada(KeyCode tecla) {
        if (tecla == KeyCode.ESCAPE) return teclasPresionadas.add(tecla);
        if (pausado || !partidaActiva()) return false;
        return teclasPresionadas.add(tecla);
    }

    /**
     * Ejecuta la operación teclaLiberada dentro de la responsabilidad de esta clase.
     */
    public void teclaLiberada(KeyCode tecla) {
        teclasPresionadas.remove(tecla);
    }

    /**
     * Ejecuta la operación manejarTecla dentro de la responsabilidad de esta clase.
     */
    public void manejarTecla(KeyCode tecla) {
        if (tecla == KeyCode.ESCAPE) {
            if (recompensaCartaPendiente || objetoInvocacionPendiente != null) return;
            if (!juegoTerminado) {
                alternarPausa();
            }
            return;
        }

        if (!partidaActiva() || pausado) {
            return;
        }

        switch (tecla) {
            case UP:
                disparar(0, -1);
                break;
            case DOWN:
                disparar(0, 1);
                break;
            case LEFT:
                disparar(-1, 0);
                break;
            case RIGHT:
                disparar(1, 0);
                break;
            default:
                break;
        }
    }

    /**
     * Ejecuta la operación alternarPausa dentro de la responsabilidad de esta clase.
     */
    private void alternarPausa() {
        if (pausado) {
            reanudar();
            return;
        }

        pausado = true;
        teclasPresionadas.clear();
        vista.mostrarPausa(this::manejarAccionPausa);
    }

    private void manejarAccionPausa(PartidaView.AccionPausa accion) {
        switch (accion) {
            case REANUDAR -> reanudar();
            case CARTAS -> {
                List<String> filas = cartaService.obtenerCartas().stream()
                        .map(c -> c.getNombre() + " x" + c.getAcumulaciones() + " — " + c.getDescripcion())
                        .toList();
                vista.mostrarConsultaPausa("CARTAS OBTENIDAS", filas,
                        () -> vista.mostrarPausa(this::manejarAccionPausa));
            }
            case INVOCACIONES -> {
                List<String> filas = juegoService.getInvocacionesActivas().stream()
                        .flatMap(i -> {
                            String ataque = switch (i.getTipoInvocacion()) {
                                case GUSANO -> "PROTECCIÓN + CUERPO A CUERPO";
                                case MURCIELAGO -> "CUERPO A CUERPO";
                                case ROBOT -> "DISPARO LÁSER";
                            };
                            String costoEnergia = i.getTipoInvocacion() == TipoInvocacion.GUSANO
                                    ? "ATAQUE " + i.getCostoEnergia() + " | PROTECCIÓN "
                                            + i.getTipoInvocacion().getCostoProteccion()
                                    : "ATAQUE " + i.getCostoEnergia();
                            String duracion = i.getDuracion() == 0 ? "PERMANENTE" : i.getDuracion() + " s";
                            return java.util.stream.Stream.of(
                                    i.getTipoInvocacion().getNombre() + " | VIDA " + i.getVida() + "/" + i.getVidaMaxima()
                                            + " | DAÑO " + i.getDano() + " | VELOCIDAD " + i.getVelocidad()
                                            + " | RANGO " + i.getRangoDeteccion(),
                                    "ATAQUE: " + ataque + " | COSTE " + costoEnergia
                                            + " | ENERGÍA " + (int) i.getEnergia() + "/" + (int) i.getEnergiaMaxima()
                                            + " | RECUPERACIÓN " + i.getRecuperacionEnergia() + "/s",
                                    "DURACIÓN: " + duracion + " | ESTADO: " + i.getEstado());
                        }).toList();
                vista.mostrarConsultaPausa("INVOCACIONES EQUIPADAS", filas,
                        () -> vista.mostrarPausa(this::manejarAccionPausa));
            }
            case GUARDAR -> mostrarSlots(true);
            case CARGAR -> mostrarSlots(false);
        }
    }

    private void mostrarSlots(boolean guardar) {
        try {
            List<PartidaView.SlotView> slots = partidaService.listarSlots().stream()
                    .map(s -> new PartidaView.SlotView(s.numero(), s.estaOcupado(),
                            s.estaOcupado() ? s.nombre() + " — " + s.fechaGuardado() : "VACÍO"))
                    .toList();
            vista.mostrarSlots(slots, guardar,
                    guardar ? this::guardarEnSlot : this::cargarDesdeSlot,
                    this::eliminarSlot,
                    () -> vista.mostrarPausa(this::manejarAccionPausa));
        } catch (RuntimeException e) {
            mostrarErrorPausa("No se pudieron consultar los espacios: " + e.getMessage());
        }
    }

    private void guardarEnSlot(int slot) {
        try {
            partidaService.guardar(slot, "Partida sala " + mapaController.getNumeroSala(), crearEstadoPartida());
            vista.mostrarConsultaPausa("PARTIDA GUARDADA", List.of("Espacio " + slot),
                    () -> vista.mostrarPausa(this::manejarAccionPausa));
        } catch (RuntimeException e) { mostrarErrorPausa("No se pudo guardar: " + e.getMessage()); }
    }

    private void cargarDesdeSlot(int slot) {
        try {
            EstadoPartida estado = partidaService.cargar(slot).orElse(null);
            if (estado == null) { mostrarErrorPausa("El espacio " + slot + " está vacío."); return; }
            alCargar.accept(estado);
        } catch (RuntimeException e) { mostrarErrorPausa("No se pudo cargar: " + e.getMessage()); }
    }

    private void eliminarSlot(int slot) {
        try { partidaService.eliminar(slot); mostrarSlots(false); }
        catch (RuntimeException e) { mostrarErrorPausa("No se pudo eliminar: " + e.getMessage()); }
    }

    private void mostrarErrorPausa(String mensaje) {
        vista.mostrarConsultaPausa("OPERACIÓN NO COMPLETADA", List.of(mensaje),
                () -> vista.mostrarPausa(this::manejarAccionPausa));
    }

    /**
     * Ejecuta la operación reanudar dentro de la responsabilidad de esta clase.
     */
    private void reanudar() {
        pausado = false;
        teclasPresionadas.clear();
        vista.ocultarPausa();
        ultimoTiempo = 0;
    }

    /**
     * Crea el modelo del jugador, inicia JuegoService y agrega su representación visual.
     */
    private void crearJugador() {
        try {
            String nombre = configuracion.getPersonaje().getNombre();
            String ruta = configuracion.getRutaPersonaje();

            Jugador jugador = new Jugador(nombre, 100, 25, 100);
            jugador.equiparProyectil(configuracion.getTipoProyectil() == ConfiguracionPartida.TipoProyectil.LAGRIMA
                    ? ProyectilEquipable.LAGRIMA : ProyectilEquipable.GOTA);
            juegoService.iniciarPartida(jugador);

            jugadorVisual = AssetVisual.crear(
                    ruta,
                    jugadorX,
                    jugadorY,
                    TAMANO_JUGADOR,
                    TAMANO_JUGADOR,
                    Color.BEIGE,
                    Color.BLACK,
                    3
            );

            vista.agregarJugador(jugadorVisual);

        } catch (
                NombreInvalidoException |
                VidaInvalidaException |
                DanoInvalidoException e) {

            throw new IllegalStateException(
                    "No se pudo crear al jugador.",
                    e
            );
        }
    }

    /**
     * Genera los enemigos del nodo, configura la vista de sala y refresca el HUD.
     */
    private void prepararSala(MapaController.SalaNodo nodo) {
        limpiarSalaAnterior();
        if (nodo == null) return;
        puertasAbiertas = false;

        Sala sala = salaService.generarSala(
                nodo.getNumeroGlobal(),
                nodo.getProfundidad(),
                nodo.esJefe(),
                nodo.getMapa()
        );

        vista.configurarSala(
                sala,
                nodo.getMapa(),
                nodo.esJefe()
        );

        vista.configurarSalidas(
                nodo.getDireccionesSalida(),
                nodo.getDireccionEntrada(),
                false
        );
        for (ObjetoSuelo objeto : objetoSueloService.listarSala(nodo.getNumeroGlobal())) {
            agregarObjetoSueloVisual(objeto);
        }

        if (nodo.esJefe()) {
            crearJefe();
        } else {
            for (Enemigo enemigo : sala.getEnemigos()) {
                juegoService.agregarEnemigo(enemigo);

                double[] posicion = generarPosicionEnemigo();

                EnemigoVisual visual = new EnemigoVisual(
                        enemigo,
                        posicion[0],
                        posicion[1]
                );

                enemigosVisuales.add(visual);
                vista.agregarEnemigo(visual);
            }
        }

        reubicarInvocacionesCercaDelJugador();
        actualizarHUD();
    }

    /**
     * Reubica las invocaciones dentro de la sala actual, cerca del jugador y separadas entre sí.
     */
    private void reubicarInvocacionesCercaDelJugador() {
        List<Invocacion> activas = juegoService.getInvocacionesActivas();
        for (int i = 0; i < activas.size(); i++) {
            double desplazamientoX = activas.size() > 1 ? (i == 0 ? -48 : 48) : -42;
            double x = Math.max(105, Math.min(895, jugadorX + desplazamientoX));
            double y = Math.max(105, Math.min(595, jugadorY + 48));
            activas.get(i).moverA(x, y);
        }
    }

    /**
     * Genera el jefe del mapa y agrega su modelo y su nodo visual a la partida.
     */
    private void crearJefe() {
        Jefe.Tipo tipo = ProgresionService.jefeDeSala(mapaController.getNumeroSala());
        if (tipo == null) throw new IllegalStateException("La sala actual no tiene un jefe de progresión asignado.");
        Jefe jefe = salaService.generarJefe(tipo);

        juegoService.agregarEnemigo(jefe);

        EnemigoVisual visual = new EnemigoVisual(
                jefe,
                500,
                270
        );

        enemigosVisuales.add(visual);
        vista.agregarEnemigo(visual);
    }

    /**
     * Retira enemigos y disparos de la escena y limpia sus colecciones.
     */
    private void limpiarSalaAnterior() {
        for (ObjetoSueloVisual objeto : objetosVisualesSala) vista.quitarObjetoSuelo(objeto);
        objetosVisualesSala.clear();
        for (EnemigoVisual enemigo : enemigosVisuales) {
            vista.quitarEnemigo(enemigo);
        }

        enemigosVisuales.clear();

        for (ProyectilVisual proyectil : proyectiles) {
            vista.quitarProyectil(proyectil);
        }

        proyectiles.clear();

        for (ProyectilVisual proyectil : proyectilesHostiles) vista.quitarProyectil(proyectil);
        proyectilesHostiles.clear();

        juegoService.getEnemigos().clear();
        teclasPresionadas.clear();
    }

    /**
     * Ubica al jugador junto a la puerta de entrada y sincroniza sus posiciones para colisiones.
     */
    private void colocarJugadorEnEntrada(
            PuertaVisual.Direccion salidaAnterior) {

        switch (salidaAnterior) {
            case ARRIBA:
                jugadorX = 500;
                jugadorY = 580;
                break;
            case ABAJO:
                jugadorX = 500;
                jugadorY = 100;
                break;
            case IZQUIERDA:
                jugadorX = 880;
                jugadorY = 350;
                break;
            case DERECHA:
                jugadorX = 120;
                jugadorY = 350;
                break;
            default:
                break;
        }

        AssetVisual.moverAlCentro(jugadorVisual, jugadorX, jugadorY,
                TAMANO_JUGADOR, TAMANO_JUGADOR);
        jugadorXAnterior = jugadorX;
        jugadorYAnterior = jugadorY;
    }

    /**
     * Elige una posición dentro de la sala, intentando mantener distancia del jugador.
     */
    private double[] generarPosicionEnemigo() {
        /**
         * Guarda x para que la clase mantenga su estado interno.
         */
        double x;
        /**
         * Guarda y para que la clase mantenga su estado interno.
         */
        double y;
        /**
         * Guarda intentos para que la clase mantenga su estado interno.
         */
        int intentos = 0;

        do {
            x = 140 + random.nextDouble() * 720;
            y = 130 + random.nextDouble() * 440;
            intentos++;
        } while (
                colisionService.distancia(x, y, jugadorX, jugadorY) < 125
                        && intentos < 100
        );

        return new double[]{x, y};
    }

    /**
     * Crea un proyectil con velocidad y orientación según la dirección y lo agrega a la vista.
     */
    private void disparar(double direccionX, double direccionY) {
        Jugador jugador = juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        ProyectilEquipable equipado = jugador.getProyectilEquipado();
        Proyectil modelo = new Proyectil(jugadorX, jugadorY,
                direccionX * VELOCIDAD_PROYECTIL,
                direccionY * VELOCIDAD_PROYECTIL,
                mejorasService.calcularDanoProyectil(jugador, equipado.getDano()),
                false, Proyectil.Origen.JUGADOR);
        ProyectilVisual proyectil = new ProyectilVisual(modelo, equipado.getRutaAsset());

        proyectiles.add(proyectil);
        vista.agregarProyectil(proyectil);
    }

    /** Equipo de proyectiles: cada intercambio deja el anterior en el suelo de esta sala. */
    private void actualizarObjetosSuelo() {
        int sala = mapaController.getNumeroSala();
        Jugador jugador = juegoService.getJugador();
        List<ObjetoSuelo> cercanos = objetoSueloService.recogerCercanos(sala, jugadorX, jugadorY, 42);
        if (debeAlejarseAntesDeRecoger) {
            if (cercanos.isEmpty()) debeAlejarseAntesDeRecoger = false;
            return;
        }
        ObjetoSuelo objeto = cercanos.stream()
                .filter(o -> o.getTipo() == TipoObjetoSuelo.PROYECTIL
                        || o.getTipo() == TipoObjetoSuelo.CARTA || o.getTipo() == TipoObjetoSuelo.INVOCACION
                        || o.getTipo() == TipoObjetoSuelo.RECOMPENSA)
                .filter(o -> o.getTipo() != TipoObjetoSuelo.CARTA || cartaDisponible(o))
                .filter(o -> o.getTipo() != TipoObjetoSuelo.INVOCACION || invocacionPermitida(o))
                .min((a, b) -> Double.compare(
                        colisionService.distancia(jugadorX, jugadorY, a.getX(), a.getY()),
                        colisionService.distancia(jugadorX, jugadorY, b.getX(), b.getY())))
                .orElse(null);
        if (objeto == null) return;
        if (objeto.getTipo() == TipoObjetoSuelo.PROYECTIL) {
            ProyectilEquipable nuevo;
            try { nuevo = ProyectilEquipable.desdeCodigo(objeto.getCodigo()); }
            catch (IllegalArgumentException e) { return; }
            ProyectilEquipable anterior = jugador.getProyectilEquipado();
            jugador.equiparProyectil(nuevo);
            objetoSueloService.retirar(objeto.getId());
            retirarObjetoVisual(objeto);
            if (anterior != nuevo) {
                ObjetoSuelo reemplazado = objetoSueloService.crear(TipoObjetoSuelo.PROYECTIL,
                        anterior.name(), sala, objeto.getX(), objeto.getY());
                agregarObjetoSueloVisual(reemplazado);
            }
            vista.actualizarEstado("PROYECTIL EQUIPADO: " + nuevo.getNombre()
                    + " (DAÑO " + nuevo.getDano() + ")");
        } else if (objeto.getTipo() == TipoObjetoSuelo.CARTA) {
            if ("SELECCION_CARTAS".equals(objeto.getCodigo())) {
                objetoSueloService.retirar(objeto.getId());
                retirarObjetoVisual(objeto);
                debeAlejarseAntesDeRecoger = true;
                recompensaCartaPendiente = true;
                pausado = true;
                vista.mostrarSeleccionCartas(cartaService.elegirTresCartasDiferentes(),
                        this::completarSeleccionCarta);
                vista.actualizarEstado("ELEGÍ UNA DE TRES CARTAS");
                return;
            }
            CartaTipo carta;
            try { carta = CartaTipo.valueOf(objeto.getCodigo()); }
            catch (IllegalArgumentException e) { return; }
            if (!cartaService.registrar(carta)) return;
            mejorasService.aplicarCarta(juegoService.getJugador(), carta);
            objetoSueloService.retirar(objeto.getId());
            retirarObjetoVisual(objeto);
            vista.actualizarEstado("CARTA OBTENIDA: " + carta.getNombre());
        } else if (objeto.getTipo() == TipoObjetoSuelo.INVOCACION) {
            recogerInvocacion(objeto);
            if (objetoInvocacionPendiente != null) return;
        } else if (recompensaService.aplicarObjetoSuelo(jugador, objeto.getCodigo())) {
            objetoSueloService.retirar(objeto.getId());
            retirarObjetoVisual(objeto);
            vista.actualizarEstado("RECOMPENSA OBTENIDA: VIDA MÁXIMA +25% Y VIDA RECUPERADA");
        }
        debeAlejarseAntesDeRecoger = true;
    }

    private boolean invocacionPermitida(ObjetoSuelo objeto) {
        try {
            TipoInvocacion tipo = TipoInvocacion.valueOf(objeto.getCodigo());
            List<Invocacion> equipadas = juegoService.getInvocacionesActivas();
            return juegoService.getJugador().tieneSegundoEspacioInvocacion()
                    || equipadas.stream().noneMatch(i -> i.getTipoInvocacion() == tipo);
        } catch (IllegalArgumentException e) { return false; }
    }

    private void recogerInvocacion(ObjetoSuelo objeto) {
        int limite = juegoService.getJugador().tieneSegundoEspacioInvocacion() ? 2 : 1;
        int cantidad = juegoService.getInvocacionesActivas().size();
        if (cantidad >= limite && limite == 2) {
            objetoInvocacionPendiente = objeto;
            pausado = true;
            vista.mostrarReemplazoInvocacion(juegoService.getInvocacionesActivas(),
                    this::completarReemplazoInvocacion);
            return;
        }
        int espacio = cantidad < limite ? cantidad : 0;
        equiparInvocacionDesdeSuelo(objeto, espacio);
    }

    private void completarReemplazoInvocacion(int espacio) {
        if (objetoInvocacionPendiente == null) return;
        equiparInvocacionDesdeSuelo(objetoInvocacionPendiente, espacio);
        objetoInvocacionPendiente = null;
        pausado = false;
        vista.ocultarReemplazoInvocacion();
        ultimoTiempo = 0;
    }

    private void equiparInvocacionDesdeSuelo(ObjetoSuelo objeto, int espacio) {
        TipoInvocacion tipo;
        try { tipo = TipoInvocacion.valueOf(objeto.getCodigo()); }
        catch (IllegalArgumentException e) { return; }
        Invocacion nueva = invocacionService.crear(tipo);
        nueva.moverA(jugadorX - 45, jugadorY + 35);
        try {
            Invocacion anterior = juegoService.equiparInvocacion(nueva, espacio);
            if (anterior != null) {
                quitarVisualInvocacion(anterior);
                ObjetoSuelo reemplazada = objetoSueloService.crear(TipoObjetoSuelo.INVOCACION,
                        anterior.getTipoInvocacion().name(), objeto.getSalaGlobal(), objeto.getX(), objeto.getY());
                agregarObjetoSueloVisual(reemplazada);
            }
        } catch (com.wachin.exception.InvocacionNoDisponibleException e) {
            return;
        }
        objetoSueloService.retirar(objeto.getId());
        retirarObjetoVisual(objeto);
        agregarVisualInvocacion(nueva);
        vista.actualizarEstado("INVOCACIÓN EQUIPADA: " + tipo.getNombre());
    }

    private void agregarVisualInvocacion(Invocacion invocacion) {
        InvocacionVisual visual = new InvocacionVisual(invocacion);
        invocacionesVisuales.add(visual);
        vista.agregarInvocacion(visual);
    }

    private void quitarVisualInvocacion(Invocacion invocacion) {
        invocacionesVisuales.stream().filter(v -> v.getInvocacion() == invocacion).findFirst()
                .ifPresent(v -> { vista.quitarInvocacion(v); invocacionesVisuales.remove(v); });
    }

    private boolean cartaDisponible(ObjetoSuelo objeto) {
        if ("SELECCION_CARTAS".equals(objeto.getCodigo())) return true;
        try { return cartaService.puedeObtener(CartaTipo.valueOf(objeto.getCodigo())); }
        catch (IllegalArgumentException e) { return false; }
    }

    private void retirarObjetoVisual(ObjetoSuelo objeto) {
        objetosVisualesSala.stream().filter(v -> v.getObjeto().getId() == objeto.getId())
                .findFirst().ifPresent(v -> {
                    vista.quitarObjetoSuelo(v);
                    objetosVisualesSala.remove(v);
                });
    }

    /**
     * Crea el ciclo JavaFX que actualiza tiempo, movimiento, colisiones y estado.
     */
    private void iniciarGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long ahora) {
                if (pausado || juegoTerminado) {
                    ultimoTiempo = 0;
                    return;
                }

                if (ultimoTiempo == 0) {
                    ultimoTiempo = ahora;
                    return;
                }

                double deltaTiempo =
                        (ahora - ultimoTiempo) / 1_000_000_000.0;

                ultimoTiempo = ahora;
                deltaTiempo = Math.min(deltaTiempo, 0.05);

                if (tiempoInvulnerable > 0) {
                    tiempoInvulnerable = Math.max(
                            0,
                            tiempoInvulnerable - deltaTiempo
                    );
                }
                vista.actualizarIndicadorDanio(tiempoInvulnerable > 0);

                actualizarJugador(deltaTiempo);

                if (detectarPuerta()) {
                    return;
                }

                actualizarEnemigos(deltaTiempo);
                actualizarInvocaciones(deltaTiempo);
                actualizarObjetosSuelo();
                actualizarProyectilesHostiles(deltaTiempo);
                actualizarProyectiles(deltaTiempo);
                verificarEstado();
                actualizarHUD();
            }
        };

        gameLoop.start();
    }

    /**
     * Ejecuta la operación partidaActiva dentro de la responsabilidad de esta clase.
     */
    private boolean partidaActiva() {
        return !juegoTerminado && juegoService.jugadorEstaVivo();
    }

    /**
     * Lee las teclas mantenidas, normaliza el movimiento, limita al jugador y mueve su sprite.
     */
    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        jugadorXAnterior = jugadorX;
        jugadorYAnterior = jugadorY;
        /**
         * Constante que define direccionx para este componente.
         */
        double direccionX = 0;
        /**
         * Constante que define direcciony para este componente.
         */
        double direccionY = 0;

        if (teclasPresionadas.contains(KeyCode.W)) direccionY--;
        if (teclasPresionadas.contains(KeyCode.S)) direccionY++;
        if (teclasPresionadas.contains(KeyCode.A)) direccionX--;
        if (teclasPresionadas.contains(KeyCode.D)) direccionX++;

        double longitud = Math.sqrt(
                direccionX * direccionX + direccionY * direccionY
        );

        if (longitud > 0) {
            direccionX /= longitud;
            direccionY /= longitud;

            double velocidad = juegoService.getJugador().getVelocidad();
            double deseadoX = jugadorX + direccionX * velocidad * deltaTiempo;
            double deseadoY = jugadorY + direccionY * velocidad * deltaTiempo;
            ColisionService.Posicion posicion = colisionService.resolverMovimiento(
                    jugadorX, jugadorY, deseadoX, deseadoY, 27,
                    obstaculosParaJugador(), LIMITE_IZQUIERDO, LIMITE_DERECHO,
                    LIMITE_SUPERIOR, LIMITE_INFERIOR);
            jugadorX = posicion.x();
            jugadorY = posicion.y();
        }

        limitarJugador();

        AssetVisual.moverAlCentro(jugadorVisual, jugadorX, jugadorY,
                TAMANO_JUGADOR, TAMANO_JUGADOR);
    }

    /**
     * Mantiene las coordenadas del jugador dentro del área jugable.
     */
    private void limitarJugador() {
        jugadorX = Math.max(
                LIMITE_IZQUIERDO,
                Math.min(LIMITE_DERECHO, jugadorX)
        );

        jugadorY = Math.max(
                LIMITE_SUPERIOR,
                Math.min(LIMITE_INFERIOR, jugadorY)
        );
    }

    /**
     * Comprueba si el jugador alcanza una salida abierta y solicita el siguiente nodo al mapa.
     */
    private boolean detectarPuerta() {
        if (!puertasAbiertas || !partidaActiva()) {
            return false;
        }

        /**
         * Guarda direccion para que la clase mantenga su estado interno.
         */
        PuertaVisual.Direccion direccion = null;

        boolean centroX = Math.abs(jugadorX - 500) <= 55;
        boolean centroY = Math.abs(jugadorY - 350) <= 55;

        if (jugadorY <= 80 && centroX) {
            direccion = PuertaVisual.Direccion.ARRIBA;
        } else if (jugadorY >= 620 && centroX) {
            direccion = PuertaVisual.Direccion.ABAJO;
        } else if (jugadorX <= 80 && centroY) {
            direccion = PuertaVisual.Direccion.IZQUIERDA;
        } else if (jugadorX >= 920 && centroY) {
            direccion = PuertaVisual.Direccion.DERECHA;
        }

        if (direccion == null) {
            return false;
        }

        MapaController.SalaNodo destino = mapaController.entrarPor(direccion);
        if (destino == null) return false;
        colocarJugadorEnEntrada(direccion);
        prepararSala(destino);
        return true;
    }

    /**
     * Actualiza los enemigos y aplica daño al detectar contacto durante sus recorridos.
     */
    private void actualizarEnemigos(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        for (EnemigoVisual enemigoVisual : enemigosVisuales) {
            double deseadoX;
            double deseadoY;
            if (enemigoVisual.getEnemigo() instanceof Jefe) {
                Jefe jefe = (Jefe) enemigoVisual.getEnemigo();
                JefeService.Actualizacion actualizacion = jefeService.actualizar(jefe,
                        enemigoVisual.getX(), enemigoVisual.getY(), jugadorX, jugadorY, deltaTiempo);
                deseadoX = actualizacion.x();
                deseadoY = actualizacion.y();
                List<double[]> zonas = actualizacion.zonas().stream()
                        .map(p -> new double[]{p.x(), p.y()}).toList();
                enemigoVisual.mostrarAvisoJefe(actualizacion.aviso().name(),
                        actualizacion.direccionX(), actualizacion.direccionY(), zonas);
                for (JefeService.Disparo disparo : actualizacion.disparos()) {
                    Proyectil proyectil = new Proyectil(disparo.x(), disparo.y(),
                            disparo.vx(), disparo.vy(), disparo.dano(), true, Proyectil.Origen.JEFE);
                    ProyectilVisual visual = new ProyectilVisual(proyectil,
                            "assets/images/projectiles/enemies/boss.png");
                    proyectilesHostiles.add(visual);
                    vista.agregarProyectil(visual);
                }
                if (actualizacion.zonasActivas() && tiempoInvulnerable <= 0
                        && actualizacion.zonas().stream().anyMatch(p ->
                                colisionService.distancia(jugadorX, jugadorY, p.x(), p.y()) < 73)) {
                    recibirDanoJugador(jefe.getDano() + jefe.getDanoAdicional());
                    tiempoInvulnerable = TIEMPO_INVULNERABLE;
                }
            } else {
                EnemigoIAService.Actualizacion actualizacion = enemigoIAService.actualizar(
                        enemigoVisual.getEnemigo(), enemigoVisual.getX(), enemigoVisual.getY(),
                        jugadorX, jugadorY, deltaTiempo);
                deseadoX = actualizacion.x();
                deseadoY = actualizacion.y();
                for (EnemigoIAService.Disparo disparo : actualizacion.disparos()) {
                    Proyectil proyectil = new Proyectil(disparo.x(), disparo.y(),
                            disparo.velocidadX(), disparo.velocidadY(), disparo.dano(),
                            true, Proyectil.Origen.ENEMIGO);
                    ProyectilVisual visual = new ProyectilVisual(proyectil,
                            "assets/images/projectiles/enemies/tirador.png");
                    proyectilesHostiles.add(visual);
                    vista.agregarProyectil(visual);
                }
            }

            ColisionService.Posicion posicionResuelta = colisionService.resolverMovimiento(
                    enemigoVisual.getX(), enemigoVisual.getY(), deseadoX, deseadoY,
                    enemigoVisual.getRadioColision(), obstaculosParaEnemigo(enemigoVisual),
                    75, 925, 75, 625);
            enemigoVisual.actualizarPosicion(posicionResuelta.x(), posicionResuelta.y());

            if (tiempoInvulnerable <= 0
                    && colisionService.colisionaJugadorEnemigo(jugadorXAnterior,
                            jugadorYAnterior, jugadorX, jugadorY,
                            enemigoVisual.getXAnterior(), enemigoVisual.getYAnterior(),
                            enemigoVisual.getX(), enemigoVisual.getY(), 27,
                            enemigoVisual.getRadioColision())) {

                int danoContacto = enemigoVisual.getEnemigo() instanceof Jefe
                        ? jefeService.obtenerDanoContacto((Jefe) enemigoVisual.getEnemigo())
                        : enemigoVisual.getDanoContacto();
                recibirDanoJugador(danoContacto);
                tiempoInvulnerable = TIEMPO_INVULNERABLE;
                break;
            }
        }
    }

    /** Sigue, protege y ejecuta los ataques automáticos de las invocaciones equipadas. */
    private void actualizarInvocaciones(double deltaTiempo) {
        List<InvocacionService.Objetivo> objetivos = enemigosVisuales.stream()
                .map(v -> new InvocacionService.Objetivo(v.getEnemigo(), v.getX(), v.getY())).toList();
        for (Invocacion invocacion : new ArrayList<>(juegoService.getInvocacionesActivas())) {
            if (invocacion.getVida() <= 0) {
                juegoService.eliminarInvocacion(invocacion);
                quitarVisualInvocacion(invocacion);
                continue;
            }
            double anteriorX = invocacion.getX();
            double anteriorY = invocacion.getY();
            InvocacionService.Actualizacion estado = invocacionService.actualizar(invocacion,
                    juegoService.getJugador(), jugadorX, jugadorY, objetivos, deltaTiempo);
            ColisionService.Posicion posicion = colisionService.resolverMovimiento(
                    anteriorX, anteriorY, invocacion.getX(), invocacion.getY(), 20,
                    obstaculosParaInvocacion(invocacion), 75, 925, 75, 625);
            invocacion.moverA(posicion.x(), posicion.y());
            invocacionesVisuales.stream().filter(v -> v.getInvocacion() == invocacion)
                    .findFirst().ifPresent(InvocacionVisual::actualizar);
            for (InvocacionService.Ataque ataque : estado.ataques()) {
                EnemigoVisual objetivo = enemigosVisuales.stream()
                        .filter(v -> v.getEnemigo() == ataque.objetivo()).findFirst().orElse(null);
                if (objetivo == null) continue;
                if (ataque.laser()) {
                    Proyectil modelo = new Proyectil(ataque.origenX(), ataque.origenY(),
                            ataque.velocidadX(), ataque.velocidadY(), ataque.dano(),
                            false, Proyectil.Origen.INVOCACION);
                    ProyectilVisual laser = new ProyectilVisual(modelo,
                            "assets/images/projectiles/summons/robot/laser.png");
                    proyectiles.add(laser);
                    vista.agregarProyectil(laser);
                } else {
                    if (colisionService.distancia(invocacion.getX(), invocacion.getY(),
                            objetivo.getX(), objetivo.getY()) <= 55) {
                        atacarEnemigo(objetivo, ataque.dano());
                    }
                }
            }
        }
    }

    /** Avanza proyectiles hostiles de enemigos y jefes, y aplica sus impactos. */
    private void actualizarProyectilesHostiles(double deltaTiempo) {
        Iterator<ProyectilVisual> it = proyectilesHostiles.iterator();
        while (it.hasNext()) {
            ProyectilVisual visual = it.next();
            Proyectil proyectil = visual.getModelo();
            visual.actualizar(deltaTiempo);
            if (proyectil.estaFueraDeLaSala()) {
                vista.quitarProyectil(visual);
                it.remove();
            } else if (impactarInvocacion(visual)) {
                vista.quitarProyectil(visual);
                it.remove();
            } else if (tiempoInvulnerable <= 0
                    && colisionService.colisionaProyectilJugador(proyectil,
                            jugadorXAnterior, jugadorYAnterior, jugadorX, jugadorY, 25)) {
                recibirDanoJugador(proyectil.getDano());
                tiempoInvulnerable = TIEMPO_INVULNERABLE;
                vista.quitarProyectil(visual);
                it.remove();
            }
        }
    }

    private boolean impactarInvocacion(ProyectilVisual visual) {
        Proyectil proyectil = visual.getModelo();
        for (Invocacion invocacion : juegoService.getInvocacionesActivas()) {
            if (colisionService.colisionaProyectilCirculo(proyectil,
                    invocacion.getX(), invocacion.getY(), invocacion.getX(), invocacion.getY(), 21)) {
                invocacion.recibirDano(proyectil.getDano());
                invocacionesVisuales.stream().filter(v -> v.getInvocacion() == invocacion)
                        .findFirst().ifPresent(InvocacionVisual::actualizar);
                return true;
            }
        }
        return false;
    }

    private List<ColisionService.Obstaculo> obstaculosParaJugador() {
        List<ColisionService.Obstaculo> obstaculos = new ArrayList<>();
        if (tiempoInvulnerable <= 0) {
            for (EnemigoVisual enemigo : enemigosVisuales)
                obstaculos.add(new ColisionService.Obstaculo(enemigo.getX(), enemigo.getY(), enemigo.getRadioColision()));
        }
        for (Invocacion invocacion : juegoService.getInvocacionesActivas())
            obstaculos.add(new ColisionService.Obstaculo(invocacion.getX(), invocacion.getY(), 20));
        return obstaculos;
    }

    private List<ColisionService.Obstaculo> obstaculosParaEnemigo(EnemigoVisual propio) {
        List<ColisionService.Obstaculo> obstaculos = new ArrayList<>();
        if (tiempoInvulnerable <= 0) {
            obstaculos.add(new ColisionService.Obstaculo(jugadorX, jugadorY, 27));
        }
        // Los enemigos no se bloquean entre si: evita que los Poli queden trabados contra los Tiradores.
        for (Invocacion invocacion : juegoService.getInvocacionesActivas())
            obstaculos.add(new ColisionService.Obstaculo(invocacion.getX(), invocacion.getY(), 20));
        return obstaculos;
    }

    private List<ColisionService.Obstaculo> obstaculosParaInvocacion(Invocacion propia) {
        List<ColisionService.Obstaculo> obstaculos = new ArrayList<>();
        obstaculos.add(new ColisionService.Obstaculo(jugadorX, jugadorY, 27));
        for (EnemigoVisual enemigo : enemigosVisuales)
            obstaculos.add(new ColisionService.Obstaculo(enemigo.getX(), enemigo.getY(), enemigo.getRadioColision()));
        for (Invocacion otra : juegoService.getInvocacionesActivas()) if (otra != propia)
            obstaculos.add(new ColisionService.Obstaculo(otra.getX(), otra.getY(), 20));
        return obstaculos;
    }

    /**
     * Escala el daño por mapa y profundidad, lo aplica al modelo y activa el aviso visual.
     */
    private void recibirDanoJugador(int dano) {
        Jugador jugador = juegoService.getJugador();

        if (jugador != null) {
            int danoEscalado = combateService.calcularDanoRecibido(dano,
                    mapaController.getActual().getMapa(), mapaController.getActual().getProfundidad());
            try { combateService.aplicarDano(danoEscalado, jugador); }
            catch (DanoInvalidoException e) { throw new IllegalStateException("No se pudo aplicar daño al jugador.", e); }
            vista.mostrarDanio();
        }
    }

    /**
     * Avanza los disparos y elimina los que salen del escenario o impactan.
     */
    private void actualizarProyectiles(double deltaTiempo) {
        Iterator<ProyectilVisual> iterador = proyectiles.iterator();

        while (iterador.hasNext()) {
            ProyectilVisual visual = iterador.next();
            Proyectil proyectil = visual.getModelo();
            visual.actualizar(deltaTiempo);

            if (proyectil.estaFueraDeLaSala()) {
                vista.quitarProyectil(visual);
                iterador.remove();
                continue;
            }

            verificarImpacto(visual, iterador);
        }
    }

    /**
     * Busca una intersección entre trayectorias y resuelve el impacto encontrado.
     */
    private void verificarImpacto(
            ProyectilVisual visual,
            Iterator<ProyectilVisual> iterador) {
        Proyectil proyectil = visual.getModelo();

        for (EnemigoVisual enemigoVisual :
                new ArrayList<>(enemigosVisuales)) {

            if (colisionService.colisionaProyectilEnemigo(proyectil,
                    enemigoVisual.getXAnterior(), enemigoVisual.getYAnterior(),
                    enemigoVisual.getX(), enemigoVisual.getY(), enemigoVisual.getRadioColision())) {
                atacarEnemigo(enemigoVisual, proyectil.getDano());

                vista.quitarProyectil(visual);
                iterador.remove();
                return;
            }
        }
    }

    /**
     * Delega el ataque a CombateService y retira enemigos derrotados otorgando experiencia.
     */
    private void atacarEnemigo(EnemigoVisual enemigoVisual, int dano) {
        Enemigo enemigo = enemigoVisual.getEnemigo();

        try {
            combateService.aplicarDano(dano, enemigo);

            if (combateService.estaDerrotado(enemigo)) {
                vista.quitarEnemigo(enemigoVisual);
                enemigosVisuales.remove(enemigoVisual);

                int nivelesGanados = juegoService.derrotarEnemigo(enemigo, jugadorService);
                if (nivelesGanados > 0) {
                    vista.actualizarEstado("¡NIVEL " + juegoService.getJugador().getNivel()
                            + "! VIDA MÁXIMA +" + (nivelesGanados * 10) + " Y VIDA RECUPERADA");
                }
                if (!(enemigo instanceof Jefe)) {
                    objetoSueloService.crearDropEnemigo(mapaController.getNumeroSala(),
                            enemigoVisual.getX(), enemigoVisual.getY(),
                            cartaService.puedeObtener(CartaTipo.SEGUNDO_ESPACIO_INVOCACION),
                            tiposInvocacionPermitidos())
                            .ifPresent(this::agregarObjetoSueloVisual);
                } else {
                    int salaJefe = mapaController.getNumeroSala();
                    objetoSueloService.crearDropInvocacionJefe(salaJefe,
                            enemigoVisual.getX(), enemigoVisual.getY(), tiposInvocacionPermitidos())
                            .ifPresent(this::agregarObjetoSueloVisual);
                    procesarRecompensaJefe(salaJefe);
                }
            }
        } catch (DanoInvalidoException e) {
            System.err.println("Error de combate: " + e.getMessage());
        }
    }

    private void agregarObjetoSueloVisual(ObjetoSuelo objeto) {
        ObjetoSueloVisual visual = new ObjetoSueloVisual(objeto);
        objetosVisualesSala.add(visual);
        vista.agregarObjetoSuelo(visual);
    }

    private List<TipoInvocacion> tiposInvocacionPermitidos() {
        if (juegoService.getJugador().tieneSegundoEspacioInvocacion()) return List.of(TipoInvocacion.values());
        return java.util.Arrays.stream(TipoInvocacion.values())
                .filter(tipo -> juegoService.getInvocacionesActivas().stream()
                        .noneMatch(invocacion -> invocacion.getTipoInvocacion() == tipo))
                .toList();
    }

    /** Aplica la recompensa fija de progresión una sola vez por hito. */
    private void procesarRecompensaJefe(int numeroSala) {
        ProgresionService.Recompensa recompensa = ProgresionService.recompensaDeSala(numeroSala);
        if (recompensa == ProgresionService.Recompensa.AUMENTAR_VIDA_MAXIMA
                && progresionService.marcarRecompensaEntregada(numeroSala)) {
            int aumento = recompensaService.otorgarAumentoVidaMaxima(juegoService.getJugador(), 0.25);
            vista.actualizarEstado("VIDA MAXIMA +" + aumento + " - VIDA RECUPERADA");
        } else if (recompensa == ProgresionService.Recompensa.ELEGIR_CARTA) {
            recompensaCartaPendiente = true;
            pausado = true;
            vista.mostrarSeleccionCartas(cartaService.elegirTresCartasDiferentes(),
                    this::completarSeleccionCarta);
            vista.actualizarEstado("ELEGÍ UNA DE TRES CARTAS");
        }
    }

    private void completarSeleccionCarta(CartaTipo carta) {
        if (!recompensaCartaPendiente || !cartaService.registrar(carta)) return;
        mejorasService.aplicarCarta(juegoService.getJugador(), carta);
        progresionService.marcarRecompensaEntregada(mapaController.getNumeroSala());
        recompensaCartaPendiente = false;
        pausado = false;
        vista.ocultarSeleccionCartas();
        vista.actualizarEstado("CARTA OBTENIDA: " + carta.getNombre());
        ultimoTiempo = 0;
    }

    /**
     * Comprueba derrota y limpieza de sala; actualiza mensajes y habilita las puertas.
     */
    private void verificarEstado() {
        if (!juegoService.jugadorEstaVivo()) {
            juegoTerminado = true;
            detener();
            alPerder.accept(configuracion);
            return;
        }

        if (recompensaCartaPendiente) return;

        boolean salaLimpia = enemigosVisuales.isEmpty();

        // Cada dos salas completadas aparece una carta seleccionable como objeto del suelo.
        int salaActual = mapaController.getNumeroSala();
        if (salaLimpia && salaActual % 2 == 0
                && !progresionService.cartaGenerada(salaActual)) {
            progresionService.marcarCartaGenerada(salaActual);
            double cartaX = Math.max(115, Math.min(885, jugadorX + (jugadorX < 500 ? 72 : -72)));
            ObjetoSuelo carta = objetoSueloService.crearObjetoSeleccionCartas(
                    salaActual, cartaX, jugadorY);
            agregarObjetoSueloVisual(carta);
            vista.actualizarEstado("APARECIÓ UNA CARTA EN EL SUELO");
        }

        if (salaLimpia && mapaController.getActual().esJefe()
                && progresionService.esCierreDeProgresion(mapaController.getNumeroSala())) {
            juegoTerminado = true;
            detener();
            vista.mostrarVictoria(alGanar);
            return;
        }

        if (salaLimpia && !puertasAbiertas) {
            puertasAbiertas = true;

            vista.configurarSalidas(
                    mapaController.getActual().getDireccionesSalida(),
                    mapaController.getActual().getDireccionEntrada(),
                    true
            );
        }

        if (!salaLimpia) {
            vista.actualizarEstado(
                    mapaController.getActual().esJefe()
                            ? "DERROTÁ AL JEFE DE LA SALA " + mapaController.getNumeroSala()
                            : "DERROTÁ A TODOS LOS ENEMIGOS"
            );
        } else {
            vista.actualizarEstado(
                    mapaController.getActual().esJefe()
                            ? "JEFE DERROTADO - ELEGÍ LA SALIDA"
                            : "SALA LIMPIA - ELEGÍ UNA PUERTA"
            );
        }
    }

    /**
     * Actualiza textos de vida, experiencia, nivel, mapa, sala y dificultad.
     */
    private void actualizarHUD() {
        if (mapaController.getActual() == null) {
            return;
        }

        vista.actualizarHUD(
                juegoService.getJugador(),
                enemigosVisuales.size(),
                mapaController.getActual().getMapa(),
                mapaController.getNumeroSala(),
                mapaController.getActual().getProfundidad(),
                mapaController.getActual().esJefe(),
                jugadorService.experienciaNecesaria(juegoService.getJugador().getNivel())
        );
        Jefe jefe = juegoService.getJefe();
        if (jefe != null) vista.mostrarBarraJefe(jefe);
        else vista.ocultarBarraJefe();
    }

}
