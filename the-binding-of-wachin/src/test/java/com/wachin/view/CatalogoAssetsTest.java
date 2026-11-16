package com.wachin.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.wachin.model.TipoObjetoSuelo;
import com.wachin.view.game.CatalogoAssets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Test;

/** Comprueba que el código, los recursos incluidos y ASSETS.md hablan de las mismas rutas. */
public class CatalogoAssetsTest {

    private static String leerAssetsMd() throws IOException {
        try (InputStream entrada = CatalogoAssetsTest.class.getResourceAsStream("/assets/ASSETS.md")) {
            assertNotNull("Falta assets/ASSETS.md", entrada);
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String sinRaiz(String ruta) {
        return ruta.substring(CatalogoAssets.RAIZ.length());
    }

    @Test
    public void todaRutaQueElCodigoPuedePedirEstaDocumentadaEnAssetsMd() throws IOException {
        String documento = leerAssetsMd();
        for (String ruta : CatalogoAssets.rutasDocumentadas()) {
            assertTrue("ASSETS.md no menciona " + sinRaiz(ruta), documento.contains(sinRaiz(ruta)));
        }
    }

    @Test
    public void losSpritesExistentesEstanEnLasRutasQueElCodigoEspera() {
        List<String> existentes = List.of("player/wachin.png", "player/wachina.png", "enemies/poli.png",
                "bosses/palinni.png", "bosses/emisor_rayos.png", "projectiles/gota.png", "projectiles/lagrima.png",
                "environment/floors/floor.png", "environment/floors/floor_alt.png", "environment/walls/wall.png",
                "environment/doors/closed.png");
        for (String ruta : existentes) {
            assertNotNull("No se encuentra el recurso " + ruta,
                    CatalogoAssetsTest.class.getResource("/" + CatalogoAssets.RAIZ + ruta));
        }
    }

    @Test
    public void losNombresHistoricosSeAceptanComoRespaldo() {
        assertEquals(CatalogoAssets.PARED, CatalogoAssets.conHistoricos(CatalogoAssets.PARED).get(0));
        assertTrue(CatalogoAssets.conHistoricos(CatalogoAssets.PARED).contains(CatalogoAssets.RAIZ + "environment/walls/walls.png"));
        assertTrue(CatalogoAssets.conHistoricos(CatalogoAssets.PISO_ALTERNATIVO)
                .contains(CatalogoAssets.RAIZ + "environment/floors/ReColorFloors.png"));
        assertEquals(1, CatalogoAssets.conHistoricos(CatalogoAssets.PUERTA_ABIERTA).size());
    }

    @Test
    public void losIconosDelSueloCaenAlSpriteDelMundoSiFaltaElPropio() {
        List<String> rutas = CatalogoAssets.objetoSuelo(TipoObjetoSuelo.PROYECTIL, "GOTA");
        assertEquals(CatalogoAssets.RAIZ + "pickups/projectiles/gota.png", rutas.get(0));
        assertEquals(CatalogoAssets.RAIZ + "projectiles/gota.png", rutas.get(1));
        assertTrue(CatalogoAssets.objetoSuelo(TipoObjetoSuelo.INVOCACION, "GUSANO").size() >= 2);
    }

    @Test
    public void elJefeEmisorTieneFondoTransparente() throws IOException {
        try (InputStream entrada = CatalogoAssetsTest.class.getResourceAsStream("/" + CatalogoAssets.RAIZ + "bosses/emisor_rayos.png")) {
            assertNotNull(entrada);
            BufferedImage imagen = ImageIO.read(entrada);
            assertTrue("Debe tener canal alfa", imagen.getColorModel().hasAlpha());
            assertEquals("La esquina debe ser transparente", 0, imagen.getRGB(2, 2) >>> 24);
            assertTrue("El centro debe ser opaco", (imagen.getRGB(imagen.getWidth() / 2, imagen.getHeight() / 2) >>> 24) > 200);
        }
    }
}
