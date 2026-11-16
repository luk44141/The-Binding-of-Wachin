package com.wachin.view.ui;

import java.net.URL;
import java.util.logging.Logger;
import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/** Paleta, tipografías y utilidades de estilo compartidas por toda la interfaz. */
public final class Tema {

    public static final Color FONDO = Color.web("#15131c");
    public static final Color PANEL = Color.web("#242130");
    public static final Color BORDE = Color.web("#49415d");
    public static final Color ROJO = Color.web("#c0283a");
    public static final Color ROJO_VIVO = Color.web("#ff4560");
    public static final Color ROJO_OSCURO = Color.web("#5a0f1c");
    public static final Color ORO = Color.web("#e6b84a");
    public static final Color ORO_CLARO = Color.web("#ffd873");
    public static final Color CIAN = Color.web("#4fd6e8");
    public static final Color VIOLETA = Color.web("#9b6dff");
    public static final Color VERDE = Color.web("#6fdc8c");
    public static final Color TEXTO = Color.web("#e9e4f2");
    public static final Color TEXTO_TENUE = Color.web("#9a93ad");

    private static final String HOJA = "/assets/styles/wachin.css";
    private static final Logger REGISTRO = Logger.getLogger(Tema.class.getName());
    private static boolean avisado;

    private Tema() { }

    /** Aplica la hoja de estilos y la clase raíz. Si la hoja falta se registra una vez y se continúa. */
    public static void aplicar(Parent raiz) {
        raiz.getStyleClass().add("root-wachin");
        URL url = Tema.class.getResource(HOJA);
        if (url == null) {
            if (!avisado) {
                avisado = true;
                REGISTRO.warning("No se encontró " + HOJA + "; se usa el estilo por defecto de JavaFX.");
            }
            return;
        }
        String externa = url.toExternalForm();
        if (!raiz.getStylesheets().contains(externa)) raiz.getStylesheets().add(externa);
    }

    public static Font titulo(double tamano) {
        return Font.font("Georgia", FontWeight.BOLD, tamano);
    }

    public static Font cuerpo(double tamano, boolean negrita) {
        return Font.font("Segoe UI", negrita ? FontWeight.BOLD : FontWeight.NORMAL, tamano);
    }

    public static Text texto(String valor, double tamano, Color color, boolean negrita) {
        Text texto = new Text(valor);
        texto.setFont(cuerpo(tamano, negrita));
        texto.setFill(color);
        return texto;
    }

    public static DropShadow brillo(Color color, double radio) {
        DropShadow sombra = new DropShadow(radio, color);
        sombra.setSpread(0.25);
        return sombra;
    }

    /** Título principal con degradado dorado y halo. */
    public static Text tituloDorado(String valor, double tamano) {
        Text texto = new Text(valor);
        texto.setFont(titulo(tamano));
        texto.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#fff3c4")), new Stop(0.55, ORO_CLARO), new Stop(1, Color.web("#b3841f"))));
        texto.setStroke(Color.web("#2a1c04"));
        texto.setStrokeWidth(1.5);
        texto.setEffect(brillo(Color.web("#e6b84a", 0.55), 22));
        return texto;
    }
}
