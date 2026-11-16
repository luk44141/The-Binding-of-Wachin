package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.InvocacionNoDisponibleException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.CartaTipo;
import com.wachin.model.Enemigo;
import com.wachin.model.EstadoPartida;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.ObjetoSuelo;
import com.wachin.model.Proyectil;
import com.wachin.model.ProyectilEquipable;
import com.wachin.model.TipoInvocacion;
import com.wachin.model.TipoObjetoSuelo;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.ProyectilVisual;
import com.wachin.view.game.PuertaVisual;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.paint.Color;

/** Convierte la partida en curso en un {@link EstadoPartida} y reconstruye una partida desde uno. */
final class GuardadoController {

    private final ContextoPartida ctx;
    private final JugadorController jugador;
    private final SalaController salas;
    private final ObjetosController objetos;
    private final InvocacionesController invocaciones;

    GuardadoController(ContextoPartida ctx, JugadorController jugador, SalaController salas,
            ObjetosController objetos, InvocacionesController invocaciones) {
        this.ctx = ctx;
        this.jugador = jugador;
        this.salas = salas;
        this.objetos = objetos;
        this.invocaciones = invocaciones;
    }

    EstadoPartida crearEstado() {
        Jugador j = ctx.jugador();
        EstadoPartida.JugadorEstado estadoJugador = new EstadoPartida.JugadorEstado(
                j.getNombre(), j.getVida(), j.getVidaMaxima(), j.getVidaMaximaInicial(), j.getDano(),
                j.getExperiencia(), j.getNivel(), j.getMonedas(), j.getEnergia(), j.getEnergiaMaxima(),
                ctx.x, ctx.y, j.getProyectilEquipado().name(), j.getVelocidad(), j.getBonoDanoProyectiles(),
                j.getBonoDanoInvocaciones(), j.tieneSegundoEspacioInvocacion());
        List<EstadoPartida.EnemigoEstado> enemigos = ctx.enemigos.stream().map(v -> {
            Enemigo e = v.getEnemigo();
            if (e instanceof Jefe jefe) {
                return new EstadoPartida.EnemigoEstado(e.getNombre(), "JEFE", e.getVida(), e.getVidaMaxima(), e.getDano(),
                        jefe.getVelocidad(), v.getX(), v.getY(), jefe.getTipoJefe().name(), jefe.getNivelJefe(),
                        jefe.getAtaqueEspecial(), jefe.getDanoAdicional(), jefe.getTiempoAtaque());
            }
            return new EstadoPartida.EnemigoEstado(e.getNombre(), e.getTipoEnemigo(), e.getVida(), e.getVidaMaxima(),
                    e.getDano(), e.getVelocidad(), v.getX(), v.getY(), null, 0, null, 0, e.getTiempoAtaque());
        }).toList();
        List<EstadoPartida.InvocacionEstado> invocacionesEstado = ctx.juego.getInvocacionesActivas().stream()
                .map(i -> new EstadoPartida.InvocacionEstado(i.getTipoInvocacion().name(), i.getVida(), i.getVidaMaxima(),
                        i.getDano(), i.getEnergia(), i.getEnergiaMaxima(), i.getX(), i.getY(), i.getTiempoAtaque(),
                        i.getEstado().name(), i.tieneProteccionActiva())).toList();
        Map<String, Integer> cartas = new HashMap<>();
        ctx.cartas.instantanea().forEach((tipo, cantidad) -> cartas.put(tipo.name(), cantidad));
        List<EstadoPartida.ObjetoEstado> objetosEstado = ctx.objetosServicio.obtenerTodos().stream()
                .map(o -> new EstadoPartida.ObjetoEstado(o.getId(), o.getTipo().name(), o.getCodigo(),
                        o.getSalaGlobal(), o.getX(), o.getY())).toList();
        List<EstadoPartida.ProyectilEstado> disparos = new ArrayList<>();
        for (ProyectilVisual p : ctx.proyectilesJugador) disparos.add(guardarProyectil(p));
        for (ProyectilVisual p : ctx.proyectilesHostiles) disparos.add(guardarProyectil(p));
        Jefe jefeActual = ctx.juego.getJefe();
        MapaController.SalaNodo nodo = ctx.mapa.getActual();
        List<String> salidas = nodo.getDireccionesSalida().stream().map(Enum::name).toList();
        String entrada = nodo.getDireccionEntrada() == null ? null : nodo.getDireccionEntrada().name();
        return new EstadoPartida(ctx.configuracion.getPersonaje().name(), estadoJugador, ctx.mapa.getNumeroSala(),
                nodo.getMapa(), nodo.getProfundidad(), salidas, entrada, ctx.puertasAbiertas,
                ctx.progresion.getRecompensasEntregadas(), enemigos, invocacionesEstado, cartas, objetosEstado,
                disparos, jefeActual == null ? null : ctx.jefeServicio.guardarEstado(jefeActual));
    }

