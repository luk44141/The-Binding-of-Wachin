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
        String url = configuracion("wachin.db.url", "WACHIN_DB_URL", URL);
        String usuario = configuracion("wachin.db.user", "WACHIN_DB_USER", USUARIO);
        String password = configuracion("wachin.db.password", "WACHIN_DB_PASSWORD", PASSWORD);
        return DriverManager.getConnection(url, usuario, password);
    }

    private static String configuracion(String propiedad, String variable, String valorDefecto) {
        String sistema = System.getProperty(propiedad);
        if (sistema != null && !sistema.isBlank()) return sistema;
        String entorno = System.getenv(variable);
        return entorno == null || entorno.isBlank() ? valorDefecto : entorno;
    }
}

