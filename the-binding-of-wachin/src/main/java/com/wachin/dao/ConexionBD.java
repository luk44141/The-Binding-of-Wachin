package com.wachin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la apertura de conexiones JDBC con la base configurada.
 */
public class ConexionBD {

    /**
     * Constante que define url para este componente.
     */
    private static final String URL = "jdbc:mysql://localhost:3306/the_binding_of_wachin";
    /**
     * Constante que define usuario para este componente.
     */
    private static final String USUARIO = "root";
    /**
     * Constante que define password para este componente.
     */
    private static final String PASSWORD = "";

    /**
     * Solicita a DriverManager una conexión con la URL y credenciales configuradas.
     */
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}

