package com.wachin.controller;

import com.wachin.model.Enemigo;
import com.wachin.exception.PuertaBloqueadaException;
import com.wachin.model.Jefe;
import com.wachin.model.Sala;
import com.wachin.service.ProgresionService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.HudView.Tono;
import com.wachin.view.game.ProyectilVisual;
import com.wachin.view.game.PuertaVisual;

/** Preparación y limpieza de salas, puertas, transiciones y detección de sala completada. */
final class SalaController {

    /** Resultado de comprobar la sala en cada fotograma. */
    enum Resultado { NINGUNO, VICTORIA }

    private final ContextoPartida ctx;
    private final ObjetosController objetos;
    private final InvocacionesController invocaciones;
    /** Evita repetir el aviso mientras el jugador permanece frente a la misma puerta cerrada. */
    private boolean avisoPuertaBloqueadaActivo;

    SalaController(ContextoPartida ctx, ObjetosController objetos, InvocacionesController invocaciones) {
        this.ctx = ctx;
        this.objetos = objetos;
        this.invocaciones = invocaciones;
    }

    void iniciar() {
        prepararSala(ctx.mapa.iniciar());
    }

    void prepararSala(MapaController.SalaNodo nodo) {
        limpiar();
        if (nodo == null) return;
        ctx.puertasAbiertas = false;
        Sala sala = ctx.salaServicio.generarSala(nodo.getNumeroGlobal(), nodo.getProfundidad(), nodo.esJefe(), nodo.getMapa());
        ctx.vista.configurarSala(nodo.getMapa(), nodo.esJefe(), nodo.getNumeroGlobal());
        ctx.vista.configurarSalidas(nodo.getDireccionesSalida(), nodo.getDireccionEntrada(), false);
        objetos.mostrarObjetosDeSala(nodo.getNumeroGlobal());
        if (nodo.esJefe()) crearJefe();
        else crearEnemigos(sala);
        invocaciones.reubicarCercaDelJugador();
    }

    private void crearEnemigos(Sala sala) {
        int orden = 0;
        for (Enemigo enemigo : sala.getEnemigos()) {
            ctx.juego.agregarEnemigo(enemigo);
            enemigo.setOrdenDispersion(orden++);
            double[] posicion = generarPosicion();
            EnemigoVisual visual = new EnemigoVisual(enemigo, posicion[0], posicion[1]);
            visual.aparecer(0.45 + ctx.azar.nextDouble() * 0.45);
            ctx.enemigos.add(visual);
            ctx.vista.agregarEnemigo(visual);
            ctx.efectos.aparicionEnemigo(posicion[0], posicion[1]);
        }
    }

    private void crearJefe() {
        int sala = ctx.mapa.getNumeroSala();
        Jefe.Tipo tipo = ProgresionService.jefeDeSala(sala);
        if (tipo == null) throw new IllegalStateException("La sala actual no tiene un jefe de progresión asignado.");
        Jefe jefe = ctx.salaServicio.generarJefe(tipo);
        ctx.juego.agregarEnemigo(jefe);
        EnemigoVisual visual = new EnemigoVisual(jefe, 500, 270);
        visual.aparecer(1.5);
        ctx.enemigos.add(visual);
        ctx.vista.agregarEnemigo(visual);
        ctx.efectos.aparicionJefe(500, 270, jefe.getNombre(), sala);
    }

    /** Retira de la escena todo lo que pertenece a la sala anterior. */
    void limpiar() {
        objetos.limpiarVisuales();
        for (EnemigoVisual enemigo : ctx.enemigos) ctx.vista.quitarEnemigo(enemigo);
        ctx.enemigos.clear();
        for (ProyectilVisual p : ctx.proyectilesJugador) ctx.vista.quitarProyectil(p);
        ctx.proyectilesJugador.clear();
        for (ProyectilVisual p : ctx.proyectilesHostiles) ctx.vista.quitarProyectil(p);
        ctx.proyectilesHostiles.clear();
        ctx.juego.getEnemigos().clear();
    }

    private double[] generarPosicion() {
        double x;
        double y;
        int intentos = 0;
        do {
            x = 140 + ctx.azar.nextDouble() * 720;
            y = 130 + ctx.azar.nextDouble() * 440;
            intentos++;
        } while (ctx.colision.distancia(x, y, ctx.x, ctx.y) < 125 && intentos < 100);
        return new double[]{x, y};
    }

    // ───────────────────────────── puertas ─────────────────────────────

