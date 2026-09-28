package com.wachin.dao;

import com.wachin.model.Jugador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JugadorDAOImpl implements JugadorDAO {

    @Override
    public void guardar(Jugador jugador) {

        String sql = "INSERT INTO Entidades " +
                "(nombre, vida, vida_max, dano, tipo, experiencia, nivel, monedas, energia, energia_maxima) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, jugador.getNombre());
            ps.setInt(2, jugador.getVida());
            ps.setInt(3, jugador.getVidaMaxima());
            ps.setInt(4, jugador.getDano());
            ps.setString(5, "JUGADOR");
            ps.setInt(6, jugador.getExperiencia());
            ps.setInt(7, jugador.getNivel());
            ps.setInt(8, jugador.getMonedas());
            ps.setInt(9, jugador.getEnergia());
            ps.setInt(10, jugador.getEnergiaMaxima());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al guardar jugador: " + e.getMessage());
        }
    }

    @Override
    public Jugador buscarPorId(int id) {

        String sql = "SELECT * FROM Entidades WHERE id = ? AND tipo = 'JUGADOR'";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

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
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar jugador: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Jugador> listarTodo() {

        List<Jugador> jugadores = new ArrayList<>();

        String sql = "SELECT * FROM Entidades WHERE tipo = 'JUGADOR'";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Jugador jugador = new Jugador(
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

                jugadores.add(jugador);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar jugadores: " + e.getMessage());
        }

        return jugadores;
    }

    @Override
    public void actualizar(Jugador jugador) {

        String sql = "UPDATE Entidades SET " +
                "nombre = ?, vida = ?, vida_max = ?, dano = ?, " +
                "experiencia = ?, nivel = ?, monedas = ?, " +
                "energia = ?, energia_maxima = ? " +
                "WHERE id = ? AND tipo = 'JUGADOR'";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, jugador.getNombre());
            ps.setInt(2, jugador.getVida());
            ps.setInt(3, jugador.getVidaMaxima());
            ps.setInt(4, jugador.getDano());
            ps.setInt(5, jugador.getExperiencia());
            ps.setInt(6, jugador.getNivel());
            ps.setInt(7, jugador.getMonedas());
            ps.setInt(8, jugador.getEnergia());
            ps.setInt(9, jugador.getEnergiaMaxima());
            ps.setInt(10, jugador.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar jugador: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {

        String sql = "DELETE FROM Entidades WHERE id = ? AND tipo = 'JUGADOR'";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar jugador: " + e.getMessage());
        }
    }
}