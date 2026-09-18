package com.wachin.dao;

import com.wachin.model.Entidad;
import java.util.List;

public interface EntidadDAO {

    void guardar(Entidad entidad);

    Entidad buscarPorId(int id);

    List<Entidad> listarTodo();

    void actualizar(Entidad entidad);

    void eliminar(int id);
}
