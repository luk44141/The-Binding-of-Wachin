package com.wachin.service;

import com.wachin.model.Carta;
import com.wachin.model.CartaTipo;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Inventario de cartas de una partida y selección de recompensas. */
public final class CartaService {
    private final Map<CartaTipo, Integer> cantidades = new EnumMap<>(CartaTipo.class);
    private final Random random;
    public CartaService() { this(new Random()); }
    public CartaService(Random random) {
        if (random == null) throw new IllegalArgumentException("El generador aleatorio es obligatorio.");
        this.random = random;
    }

    public boolean puedeObtener(CartaTipo carta) {
        return carta != null && (!carta.esUnica() || !cantidades.containsKey(carta));
    }
    public boolean registrar(CartaTipo carta) {
        if (!puedeObtener(carta)) return false;
        cantidades.merge(carta, 1, Integer::sum);
        return true;
    }
    public List<CartaTipo> elegirTresCartasDiferentes() {
        List<CartaTipo> basicas = new ArrayList<>(List.of(CartaTipo.values()).stream()
                .filter(c -> !c.esUnica()).toList());
        java.util.Collections.shuffle(basicas, random);
        return List.copyOf(basicas.subList(0, 3));
    }
    public List<Carta> obtenerCartas() {
        return cantidades.entrySet().stream().map(e -> new Carta(e.getKey(), e.getValue())).toList();
    }
    public int cantidad(CartaTipo tipo) { return cantidades.getOrDefault(tipo, 0); }
    public Map<CartaTipo, Integer> instantanea() { return Map.copyOf(cantidades); }
    public void restaurar(Map<CartaTipo, Integer> estado) {
        cantidades.clear();
        if (estado == null) return;
        estado.forEach((tipo, cantidad) -> {
            if (tipo == null || cantidad == null || cantidad < 1 || (tipo.esUnica() && cantidad > 1)) {
                throw new IllegalArgumentException("El guardado contiene acumulaciones de carta inválidas.");
            }
            cantidades.put(tipo, cantidad);
        });
    }
}
