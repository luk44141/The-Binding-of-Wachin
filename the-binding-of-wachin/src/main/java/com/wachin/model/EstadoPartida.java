package com.wachin.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Instantánea completa y versionable del estado necesario para continuar una partida. */
public record EstadoPartida(
        String personaje, JugadorEstado jugador, int salaGlobal, int mapa, int profundidad,
        List<String> salidas, String entrada, boolean puertasAbiertas, Set<Integer> recompensasEntregadas,
        List<EnemigoEstado> enemigos, List<InvocacionEstado> invocaciones,
        Map<String, Integer> cartas, List<ObjetoEstado> objetos,
        List<ProyectilEstado> proyectiles, JefeEstado estadoJefe) implements Serializable {

    public EstadoPartida {
        if (personaje == null || jugador == null || salaGlobal < 1 || mapa < 1 || profundidad < 1) {
            throw new IllegalArgumentException("Estado de partida incompleto.");
        }
        salidas = List.copyOf(salidas == null ? List.of() : salidas);
        recompensasEntregadas = Set.copyOf(recompensasEntregadas == null ? Set.of() : recompensasEntregadas);
        enemigos = List.copyOf(enemigos == null ? List.of() : enemigos);
        invocaciones = List.copyOf(invocaciones == null ? List.of() : invocaciones);
        cartas = Map.copyOf(cartas == null ? Map.of() : cartas);
        objetos = List.copyOf(objetos == null ? List.of() : objetos);
        proyectiles = List.copyOf(proyectiles == null ? List.of() : proyectiles);
    }

    public record JugadorEstado(String nombre, int vida, int vidaMaxima, int vidaInicial,
            int dano, int experiencia, int nivel, int monedas, int energia, int energiaMaxima,
            double x, double y, String proyectil, double velocidad, int bonoDano,
            int bonoDanoInvocaciones, boolean segundoEspacio) implements Serializable { }
    public record EnemigoEstado(String nombre, String tipo, int vida, int vidaMaxima,
            int dano, double velocidad, double x, double y, String tipoJefe,
            int nivelJefe, String ataqueEspecial, int danoAdicional, double tiempoAtaque) implements Serializable { }
    public record InvocacionEstado(String tipo, int vida, int vidaMaxima, int dano,
            double energia, double energiaMaxima, double x, double y, double tiempoAtaque,
            String estado, boolean proteccionActiva) implements Serializable { }
    public record ObjetoEstado(long id, String tipo, String codigo, int sala,
            double x, double y) implements Serializable { }
    public record ProyectilEstado(double x, double y, double velocidadX, double velocidadY,
            int dano, String ruta, String origen) implements Serializable { }
    public record JefeEstado(String fase, String ataque, double tiempo, double direccionX,
            double direccionY, int ultimoAtaque, int cadena, boolean segundaFase,
            List<Double> zonasX, List<Double> zonasY) implements Serializable {
        public JefeEstado {
            zonasX = List.copyOf(zonasX == null ? List.of() : zonasX);
            zonasY = List.copyOf(zonasY == null ? List.of() : zonasY);
        }
    }
}
