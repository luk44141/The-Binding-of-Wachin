package com.wachin.view.game;

import com.wachin.model.CartaTipo;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.view.ui.BarraRecurso;
import com.wachin.view.ui.EtiquetaContorno;
import com.wachin.view.ui.Tema;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/** Interfaz durante la partida: vida, experiencia, invocaciones, cartas, progreso y avisos. */
public final class HudView {

    /** Tono visual de un aviso. */
    public enum Tono { NORMAL, BUENO, MALO, ESPECIAL }

    private static final double ANCHO = PartidaView.ANCHO;

    private final Pane raiz = new Pane();
    private final EtiquetaContorno textoVida = new EtiquetaContorno(Tema.cuerpo(12, true), Color.WHITE, 1.2);
    private final EtiquetaContorno textoNivel = new EtiquetaContorno(Tema.cuerpo(11, true), Tema.ORO_CLARO, 1.1);
    private final EtiquetaContorno textoXp = new EtiquetaContorno(Tema.cuerpo(9, true), Color.WHITE, 1.0);
    private final BarraRecurso barraVida = new BarraRecurso(226, 17, Color.web("#ff5a72"), Color.web("#a8162b"));
    private final BarraRecurso barraXp = new BarraRecurso(226, 10, Color.web("#ffe08a"), Color.web("#c89a2a"));
    private final Text textoMapa = Tema.texto("", 14, Tema.TEXTO, true);
    private final Text textoEnemigos = Tema.texto("", 13, Tema.TEXTO_TENUE, true);
    private final List<Node> pips = new ArrayList<>();
    private final Group zonaPips = new Group();
    private final Group slotsInvocacion = new Group();
    private final Group fichasCartas = new Group();
    private final ImageView iconoProyectil = new ImageView();
    private final Text textoProyectil = Tema.texto("", 13, Tema.TEXTO, true);
    private final EtiquetaContorno etiquetaFps = new EtiquetaContorno(Tema.cuerpo(12, true), Tema.VERDE, 1.1);
    private final EtiquetaContorno objetivo = new EtiquetaContorno(Tema.cuerpo(14, true), Tema.TEXTO_TENUE, 1.3);
    private final Group grupoJefe = new Group();
    private final EtiquetaContorno nombreJefe = new EtiquetaContorno(Tema.cuerpo(14, true), Tema.ORO_CLARO, 1.3);
    private final BarraRecurso barraJefe = new BarraRecurso(440, 15, Color.web("#ffd873"), Color.web("#b3841f"));
    private final Group avisoGrupo = new Group();
    private final Rectangle avisoFondo = new Rectangle();
    private final EtiquetaContorno avisoTexto = new EtiquetaContorno(Tema.cuerpo(17, true), Color.WHITE, 1.4);
    private final Group cartel = new Group();
    private final EtiquetaContorno cartelTitulo = new EtiquetaContorno(Tema.titulo(54), Tema.ORO_CLARO, 3);
    private final EtiquetaContorno cartelSubtitulo = new EtiquetaContorno(Tema.cuerpo(18, true), Tema.TEXTO, 1.5);
    private final LinkedList<String[]> colaAvisos = new LinkedList<>();
    private final List<SlotInvocacion> slots = new ArrayList<>();

    private double tiempoAviso;
    private double tiempoCartel;
    private double duracionCartel;
    private int ultimaVida = -1;
    private int ultimaVidaMaxima = -1;
    private int ultimoNivel = -1;
    private int ultimaExperiencia = -1;
    private int ultimaExperienciaSiguiente = -1;
    private int ultimosEnemigos = -1;
    private int ultimaSala = -1;
    private int ultimoMapaNumero = -1;
    private int ultimaProfundidad = -1;
    private boolean ultimoJefe;
    private Map<CartaTipo, Integer> ultimasCartas = new EnumMap<>(CartaTipo.class);
    private String ultimoProyectil = "";