    /**
     * Detecta el intento de cruzar una puerta. Si está cerrada (por enemigos o porque es
     * la puerta de entrada que no permite volver), informa el bloqueo mediante una excepción.
     */
    boolean detectarPuerta() {
        if (!ctx.partidaActiva()) {
            avisoPuertaBloqueadaActivo = false;
            return false;
        }
        boolean centroX = Math.abs(ctx.x - 500) <= 55;
        boolean centroY = Math.abs(ctx.y - 350) <= 55;
        PuertaVisual.Direccion direccion = null;
        if (ctx.y <= 80 && centroX) direccion = PuertaVisual.Direccion.ARRIBA;
        else if (ctx.y >= 620 && centroX) direccion = PuertaVisual.Direccion.ABAJO;
        else if (ctx.x <= 80 && centroY) direccion = PuertaVisual.Direccion.IZQUIERDA;
        else if (ctx.x >= 920 && centroY) direccion = PuertaVisual.Direccion.DERECHA;

        if (direccion == null) {
            avisoPuertaBloqueadaActivo = false;
            return false;
        }

        MapaController.SalaNodo nodo = ctx.mapa.getActual();
        if (nodo == null) {
            avisoPuertaBloqueadaActivo = false;
            return false;
        }
        boolean esSalida = nodo.getDireccionesSalida().contains(direccion);
        boolean esEntrada = direccion == nodo.getDireccionEntrada();
        // No mostrar avisos al chocar contra una pared donde no hay una puerta visible.
        if (!esSalida && !esEntrada) {
            avisoPuertaBloqueadaActivo = false;
            return false;
        }

        try {
            if (!ctx.puertasAbiertas || esEntrada) {
                throw new PuertaBloqueadaException();
            }
            MapaController.SalaNodo destino = ctx.mapa.entrarPor(direccion);
            if (destino == null) {
                throw new PuertaBloqueadaException();
            }

            avisoPuertaBloqueadaActivo = false;
            PuertaVisual.Direccion salida = direccion;
            ctx.enTransicion = true;
            ctx.efectos.puertasAbiertas();
            ctx.vista.transicionar(() -> {
                colocarJugadorEnEntrada(salida);
                prepararSala(destino);
            }, () -> ctx.enTransicion = false);
            return true;
        } catch (PuertaBloqueadaException excepcion) {
            if (!avisoPuertaBloqueadaActivo) {
                ctx.vista.mostrarAviso(excepcion.getMessage(), Tono.MALO);
                avisoPuertaBloqueadaActivo = true;
            }
            return false;
        }
    }

    private void colocarJugadorEnEntrada(PuertaVisual.Direccion salidaAnterior) {
        switch (salidaAnterior) {
            case ARRIBA -> { ctx.x = 500; ctx.y = 580; }
            case ABAJO -> { ctx.x = 500; ctx.y = 100; }
            case IZQUIERDA -> { ctx.x = 880; ctx.y = 350; }
            case DERECHA -> { ctx.x = 120; ctx.y = 350; }
        }
        ctx.xAnterior = ctx.x;
        ctx.yAnterior = ctx.y;
        ctx.invulnerable = Math.max(ctx.invulnerable, 0.6);
        ctx.jugadorVisual.actualizar(0, ctx.x, ctx.y, 0, 0, false);
    }

    // ───────────────────────────── estado de la sala ─────────────────────────────

    /** Genera la carta de las salas pares, abre las puertas al limpiar y detecta el cierre de la progresión. */
    Resultado comprobarSala() {
        if (ctx.cartaPendiente) return Resultado.NINGUNO;
        MapaController.SalaNodo nodo = ctx.mapa.getActual();
        int sala = ctx.mapa.getNumeroSala();
        boolean limpia = ctx.enemigos.isEmpty();
        Resultado resultado = Resultado.NINGUNO;
        if (limpia && !ctx.puertasAbiertas) {
            if (ctx.progresion.marcarExperienciaEntregada(sala)) {
                int niveles = ctx.jugadorServicio.ganarExperiencia(ctx.jugador(), 50);
                if (niveles > 0) {
                    ctx.efectos.nivelSubido(ctx.x, ctx.y, ctx.jugador().getNivel());
                    ctx.vista.mostrarAviso("¡Nivel " + ctx.jugador().getNivel()
                            + "! Vida completa y vida máxima +10", Tono.ESPECIAL);
                    // La sala 20 abre el modal de transición a hardcore en este mismo ciclo.
                    // Evitamos superponerle la selección de cartas, que dejaría la partida marcada
                    // como cartaPendiente y bloquearía ESC después de cerrar la victoria.
                    if (!ProgresionService.esCierreDeProgresion(sala)) {
                        objetos.mostrarSeleccionCartasPorNivel();
                    }
                } else {
                    ctx.vista.mostrarAviso("+50 EXP", Tono.BUENO);
                }
            }
            ctx.puertasAbiertas = true;
            ctx.vista.configurarSalidas(nodo.getDireccionesSalida(), nodo.getDireccionEntrada(), true);
            ctx.efectos.puertasAbiertas();
            ctx.vista.mostrarAviso(nodo.esJefe() ? "Jefe derrotado" : "Sala limpia", Tono.BUENO);
            if (nodo.esJefe() && ProgresionService.esCierreDeProgresion(sala)) resultado = Resultado.VICTORIA;
        }
        ctx.vista.fijarObjetivo(limpia
                ? (nodo.esJefe() ? "Jefe derrotado · elegí la salida" : "Sala limpia · elegí una puerta")
                : (nodo.esJefe() ? "Derrotá al jefe de la sala " + sala : "Derrotá a todos los enemigos"));
        return resultado;
    }
}
