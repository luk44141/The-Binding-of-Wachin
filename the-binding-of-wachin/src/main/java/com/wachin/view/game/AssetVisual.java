package com.wachin.view.game;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Carga recursos gráficos del classpath con caché y resuelve la imagen que debe mostrarse:
 * el sprite real si existe o, si falta, una ilustración generada por código.
 *
 * <p>Las imágenes se leen una sola vez por ruta; antes cada proyectil volvía a abrir el archivo
 * del disco. Un archivo ausente no es un error (se usa la alternativa dibujada); un archivo
 * presente pero ilegible se registra una única vez como advertencia.</p>
 */
public final class AssetVisual {

    private static final Logger REGISTRO = Logger.getLogger(AssetVisual.class.getName());
    private static final Map<String, Optional<Image>> IMAGENES = new HashMap<>();
    private static final Map<String, Image> PROCEDURALES = new HashMap<>();
    private static final Set<String> RUTAS_AVISADAS = new HashSet<>();

    private AssetVisual() { }

    /** Carga una imagen del classpath. Devuelve vacío cuando el archivo no existe o no se puede leer. */
    public static synchronized Optional<Image> cargar(String ruta) {
        if (ruta == null || ruta.isBlank()) return Optional.empty();
        return IMAGENES.computeIfAbsent(ruta, AssetVisual::leer);
    }

    private static Optional<Image> leer(String ruta) {
        String rutaClasspath = ruta.startsWith("/") ? ruta : "/" + ruta;
        try (InputStream entrada = AssetVisual.class.getResourceAsStream(rutaClasspath)) {
            if (entrada == null) return Optional.empty();
            Image imagen = new Image(entrada);
            if (imagen.isError()) {
                avisarUnaVez(ruta, "El archivo no es una imagen válida.", imagen.getException());
                return Optional.empty();
            }
            return Optional.of(imagen);
        } catch (IOException | RuntimeException e) {
            avisarUnaVez(ruta, "No se pudo leer el recurso.", e);
            return Optional.empty();
        }
    }

    private static void avisarUnaVez(String ruta, String detalle, Throwable causa) {
        if (RUTAS_AVISADAS.add(ruta)) {
            REGISTRO.log(Level.WARNING, "Recurso gráfico no utilizable (" + ruta + "): " + detalle, causa);
        }
    }

    /** Indica si existe un sprite real para alguna de las rutas candidatas. */
    public static boolean existe(List<String> candidatas) {
        return buscar(candidatas).isPresent();
    }

    /** Primer sprite real disponible entre las rutas candidatas. */
    public static Optional<Image> buscar(List<String> candidatas) {
        for (String ruta : candidatas) {
            Optional<Image> imagen = cargar(ruta);
            if (imagen.isPresent()) return imagen;
        }
        return Optional.empty();
    }

    /**
     * Devuelve el sprite real o, cuando falta, la ilustración procedural identificada por
     * {@code clave}. La ilustración se genera una sola vez y se reutiliza.
     */
    public static synchronized Image imagen(List<String> candidatas, String clave, Supplier<Image> procedural) {
        Optional<Image> real = buscar(candidatas);
        if (real.isPresent()) return real.get();
        return PROCEDURALES.computeIfAbsent(clave, k -> procedural.get());
    }

    /** Crea un nodo de imagen estilo pixel art (sin suavizado) ajustado al recuadro indicado. */
    public static ImageView vista(Image imagen, double ancho, double alto) {
        ImageView vista = new ImageView(imagen);
        vista.setFitWidth(ancho);
        vista.setFitHeight(alto);
        vista.setPreserveRatio(true);
        vista.setSmooth(false);
        vista.setMouseTransparent(true);
        centrarEnOrigen(vista, ancho, alto);
        return vista;
    }

    /**
     * Ubica el nodo para que el centro de su recuadro quede en el origen local (0, 0), de modo que
     * el grupo contenedor se posicione y rote alrededor del centro.
     */
    public static void centrarEnOrigen(ImageView vista, double ancho, double alto) {
        Image imagen = vista.getImage();
        double escala = Math.min(ancho / imagen.getWidth(), alto / imagen.getHeight());
        vista.setX(-imagen.getWidth() * escala / 2.0);
        vista.setY(-imagen.getHeight() * escala / 2.0);
        vista.setFitWidth(imagen.getWidth() * escala);
        vista.setFitHeight(imagen.getHeight() * escala);
    }

    /**
     * Posiciona un nodo cuyo contenido está centrado en el origen local. Usa la traslación en lugar
     * del layout para no invalidar el diseño del contenedor en cada fotograma.
     */
    public static void moverAlCentro(Node visual, double centroX, double centroY) {
        visual.setTranslateX(centroX);
        visual.setTranslateY(centroY);
    }
}
