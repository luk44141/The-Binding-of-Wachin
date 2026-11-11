package com.wachin.service;

import com.wachin.dao.PartidaDAO;
import com.wachin.dao.PartidaDAOImpl;
import com.wachin.model.EstadoPartida;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Reglas y operaciones de los tres espacios locales de guardado. */
public final class PartidaService {
    private final PartidaDAO dao;
    public PartidaService() { this(new PartidaDAOImpl()); }
    public PartidaService(PartidaDAO dao) {
        if (dao == null) throw new IllegalArgumentException("El DAO de partidas es obligatorio.");
        this.dao = dao;
    }
    public List<PartidaDAO.Slot> listarSlots() { return ejecutar(dao::listarSlots); }
    public Optional<EstadoPartida> cargar(int slot) {
        validarSlot(slot);
        return ejecutar(() -> dao.cargar(slot));
    }
    public void guardar(int slot, String nombre, EstadoPartida estado) {
        validarSlot(slot);
        if (estado == null) throw new IllegalArgumentException("No se puede guardar un estado vacío.");
        ejecutar(() -> { dao.guardar(slot, nombre, estado); return null; });
    }
    public void eliminar(int slot) {
        validarSlot(slot);
        ejecutar(() -> { dao.eliminar(slot); return null; });
    }
    private void validarSlot(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Solo existen los slots locales 1, 2 y 3.");
    }
    private <T> T ejecutar(Operacion<T> operacion) {
        try { return operacion.ejecutar(); }
        catch (SQLException e) { throw new IllegalStateException("Error de persistencia MySQL: " + e.getMessage(), e); }
    }
    @FunctionalInterface private interface Operacion<T> { T ejecutar() throws SQLException; }
}
