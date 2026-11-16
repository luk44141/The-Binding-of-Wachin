package com.wachin.controller;

import com.wachin.model.EstadoPartida;
import com.wachin.service.AjustesService;
import com.wachin.service.PartidaService;
import com.wachin.view.game.PartidaView;
import com.wachin.view.ui.Pantallas;
import java.util.List;
import java.util.function.Consumer;

/** Menú de pausa: consulta de cartas e invocaciones, guardado, carga y ajustes. */
final class PausaController {

    private final ContextoPartida ctx;
    private final GuardadoController guardado;
    private final PartidaService partidas;
    private final Consumer<EstadoPartida> alCargar;
    private final Runnable alSalir;

    PausaController(ContextoPartida ctx, GuardadoController guardado, PartidaService partidas,
            Consumer<EstadoPartida> alCargar, Runnable alSalir) {
        this.ctx = ctx;
        this.guardado = guardado;
        this.partidas = partidas;
        this.alCargar = alCargar;
        this.alSalir = alSalir;
    }

    void alternar() {
        if (ctx.pausado) reanudar();
        else abrir();
    }

    private void abrir() {
        ctx.pausado = true;
        ctx.limpiarEntrada.run();
        mostrarMenu();
    }

    void reanudar() {
        ctx.pausado = false;
        ctx.limpiarEntrada.run();
        ctx.vista.ocultarPantalla();
    }

    private void mostrarMenu() {
        ctx.vista.mostrarPantalla(Pantallas.pausa(this::accion));
    }

    private void accion(PartidaView.AccionPausa accion) {
        switch (accion) {
            case REANUDAR -> reanudar();
            case CARTAS -> ctx.vista.mostrarPantalla(Pantallas.cartasObtenidas(ctx.cartas.obtenerCartas(), this::mostrarMenu));
            case INVOCACIONES -> ctx.vista.mostrarPantalla(Pantallas.invocaciones(ctx.juego.getInvocacionesActivas(), this::mostrarMenu));
            case GUARDAR -> mostrarSlots(true);
            case CARGAR -> mostrarSlots(false);
            case AJUSTES -> ctx.vista.mostrarPantalla(Pantallas.ajustes(AjustesService.obtener(), this::mostrarMenu));
            case SALIR -> alSalir.run();
        }
    }

    private void mostrarSlots(boolean guardar) {
        try {
            List<PartidaView.SlotView> slots = partidas.listarSlots().stream()
                    .map(s -> new PartidaView.SlotView(s.numero(), s.estaOcupado(),
                            s.estaOcupado() ? s.nombre() + " — " + s.fechaGuardado() : "VACÍO")).toList();
            ctx.vista.mostrarPantalla(Pantallas.slots(slots, guardar, guardar ? this::guardarEn : this::cargarDesde,
                    this::eliminar, this::mostrarMenu));
        } catch (RuntimeException e) {
            mostrarError("No se pudieron consultar los espacios de guardado.", e);
        }
    }

    private void guardarEn(int slot) {
        try {
            partidas.guardar(slot, "Partida sala " + ctx.mapa.getNumeroSala(), guardado.crearEstado());
            ctx.vista.mostrarPantalla(Pantallas.mensaje("PARTIDA GUARDADA", List.of("Espacio " + slot), false, this::mostrarMenu));
        } catch (RuntimeException e) {
            mostrarError("No se pudo guardar la partida.", e);
        }
    }

    private void cargarDesde(int slot) {
        try {
            EstadoPartida estado = partidas.cargar(slot).orElse(null);
            if (estado == null) {
                mostrarError("El espacio " + slot + " está vacío.", null);
                return;
            }
            alCargar.accept(estado);
        } catch (RuntimeException e) {
            mostrarError("No se pudo cargar la partida.", e);
        }
    }

    private void eliminar(int slot) {
        try {
            partidas.eliminar(slot);
            mostrarSlots(false);
        } catch (RuntimeException e) {
            mostrarError("No se pudo eliminar el espacio " + slot + ".", e);
        }
    }

    private void mostrarError(String mensaje, Exception causa) {
        java.util.logging.Logger.getLogger(PausaController.class.getName())
                .log(java.util.logging.Level.WARNING, mensaje, causa);
        List<String> filas = causa == null || causa.getMessage() == null
                ? List.of(mensaje) : List.of(mensaje, causa.getMessage());
        ctx.vista.mostrarPantalla(Pantallas.mensaje("OPERACIÓN NO COMPLETADA", filas, true, this::mostrarMenu));
    }
}
