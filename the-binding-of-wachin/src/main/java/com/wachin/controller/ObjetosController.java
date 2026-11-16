package com.wachin.controller;

import com.wachin.exception.InvocacionNoDisponibleException;
import com.wachin.model.CartaTipo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.model.ObjetoSuelo;
import com.wachin.model.ProyectilEquipable;
import com.wachin.model.TipoInvocacion;
import com.wachin.model.TipoObjetoSuelo;
import com.wachin.service.ProgresionService;
import com.wachin.service.SonidoService;
import com.wachin.view.game.HudView.Tono;
import com.wachin.view.game.ObjetoSueloVisual;
import com.wachin.view.ui.Pantallas;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Objetos del suelo: aparición, recogida y las recompensas que otorgan (cartas, invocaciones, mejoras). */
final class ObjetosController {

    private static final double RADIO_RECOGIDA = 42;
    private static final double RADIO_RESALTE = 90;

    private final ContextoPartida ctx;
    private final InvocacionesController invocaciones;
    private boolean seleccionCuentaComoRecompensaSala = true;

    ObjetosController(ContextoPartida ctx, InvocacionesController invocaciones) {
        this.ctx = ctx;
        this.invocaciones = invocaciones;
    }

    // ───────────────────────────── visuales ─────────────────────────────

    void agregarVisual(ObjetoSuelo objeto) {
        ObjetoSueloVisual visual = new ObjetoSueloVisual(objeto);
        ctx.objetosVisuales.add(visual);
        ctx.vista.agregarObjetoSuelo(visual);
    }

    void retirarVisual(ObjetoSuelo objeto) {
        ctx.objetosVisuales.stream().filter(v -> v.getObjeto().getId() == objeto.getId()).findFirst()
                .ifPresent(v -> {
                    ctx.vista.quitarObjetoSuelo(v);
                    ctx.objetosVisuales.remove(v);
                });
    }

    void limpiarVisuales() {
        for (ObjetoSueloVisual objeto : ctx.objetosVisuales) ctx.vista.quitarObjetoSuelo(objeto);
        ctx.objetosVisuales.clear();
    }

    void mostrarObjetosDeSala(int sala) {
        for (ObjetoSuelo objeto : ctx.objetosServicio.listarSala(sala)) agregarVisual(objeto);
    }

    // ───────────────────────────── ciclo ─────────────────────────────

    void actualizar(double dt) {
        for (ObjetoSueloVisual visual : ctx.objetosVisuales) {
            ObjetoSuelo objeto = visual.getObjeto();
            visual.actualizar(dt, ctx.colision.distancia(ctx.x, ctx.y, objeto.getX(), objeto.getY()) <= RADIO_RESALTE);
        }
        recogerCercano();
    }

    private void recogerCercano() {
        int sala = ctx.mapa.getNumeroSala();
        List<ObjetoSuelo> cercanos = ctx.objetosServicio.recogerCercanos(sala, ctx.x, ctx.y, RADIO_RECOGIDA);
        if (ctx.debeAlejarseAntesDeRecoger) {
            if (cercanos.isEmpty()) ctx.debeAlejarseAntesDeRecoger = false;
            return;
        }
        Optional<ObjetoSuelo> candidato = cercanos.stream()
                .filter(o -> o.getTipo() != TipoObjetoSuelo.CARTA || cartaDisponible(o))
                .filter(o -> o.getTipo() != TipoObjetoSuelo.INVOCACION || invocacionPermitida(o))
                .min(Comparator.comparingDouble(o -> ctx.colision.distancia(ctx.x, ctx.y, o.getX(), o.getY())));
        if (candidato.isEmpty()) return;
        ObjetoSuelo objeto = candidato.get();
        switch (objeto.getTipo()) {
            case PROYECTIL -> recogerProyectil(objeto, sala);
            case CARTA -> recogerCarta(objeto);
            case INVOCACION -> recogerInvocacion(objeto);
            case RECOMPENSA -> recogerRecompensa(objeto);
        }
        if (ctx.invocacionPendiente == null) ctx.debeAlejarseAntesDeRecoger = true;
    }

    private void confirmarRecogida(ObjetoSuelo objeto) {
        ctx.objetosServicio.retirar(objeto.getId());
        retirarVisual(objeto);
        ctx.efectos.recoger(objeto.getX(), objeto.getY(), ObjetoSueloVisual.color(objeto.getTipo()));
    }

