package com.wachin.dao;

import com.wachin.model.Entidad;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jefe;
import com.wachin.model.Jugador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO general. Guarda los atributos propios del jugador cuando corresponde. */
/**
 * Implementa EntidadDAO y reconstruye el subtipo de entidad indicado por la base.
 */
public class EntidadDAOImpl implements EntidadDAO {
    @Override
    /**
     * Inserta el estado mediante una consulta parametrizada y cierra los recursos JDBC.
     */
    public void guardar(Entidad entidad) {
        if (entidad == null) return;
        String sql = "INSERT INTO Entidades (nombre, vida, vida_max, dano, tipo, experiencia, nivel, monedas, energia, energia_maxima) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getVida());
            ps.setInt(3, entidad.getVidaMaxima());
            ps.setInt(4, entidad.getDano());
            ps.setString(5, entidad.getTipo());
            if (entidad instanceof Jugador) {
                Jugador jugador = (Jugador) entidad;
                ps.setInt(6, jugador.getExperiencia());
                ps.setInt(7, jugador.getNivel());
                ps.setInt(8, jugador.getMonedas());
                ps.setInt(9, jugador.getEnergia());
                ps.setInt(10, jugador.getEnergiaMaxima());
            } else {
                ps.setInt(6, 0); ps.setInt(7, 1); ps.setInt(8, 0); ps.setInt(9, 0); ps.setInt(10, 0);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar entidad: " + e.getMessage());
        }
    }

    @Override
    /**
     * Consulta por identificador y reconstruye el objeto encontrado.
     */
    public Entidad buscarPorId(int id) {
        /**
         * Guarda sql para que la clase mantenga su estado interno.
         */
        String sql = "SELECT * FROM Entidades WHERE id = ?";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return convertirEntidad(rs);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar entidad: " + e.getMessage());
        }
        return null;
    }

    @Override
    /**
     * Consulta los registros disponibles y devuelve los objetos reconstruidos.
     */
    public List<Entidad> listarTodo() {
        List<Entidad> entidades = new ArrayList<>();
        /**
         * Guarda sql para que la clase mantenga su estado interno.
         */
        String sql = "SELECT * FROM Entidades";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Entidad entidad = convertirEntidad(rs);
                if (entidad != null) entidades.add(entidad);
            }
        } catch (Exception e) {
            System.err.println("Error al listar entidades: " + e.getMessage());
        }
        return entidades;
    }

    /**
     * Lee el tipo del registro y reconstruye la subclase correspondiente.
     */
    private Entidad convertirEntidad(ResultSet rs) throws Exception {
        String tipo = rs.getString("tipo");
        if (tipo == null) return null;
        switch (tipo.toUpperCase()) {
            case "JUGADOR":
                return new Jugador(rs.getInt("id"), rs.getString("nombre"), rs.getInt("vida"),
                        rs.getInt("vida_max"), rs.getInt("dano"), rs.getInt("experiencia"),
                        rs.getInt("nivel"), rs.getInt("monedas"), rs.getInt("energia"), rs.getInt("energia_maxima"));
            case "ENEMIGO":
                // El esquema no persiste tipo visual ni velocidad: se restauran valores por defecto.
                return new Enemigo(rs.getInt("id"), rs.getString("nombre"), rs.getInt("vida"),
                        rs.getInt("vida_max"), rs.getInt("dano"), "POLI", 85);
            case "JEFE":
                return new Jefe(rs.getInt("id"), rs.getString("nombre"), rs.getInt("vida"),
                        rs.getInt("vida_max"), rs.getInt("dano"), 105, 1, "EMBESTIDA", 12);
            case "INVOCACION":
                return new Invocacion(rs.getInt("id"), rs.getString("nombre"), rs.getInt("vida"),
                        rs.getInt("vida_max"), rs.getInt("dano"), 10, 10);
            default:
                System.err.println("Tipo de entidad desconocido: " + tipo);
                return null;
        }
    }

    @Override
    /**
     * Actualiza los datos del registro existente usando una consulta parametrizada.
     */
    public void actualizar(Entidad entidad) {
        if (entidad == null) return;
        /**
         * Guarda sql para que la clase mantenga su estado interno.
         */
        String sql = "UPDATE Entidades SET nombre = ?, vida = ?, vida_max = ?, dano = ?, tipo = ?, experiencia = ?, nivel = ?, monedas = ?, energia = ?, energia_maxima = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getVida());
            ps.setInt(3, entidad.getVidaMaxima());
            ps.setInt(4, entidad.getDano());
            ps.setString(5, entidad.getTipo());
            if (entidad instanceof Jugador) {
                Jugador jugador = (Jugador) entidad;
                ps.setInt(6, jugador.getExperiencia());
                ps.setInt(7, jugador.getNivel());
                ps.setInt(8, jugador.getMonedas());
                ps.setInt(9, jugador.getEnergia());
                ps.setInt(10, jugador.getEnergiaMaxima());
            } else {
                ps.setInt(6, 0); ps.setInt(7, 1); ps.setInt(8, 0); ps.setInt(9, 0); ps.setInt(10, 0);
            }
            ps.setInt(11, entidad.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar entidad: " + e.getMessage());
        }
    }

    @Override
    /**
     * Borra el registro identificado mediante una consulta parametrizada.
     */
    public void eliminar(int id) {
        /**
         * Guarda sql para que la clase mantenga su estado interno.
         */
        String sql = "DELETE FROM Entidades WHERE id = ?";
        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar entidad: " + e.getMessage());
        }
    }
}
