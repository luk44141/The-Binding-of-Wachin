package com.wachin.dao;

import java.util.List;

import com.wachin.model.Entidad;

/**
 * Interfaz que abstrae las operaciones de persistencia comunes a las entidades.
 */
public interface EntidadDAO {

    /**
     * Inserta el estado mediante una consulta parametrizada y cierra los recursos JDBC.
     */
    void guardar(Entidad entidad);

    /**
     * Consulta por identificador y reconstruye el objeto encontrado.
     */
    Entidad buscarPorId(int id);

    /**
     * Consulta los registros disponibles y devuelve los objetos reconstruidos.
     */
    List<Entidad> listarTodo();

    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    void actualizar(Entidad entidad);

    /**
     * Borra el registro identificado mediante una consulta parametrizada.
     */
    void eliminar(int id);
}
 
