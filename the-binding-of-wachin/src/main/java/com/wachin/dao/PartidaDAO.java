package com.wachin.dao;

import com.wachin.model.EstadoPartida;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistencia de los tres slots locales de partidas completas. */
public interface PartidaDAO {
    record Slot(int numero, String nombre, LocalDateTime fechaGuardado) {
        public boolean estaOcupado() { return nombre != null; }
    }
    List<Slot> listarSlots() throws SQLException;
    Optional<EstadoPartida> cargar(int slot) throws SQLException;
    void guardar(int slot, String nombre, EstadoPartida estado) throws SQLException;
    void eliminar(int slot) throws SQLException;
}
