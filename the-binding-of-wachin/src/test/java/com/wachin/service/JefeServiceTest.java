package com.wachin.service;

import com.wachin.model.Jefe;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JefeServiceTest {

    @Test
    public void jefeSala10AnunciaLaEmbestidaExistente() throws Exception {
        Jefe jefe = new SalaService().generarJefe(Jefe.Tipo.SALA_10);
        JefeService servicio = new JefeService();
        JefeService.Actualizacion actualizacion = null;
        for (int i = 0; i < 30; i++) {
            actualizacion = servicio.actualizar(jefe, 300, 300, 500, 350, 0.1);
            if (actualizacion.aviso() == JefeService.Ataque.EMBESTIDA) break;
        }
        assertNotNull(actualizacion);
        assertEquals(JefeService.Ataque.EMBESTIDA, actualizacion.aviso());
    }

    @Test
    public void jefesDeSalas15Y20PreparanAtaquesAntesDeEjecutarlos() throws Exception {
        for (Jefe.Tipo tipo : new Jefe.Tipo[]{Jefe.Tipo.SALA_15, Jefe.Tipo.SALA_20}) {
            Jefe jefe = new SalaService().generarJefe(tipo);
            JefeService servicio = new JefeService();
            boolean avisoVisto = false;
            boolean ataqueEjecutado = false;
            double x = 300;
            double y = 300;
            for (int i = 0; i < 500; i++) {
                JefeService.Actualizacion estado = servicio.actualizar(jefe, x, y, 500, 350, 0.05);
                x = estado.x(); y = estado.y();
                avisoVisto |= estado.aviso() != JefeService.Ataque.NINGUNO;
                ataqueEjecutado |= !estado.disparos().isEmpty() || estado.zonasActivas();
                if (avisoVisto && ataqueEjecutado) break;
            }
            assertTrue("El jefe " + tipo + " debe anunciar ataques", avisoVisto);
            assertTrue("El jefe " + tipo + " debe ejecutar ataques", ataqueEjecutado);
        }
    }
}
