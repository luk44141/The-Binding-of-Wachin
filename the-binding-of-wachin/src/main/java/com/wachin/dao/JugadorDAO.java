package com.wachin.dao;

import com.wachin.model.Jugador;
import java.util.List;

public interface JugadorDAO {

    void guardar(Jugador jugador);

    Jugador buscarPorId(int id);

    List<Jugador> listarTodo();

    void actualizar(Jugador jugador);

    void eliminar(int id);
}