    /** Ranura que muestra una invocación equipada. */
    private static final class SlotInvocacion {
        final Group nodo = new Group();
        final ImageView icono = new ImageView();
        final Text nombre = Tema.texto("", 11, Color.WHITE, true);
        final BarraRecurso vida = new BarraRecurso(104, 8, Color.web("#ff5a72"), Color.web("#a8162b"));
        final BarraRecurso energia = new BarraRecurso(104, 6, Color.web("#8cf2ff"), Color.web("#2a9fb4"));
        Invocacion actual;
    }

    public HudView(Image retrato) {
        raiz.setPickOnBounds(false);
        raiz.setMouseTransparent(true);
        construirBarraSuperior(retrato);
        construirBarraInferior();
        construirJefe();
        construirAvisos();
        etiquetaFps.setLayoutX(318);
        etiquetaFps.setLayoutY(64);
        etiquetaFps.setVisible(false);
        raiz.getChildren().addAll(etiquetaFps, objetivo, grupoJefe, avisoGrupo, cartel);
        cachearEstaticos(raiz);
    }

    public Pane getRaiz() { return raiz; }

    /**
     * Marca como bitmap en caché los nodos que casi nunca cambian (placas y textos con contorno).
     * Sin esto JavaFX los vuelve a rasterizar en cada fotograma porque el mundo cambia constantemente.
     */
    private static void cachearEstaticos(Node nodo) {
        if (nodo instanceof BarraRecurso || nodo instanceof EtiquetaContorno) return;
        if (nodo instanceof Text || nodo instanceof Rectangle || nodo instanceof Polygon || nodo instanceof Circle) {
            nodo.setCache(true);
            nodo.setCacheHint(javafx.scene.CacheHint.SPEED);
        }
        if (nodo instanceof javafx.scene.Parent padre) {
            for (Node hijo : padre.getChildrenUnmodifiable()) cachearEstaticos(hijo);
        }
    }

    private static Rectangle placa(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(14);
        r.setArcHeight(14);
        r.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1d1929", 0.9)), new Stop(1, Color.web("#0d0b14", 0.9))));
        r.setStroke(Color.web("#4a4066"));
        r.setStrokeWidth(1.5);
        return r;
    }

    private void construirBarraSuperior(Image retrato) {
        Rectangle placaIzq = placa(8, 4, 300, 42);
        Circle marco = new Circle(30, 24, 17);
        marco.setFill(Color.web("#2a2238"));
        marco.setStroke(Tema.ORO);
        marco.setStrokeWidth(2);
        Group retratoGrupo = new Group(marco);
        if (retrato != null) {
            ImageView vista = new ImageView(retrato);
            vista.setFitWidth(28);
            vista.setFitHeight(28);
            vista.setPreserveRatio(true);
            vista.setSmooth(false);
            vista.setX(30 - 14);
            vista.setY(24 - 14);
            retratoGrupo.getChildren().add(vista);
        }
        barraVida.setLayoutX(62);
        barraVida.setLayoutY(9);
        barraVida.setMarcas(10);
        barraXp.setLayoutX(62);
        barraXp.setLayoutY(30);
        textoVida.setLayoutX(62 + 113);
        textoVida.setLayoutY(22);
        Circle insignia = new Circle(45, 37, 8.5);
        insignia.setFill(Color.web("#2a2238"));
        insignia.setStroke(Tema.ORO);
        insignia.setStrokeWidth(1.6);
        textoNivel.setLayoutY(41);
        textoXp.setFill(Color.WHITE);
        textoXp.setLayoutY(38);
        Rectangle placaDer = placa(ANCHO - 308, 4, 300, 42);
        textoMapa.setTextAlignment(TextAlignment.RIGHT);
        textoEnemigos.setTextAlignment(TextAlignment.RIGHT);
        zonaPips.setLayoutX(ANCHO - 22);
        zonaPips.setLayoutY(35);
        raiz.getChildren().addAll(placaIzq, retratoGrupo, insignia, barraVida, barraXp, textoVida, textoNivel, textoXp,
                placaDer, textoMapa, textoEnemigos, zonaPips);
    }

