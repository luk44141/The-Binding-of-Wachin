package com.wachin.dao;

import com.wachin.model.EstadoPartida;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO transaccional para slots locales y snapshots versionados. */
public final class PartidaDAOImpl implements PartidaDAO {
    private static final int VERSION = 1;

    @Override
    public List<Slot> listarSlots() throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            asegurarSlots(c);
            List<Slot> slots = new ArrayList<>();
            String sql = "SELECT s.slot_numero, p.nombre, p.fecha_guardado FROM partida_slots s "
                    + "LEFT JOIN partidas p ON p.id = s.partida_id ORDER BY s.slot_numero";
            try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    var fecha = rs.getTimestamp("fecha_guardado");
                    slots.add(new Slot(rs.getInt("slot_numero"), rs.getString("nombre"),
                            fecha == null ? null : fecha.toLocalDateTime()));
                }
            }
            return List.copyOf(slots);
        }
    }

    @Override
    public Optional<EstadoPartida> cargar(int slot) throws SQLException {
        validarSlot(slot);
        String sql = "SELECT s.partida_id, p.version_snapshot, p.payload FROM partida_slots s "
                + "JOIN partida_snapshot p ON p.partida_id = s.partida_id WHERE s.slot_numero = ?";
        try (Connection c = ConexionBD.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, slot);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                if (rs.getInt("version_snapshot") != VERSION) {
                    throw new SQLException("La versión del guardado no es compatible.");
                }
                try { return Optional.of(deserializar(rs.getBytes("payload"))); }
                catch (IOException | ClassNotFoundException e) {
                    throw new SQLException("No se pudo leer la instantánea de la partida.", e);
                }
            }
        }
    }

    @Override
    public void guardar(int slot, String nombre, EstadoPartida estado) throws SQLException {
        validarSlot(slot);
        if (estado == null) throw new IllegalArgumentException("El estado de partida es obligatorio.");
        String nombreSeguro = nombre == null || nombre.isBlank() ? "Partida" : nombre.trim();
        byte[] payload;
        try { payload = serializar(estado); }
        catch (IOException e) { throw new SQLException("No se pudo serializar la partida.", e); }

        try (Connection c = ConexionBD.conectar()) {
            boolean autoCommitAnterior = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                asegurarSlots(c);
                Integer idExistente = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT partida_id FROM partida_slots WHERE slot_numero = ? FOR UPDATE")) {
                    ps.setInt(1, slot);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) idExistente = (Integer) rs.getObject(1); }
                }
                int partidaId;
                if (idExistente == null) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO partidas (nombre, sala_actual) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, nombreSeguro); ps.setInt(2, estado.salaGlobal()); ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("MySQL no devolvió el ID de la partida.");
                            partidaId = keys.getInt(1);
                        }
                    }
                } else {
                    partidaId = idExistente;
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE partidas SET nombre = ?, sala_actual = ? WHERE id = ?")) {
                        ps.setString(1, nombreSeguro); ps.setInt(2, estado.salaGlobal());
                        ps.setInt(3, partidaId); ps.executeUpdate();
                    }
                    borrarEstadoRelacional(c, partidaId);
                }
                guardarEstadoRelacional(c, partidaId, estado);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO partida_snapshot (partida_id, version_snapshot, payload) VALUES (?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE version_snapshot = VALUES(version_snapshot), payload = VALUES(payload)")) {
                    ps.setInt(1, partidaId); ps.setInt(2, VERSION); ps.setBytes(3, payload); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE partida_slots SET partida_id = ? WHERE slot_numero = ?")) {
                    ps.setInt(1, partidaId); ps.setInt(2, slot);
                    if (ps.executeUpdate() != 1) throw new SQLException("El slot local no existe.");
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException sql) throw sql;
                throw new SQLException("No se pudo guardar la partida completa.", e);
            } finally { c.setAutoCommit(autoCommitAnterior); }
        }
    }

    @Override
    public void eliminar(int slot) throws SQLException {
        validarSlot(slot);
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                Integer id = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT partida_id FROM partida_slots WHERE slot_numero = ? FOR UPDATE")) {
                    ps.setInt(1, slot);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) id = (Integer) rs.getObject(1); }
                }
                if (id != null) try (PreparedStatement ps = c.prepareStatement("DELETE FROM partidas WHERE id = ?")) {
                    ps.setInt(1, id); ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    private void guardarEstadoRelacional(Connection c, int id, EstadoPartida s) throws SQLException {
        EstadoPartida.JugadorEstado j = s.jugador();
        String jugadorSql = "INSERT INTO jugadores_partida (partida_id, nombre, vida, vida_maxima, dano, experiencia, nivel, monedas, energia, energia_maxima, posicion_x, posicion_y) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(jugadorSql)) {
            ps.setInt(1, id); ps.setString(2, j.nombre()); ps.setInt(3, j.vida()); ps.setInt(4, j.vidaMaxima());
            ps.setInt(5, j.dano()); ps.setInt(6, j.experiencia()); ps.setInt(7, j.nivel()); ps.setInt(8, j.monedas());
            ps.setInt(9, j.energia()); ps.setInt(10, j.energiaMaxima()); ps.setDouble(11, j.x()); ps.setDouble(12, j.y());
            ps.executeUpdate();
        }
        String enemigoSql = "INSERT INTO enemigos_partida (partida_id, nombre, vida, vida_maxima, dano, posicion_x, posicion_y, sala, derrotado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
        try (PreparedStatement ps = c.prepareStatement(enemigoSql)) {
            for (EstadoPartida.EnemigoEstado e : s.enemigos()) {
                ps.setInt(1, id); ps.setString(2, e.nombre()); ps.setInt(3, e.vida()); ps.setInt(4, e.vidaMaxima());
                ps.setInt(5, e.dano()); ps.setDouble(6, e.x()); ps.setDouble(7, e.y()); ps.setInt(8, s.salaGlobal()); ps.addBatch();
            }
            ps.executeBatch();
        }
        String invSql = "INSERT INTO invocaciones_partida (partida_id, nombre, vida, vida_maxima, dano, duracion_restante, posicion_x, posicion_y, sala) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(invSql)) {
            for (EstadoPartida.InvocacionEstado i : s.invocaciones()) {
                ps.setInt(1, id); ps.setString(2, i.tipo()); ps.setInt(3, i.vida()); ps.setInt(4, i.vidaMaxima());
                ps.setInt(5, i.dano()); ps.setDouble(6, i.x()); ps.setDouble(7, i.y()); ps.setInt(8, s.salaGlobal()); ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void borrarEstadoRelacional(Connection c, int id) throws SQLException {
        for (String tabla : List.of("jugadores_partida", "enemigos_partida", "invocaciones_partida")) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + tabla + " WHERE partida_id = ?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
        }
    }

    private void asegurarSlots(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT IGNORE INTO partida_slots (slot_numero, partida_id) VALUES (1, NULL), (2, NULL), (3, NULL)")) {
            ps.executeUpdate();
        }
    }
    private void validarSlot(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("El slot debe estar entre 1 y 3.");
    }
    byte[] serializar(EstadoPartida estado) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(salida)) { oos.writeObject(estado); }
        return salida.toByteArray();
    }
    EstadoPartida deserializar(byte[] payload) throws IOException, ClassNotFoundException {
        if (payload == null) throw new IOException("El payload del guardado está vacío.");
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload))) {
            ois.setObjectInputFilter(info -> {
                if (info.depth() > 30 || info.references() > 200_000 || info.streamBytes() > 64_000_000) return ObjectInputFilter.Status.REJECTED;
                Class<?> c = info.serialClass();
                if (c == null) return ObjectInputFilter.Status.UNDECIDED;
                String n = c.getName();
                return n.startsWith("com.wachin.model.EstadoPartida") || n.startsWith("java.lang.")
                        || n.startsWith("java.util.") || n.startsWith("[Lcom.wachin.model.EstadoPartida")
                        || n.equals("[Ljava.lang.Object;") || n.startsWith("[Ljava.lang.")
                        ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
            Object objeto = ois.readObject();
            if (!(objeto instanceof EstadoPartida estado)) throw new IOException("Tipo de instantánea desconocido.");
            return estado;
        }
    }
}
