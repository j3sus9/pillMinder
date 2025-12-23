package com.example.pillminder.utils;

public class FormatUtils {

    /**
     * Devuelve la unidad correcta (singular/plural) y el género del interrogativo.
     */
    public static String obtenerUnidadFormateada(int cantidad, String tipo) {
        if (tipo == null) return "";
        String t = tipo.toLowerCase().trim();

        // Si la cantidad es 1, aplicamos reglas de singular
        if (cantidad == 1) {
            switch (t) {
                case "pastillas": return "pastilla";
                case "gotas": return "gota";
                case "inhalaciones": return "inhalación";
                default:
                    // Si termina en 's', intentamos quitarla, si no, devolvemos tal cual (ej. ml)
                    return (t.endsWith("s") && t.length() > 1) ? t.substring(0, t.length() - 1) : t;
            }
        }
        return t;
    }

    public static String obtenerInterrogativo(String tipo) {
        if (tipo == null) return "¿Cuánto";
        String t = tipo.toLowerCase().trim();
        // Femeninos: pastillas, gotas, inhalaciones
        if (t.endsWith("as") || t.endsWith("es") || t.equals("pastilla") || t.equals("gota") || t.equals("inhalación")) {
            return "¿Cuántas";
        }
        return "¿Cuántos";
    }
}