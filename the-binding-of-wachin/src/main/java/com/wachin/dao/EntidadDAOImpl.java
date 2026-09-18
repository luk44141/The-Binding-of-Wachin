package com.wachin.dao;

import com.wachin.model.Entidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntidadDAOImpl implements EntidadDAO {

    @Override
    public void guardar(Entidad entidad) {

        String sql = "INSERT INTO Entidades (nombre, vida, vida_max, dano, tipo) VALUES (?, ?, ?, ?, ?)";

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

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Entidad(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("vida"),
                        rs.getInt("vida_max"),
                        rs.getInt("dano"),
                        rs.getString("tipo")
                );
            }

        } catch (SQLException e) {
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

                Entidad entidad = new Entidad(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("vida"),
                        rs.getInt("vida_max"),
                        rs.getInt("dano"),
                        rs.getString("tipo")
                );

                entidades.add(entidad);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar entidades: " + e.getMessage());
        }

        return entidades;
    }

    @Override
    public void actualizar(Entidad entidad) {

        String sql = "UPDATE Entidades SET nombre = ?, vida = ?, vida_max = ?, dano = ?, tipo = ? WHERE id = ?";

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