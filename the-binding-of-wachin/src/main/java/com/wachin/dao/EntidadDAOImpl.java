package com.wachin.dao;

import com.wachin.model.Entidad;
import com.wachin.model.Jugador;
import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntidadDAOImpl implements EntidadDAO {

    @Override
    public void guardar(Entidad entidad) {

        String sql = "INSERT INTO Entidades " +
                "(nombre, vida, vida_max, dano, tipo) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getVida());
            ps.setInt(3, entidad.getVidaMaxima());
            ps.setInt(4, entidad.getDano());
            ps.setString(5, entidad.getTipo());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar entidad: " + e.getMessage());
        }
    }

    @Override
    public Entidad buscarPorId(int id) {

        String sql = "SELECT * FROM Entidades WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirEntidad(rs);
                }
            }

        } catch (Exception e) {
            System.out.println("Error al buscar entidad: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Entidad> listarTodo() {

        List<Entidad> entidades = new ArrayList<>();

        String sql = "SELECT * FROM Entidades";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Entidad entidad = convertirEntidad(rs);

                if (entidad != null) {
                    entidades.add(entidad);
                }
            }

        } catch (Exception e) {
            System.out.println("Error al listar entidades: " + e.getMessage());
        }

        return entidades;
    }

    private Entidad convertirEntidad(ResultSet rs) throws Exception {

        String tipo = rs.getString("tipo");

        switch (tipo) {

            case "JUGADOR":
                return new Jugador(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("vida"),
                        rs.getInt("vida_max"),
                        rs.getInt("dano"),
                        rs.getInt("experiencia"),
                        rs.getInt("nivel"),
                        rs.getInt("monedas"),
                        rs.getInt("energia"),
                        rs.getInt("energia_maxima")
                );

            case "ENEMIGO":
                return new Enemigo(
                        rs.getString("nombre"),
                        rs.getInt("vida_max"),
                        rs.getInt("dano"),
                        "NORMAL",
                        1.0
                );

            case "INVOCACION":
                return new Invocacion(
                        rs.getString("nombre"),
                        rs.getInt("vida_max"),
                        rs.getInt("dano"),
                        10,
                        10
                );

            default:
                System.out.println("Tipo de entidad desconocido: " + tipo);
                return null;
        }
    }

    @Override
    public void actualizar(Entidad entidad) {

        String sql = "UPDATE Entidades SET " +
                "nombre = ?, vida = ?, vida_max = ?, dano = ?, tipo = ? " +
                "WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getVida());
            ps.setInt(3, entidad.getVidaMaxima());
            ps.setInt(4, entidad.getDano());
            ps.setString(5, entidad.getTipo());
            ps.setInt(6, entidad.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar entidad: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {

        String sql = "DELETE FROM Entidades WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar entidad: " + e.getMessage());
        }
    }
}