    private void construirBarraInferior() {
        Rectangle placaCartas = placa(ANCHO / 2 - 170, 654, 340, 40);
        placaCartas.setOpacity(0.0);
        slotsInvocacion.setLayoutX(10);
        slotsInvocacion.setLayoutY(654);
        for (int i = 0; i < 2; i++) {
            SlotInvocacion slot = new SlotInvocacion();
            Rectangle fondo = placa(0, 0, 176, 40);
            slot.icono.setFitWidth(28);
            slot.icono.setFitHeight(28);
            slot.icono.setPreserveRatio(true);
            slot.icono.setSmooth(false);
            slot.icono.setX(6);
            slot.icono.setY(6);
            slot.nombre.setX(40);
            slot.nombre.setY(14);
            slot.vida.setLayoutX(40);
            slot.vida.setLayoutY(18);
            slot.energia.setLayoutX(40);
            slot.energia.setLayoutY(28);
            slot.nodo.getChildren().addAll(fondo, slot.icono, slot.nombre, slot.vida, slot.energia);
            slot.nodo.setLayoutX(i * 184);
            slots.add(slot);
            slotsInvocacion.getChildren().add(slot.nodo);
        }
        fichasCartas.setLayoutX(ANCHO / 2 - 40);
        fichasCartas.setLayoutY(660);
        Rectangle placaProyectil = placa(ANCHO - 170, 654, 160, 40);
        iconoProyectil.setFitWidth(26);
        iconoProyectil.setFitHeight(26);
        iconoProyectil.setPreserveRatio(true);
        iconoProyectil.setSmooth(false);
        iconoProyectil.setX(ANCHO - 160);
        iconoProyectil.setY(661);
        textoProyectil.setX(ANCHO - 126);
        textoProyectil.setY(679);
        Text ayuda = Tema.texto("WASD mover · FLECHAS disparar · ESC pausa", 10, Tema.TEXTO_TENUE, false);
        ayuda.setX(ANCHO - 292);
        ayuda.setY(692);
        ayuda.setOpacity(0.0);
        raiz.getChildren().addAll(slotsInvocacion, fichasCartas, placaProyectil, iconoProyectil, textoProyectil);
    }

    private void construirJefe() {
        Rectangle marco = new Rectangle(-6, -2, 452, 21);
        marco.setArcWidth(12);
        marco.setArcHeight(12);
        marco.setFill(Color.web("#0b0911", 0.92));
        marco.setStroke(Tema.ORO);
        marco.setStrokeWidth(2);
        nombreJefe.setX(0);
        nombreJefe.setY(-9);
        Polygon adornoIzq = new Polygon(-14, 8, -6, 0, -6, 17);
        Polygon adornoDer = new Polygon(454, 8, 446, 0, 446, 17);
        adornoIzq.setFill(Tema.ORO);
        adornoDer.setFill(Tema.ORO);
        grupoJefe.getChildren().addAll(marco, barraJefe, nombreJefe, adornoIzq, adornoDer);
        grupoJefe.setLayoutX(ANCHO / 2 - 220);
        grupoJefe.setLayoutY(78);
        barraJefe.setMarcas(4);
        grupoJefe.setVisible(false);
    }

    private void construirAvisos() {
        avisoFondo.setArcWidth(18);
        avisoFondo.setArcHeight(18);
        avisoFondo.setHeight(34);
        avisoGrupo.getChildren().addAll(avisoFondo, avisoTexto);
        avisoGrupo.setOpacity(0);
        cartelTitulo.setEffect(Tema.brillo(Color.web("#e6b84a", 0.7), 24));
        cartel.getChildren().addAll(cartelTitulo, cartelSubtitulo);
        cartel.setOpacity(0);
    }

    // ───────────────────────────── datos ─────────────────────────────

