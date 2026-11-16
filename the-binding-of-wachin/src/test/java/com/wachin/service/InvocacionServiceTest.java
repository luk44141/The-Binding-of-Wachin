package com.wachin.service;

import com.wachin.model.Enemigo;
import com.wachin.model.Invocacion;
import com.wachin.model.Jugador;
import com.wachin.model.TipoInvocacion;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InvocacionServiceTest {
    @Test
    public void gusanoSeInterponeYEsMasResistenteQueLasOtrasInvocaciones() throws Exception {
        InvocacionService servicio = new InvocacionService();
        Invocacion gusano = servicio.crear(TipoInvocacion.GUSANO);
        Invocacion murcielago = servicio.crear(TipoInvocacion.MURCIELAGO);
        Invocacion robot = servicio.crear(TipoInvocacion.ROBOT);
        assertTrue(gusano.getVidaMaxima() > murcielago.getVidaMaxima());
        assertTrue(gusano.getVidaMaxima() > robot.getVidaMaxima());
        assertTrue("El gusano debe hacer menos daño que el murcielago",
                gusano.getDano() < murcielago.getDano());
        gusano.moverA(480, 350);
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        Enemigo enemigo = new Enemigo("Poli", 100, 5, "POLI", 80);
        InvocacionService.Actualizacion estado = servicio.actualizar(gusano, jugador,
                500, 350, List.of(new InvocacionService.Objetivo(enemigo, 620, 350)), 0.1);
        assertEquals(Invocacion.Estado.PROTEGIENDO, estado.invocacion().getEstado());
        assertTrue(gusano.getEnergia() < gusano.getEnergiaMaxima());
        assertTrue(gusano.getX() > 480);
    }

    @Test
    public void murcielagoAtacaCuerpoACuerpoYRobotGeneraLaser() throws Exception {
        InvocacionService servicio = new InvocacionService();
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        Enemigo enemigo = new Enemigo("Poli", 100, 5, "POLI", 80);
        Invocacion bat = servicio.crear(TipoInvocacion.MURCIELAGO);
        bat.moverA(500, 350);
        var ataqueBat = servicio.actualizar(bat, jugador, 500, 350,
                List.of(new InvocacionService.Objetivo(enemigo, 520, 350)), 0.1).ataques();
        assertEquals(1, ataqueBat.size());
        assertFalse(ataqueBat.getFirst().laser());

        Invocacion robot = servicio.crear(TipoInvocacion.ROBOT);
        robot.moverA(500, 350);
        var ataqueRobot = servicio.actualizar(robot, jugador, 500, 350,
                List.of(new InvocacionService.Objetivo(enemigo, 620, 350)), 0.1).ataques();
        assertEquals(1, ataqueRobot.size());
        assertTrue(ataqueRobot.getFirst().laser());
    }


    @Test
    public void lasInvocacionesNoPuedenMorirPorDano() throws Exception {
        Invocacion gusano = new InvocacionService().crear(TipoInvocacion.GUSANO);
        gusano.recibirDano(999999);
        assertEquals(gusano.getVidaMaxima(), gusano.getVida());
    }

    @Test
    public void gusanoBloqueaUnProyectilCadaTresSegundos() throws Exception {
        InvocacionService servicio = new InvocacionService();
        Invocacion gusano = servicio.crear(TipoInvocacion.GUSANO);
        Invocacion murcielago = servicio.crear(TipoInvocacion.MURCIELAGO);
        Invocacion robot = servicio.crear(TipoInvocacion.ROBOT);

        assertTrue(gusano.puedeBloquearProyectil());
        gusano.bloquearProyectil();
        assertFalse(gusano.puedeBloquearProyectil());
        gusano.reducirRecargaBloqueoProyectil(2.5);
        assertFalse(gusano.puedeBloquearProyectil());
        gusano.reducirRecargaBloqueoProyectil(0.5);
        assertTrue(gusano.puedeBloquearProyectil());
        assertFalse(murcielago.puedeBloquearProyectil());
        assertFalse(robot.puedeBloquearProyectil());
    }

    @Test
    public void invocacionSinEnergiaEntraEnRecuperacionAutomatica() throws Exception {
        InvocacionService servicio = new InvocacionService();
        Invocacion robot = servicio.crear(TipoInvocacion.ROBOT);
        robot.moverA(500, 350);
        for (int i = 0; i < 5; i++) robot.gastarEnergia(18);
        Jugador jugador = new Jugador("Wachin", 100, 25, 100);
        Enemigo enemigo = new Enemigo("Poli", 100, 5, "POLI", 80);
        servicio.actualizar(robot, jugador, 500, 350,
                List.of(new InvocacionService.Objetivo(enemigo, 520, 350)), 0.1);
        assertEquals(Invocacion.Estado.RECUPERANDO, robot.getEstado());
        assertTrue(robot.getEnergia() > 10);
    }

    @Test
    public void lasInvocacionesSeColocanAAmbosLadosSegunSuRanura() throws Exception {
        InvocacionService servicio = new InvocacionService();
        com.wachin.model.Jugador jugador = new com.wachin.model.Jugador("Wachin", 100, 25, 100);
        com.wachin.model.Invocacion izquierda = servicio.crear(com.wachin.model.TipoInvocacion.GUSANO);
        com.wachin.model.Invocacion derecha = servicio.crear(com.wachin.model.TipoInvocacion.ROBOT);
        izquierda.setEspacio(0);
        derecha.setEspacio(1);
        izquierda.moverA(500, 400);
        derecha.moverA(500, 400);
        for (int i = 0; i < 200; i++) {
            servicio.actualizar(izquierda, jugador, 500, 350, java.util.List.of(), 0.05);
            servicio.actualizar(derecha, jugador, 500, 350, java.util.List.of(), 0.05);
        }
        assertTrue(izquierda.getX() < 500);
        assertTrue(derecha.getX() > 500);
    }

    @Test
    public void unaInvocacionLejanaAlcanzaAlJugadorSinAtravesarLosLimitesDeLaSala() throws Exception {
        InvocacionService servicio = new InvocacionService();
        com.wachin.model.Jugador jugador = new com.wachin.model.Jugador("Wachin", 100, 25, 100);
        com.wachin.model.Invocacion bat = servicio.crear(com.wachin.model.TipoInvocacion.MURCIELAGO);
        bat.moverA(100, 100);
        for (int i = 0; i < 200; i++) servicio.actualizar(bat, jugador, 900, 600, java.util.List.of(), 0.05);
        assertTrue(Math.hypot(bat.getX() - 900, bat.getY() - 600) < 120);
        assertTrue(bat.getX() >= 75 && bat.getX() <= 925 && bat.getY() >= 75 && bat.getY() <= 625);
    }
}
