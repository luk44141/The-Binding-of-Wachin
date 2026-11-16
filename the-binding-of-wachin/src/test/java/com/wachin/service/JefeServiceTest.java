package com.wachin.service;

import com.wachin.model.Jefe;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JefeServiceTest {

    @Test
    public void jefeSala10AnunciaYEjecutaSusNuevosAtaques() throws Exception {
        Jefe jefe = new SalaService().generarJefe(Jefe.Tipo.SALA_10);
        JefeService servicio = new JefeService();
        boolean avisoVisto = false;
        boolean ataqueEjecutado = false;
        double x = 300;
        double y = 300;
        for (int i = 0; i < 700; i++) {
            JefeService.Actualizacion estado = servicio.actualizar(jefe, x, y, 500, 350, 0.05);
            x = estado.x();
            y = estado.y();
            avisoVisto |= estado.aviso() != JefeService.Ataque.NINGUNO;
            ataqueEjecutado |= !estado.disparos().isEmpty();
            if (avisoVisto && ataqueEjecutado) break;
        }
        assertTrue("El jefe de sala 10 debe anunciar sus ataques", avisoVisto);
        assertTrue("El jefe de sala 10 debe ejecutar disparos o ataques radiales", ataqueEjecutado);
    }

    @Test
    public void jobUsaAtaqueLaserYUnSegundoAtaqueRadial() throws Exception {
        Jefe jefe = new SalaService().generarJefe(Jefe.Tipo.SALA_10);
        JefeService servicio = new JefeService();
        java.util.Set<JefeService.Ataque> avisos = new java.util.HashSet<>();
        double x = 300;
        double y = 300;
        for (int i = 0; i < 6000; i++) {
            JefeService.Actualizacion estado = servicio.actualizar(jefe, x, y, 500, 350, 0.05);
            x = estado.x();
            y = estado.y();
            if (estado.aviso() != JefeService.Ataque.NINGUNO) avisos.add(estado.aviso());
            if (avisos.contains(JefeService.Ataque.LASER)
                    && avisos.contains(JefeService.Ataque.RADIAL)) break;
        }
        assertTrue("Job debe conservar el ataque laser", avisos.contains(JefeService.Ataque.LASER));
        assertTrue("Job debe tener un segundo ataque radial", avisos.contains(JefeService.Ataque.RADIAL));
    }

    @Test
    public void emisorDeSala15UsaLosAtaquesDeJob() throws Exception {
        Jefe jefe = new SalaService().generarJefe(Jefe.Tipo.EMISOR_RAYOS);
        JefeService servicio = new JefeService();
        java.util.Set<JefeService.Ataque> avisos = new java.util.HashSet<>();
        double x = 300;
        double y = 300;
        for (int i = 0; i < 6000; i++) {
            JefeService.Actualizacion estado = servicio.actualizar(jefe, x, y, 500, 350, 0.05);
            x = estado.x();
            y = estado.y();
            if (estado.aviso() != JefeService.Ataque.NINGUNO) avisos.add(estado.aviso());
            if (avisos.contains(JefeService.Ataque.EMBESTIDA)
                    && avisos.contains(JefeService.Ataque.DISPARO)
                    && avisos.contains(JefeService.Ataque.RADIAL)) break;
        }
        assertTrue("El Emisor debe poder embestir", avisos.contains(JefeService.Ataque.EMBESTIDA));
        assertTrue("El Emisor debe disparar", avisos.contains(JefeService.Ataque.DISPARO));
        assertTrue("El Emisor debe realizar ataques radiales", avisos.contains(JefeService.Ataque.RADIAL));
        assertTrue("El Emisor ya no debe usar el laser de Job", !avisos.contains(JefeService.Ataque.LASER));
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
