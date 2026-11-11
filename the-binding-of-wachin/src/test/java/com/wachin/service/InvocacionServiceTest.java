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
}
