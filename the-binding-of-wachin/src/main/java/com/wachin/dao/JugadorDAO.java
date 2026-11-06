package com.wachin.dao;

import com.wachin.model.Jugador;
import java.util.List;

/**
 * Interfaz que abstrae las operaciones de persistencia específicas de jugadores.
 */
public interface JugadorDAO {

    /**
     * Inserta el estado mediante una consulta parametrizada y cierra los recursos JDBC.
     */
    void guardar(Jugador jugador);

    /**
     * Consulta por identificador y reconstruye el objeto encontrado.
     */
    Jugador buscarPorId(int id);

    /**
     * Consulta los registros disponibles y devuelve los objetos reconstruidos.
     */
    List<Jugador> listarTodo();

    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    void actualizar(Jugador jugador);

    /**
     * Borra el registro identificado mediante una consulta parametrizada.
     */
    void eliminar(int id);
}
