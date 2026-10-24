
package com.wachin.controller;

import com.wachin.exception.DanoInvalidoException;
import com.wachin.exception.NombreInvalidoException;
import com.wachin.exception.VidaInvalidaException;
import com.wachin.model.Enemigo;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import com.wachin.model.Sala;
import com.wachin.service.CombateService;
import com.wachin.service.JuegoService;
import com.wachin.service.JugadorService;
import com.wachin.service.SalaService;
import com.wachin.view.game.EnemigoVisual;
import com.wachin.view.game.PartidaView;
import com.wachin.view.game.Proyectil;
import com.wachin.view.game.PuertaVisual;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PartidaController {

    private static final double LIMITE_IZQUIERDO = 75;
    private static final double LIMITE_DERECHO = 925;
    private static final double LIMITE_SUPERIOR = 75;
    private static final double LIMITE_INFERIOR = 625;

    private static final double VELOCIDAD_JUGADOR = 225;
    private static final double VELOCIDAD_PROYECTIL = 430;
    private static final double RADIO_IMPACTO = 28;
    private static final double TIEMPO_INVULNERABLE = 0.45;

    private final PartidaView vista;

    private final Set<KeyCode> teclasPresionadas = new HashSet<>();
    private final List<Proyectil> proyectiles = new ArrayList<>();
    private final List<EnemigoVisual> enemigosVisuales = new ArrayList<>();

    private final JuegoService juegoService = new JuegoService();
    private final CombateService combateService = new CombateService();
    private final JugadorService jugadorService = new JugadorService();
    private final SalaService salaService = new SalaService();
    private final Random random = new Random();

    /*
     * Cada nodo representa una sala concreta.
     * Las conexiones son unidireccionales: no se crean puertas
     * que permitan regresar a una sala anterior.
     */
    private static class NodoSala {
        final int id;
        final int profundidad;
        final boolean esJefe;
        final int mapa;

        final Map<PuertaVisual.Direccion, NodoSala> destinos =
                new EnumMap<>(PuertaVisual.Direccion.class);

        NodoSala(
                int id,
                int profundidad,
                boolean esJefe,
                int mapa) {

            this.id = id;
            this.profundidad = profundidad;
            this.esJefe = esJefe;
            this.mapa = mapa;
        }
    }

    private final Set<Integer> salasVisitadas = new HashSet<>();
    private final Map<Integer, NodoSala> jefesPorMapa = new java.util.HashMap<>();

    private NodoSala salaActual;
    private int siguienteIdSala = 1;
    private int siguienteNumeroSala = 1;
    private int numeroSalaActual = 1;

    private Circle jugadorVisual;

    private double jugadorX = 500;
    private double jugadorY = 350;
    private double tiempoInvulnerable;

    private long ultimoTiempo;
    private AnimationTimer gameLoop;

    private boolean puertasAbiertas;
    private boolean juegoTerminado;
    private boolean pausado;

    public PartidaController(PartidaView vista) {
        this.vista = vista;
    }

    public void iniciar() {
        crearJugador();

        salaActual = crearNodo(1, false, 1);
        entrarEnSala(salaActual, null);

        iniciarGameLoop();
    }

    public void detener() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void teclaPresionada(KeyCode tecla) {
        if (tecla != KeyCode.ESCAPE && !pausado) {
            teclasPresionadas.add(tecla);
        }
    }

    public void teclaLiberada(KeyCode tecla) {
        teclasPresionadas.remove(tecla);
    }

    public void manejarTecla(KeyCode tecla) {
        if (tecla == KeyCode.ESCAPE) {
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

    private void alternarPausa() {
        if (pausado) {
            reanudar();
            return;
        }

        pausado = true;
        teclasPresionadas.clear();
        vista.mostrarPausa(this::reanudar);
    }

    private void reanudar() {
        pausado = false;
        teclasPresionadas.clear();
        vista.ocultarPausa();
        ultimoTiempo = 0;
    }

    private void crearJugador() {
        try {
            Jugador jugador = new Jugador("Wachin", 100, 25, 100);
            juegoService.iniciarPartida(jugador);

            jugadorVisual = new Circle(jugadorX, jugadorY, 25);
            jugadorVisual.setFill(Color.BEIGE);
            jugadorVisual.setStroke(Color.BLACK);
            jugadorVisual.setStrokeWidth(3);

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

    private NodoSala crearNodo(
            int profundidad,
            boolean esJefe,
            int mapa) {

        return new NodoSala(
                siguienteIdSala++,
                profundidad,
                esJefe,
                mapa
        );
    }

    private int mapaParaProfundidad(int profundidad) {
        return ((profundidad - 1) / 5) + 1;
    }

    private NodoSala obtenerJefe(int mapa) {
        return jefesPorMapa.computeIfAbsent(
                mapa,
                m -> crearNodo(5, true, m)
        );
    }

    private PuertaVisual.Direccion opuesta(
            PuertaVisual.Direccion direccion) {

        switch (direccion) {
            case ARRIBA:
                return PuertaVisual.Direccion.ABAJO;
            case ABAJO:
                return PuertaVisual.Direccion.ARRIBA;
            case IZQUIERDA:
                return PuertaVisual.Direccion.DERECHA;
            case DERECHA:
                return PuertaVisual.Direccion.IZQUIERDA;
            default:
                throw new IllegalArgumentException(
                        "Dirección inválida."
                );
        }
    }

    /*
     * Elige direcciones al azar, excluyendo el lado por el que
     * entra el jugador. Las salidas se guardan en el nodo.
     */
    private List<PuertaVisual.Direccion> elegirDirecciones(
            PuertaVisual.Direccion entrada,
            int cantidad) {

        List<PuertaVisual.Direccion> disponibles =
                new ArrayList<>(
                        List.of(PuertaVisual.Direccion.values())
                );

        if (entrada != null) {
            disponibles.remove(entrada);
        }

        Collections.shuffle(disponibles, random);

        return new ArrayList<>(
                disponibles.subList(
                        0,
                        Math.min(cantidad, disponibles.size())
                )
        );
    }

    /*
     * Cada mapa tiene cuatro salas normales y un jefe.
     * Las salas 1-3 tienen dos salidas hacia la siguiente profundidad;
     * la sala 4 conduce obligatoriamente al jefe del mapa.
     * Tras derrotar al jefe se pasa a la sala 1 del mapa siguiente.
     */
    private void prepararSalidas(
            NodoSala nodo,
            PuertaVisual.Direccion entrada) {

        if (!nodo.destinos.isEmpty()) {
            return;
        }

        if (nodo.esJefe) {
            PuertaVisual.Direccion salida =
                    elegirDirecciones(entrada, 1).get(0);

            NodoSala siguienteMapa = crearNodo(
                    1,
                    false,
                    nodo.mapa + 1
            );

            nodo.destinos.put(salida, siguienteMapa);
            return;
        }

        List<PuertaVisual.Direccion> direcciones =
                elegirDirecciones(entrada, nodo.profundidad >= 4 ? 1 : 2);

        if (nodo.profundidad >= 4) {
            PuertaVisual.Direccion direccionJefe = direcciones.get(0);
            nodo.destinos.put(direccionJefe, obtenerJefe(nodo.mapa));
            return;
        }

        for (PuertaVisual.Direccion direccion : direcciones) {
            NodoSala siguiente = crearNodo(
                    nodo.profundidad + 1,
                    false,
                    nodo.mapa
            );
            nodo.destinos.put(direccion, siguiente);
        }
    }

    private void entrarEnSala(
            NodoSala nodo,
            PuertaVisual.Direccion salidaAnterior) {

        if (salasVisitadas.contains(nodo.id)) {
            vista.actualizarEstado("ESA SALA YA FUE VISITADA");
            return;
        }

        limpiarSalaAnterior();

        if (salidaAnterior != null) {
            colocarJugadorEnEntrada(salidaAnterior);
        }

        salaActual = nodo;
        numeroSalaActual = siguienteNumeroSala++;
        salasVisitadas.add(nodo.id);

        prepararSalidas(
                nodo,
                salidaAnterior == null ? null : opuesta(salidaAnterior)
        );

        puertasAbiertas = false;

        Sala sala = salaService.generarSala(
                nodo.id,
                nodo.profundidad,
                nodo.esJefe
        );

        vista.configurarSala(
                sala,
                nodo.mapa,
                nodo.esJefe
        );

        vista.configurarSalidas(
                nodo.destinos.keySet(),
                false
        );

        if (nodo.esJefe) {
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

        actualizarHUD();
    }

    private void crearJefe() {
        Jefe jefe = salaService.generarJefe(
                salaActual.id,
                salaActual.mapa
        );

        juegoService.agregarEnemigo(jefe);

        EnemigoVisual visual = new EnemigoVisual(
                jefe,
                500,
                270
        );

        enemigosVisuales.add(visual);
        vista.agregarEnemigo(visual);
    }

    private void limpiarSalaAnterior() {
        for (EnemigoVisual enemigo : enemigosVisuales) {
            vista.quitarEnemigo(enemigo);
        }

        enemigosVisuales.clear();

        for (Proyectil proyectil : proyectiles) {
            vista.quitarProyectil(proyectil);
        }

        proyectiles.clear();

        juegoService.getEnemigos().clear();
        teclasPresionadas.clear();
    }

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

        jugadorVisual.setCenterX(jugadorX);
        jugadorVisual.setCenterY(jugadorY);
    }

    private double[] generarPosicionEnemigo() {
        double x;
        double y;
        int intentos = 0;

        do {
            x = 140 + random.nextDouble() * 720;
            y = 130 + random.nextDouble() * 440;
            intentos++;
        } while (
                calcularDistancia(x, y, jugadorX, jugadorY) < 125
                        && intentos < 100
        );

        return new double[]{x, y};
    }

    private void disparar(double direccionX, double direccionY) {
        Jugador jugador = juegoService.getJugador();

        if (jugador == null) {
            return;
        }

        Proyectil proyectil = new Proyectil(
                jugadorX,
                jugadorY,
                direccionX * VELOCIDAD_PROYECTIL,
                direccionY * VELOCIDAD_PROYECTIL,
                jugador.getDano()
        );

        proyectiles.add(proyectil);
        vista.agregarProyectil(proyectil);
    }

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

                actualizarJugador(deltaTiempo);

                if (detectarPuerta()) {
                    return;
                }

                actualizarEnemigos(deltaTiempo);
                actualizarProyectiles(deltaTiempo);
                verificarEstado();
                actualizarHUD();
            }
        };

        gameLoop.start();
    }

    private boolean partidaActiva() {
        return !juegoTerminado && juegoService.jugadorEstaVivo();
    }

    private void actualizarJugador(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        double direccionX = 0;
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

            jugadorX += direccionX * VELOCIDAD_JUGADOR * deltaTiempo;
            jugadorY += direccionY * VELOCIDAD_JUGADOR * deltaTiempo;
        }

        limitarJugador();

        jugadorVisual.setCenterX(jugadorX);
        jugadorVisual.setCenterY(jugadorY);
    }

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

    private boolean detectarPuerta() {
        if (!puertasAbiertas || !partidaActiva()) {
            return false;
        }

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

        NodoSala destino = salaActual.destinos.get(direccion);

        if (destino == null || salasVisitadas.contains(destino.id)) {
            return false;
        }

        entrarEnSala(destino, direccion);
        return true;
    }

    private void actualizarEnemigos(double deltaTiempo) {
        if (!partidaActiva()) {
            return;
        }

        for (EnemigoVisual enemigoVisual : enemigosVisuales) {
            enemigoVisual.actualizar(jugadorX, jugadorY, deltaTiempo);

            if (tiempoInvulnerable <= 0
                    && enemigoVisual.tocaJugador(jugadorX, jugadorY)) {

                recibirDanoJugador(enemigoVisual.getEnemigo().getDano());
                tiempoInvulnerable = TIEMPO_INVULNERABLE;
                break;
            }
        }
    }

    private void recibirDanoJugador(int dano) {
        Jugador jugador = juegoService.getJugador();

        if (jugador != null) {
            double multiplicador = 1.50
                    + Math.min(0.90, (salaActual.mapa - 1) * 0.12)
                    + Math.min(0.40, (salaActual.profundidad - 1) * 0.08);
            int danoAumentado = Math.max(
                    dano + 1,
                    (int) Math.ceil(dano * multiplicador)
            );
            jugador.recibirDano(danoAumentado);
        }
    }

    private void actualizarProyectiles(double deltaTiempo) {
        Iterator<Proyectil> iterador = proyectiles.iterator();

        while (iterador.hasNext()) {
            Proyectil proyectil = iterador.next();
            proyectil.actualizar(deltaTiempo);

            if (proyectil.estaFueraDeLaSala()) {
                vista.quitarProyectil(proyectil);
                iterador.remove();
                continue;
            }

            verificarImpacto(proyectil, iterador);
        }
    }

    private void verificarImpacto(
            Proyectil proyectil,
            Iterator<Proyectil> iterador) {

        for (EnemigoVisual enemigoVisual :
                new ArrayList<>(enemigosVisuales)) {

            double distancia = calcularDistancia(
                    proyectil.getX(),
                    proyectil.getY(),
                    enemigoVisual.getX(),
                    enemigoVisual.getY()
            );

            double radioExtra =
                    enemigoVisual.getEnemigo() instanceof Jefe ? 16 : 0;

            if (distancia < RADIO_IMPACTO + radioExtra) {
                atacarEnemigo(enemigoVisual);

                vista.quitarProyectil(proyectil);
                iterador.remove();
                return;
            }
        }
    }

    private void atacarEnemigo(EnemigoVisual enemigoVisual) {
        Enemigo enemigo = enemigoVisual.getEnemigo();

        try {
            combateService.atacar(juegoService.getJugador(), enemigo);

            if (combateService.estaDerrotado(enemigo)) {
                vista.quitarEnemigo(enemigoVisual);
                enemigosVisuales.remove(enemigoVisual);

                juegoService.derrotarEnemigo(enemigo, jugadorService);
            }
        } catch (DanoInvalidoException e) {
            System.err.println("Error de combate: " + e.getMessage());
        }
    }

    private void verificarEstado() {
        if (!juegoService.jugadorEstaVivo()) {
            juegoTerminado = true;
            vista.actualizarEstado(
                    "GAME OVER - MAPA " + salaActual.mapa
            );
            detener();
            return;
        }

        boolean salaLimpia = enemigosVisuales.isEmpty();

        if (salaLimpia && !puertasAbiertas) {
            puertasAbiertas = true;

            vista.configurarSalidas(
                    salaActual.destinos.keySet(),
                    true
            );
        }

        if (!salaLimpia) {
            vista.actualizarEstado(
                    salaActual.esJefe
                            ? "DERROTÁ AL JEFE DEL MAPA " + salaActual.mapa
                            : "DERROTÁ A TODOS LOS ENEMIGOS"
            );
        } else {
            vista.actualizarEstado(
                    salaActual.esJefe
                            ? "JEFE DERROTADO - ELEGÍ LA SALIDA"
                            : "SALA LIMPIA - ELEGÍ UNA PUERTA"
            );
        }
    }

    private void actualizarHUD() {
        if (salaActual == null) {
            return;
        }

        vista.actualizarHUD(
                juegoService.getJugador(),
                enemigosVisuales.size(),
                salaActual.mapa,
                numeroSalaActual,
                numeroSalaActual,
                salaActual.esJefe
        );
    }

    private double calcularDistancia(
            double x1,
            double y1,
            double x2,
            double y2) {

        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);
    }
}
