package com.wachin.exception;

/** Se produce cuando el jugador intenta atravesar una puerta que no está habilitada. */
public final class PuertaBloqueadaException extends Exception {

    public PuertaBloqueadaException() {
        super("No podés pasar por esta puerta");
    }
}
