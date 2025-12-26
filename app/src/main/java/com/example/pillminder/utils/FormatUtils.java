package com.example.pillminder.utils;

import android.content.Context;
import android.content.res.Resources;
import com.example.pillminder.R;

public class FormatUtils {
    public static String obtenerUnidadFormateada(Context context, int cantidad, String tipo) {
        if (tipo == null) return "";
        String t = tipo.toLowerCase().trim();
        Resources res = context.getResources();

        int pluralResId;
        switch (t) {
            case "tablets":
                pluralResId = R.plurals.unit_tablets;
                break;
            case "capsules":
                pluralResId = R.plurals.unit_capsules;
                break;
            case "ml":
                pluralResId = R.plurals.unit_ml;
                break;
            case "drops":
                pluralResId = R.plurals.unit_drops;
                break;
            case "pills":
                pluralResId = R.plurals.unit_pills;
                break;
            case "sachets":
                pluralResId = R.plurals.unit_sachets;
                break;
            case "inhalations":
                pluralResId = R.plurals.unit_inhalations;
                break;
            case "units":
            default:
                pluralResId = R.plurals.unit_units;
                break;
        }

        return res.getQuantityString(pluralResId, cantidad);
    }
}