    private EstadoPartida.ProyectilEstado guardarProyectil(ProyectilVisual visual) {
        Proyectil p = visual.getModelo();
        return new EstadoPartida.ProyectilEstado(p.getX(), p.getY(), p.getVelocidadX(), p.getVelocidadY(),
                p.getDano(), visual.getRutaAsset(), p.getOrigen().name());
    }

    /** Reconstruye jugador, sala, enemigos, invocaciones, cartas, objetos y proyectiles. */
    void restaurar(EstadoPartida estado) {
        salas.limpiar();
        EstadoPartida.JugadorEstado j = estado.jugador();
        try {
            Jugador restaurado = new Jugador(0, j.nombre(), j.vida(), j.vidaMaxima(), j.dano(), j.experiencia(),
                    j.nivel(), j.monedas(), j.energia(), j.energiaMaxima());
            restaurado.restaurarMejorasYEquipo(j.vidaInicial(), j.velocidad(), j.bonoDano(), j.bonoDanoInvocaciones(),
                    j.segundoEspacio(), ProyectilEquipable.valueOf(j.proyectil()));
            ctx.juego.iniciarPartida(restaurado);
        } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException e) {
            throw new IllegalStateException("No se pudo restaurar al jugador.", e);
        }
        ctx.x = j.x();
        ctx.y = j.y();
        ctx.xAnterior = ctx.x;
        ctx.yAnterior = ctx.y;
        ctx.progresion.restaurarRecompensasEntregadas(estado.recompensasEntregadas());
        Set<PuertaVisual.Direccion> direcciones = estado.salidas().stream()
                .map(PuertaVisual.Direccion::valueOf).collect(Collectors.toSet());
        PuertaVisual.Direccion entrada = estado.entrada() == null ? null : PuertaVisual.Direccion.valueOf(estado.entrada());
        MapaController.SalaNodo nodo = ctx.mapa.restaurarSala(estado.salaGlobal(), estado.mapa(),
                estado.profundidad(), direcciones, entrada);
        ctx.puertasAbiertas = estado.puertasAbiertas();
        jugador.crearVisual();
        ctx.vista.configurarSala(nodo.getMapa(), nodo.esJefe(), nodo.getNumeroGlobal());
        ctx.vista.configurarSalidas(direcciones, entrada, ctx.puertasAbiertas);
        restaurarEnemigos(estado);
        restaurarInvocaciones(estado);
        Map<CartaTipo, Integer> cartas = new EnumMap<>(CartaTipo.class);
        estado.cartas().forEach((tipo, cantidad) -> cartas.put(CartaTipo.valueOf(tipo), cantidad));
        ctx.cartas.restaurar(cartas);
        List<ObjetoSuelo> objetosRestaurados = estado.objetos().stream().map(o -> new ObjetoSuelo(o.id(),
                TipoObjetoSuelo.valueOf(o.tipo()), o.codigo(), o.sala(), o.x(), o.y())).toList();
        ctx.objetosServicio.restaurar(objetosRestaurados);
        objetos.mostrarObjetosDeSala(estado.salaGlobal());
        restaurarProyectiles(estado);
    }

    private void restaurarEnemigos(EstadoPartida estado) {
        int orden = 0;
        for (EstadoPartida.EnemigoEstado e : estado.enemigos()) {
            try {
                Enemigo modelo;
                if ("JEFE".equals(e.tipo())) {
                    modelo = new Jefe(0, e.nombre(), e.vida(), e.vidaMaxima(), e.dano(), e.velocidad(), e.nivelJefe(),
                            e.ataqueEspecial(), e.danoAdicional(), Jefe.Tipo.valueOf(e.tipoJefe()));
                } else {
                    modelo = new Enemigo(0, e.nombre(), e.vida(), e.vidaMaxima(), e.dano(), e.tipo(), e.velocidad());
                }
                modelo.restaurarTiempoAtaque(e.tiempoAtaque());
                modelo.setOrdenDispersion(orden++);
                ctx.juego.agregarEnemigo(modelo);
                EnemigoVisual visual = new EnemigoVisual(modelo, e.x(), e.y());
                ctx.enemigos.add(visual);
                ctx.vista.agregarEnemigo(visual);
                if (modelo instanceof Jefe jefe) ctx.jefeServicio.restaurarEstado(jefe, estado.estadoJefe());
            } catch (NombreInvalidoException | VidaInvalidaException | DanoInvalidoException ex) {
                throw new IllegalStateException("No se pudo restaurar un enemigo.", ex);
            }
        }
    }

    private void restaurarInvocaciones(EstadoPartida estado) {
        for (int i = 0; i < estado.invocaciones().size(); i++) {
            EstadoPartida.InvocacionEstado e = estado.invocaciones().get(i);
            Invocacion invocacion = ctx.invocacionServicio.crear(TipoInvocacion.valueOf(e.tipo()));
            invocacion.recibirDano(invocacion.getVidaMaxima() - e.vida());
            invocacion.restaurarEstado(e.energia(), e.x(), e.y(), e.tiempoAtaque(),
                    Invocacion.Estado.valueOf(e.estado()), e.proteccionActiva());
            try {
                ctx.juego.equiparInvocacion(invocacion, i);
            } catch (InvocacionNoDisponibleException ex) {
                throw new IllegalStateException("No se pudo restaurar el equipamiento de invocaciones.", ex);
            }
            invocaciones.agregarVisual(invocacion);
        }
        invocaciones.reubicarCercaDelJugador();
    }

    private void restaurarProyectiles(EstadoPartida estado) {
        Color colorJefe = estado.enemigos().stream().filter(e -> "JEFE".equals(e.tipo())).findFirst()
                .map(e -> EnemigosController.colorProyectilJefe(Jefe.Tipo.valueOf(e.tipoJefe())))
                .orElse(Retroalimentacion.ROJO);
        for (EstadoPartida.ProyectilEstado p : estado.proyectiles()) {
            Proyectil.Origen origen = Proyectil.Origen.valueOf(p.origen());
            boolean hostil = origen == Proyectil.Origen.JEFE || origen == Proyectil.Origen.ENEMIGO;
            Proyectil modelo = new Proyectil(p.x(), p.y(), p.velocidadX(), p.velocidadY(), p.dano(), hostil, origen);
            Color color = switch (origen) {
                case JUGADOR -> p.ruta().contains("lagrima") ? Color.web("#4fd6e8") : Color.web("#ffc93a");
                case ENEMIGO -> Color.web("#ff7a2a");
                case JEFE -> colorJefe;
                case INVOCACION -> Color.web("#4fd6e8");
            };
            ProyectilVisual visual = new ProyectilVisual(modelo, p.ruta(), color, origen == Proyectil.Origen.INVOCACION);
            (hostil ? ctx.proyectilesHostiles : ctx.proyectilesJugador).add(visual);
            ctx.vista.agregarProyectil(visual);
        }
    }
}
