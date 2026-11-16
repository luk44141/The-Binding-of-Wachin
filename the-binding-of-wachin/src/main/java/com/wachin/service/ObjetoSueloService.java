package com.wachin.service;

import com.wachin.model.ObjetoSuelo;
import com.wachin.model.TipoObjetoSuelo;
import com.wachin.model.CartaTipo;
import com.wachin.model.TipoInvocacion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/** Administra objetos persistentes en el suelo y las reglas generales de recogida/aparición. */
public final class ObjetoSueloService {
    private static final double PROBABILIDAD_DROP_ENEMIGO = 0.02;
    private static final double PROBABILIDAD_CARTA_UNICA = 0.002;
    /** Probabilidad de que cualquier jefe suelte una invocación. */
    public static final double PROBABILIDAD_INVOCACION_JEFE = 0.75;
    private final Map<Long, ObjetoSuelo> objetos = new LinkedHashMap<>();
    private final Random random;

    public ObjetoSueloService() { this(new Random()); }
    public ObjetoSueloService(Random random) {
        if (random == null) throw new IllegalArgumentException("El generador aleatorio es obligatorio.");
        this.random = random;
    }

    public ObjetoSuelo crear(TipoObjetoSuelo tipo, String codigo, int sala, double x, double y) {
        ObjetoSuelo objeto = new ObjetoSuelo(tipo, codigo, sala, x, y);
        objetos.put(objeto.getId(), objeto);
        return objeto;
    }

    /** Los enemigos normales tienen una probabilidad baja y configurable de soltar un proyectil. */
    public Optional<ObjetoSuelo> crearDropEnemigo(int sala, double x, double y) {
        return crearDropEnemigo(sala, x, y, false);
    }

    public Optional<ObjetoSuelo> crearDropEnemigo(int sala, double x, double y,
            boolean puedeSoltarCartaUnica) {
        return crearDropEnemigo(sala, x, y, puedeSoltarCartaUnica, List.of());
    }

    public Optional<ObjetoSuelo> crearDropEnemigo(int sala, double x, double y,
            boolean puedeSoltarCartaUnica, List<TipoInvocacion> invocacionesPermitidas) {
        // Las cartas comunes pueden aparecer en el 5% de las muertes de enemigos normales.
        if (random.nextDouble() < 0.05) {
            List<CartaTipo> cartasComunes = java.util.Arrays.stream(CartaTipo.values())
                    .filter(carta -> !carta.esUnica()).toList();
            CartaTipo carta = cartasComunes.get(random.nextInt(cartasComunes.size()));
            return Optional.of(crear(TipoObjetoSuelo.CARTA, carta.name(), sala, x, y));
        }
        double tirada = random.nextDouble();
        if (puedeSoltarCartaUnica && tirada < PROBABILIDAD_CARTA_UNICA) {
            return Optional.of(crear(TipoObjetoSuelo.CARTA,
                    CartaTipo.SEGUNDO_ESPACIO_INVOCACION.name(), sala, x, y));
        }
        if (invocacionesPermitidas != null && !invocacionesPermitidas.isEmpty()
                && tirada >= (puedeSoltarCartaUnica ? PROBABILIDAD_CARTA_UNICA : 0)
                && tirada < 0.008) {
            TipoInvocacion tipo = invocacionesPermitidas.get(random.nextInt(invocacionesPermitidas.size()));
            return Optional.of(crear(TipoObjetoSuelo.INVOCACION, tipo.name(), sala, x, y));
        }
        double limiteProyectil = PROBABILIDAD_DROP_ENEMIGO;
        if (puedeSoltarCartaUnica && tirada < limiteProyectil + PROBABILIDAD_CARTA_UNICA) {
            String codigo = random.nextBoolean() ? "LAGRIMA" : "GOTA";
            return Optional.of(crear(TipoObjetoSuelo.PROYECTIL, codigo, sala, x, y));
        }
        if (!puedeSoltarCartaUnica && tirada < PROBABILIDAD_DROP_ENEMIGO) {
            String codigo = random.nextBoolean() ? "LAGRIMA" : "GOTA";
            return Optional.of(crear(TipoObjetoSuelo.PROYECTIL, codigo, sala, x, y));
        }
        return Optional.empty();
    }


    /** Todos los jefes sueltan una invocación con una probabilidad del 75%. */
    public Optional<ObjetoSuelo> crearDropInvocacionJefe(int sala, double x, double y,
            List<TipoInvocacion> invocacionesPermitidas) {
        if (random.nextDouble() >= PROBABILIDAD_INVOCACION_JEFE
                || invocacionesPermitidas == null || invocacionesPermitidas.isEmpty()) {
            return Optional.empty();
        }
        TipoInvocacion tipo = invocacionesPermitidas.get(random.nextInt(invocacionesPermitidas.size()));
        return Optional.of(crear(TipoObjetoSuelo.INVOCACION, tipo.name(), sala, x, y));
    }

    /** Crea el objeto que inicia la seleccion de una carta al recogerlo. */
    public ObjetoSuelo crearObjetoSeleccionCartas(int sala, double x, double y) {
        return crear(TipoObjetoSuelo.CARTA, "SELECCION_CARTAS", sala, x, y);
    }

    public List<ObjetoSuelo> listarSala(int sala) {
        return objetos.values().stream().filter(o -> o.getSalaGlobal() == sala).toList();
    }

    /** Devuelve candidatos cercanos; se retiran solo después de aplicar su efecto con éxito. */
    public List<ObjetoSuelo> recogerCercanos(int sala, double x, double y, double radio) {
        if (!Double.isFinite(radio) || radio < 0) throw new IllegalArgumentException("Radio de recogida inválido.");
        List<ObjetoSuelo> recogidos = new ArrayList<>();
        for (ObjetoSuelo objeto : listarSala(sala)) {
            if (Math.hypot(x - objeto.getX(), y - objeto.getY()) <= radio) recogidos.add(objeto);
        }
        return List.copyOf(recogidos);
    }

    public boolean retirar(long id) { return objetos.remove(id) != null; }
    public List<ObjetoSuelo> obtenerTodos() { return List.copyOf(objetos.values()); }
    public void restaurar(List<ObjetoSuelo> estado) {
        objetos.clear();
        if (estado != null) for (ObjetoSuelo objeto : estado) objetos.put(objeto.getId(), objeto);
    }
}
