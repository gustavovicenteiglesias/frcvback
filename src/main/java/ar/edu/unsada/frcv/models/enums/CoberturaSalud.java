package ar.edu.unsada.frcv.models.enums;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum CoberturaSalud {
    OBRA_SOCIAL("Obra Social"),
    PREPAGA("Empresa de Medicina Privada (Prepaga)"),
    COBERTURA_PUBLICA_EXCLUSIVA("Cobertura Pública Exclusiva"),
    INCLUIR_SALUD("Incluir Salud (ex Profe)"),
    PAMI("PAMI"),
    NO_SABE("No sabe");

    private final String display;
    CoberturaSalud(String display) { this.display = display; }
    public String getDisplay() { return display; }

    /** Permite deserializar de strings variados (camel/snake/labels) sin romper. */
    @JsonCreator
    public static CoberturaSalud from(String v) {
        if (v == null || v.isBlank()) return null; // si preferís forzar, cambialo a NO_SABE
        String n = v.trim().toUpperCase()
                .replace('Á','A').replace('É','E').replace('Í','I')
                .replace('Ó','O').replace('Ú','U')
                .replace("(", "").replace(")", "")
                .replace("-", "_").replace(" ", "_");
        switch (n) {
            case "OBRA_SOCIAL": return OBRA_SOCIAL;
            case "PREPAGA":
            case "EMPRESA_DE_MEDICINA_PRIVADA_PREPAGA": return PREPAGA;
            case "COBERTURA_PUBLICA_EXCLUSIVA": return COBERTURA_PUBLICA_EXCLUSIVA;
            case "INCLUIR_SALUD":
            case "INCLUIR_SALUD_EX_PROFE": return INCLUIR_SALUD;
            case "PAMI": return PAMI;
            case "NO_SABE": return NO_SABE;
            default: return NO_SABE;
        }
    }

    @Override
    public String toString() { return name(); } // JSON por nombre del enum
}
