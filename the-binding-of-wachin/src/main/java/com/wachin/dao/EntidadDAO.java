package com.wachin.dao;

import java.util.List;

import com.wachin.model.Entidad;

public interface EntidadDAO {

    void guardar(Entidad entidad);

    Entidad buscarPorId(int id);

    List<Entidad> listarTodo();

    void actualizar(Entidad entidad);

    void eliminar(int id);
}
 