    /** Actualiza los datos del HUD; solo toca los nodos cuyo contenido cambió. */
    public void actualizar(Jugador jugador, int enemigos, int mapa, int sala, int profundidad, boolean esJefe,
            int xpSiguiente, List<Invocacion> invocaciones, Map<CartaTipo, Integer> cartas) {
        if (jugador == null) return;
        barraVida.establecer(jugador.getVida(), jugador.getVidaMaxima());
        barraXp.establecer(jugador.getExperiencia(), xpSiguiente);
        if (jugador.getVida() != ultimaVida || jugador.getVidaMaxima() != ultimaVidaMaxima
                || jugador.getNivel() != ultimoNivel || jugador.getExperiencia() != ultimaExperiencia
                || xpSiguiente != ultimaExperienciaSiguiente) {
            ultimaVida = jugador.getVida();
            ultimaVidaMaxima = jugador.getVidaMaxima();
            ultimoNivel = jugador.getNivel();
            ultimaExperiencia = jugador.getExperiencia();
            ultimaExperienciaSiguiente = xpSiguiente;
            textoVida.setText(ultimaVida + " / " + ultimaVidaMaxima);
            textoVida.setLayoutX(62 + 113 - textoVida.getAncho() / 2);
            textoNivel.setText(String.valueOf(jugador.getNivel()));
            textoNivel.setLayoutX(45 - textoNivel.getAncho() / 2);
            textoXp.setText("XP " + jugador.getExperiencia() + " / " + xpSiguiente);
            textoXp.setLayoutX(144 - textoXp.getAncho() / 2);
        }
        if (mapa != ultimoMapaNumero || sala != ultimaSala || profundidad != ultimaProfundidad || esJefe != ultimoJefe) {
            ultimoMapaNumero = mapa;
            ultimaSala = sala;
            ultimaProfundidad = profundidad;
            ultimoJefe = esJefe;
            textoMapa.setText("MAPA " + mapa + "  ·  SALA " + sala);
            textoMapa.setLayoutX(ANCHO - 20 - textoMapa.getLayoutBounds().getWidth());
            textoMapa.setLayoutY(21);
            reconstruirPips(profundidad);
        }
        if (enemigos != ultimosEnemigos || esJefe != ultimoJefe || textoEnemigos.getText().isEmpty()) {
            ultimosEnemigos = enemigos;
            textoEnemigos.setText((esJefe ? "JEFE" : "ENEMIGOS") + "  " + enemigos);
            textoEnemigos.setLayoutX(ANCHO - 140 - textoEnemigos.getLayoutBounds().getWidth());
            textoEnemigos.setLayoutY(40);
            textoEnemigos.setFill(enemigos == 0 ? Tema.VERDE : Tema.TEXTO_TENUE);
        }
        actualizarSlots(jugador, invocaciones);
        actualizarCartas(cartas);
        String proyectil = jugador.getProyectilEquipado().name() + jugador.getBonoDanoProyectiles();
        if (!proyectil.equals(ultimoProyectil)) {
            ultimoProyectil = proyectil;
            Image imagen = AssetVisual.imagen(List.of(jugador.getProyectilEquipado().getRutaAsset()),
                    "proyectil_hud_" + jugador.getProyectilEquipado(),
                    () -> Dibujos.orbe(jugador.getProyectilEquipado() == com.wachin.model.ProyectilEquipable.LAGRIMA
                            ? Color.web("#4fd6e8") : Color.web("#ffc93a")));
            iconoProyectil.setImage(imagen);
            textoProyectil.setText(jugador.getProyectilEquipado().getNombre().toUpperCase() + "  "
                    + (jugador.getProyectilEquipado().getDano() + jugador.getBonoDanoProyectiles()));
        }
    }

    private void reconstruirPips(int profundidad) {
        zonaPips.getChildren().clear();
        pips.clear();
        for (int i = 5; i >= 1; i--) {
            double x = -(5 - i) * 20;
            Node pip;
            boolean ultimo = i == 5;
            boolean actual = i == profundidad;
            boolean pasado = i < profundidad;
            Color color = actual ? Tema.ORO_CLARO : pasado ? Color.web("#6a5a8f") : Color.web("#2a2438");
            if (ultimo) {
                Polygon rombo = new Polygon(0, -8, 8, 0, 0, 8, -8, 0);
                rombo.setFill(actual ? Tema.ROJO_VIVO : pasado ? Color.web("#7a1a2a") : Color.web("#2a1018"));
                rombo.setStroke(Tema.ROJO);
                rombo.setTranslateX(x);
                pip = rombo;
            } else {
                Circle punto = new Circle(x, 0, 5.5, color);
                punto.setStroke(Color.web("#0b0911"));
                punto.setStrokeWidth(1.5);
                pip = punto;
            }
            pips.add(pip);
            zonaPips.getChildren().add(pip);
        }
    }

