package ar.edu.unsada.frcv.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;
import java.util.Locale;

public enum EstadoAcceso {
    ACCEDIO("Accedió"),
    AUSENTE("Ausente"),
    RECHAZO("Rechazo"),
    NO_APLICA("No aplica");

    private final String label;

    EstadoAcceso(String label) { this.label = label; }

    /** Etiqueta “linda” para el JSON */
    @JsonValue
    public String getLabel() { return label; }

    /** Normaliza para comparar sin tildes y sin importar mayúsculas */
    private static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", ""); // quita diacríticos
        return n.toLowerCase(Locale.ROOT).trim();
    }

    /** Acepta: "Accedió", "accedio", "ACCEDIO", etc. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EstadoAcceso fromJson(String value) {
        String v = norm(value);
        switch (v) {
            case "accedio":     return ACCEDIO;
            case "ausente":     return AUSENTE;
            case "rechazo":     return RECHAZO;
            case "no aplica":
            case "noaplica":
            case "n/a":
            case "na":          return NO_APLICA;
            default:
                // fallback: intentar por name() directo
                try { return EstadoAcceso.valueOf(value.toUpperCase(Locale.ROOT)); }
                catch (Exception ignore) { return NO_APLICA; }
        }
    }
}