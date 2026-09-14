package com.wachin;

import com.wachin.dao.ConexionDAO;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        try (Connection conexion = ConexionDAO.conectar()) {

            System.out.println("Conexion con MySQL exitosa.");

        } catch (Exception e) {

            System.out.println("Error al conectar con MySQL.");
            e.printStackTrace();
        }
    }
}