    private void recogerProyectil(ObjetoSuelo objeto, int sala) {
        ProyectilEquipable nuevo;
        try { nuevo = ProyectilEquipable.desdeCodigo(objeto.getCodigo()); }
        catch (IllegalArgumentException e) { return; }
        Jugador jugador = ctx.jugador();
        ProyectilEquipable anterior = jugador.getProyectilEquipado();
        jugador.equiparProyectil(nuevo);
        confirmarRecogida(objeto);
        if (anterior != nuevo) {
            agregarVisual(ctx.objetosServicio.crear(TipoObjetoSuelo.PROYECTIL, anterior.name(), sala,
                    objeto.getX(), objeto.getY()));
        }
        ctx.vista.mostrarAviso("Proyectil equipado: " + nuevo.getNombre() + " (daño " + nuevo.getDano() + ")", Tono.BUENO);
    }

    private void recogerCarta(ObjetoSuelo objeto) {
        if ("SELECCION_CARTAS".equals(objeto.getCodigo())) {
            confirmarRecogida(objeto);
            mostrarSeleccionCartas();
            return;
        }
        CartaTipo carta;
        try { carta = CartaTipo.valueOf(objeto.getCodigo()); }
        catch (IllegalArgumentException e) { return; }
        if (!ctx.cartas.registrar(carta)) return;
        ctx.mejoras.aplicarCarta(ctx.jugador(), carta);
        confirmarRecogida(objeto);
        ctx.vista.mostrarAviso("Carta obtenida: " + carta.getNombre(), Tono.ESPECIAL);
    }

    private void recogerRecompensa(ObjetoSuelo objeto) {
        if (ctx.recompensas.aplicarObjetoSuelo(ctx.jugador(), objeto.getCodigo())) {
            confirmarRecogida(objeto);
            ctx.vista.mostrarAviso("Vida máxima +25% y vida recuperada", Tono.BUENO);
        }
    }

    // ───────────────────────────── cartas ─────────────────────────────

    private void mostrarSeleccionCartas() {
        mostrarSeleccionCartas(true);
    }

    /** Abre la misma selección de tres cartas al subir de nivel, sin consumir una recompensa de jefe. */
    void mostrarSeleccionCartasPorNivel() {
        mostrarSeleccionCartas(false);
    }

    private void mostrarSeleccionCartas(boolean cuentaComoRecompensaSala) {
        seleccionCuentaComoRecompensaSala = cuentaComoRecompensaSala;
        ctx.debeAlejarseAntesDeRecoger = true;
        ctx.cartaPendiente = true;
        ctx.pausado = true;
        ctx.limpiarEntrada.run();
        ctx.efectos.sonar(SonidoService.Efecto.CARTA);
        ctx.vista.mostrarPantalla(Pantallas.seleccionCartas(ctx.cartas.elegirTresCartasDiferentes(), this::completarSeleccionCarta));
    }

    private void completarSeleccionCarta(CartaTipo carta) {
        if (!ctx.cartaPendiente || !ctx.cartas.registrar(carta)) return;
        ctx.mejoras.aplicarCarta(ctx.jugador(), carta);
        if (seleccionCuentaComoRecompensaSala) {
            ctx.progresion.marcarRecompensaEntregada(ctx.mapa.getNumeroSala());
        }
        seleccionCuentaComoRecompensaSala = true;
        ctx.cartaPendiente = false;
        ctx.pausado = false;
        ctx.vista.ocultarPantalla();
        ctx.efectos.recoger(ctx.x, ctx.y, Retroalimentacion.DORADO);
        ctx.vista.mostrarAviso("Carta obtenida: " + carta.getNombre(), Tono.ESPECIAL);
    }

    /** Cada dos salas completadas aparece una carta seleccionable junto al jugador. */
    void aparecerCartaDeSala(int sala) {
        double x = Math.max(115, Math.min(885, ctx.x + (ctx.x < 500 ? 72 : -72)));
        ObjetoSuelo carta = ctx.objetosServicio.crearObjetoSeleccionCartas(sala, x, ctx.y);
        agregarVisual(carta);
        ctx.efectos.recoger(x, ctx.y, Retroalimentacion.DORADO);
        ctx.vista.mostrarAviso("Apareció una carta en el suelo", Tono.ESPECIAL);
    }

    private boolean cartaDisponible(ObjetoSuelo objeto) {
        if ("SELECCION_CARTAS".equals(objeto.getCodigo())) return true;
        try { return ctx.cartas.puedeObtener(CartaTipo.valueOf(objeto.getCodigo())); }
        catch (IllegalArgumentException e) { return false; }
    }

    // ───────────────────────────── invocaciones ─────────────────────────────

