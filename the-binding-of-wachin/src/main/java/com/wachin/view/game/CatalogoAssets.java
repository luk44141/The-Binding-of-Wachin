package com.wachin.view.game;

import com.wachin.ConfiguracionPartida;
import com.wachin.model.CartaTipo;
import com.wachin.model.Jefe;
import com.wachin.model.ObjetoSuelo;
import com.wachin.model.TipoInvocacion;
import com.wachin.model.TipoObjetoSuelo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Única fuente de verdad de las rutas de recursos gráficos.
 *
 * <p>Cada método devuelve la lista de rutas candidatas, en orden de preferencia. La primera ruta
 * es siempre el nombre documentado en {@code ASSETS.md}; las siguientes son alternativas
 * (nombres históricos o sprites equivalentes del mundo). Si ninguna existe, la vista dibuja una
 * alternativa por código, de modo que ningún PNG es imprescindible. Esta clase no depende de
 * JavaFX para poder verificarse en pruebas.</p>
 */
public final class CatalogoAssets {

    public static final String RAIZ = "assets/images/";

    public static final String PISO = RAIZ + "environment/floors/floor.png";
    public static final String PISO_ALTERNATIVO = RAIZ + "environment/floors/floor_alt.png";
    public static final String PARED = RAIZ + "environment/walls/wall.png";
    public static final String PUERTA_CERRADA = RAIZ + "environment/doors/closed.png";
    public static final String PUERTA_ABIERTA = RAIZ + "environment/doors/open.png";
    public static final String PUERTA_JEFE_CERRADA = RAIZ + "environment/doors/boss_closed.png";
    public static final String PUERTA_JEFE_ABIERTA = RAIZ + "environment/doors/boss_open.png";

    public static final String PROYECTIL_TIRADOR = RAIZ + "projectiles/enemies/tirador.png";
    public static final String PROYECTIL_JEFE = RAIZ + "projectiles/enemies/boss.png";
    public static final String LASER_ROBOT = RAIZ + "projectiles/summons/robot/laser.png";

    public static final String ENEMIGO_POLI = RAIZ + "enemies/poli.png";
    public static final String ENEMIGO_TIRADOR = RAIZ + "enemies/ranged/tirador.png";

    /** Nombres históricos que el cargador sigue aceptando si el archivo documentado no existe. */
    private static final Map<String, List<String>> HISTORICOS = new LinkedHashMap<>();
    static {
        HISTORICOS.put(PISO, List.of(RAIZ + "environment/floors/floors.png"));
        HISTORICOS.put(PISO_ALTERNATIVO, List.of(RAIZ + "environment/floors/ReColorFloors.png"));
        HISTORICOS.put(PARED, List.of(RAIZ + "environment/walls/walls.png"));
        HISTORICOS.put(PUERTA_CERRADA, List.of(RAIZ + "environment/doors/doors.png"));
    }

    private CatalogoAssets() { }

    /** Devuelve la ruta documentada seguida de sus nombres históricos aceptados. */
    public static List<String> conHistoricos(String rutaDocumentada) {
        List<String> lista = new ArrayList<>();
        lista.add(rutaDocumentada);
        lista.addAll(HISTORICOS.getOrDefault(rutaDocumentada, List.of()));
        return List.copyOf(lista);
    }

    public static List<String> personaje(ConfiguracionPartida.Personaje personaje) {
        return List.of(personaje.getRuta());
    }

    public static List<String> proyectil(ConfiguracionPartida.TipoProyectil tipo) {
        return List.of(tipo.getRuta());
    }

    public static List<String> enemigo(String tipoEnemigo) {
        return List.of("TIRADOR".equals(tipoEnemigo) ? ENEMIGO_TIRADOR : ENEMIGO_POLI);
    }

    public static List<String> jefe(Jefe.Tipo tipo) {
        return List.of(switch (tipo) {
            case SALA_10 -> RAIZ + "bosses/job.png";
            case EMISOR_RAYOS -> RAIZ + "bosses/emisor_rayos.png";
            case SALA_15 -> RAIZ + "bosses/palinni.png";
            case SALA_20 -> RAIZ + "bosses/demonboss.png";
        });
    }

    public static List<String> invocacion(TipoInvocacion tipo) {
        return List.of(tipo.getRutaAsset());
    }

    public static List<String> carta(String codigo) {
        return List.of(RAIZ + "cards/" + codigo.toLowerCase(Locale.ROOT) + ".png");
    }

    public static List<String> carta(CartaTipo tipo) {
        return carta(tipo.name());
    }

    /**
     * Rutas candidatas para el icono de un objeto del suelo: primero el icono propio y, si no
     * existe, el sprite equivalente que ya se usa en el mundo (por ejemplo, el del proyectil).
     */
    public static List<String> objetoSuelo(ObjetoSuelo objeto) {
        return objetoSuelo(objeto.getTipo(), objeto.getCodigo());
    }

    public static List<String> objetoSuelo(TipoObjetoSuelo tipo, String codigo) {
        String nombre = codigo.toLowerCase(Locale.ROOT);
        List<String> rutas = new ArrayList<>();
        rutas.add(RAIZ + "pickups/" + carpetaPickup(tipo) + "/" + nombre + ".png");
        switch (tipo) {
            case PROYECTIL -> rutas.add(RAIZ + "projectiles/" + nombre + ".png");
            case INVOCACION -> {
                for (TipoInvocacion invocacion : TipoInvocacion.values()) {
                    if (invocacion.name().equals(codigo)) rutas.add(invocacion.getRutaAsset());
                }
            }
            case CARTA -> rutas.add(RAIZ + "cards/" + nombre + ".png");
            case RECOMPENSA -> rutas.add(RAIZ + "rewards/" + nombre + ".png");
        }
        return List.copyOf(rutas);
    }

    private static String carpetaPickup(TipoObjetoSuelo tipo) {
        return switch (tipo) {
            case PROYECTIL -> "projectiles";
            case INVOCACION -> "summons";
            case CARTA -> "cards";
            case RECOMPENSA -> "rewards";
        };
    }

    /**
     * Todas las rutas que el código puede solicitar, para comprobar que {@code ASSETS.md} las
     * documenta. Incluye las de cada enumeración y las del entorno.
     */
    public static List<String> rutasDocumentadas() {
        List<String> rutas = new ArrayList<>();
        for (ConfiguracionPartida.Personaje p : ConfiguracionPartida.Personaje.values()) rutas.addAll(personaje(p));
        for (ConfiguracionPartida.TipoProyectil p : ConfiguracionPartida.TipoProyectil.values()) rutas.addAll(proyectil(p));
        rutas.add(ENEMIGO_POLI);
        rutas.add(ENEMIGO_TIRADOR);
        for (Jefe.Tipo tipo : Jefe.Tipo.values()) rutas.addAll(jefe(tipo));
        for (TipoInvocacion tipo : TipoInvocacion.values()) rutas.addAll(invocacion(tipo));
        rutas.add(PROYECTIL_TIRADOR);
        rutas.add(PROYECTIL_JEFE);
        rutas.add(LASER_ROBOT);
        for (CartaTipo tipo : CartaTipo.values()) rutas.addAll(carta(tipo));
        rutas.addAll(carta("SELECCION_CARTAS"));
        rutas.add(PISO);
        rutas.add(PISO_ALTERNATIVO);
        rutas.add(PARED);
        rutas.add(PUERTA_CERRADA);
        rutas.add(PUERTA_ABIERTA);
        rutas.add(PUERTA_JEFE_CERRADA);
        rutas.add(PUERTA_JEFE_ABIERTA);
        return List.copyOf(rutas);
    }
}
