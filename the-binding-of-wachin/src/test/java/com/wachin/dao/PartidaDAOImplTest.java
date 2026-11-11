package com.wachin.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.wachin.model.EstadoPartida;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class PartidaDAOImplTest {

    @Test
    public void snapshotCompletaSobreviveSerializacionYFiltro() throws Exception {
        EstadoPartida estado = new EstadoPartida("WACHINA",
                new EstadoPartida.JugadorEstado("Wachina", 125, 150, 100, 25,
                        40, 2, 3, 10, 10, 211.5, 488.25, "LAGRIMA", 240,
                        5, 7, true),
                15, 4, 3, List.of("ARRIBA", "DERECHA"), "ABAJO", true, Set.of(10),
                List.of(new EstadoPartida.EnemigoEstado("Artillero", "JEFE", 1400, 2600,
                        24, 82, 430, 200, "SALA_15", 2, "RAFAGA_RADIAL_EMBESTIDA", 24, 1.25)),
                List.of(new EstadoPartida.InvocacionEstado("ROBOT", 80, 100, 20,
                        35.5, 100, 250, 300, 0.4, "ATACANDO", false)),
                Map.of("DANO", 2, "SEGUNDO_ESPACIO_INVOCACION", 1),
                List.of(new EstadoPartida.ObjetoEstado(81, "CARTA", "VELOCIDAD", 15, 600, 350)),
                List.of(new EstadoPartida.ProyectilEstado(302, 178, 420, -20,
                        35, "assets/images/projectiles/lagrima.png", "INVOCACION"),
                        new EstadoPartida.ProyectilEstado(410, 220, -330, 10,
                        12, "assets/images/projectiles/enemies/tirador.png", "ENEMIGO")),
                new EstadoPartida.JefeEstado("PREPARAR", "ZONAS", 0.6, 1, 0,
                        2, 1, true, List.of(100.0, 200.0), List.of(300.0, 400.0)));

        PartidaDAOImpl dao = new PartidaDAOImpl();
        byte[] bytes = dao.serializar(estado);

        assertEquals(estado, dao.deserializar(bytes));
    }

    @Test
    public void snapshotRechazaClasesFueraDelModeloPermitido() throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ObjectOutputStream objetos = new ObjectOutputStream(salida)) {
            objetos.writeObject(new java.io.File("unexpected"));
        }

        try {
            new PartidaDAOImpl().deserializar(salida.toByteArray());
            fail("El filtro debía rechazar tipos ajenos a la instantánea.");
        } catch (IOException esperado) {
            assertTrue(esperado.getMessage() != null);
        }
    }
}