    private boolean invocacionPermitida(ObjetoSuelo objeto) {
        try {
            TipoInvocacion tipo = TipoInvocacion.valueOf(objeto.getCodigo());
            return ctx.jugador().tieneSegundoEspacioInvocacion()
                    || ctx.juego.getInvocacionesActivas().stream().noneMatch(i -> i.getTipoInvocacion() == tipo);
        } catch (IllegalArgumentException e) { return false; }
    }

    private void recogerInvocacion(ObjetoSuelo objeto) {
        int limite = ctx.jugador().tieneSegundoEspacioInvocacion() ? 2 : 1;
        int cantidad = ctx.juego.getInvocacionesActivas().size();
        if (cantidad >= limite && limite == 2) {
            ctx.invocacionPendiente = objeto;
            ctx.pausado = true;
            ctx.limpiarEntrada.run();
            ctx.vista.mostrarPantalla(Pantallas.reemplazoInvocacion(
                    ctx.juego.getInvocacionesActivas(), this::completarReemplazoInvocacion));
            return;
        }
        equiparDesdeSuelo(objeto, cantidad < limite ? cantidad : 0);
    }

    private void completarReemplazoInvocacion(int espacio) {
        if (ctx.invocacionPendiente == null) return;
        equiparDesdeSuelo(ctx.invocacionPendiente, espacio);
        ctx.invocacionPendiente = null;
        ctx.pausado = false;
        ctx.debeAlejarseAntesDeRecoger = true;
        ctx.vista.ocultarPantalla();
    }

    private void equiparDesdeSuelo(ObjetoSuelo objeto, int espacio) {
        TipoInvocacion tipo;
        try { tipo = TipoInvocacion.valueOf(objeto.getCodigo()); }
        catch (IllegalArgumentException e) { return; }
        Invocacion nueva = ctx.invocacionServicio.crear(tipo);
        nueva.moverA(ctx.x - 45, ctx.y + 35);
        try {
            Invocacion anterior = ctx.juego.equiparInvocacion(nueva, espacio);
            if (anterior != null) {
                invocaciones.quitarVisual(anterior);
                agregarVisual(ctx.objetosServicio.crear(TipoObjetoSuelo.INVOCACION, anterior.getTipoInvocacion().name(),
                        objeto.getSalaGlobal(), objeto.getX(), objeto.getY()));
            }
        } catch (InvocacionNoDisponibleException e) {
            ctx.vista.mostrarAviso(e.getMessage(), Tono.MALO);
            return;
        }
        confirmarRecogida(objeto);
        invocaciones.agregarVisual(nueva);
        ctx.vista.mostrarAviso("Invocación equipada: " + tipo.getNombre(), Tono.ESPECIAL);
    }

    List<TipoInvocacion> tiposInvocacionPermitidos() {
        if (ctx.jugador().tieneSegundoEspacioInvocacion()) return List.of(TipoInvocacion.values());
        return Arrays.stream(TipoInvocacion.values())
                .filter(t -> ctx.juego.getInvocacionesActivas().stream().noneMatch(i -> i.getTipoInvocacion() == t))
                .toList();
    }

    // ───────────────────────────── drops y recompensas ─────────────────────────────

    void soltarDropEnemigo(double x, double y) {
        ctx.objetosServicio.crearDropEnemigo(ctx.mapa.getNumeroSala(), x, y,
                ctx.cartas.puedeObtener(CartaTipo.SEGUNDO_ESPACIO_INVOCACION), tiposInvocacionPermitidos())
                .ifPresent(this::agregarVisual);
    }

    void soltarRecompensaJefe(int sala, double x, double y) {
        ctx.objetosServicio.crearDropInvocacionJefe(sala, x, y, tiposInvocacionPermitidos()).ifPresent(this::agregarVisual);
        ProgresionService.Recompensa recompensa = ProgresionService.recompensaDeSala(sala);
        if (recompensa == ProgresionService.Recompensa.AUMENTAR_VIDA_MAXIMA
                && ctx.progresion.marcarRecompensaEntregada(sala)) {
            int aumento = ctx.recompensas.otorgarAumentoVidaMaxima(ctx.jugador(), 0.25);
            ctx.efectos.recoger(ctx.x, ctx.y, Retroalimentacion.DORADO);
            ctx.vista.mostrarAviso("Vida máxima +" + aumento + " · vida recuperada", Tono.BUENO);
        } else if (recompensa == ProgresionService.Recompensa.ELEGIR_CARTA && !ctx.progresion.recompensaEntregada(sala)) {
            mostrarSeleccionCartas();
        }
    }
}
