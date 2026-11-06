package com.wachin;

import com.wachin.dao.ConexionBD;
import java.sql.Connection;

/**
 * Punto de entrada auxiliar que comprueba la conexión JDBC con MySQL.
 */
public class Main {

    /**
     * Inicia el programa auxiliar, prueba la conexión a la base de datos e informa el resultado.
     */
    public static void main(String[] args) {

        try (Connection conexion = ConexionBD.conectar()) {
            if (conexion != null && !conexion.isClosed()) {
                System.out.println("Conexion con MySQL exitosa.");
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con MySQL.");
            e.printStackTrace();
        }
    }
}