    private void actualizarSlots(Jugador jugador, List<Invocacion> invocaciones) {
        for (int i = 0; i < slots.size(); i++) {
            SlotInvocacion slot = slots.get(i);
            Invocacion inv = i < invocaciones.size() ? invocaciones.get(i) : null;
            boolean bloqueado = i == 1 && !jugador.tieneSegundoEspacioInvocacion();
            slot.nodo.setVisible(true);
            slot.nodo.setOpacity(inv != null ? 1 : bloqueado ? 0.28 : 0.55);
            if (inv != slot.actual) {
                slot.actual = inv;
                if (inv == null) {
                    slot.icono.setImage(null);
                    slot.nombre.setText(bloqueado ? "Ranura 2 (Pacto)" : "Ranura vacía");
                } else {
                    slot.icono.setImage(AssetVisual.imagen(CatalogoAssets.invocacion(inv.getTipoInvocacion()),
                            "invocacion_" + inv.getTipoInvocacion(), () -> Dibujos.invocacion(inv.getTipoInvocacion())));
                    slot.nombre.setText(inv.getTipoInvocacion().getNombre());
                }
            }
            slot.vida.setVisible(inv != null);
            slot.energia.setVisible(inv != null);
            if (inv != null) {
                slot.vida.establecer(inv.getVida(), inv.getVidaMaxima());
                slot.energia.establecer(inv.getEnergia(), inv.getEnergiaMaxima());
            }
        }
    }

    private void actualizarCartas(Map<CartaTipo, Integer> cartas) {
        if (Objects.equals(cartas, ultimasCartas)) return;
        ultimasCartas = new EnumMap<>(CartaTipo.class);
        ultimasCartas.putAll(cartas);
        fichasCartas.getChildren().clear();
        double x = 0;
        for (Map.Entry<CartaTipo, Integer> e : ultimasCartas.entrySet()) {
            Rectangle ficha = placa(x, 0, 50, 30);
            ImageView icono = new ImageView(AssetVisual.imagen(CatalogoAssets.carta(e.getKey()),
                    "carta_" + e.getKey(), () -> Dibujos.iconoCarta(e.getKey())));
            icono.setFitWidth(22);
            icono.setFitHeight(22);
            icono.setPreserveRatio(true);
            icono.setX(x + 4);
            icono.setY(4);
            Text cantidad = Tema.texto("x" + e.getValue(), 12, Tema.ORO_CLARO, true);
            cantidad.setX(x + 28);
            cantidad.setY(20);
            fichasCartas.getChildren().addAll(ficha, icono, cantidad);
            x += 56;
        }
        fichasCartas.setLayoutX(ANCHO / 2 - Math.max(0, x - 6) / 2);
    }

    public void mostrarFps(boolean visible) { etiquetaFps.setVisible(visible); }

    public void actualizarFps(int fps) {
        if (!etiquetaFps.isVisible()) return;
        etiquetaFps.setText(fps + " FPS");
        etiquetaFps.setFill(fps >= 100 ? Tema.VERDE : fps >= 55 ? Tema.ORO_CLARO : Tema.ROJO_VIVO);
    }

    public void fijarObjetivo(String texto) {
        if (texto.equals(objetivo.getText())) return;
        objetivo.setText(texto);
        objetivo.setX(ANCHO / 2 - objetivo.getAncho() / 2);
        objetivo.setY(grupoJefe.isVisible() ? 118 : 86);
    }

    public void mostrarBarraJefe(Enemigo jefe) {
        grupoJefe.setVisible(true);
        nombreJefe.setText(jefe.getNombre().toUpperCase());
        barraJefe.establecer(jefe.getVida(), jefe.getVidaMaxima());
        objetivo.setY(118);
    }

