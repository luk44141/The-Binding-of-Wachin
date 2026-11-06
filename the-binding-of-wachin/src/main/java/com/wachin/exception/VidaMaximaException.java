package com.wachin.exception;

/**
 * Representa una excepción específica del dominio para comunicar una condición inválida o una operación no disponible.
 */
public class VidaMaximaException extends Exception {

    /**
     * Constructor: valida los parámetros e inicializa el estado necesario del objeto.
     */
    public VidaMaximaException(String mensaje) {
        super(mensaje);
    }
}