    public void ocultarBarraJefe() {
        grupoJefe.setVisible(false);
        objetivo.setY(86);
    }

    // ───────────────────────────── avisos y carteles ─────────────────────────────

    public void mostrarAviso(String mensaje, Tono tono) {
        if (mensaje == null || mensaje.isBlank()) return;
        if (!colaAvisos.isEmpty() && colaAvisos.getLast()[0].equals(mensaje)) return;
        if (colaAvisos.size() > 3) colaAvisos.removeFirst();
        colaAvisos.add(new String[]{mensaje, tono.name()});
    }

    /** Cartel grande y breve (jefe, subida de nivel, victoria). */
    public void mostrarCartel(String titulo, String subtitulo, double segundos) {
        cartelTitulo.setText(titulo);
        cartelSubtitulo.setText(subtitulo == null ? "" : subtitulo);
        cartelTitulo.setX(ANCHO / 2 - cartelTitulo.getAncho() / 2);
        cartelTitulo.setY(300);
        cartelSubtitulo.setX(ANCHO / 2 - cartelSubtitulo.getAncho() / 2);
        cartelSubtitulo.setY(338);
        tiempoCartel = duracionCartel = segundos;
    }

    public void animar(double dt) {
        barraVida.actualizar(dt);
        barraXp.actualizar(dt);
        barraJefe.actualizar(dt);
        for (SlotInvocacion slot : slots) {
            if (slot.actual != null) { slot.vida.actualizar(dt); slot.energia.actualizar(dt); }
        }
        animarAviso(dt);
        animarCartel(dt);
    }

    private void animarAviso(double dt) {
        if (tiempoAviso > 0) {
            tiempoAviso -= dt;
            double restante = Math.max(0, tiempoAviso);
            double aparecer = Math.min(1, (2.4 - restante) / 0.18);
            avisoGrupo.setOpacity(Math.min(aparecer, Math.min(1, restante / 0.4)));
            avisoGrupo.setTranslateY((1 - aparecer) * -8);
            return;
        }
        avisoGrupo.setOpacity(0);
        if (colaAvisos.isEmpty()) return;
        String[] siguiente = colaAvisos.removeFirst();
        Tono tono = Tono.valueOf(siguiente[1]);
        avisoTexto.setText(siguiente[0]);
        double ancho = avisoTexto.getAncho() + 36;
        avisoFondo.setWidth(ancho);
        avisoFondo.setX(ANCHO / 2 - ancho / 2);
        avisoFondo.setY(grupoJefe.isVisible() ? 140 : 108);
        avisoTexto.setX(ANCHO / 2 - ancho / 2 + 18);
        avisoTexto.setY(avisoFondo.getY() + 23);
        Color acento = switch (tono) {
            case NORMAL -> Color.web("#9a93ad");
            case BUENO -> Tema.VERDE;
            case MALO -> Tema.ROJO_VIVO;
            case ESPECIAL -> Tema.ORO_CLARO;
        };
        avisoFondo.setFill(Color.web("#0d0b14", 0.88));
        avisoFondo.setStroke(acento);
        avisoFondo.setStrokeWidth(1.8);
        avisoTexto.setFill(tono == Tono.NORMAL ? Tema.TEXTO : acento.brighter());
        tiempoAviso = 2.4;
    }

    private void animarCartel(double dt) {
        if (tiempoCartel <= 0) { cartel.setOpacity(0); return; }
        tiempoCartel -= dt;
        double progreso = 1 - Math.max(0, tiempoCartel) / duracionCartel;
        double entrada = Math.min(1, progreso / 0.12);
        double salida = Math.min(1, Math.max(0, tiempoCartel) / (duracionCartel * 0.25));
        cartel.setOpacity(Math.min(entrada, salida));
        double escala = 1.35 - 0.35 * Math.min(1, progreso / 0.15) + progreso * 0.04;
        cartel.setScaleX(escala);
        cartel.setScaleY(escala);
        cartel.setTranslateX(ANCHO / 2 * (1 - escala));
        cartel.setTranslateY(320 * (1 - escala));
    }
}
