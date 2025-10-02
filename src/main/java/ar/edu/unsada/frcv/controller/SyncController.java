package ar.edu.unsada.frcv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final JdbcTemplate jdbc;

    public SyncController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* =========================================================
       Orden de importación (padres → hijos) para respetar FKs
       ========================================================= */
    private static final List<String> TABLE_ORDER = List.of(
            "barrios",
            "caps",
            "viviendas",
            "personas",
            "responsables",
            "visitas",
            "control_domicilio",
            "control_consultorio",
            "medicacion_hta",                 // catálogo con LM/SD
            "antecedentes",                   // dueño lógico
            "antecedente_has_medicacion_hta", // puente con LM/SD
            "lab_tipos",
            "lab_resultados"
    );

    /* =========================================================
       Orden de columnas móvil EXACTO (arrays de arrays)
       ========================================================= */
    private static final Map<String, List<String>> MOBILE_COLS = new LinkedHashMap<>() {{
        put("barrios", List.of("id","nombre","last_modified","sql_deleted"));
        put("caps", List.of("id","nombre","last_modified","sql_deleted"));
        put("viviendas", List.of(
                "id","barrios_id","caps_id","fecha","accedio","motivo","casa","manzana",
                "latitud","longitud","direccion","last_modified","sql_deleted"
        ));
        put("personas", List.of(
                "id","dni","apellido","nombre","sexo","fecha_nac","telefono","cobertura_salud",
                "viviendas_id","last_modified","sql_deleted"
        ));
        put("responsables", List.of(
                "id","user_id","nombre","matricula","email","activo","last_modified","sql_deleted"
        ));
        put("visitas", List.of(
                "id","persona_id","responsable_id","created_by_user_id","tipo","fecha",
                "ubicacion_gps","observaciones","last_modified","sql_deleted"
        ));
        put("control_domicilio", List.of(
                "id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica",
                "accede_programa","acepta_laboratotio","observaciones","last_modified","sql_deleted"
        ));
        put("control_consultorio", List.of(
                "id","visita_id","created_by_user_id","fecha","asistencia",
                "ta_sistolica","ta_diastolica",
                "peso","talla","imc",
                "circ_cintura","fumador",
                "confirm_hta","control_medicacion","derivacion","entrega_medicacion","eventos","conducta",
                "medicacion","observaciones","observaciones_derivacion","observaciones_eventos",
                "last_modified","sql_deleted"
        ));
        put("antecedentes", List.of(
                "id","persona_id","visita_id","created_by_user_id",
                "diabetes","dislipemia","enf_cardiovascular","enf_renal_cronica","hta_previa","tabaquismo",
                "tratamiento_enf_cardiovascular","tratamiento_enf_diabetes","tratamiento_enf_dislipemia",
                "tratamiento_enf_renal","tratamiento_hta_previa",
                "desc_trat_enf_diabetes","desc_trat_hta_previa","desc_trat_enf_cardiovascular","desc_trat_enf_dislipemia",
                "desc_trat_enf_renal",
                "otros","last_modified","sql_deleted"
        ));
        put("lab_tipos", List.of(
                "id","codigo","nombre","unidad_default","ref_min","ref_max","last_modified","sql_deleted"
        ));
        put("lab_resultados", List.of(
                "id","persona_id","visita_id","tipo_id","created_by_user_id","fecha_realizado",
                "valor_num","valor_texto","unidad","laboratorio","observaciones","last_modified","sql_deleted"
        ));

        // ==== NUEVAS TABLAS con LM/SD ====
        put("medicacion_hta", List.of(
                "id","nombre","last_modified","sql_deleted"
        ));
        put("antecedente_has_medicacion_hta", List.of(
                "antecedente_id","medicacion_id","last_modified","sql_deleted"
        ));
    }};

    /** PK compuesta por tabla (si no está, se toma la primera columna como PK simple) */
    private static final Map<String, List<String>> COMPOSITE_PKS = new HashMap<>() {{
        put("antecedente_has_medicacion_hta", List.of("antecedente_id","medicacion_id"));
    }};

    private List<String> pkFor(String table) {
        return COMPOSITE_PKS.getOrDefault(table, List.of(pkNameFor(table)));
    }

    private String pkNameFor(String table) {
        List<String> cols = MOBILE_COLS.get(table);
        if (cols != null && !cols.isEmpty()) return cols.get(0);
        return "id";
    }

    private static final String LM = "last_modified";

    /* ===========================
       PULL (parcial, por last_modified)
       =========================== */
    @GetMapping("/pull")
    public ResponseEntity<Map<String, Object>> pull(@RequestParam(required = false) String since) throws Exception {
        long cutoffSec = toEpochSeconds(since);
        List<Map<String, Object>> tables = new ArrayList<>();

        for (String table : TABLE_ORDER) {
            if (!tableExists(table)) continue;

            List<String> cols = MOBILE_COLS.getOrDefault(table, getTableColumns(table));
            boolean hasLm = cols.contains(LM);

            String sql = hasLm
                    ? "SELECT " + String.join(",", cols) + " FROM " + table + " WHERE " + LM + " > ?"
                    : "SELECT " + String.join(",", cols) + " FROM " + table;

            List<Map<String, Object>> rows = hasLm
                    ? jdbc.queryForList(sql, cutoffSec)
                    : jdbc.queryForList(sql);

            List<List<Object>> values = new ArrayList<>(rows.size());
            for (Map<String, Object> r : rows) {
                List<Object> arr = new ArrayList<>(cols.size());
                for (String c : cols) {
                    Object v = r.get(c);
                    if (v instanceof Boolean b) v = b ? 1 : 0;
                    if (LM.equalsIgnoreCase(c)) v = toEpochSeconds(v);
                    if (isBooleanishColumn(c)) v = boolAsInt(v);
                    arr.add(v);
                }
                values.add(arr);
            }

            Map<String, Object> t = new LinkedHashMap<>();
            t.put("name", table);
            t.put("values", values);
            tables.add(t);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("database", "frcv");
        body.put("version", 1);
        body.put("encrypted", false);
        body.put("mode", "partial");
        body.put("tables", tables);

        return ResponseEntity.ok(body);
    }

    /* ===========================
       PUSH (parcial, upsert por last_modified)
       =========================== */
    @PostMapping("/push")
    public ResponseEntity<Map<String, Object>> push(@RequestBody Map<String, Object> partial) throws Exception {
        Object tablesObj = partial.get("tables");
        if (!(tablesObj instanceof List<?> rawTables)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido: falta 'tables'"));
        }

        Map<String, List<List<Object>>> incoming = new HashMap<>();
        for (Object o : rawTables) {
            if (!(o instanceof Map<?, ?> tm)) continue;
            String name = Objects.toString(tm.get("name"), "");
            Object vals = tm.get("values");
            if (name.isBlank() || !(vals instanceof List<?>)) continue;

            @SuppressWarnings("unchecked")
            List<List<Object>> rows = ((List<?>) vals).stream()
                    .filter(v -> v instanceof List<?>)
                    .map(v -> (List<Object>) v)
                    .collect(Collectors.toList());
            incoming.put(name, rows);
        }

        int applied = 0, skipped = 0;

        for (String table : TABLE_ORDER) {
            if (!incoming.containsKey(table) || !tableExists(table)) continue;

            List<String> cols = MOBILE_COLS.getOrDefault(table, getTableColumns(table));
            boolean hasLm = cols.contains(LM);
            List<String> pkCols = pkFor(table);

            for (List<Object> rowArr : incoming.get(table)) {
                Map<String, Object> row = zipRow(cols, rowArr);

                List<Object> pkVals = new ArrayList<>(pkCols.size());
                boolean missingPk = false;
                for (String pkCol : pkCols) {
                    Object v = row.get(pkCol);
                    if (v == null || (v instanceof String s && s.isBlank())) { missingPk = true; break; }
                    pkVals.add(v);
                }
                if (missingPk) { skipped++; continue; }

                Map<String, Object> existing = findByPk(table, pkCols, pkVals);
                if (existing == null) {
                    insertRow(table, row);
                    applied++;
                } else if (!hasLm) {
                    updateRow(table, row, pkCols, pkVals);
                    applied++;
                } else {
                    long inLm = toEpochSeconds(row.get(LM));
                    long exLm = toEpochSeconds(existing.get(LM));
                    if (inLm > exLm) {
                        updateRow(table, row, pkCols, pkVals);
                        applied++;
                    } else {
                        skipped++;
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("applied", applied, "skipped", skipped));
    }

    /* ===========================
       PULL-FULL (schema + indexes + values)
       =========================== */
    @GetMapping("/pull-full")
    public ResponseEntity<Map<String, Object>> pullFull() throws Exception {
        List<Map<String, Object>> tables = new ArrayList<>();

        // ---------- barrios ----------
        tables.add(tableWithSchemaIdxValues(
                "barrios",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("nombre","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(idx("idx_barrios_lm","last_modified"), idx("idx_barrios_sd","sql_deleted")),
                null,
                selectValuesSec("barrios", List.of("id","nombre","last_modified","sql_deleted"))
        ));

        // ---------- caps ----------
        tables.add(tableWithSchemaIdxValues(
                "caps",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("nombre","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(idx("idx_caps_lm","last_modified"), idx("idx_caps_sd","sql_deleted")),
                null,
                selectValuesSec("caps", List.of("id","nombre","last_modified","sql_deleted"))
        ));

        // ---------- viviendas ----------
        tables.add(tableWithSchemaIdxValues(
                "viviendas",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("barrios_id","TEXT"),
                        col("caps_id","TEXT"),
                        col("fecha","TEXT"),
                        col("accedio",        bool("accedio")),
                        col("motivo",         "TEXT"), // enum como texto
                        col("casa","TEXT"),
                        col("manzana","TEXT"),
                        col("latitud","TEXT"),
                        col("longitud","TEXT"),
                        col("direccion","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted",    bool0("sql_deleted")),
                        fk("barrios_id","REFERENCES barrios(id) ON DELETE SET NULL"),
                        fk("caps_id","REFERENCES caps(id) ON DELETE SET NULL")
                ),
                List.of(
                        idx("idx_viviendas_lm","last_modified"),
                        idx("idx_viviendas_sd","sql_deleted"),
                        idx("idx_viviendas_barrio","barrios_id"),
                        idx("idx_viviendas_caps","caps_id")
                ),
                null,
                selectValuesSec("viviendas", List.of(
                        "id","barrios_id","caps_id","fecha","accedio","motivo","casa","manzana",
                        "latitud","longitud","direccion","last_modified","sql_deleted"
                ))
        ));


        // ---------- personas ----------
        tables.add(tableWithSchemaIdxValues(
                "personas",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("dni","TEXT"),
                        col("apellido","TEXT NOT NULL"),
                        col("nombre","TEXT NOT NULL"),
                        col("sexo","TEXT"),
                        col("fecha_nac","TEXT"),
                        col("telefono","TEXT"),
                        col("cobertura_salud","TEXT"),
                        col("viviendas_id","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("viviendas_id","REFERENCES viviendas(id) ON DELETE SET NULL")
                ),
                List.of(idx("idx_personas_lm","last_modified"), idx("idx_personas_sd","sql_deleted")),
                null,
                selectValuesSec("personas", List.of(
                        "id","dni","apellido","nombre","sexo","fecha_nac","telefono","cobertura_salud",
                        "viviendas_id","last_modified","sql_deleted"
                ))
        ));

        // ---------- responsables ----------
        tables.add(tableWithSchemaIdxValues(
                "responsables",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("user_id","TEXT"),
                        col("nombre","TEXT NOT NULL"),
                        col("matricula","TEXT"),
                        col("email","TEXT"),
                        col("activo", bool1("activo")),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(idx("idx_resp_lm","last_modified"), idx("idx_resp_sd","sql_deleted")),
                null,
                selectValuesSec("responsables", List.of(
                        "id","user_id","nombre","matricula","email","activo","last_modified","sql_deleted"
                ))
        ));

        // ---------- visitas ----------
        tables.add(tableWithSchemaIdxValues(
                "visitas",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id","TEXT NOT NULL"),
                        col("responsable_id","TEXT NOT NULL"),
                        col("created_by_user_id","TEXT"),
                        col("tipo","TEXT NOT NULL"),
                        col("fecha","TEXT NOT NULL"),
                        col("ubicacion_gps","TEXT"),
                        col("observaciones","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("persona_id","REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("responsable_id","REFERENCES responsables(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_visitas_lm","last_modified"),
                        idx("idx_visitas_sd","sql_deleted"),
                        idx("idx_visitas_persona","persona_id"),
                        idx("idx_visitas_resp","responsable_id"),
                        idx("idx_visitas_tipo","tipo")
                ),
                null,
                selectValuesSec("visitas", List.of(
                        "id","persona_id","responsable_id","created_by_user_id","tipo","fecha","ubicacion_gps","observaciones","last_modified","sql_deleted"
                ))
        ));

        // ---------- control_domicilio ----------
        tables.add(tableWithSchemaIdxValues(
                "control_domicilio",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("visita_id","TEXT NOT NULL"),
                        col("created_by_user_id","TEXT"),
                        col("ta_sistolica","INTEGER"),
                        col("ta_diastolica","INTEGER"),
                        col("accede_programa", bool("accede_programa")),
                        col("acepta_laboratotio", bool("acepta_laboratotio")),
                        col("observaciones","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("visita_id","REFERENCES visitas(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_cd_lm","last_modified"),
                        idx("idx_cd_sd","sql_deleted"),
                        idxUnique("idx_cd_visita","visita_id")
                ),
                null,
                selectValuesSec("control_domicilio", List.of(
                        "id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica",
                        "accede_programa","acepta_laboratotio","observaciones","last_modified","sql_deleted"
                ))
        ));

        // ---------- control_consultorio ----------
        tables.add(tableWithSchemaIdxValues(
                "control_consultorio",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("visita_id","TEXT NOT NULL"),
                        col("created_by_user_id","TEXT"),
                        col("fecha","TEXT"),
                        col("asistencia", bool("asistencia")),
                        col("ta_sistolica","INTEGER"),
                        col("ta_diastolica","INTEGER"),
                        col("peso","REAL"),
                        col("talla","REAL"),
                        col("imc","REAL"),
                        col("circ_cintura","INTEGER"),
                        col("fumador", bool("fumador")),
                        col("confirm_hta", bool("confirm_hta")),
                        col("control_medicacion", bool("control_medicacion")),
                        col("derivacion", bool("derivacion")),
                        col("entrega_medicacion", bool("entrega_medicacion")),
                        col("eventos", bool("eventos")),
                        col("conducta", bool("conducta")),
                        col("medicacion","TEXT"),
                        col("observaciones","TEXT"),
                        col("observaciones_derivacion","TEXT"),
                        col("observaciones_eventos","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("visita_id","REFERENCES visitas(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_cc_lm","last_modified"),
                        idx("idx_cc_sd","sql_deleted"),
                        idxUnique("idx_cc_visita","visita_id")
                ),
                null,
                selectValuesSec("control_consultorio", List.of(
                        "id","visita_id","created_by_user_id","fecha","asistencia",
                        "ta_sistolica","ta_diastolica","peso","talla","imc",
                        "circ_cintura","fumador","confirm_hta","control_medicacion","derivacion",
                        "entrega_medicacion","eventos","conducta",
                        "medicacion","observaciones","observaciones_derivacion","observaciones_eventos",
                        "last_modified","sql_deleted"
                ))
        ));

        // ---------- medicacion_hta (con LM/SD) ----------
        tables.add(tableWithSchemaIdxValues(
                "medicacion_hta",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("nombre","TEXT NOT NULL"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(
                        idx("idx_medhta_nombre","nombre"),
                        idx("idx_medhta_lm","last_modified"),
                        idx("idx_medhta_sd","sql_deleted")
                ),
                null,
                selectValuesSec("medicacion_hta", List.of("id","nombre","last_modified","sql_deleted"))
        ));

        // ---------- antecedentes ----------
        tables.add(tableWithSchemaIdxValues(
                "antecedentes",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id","TEXT NOT NULL"),
                        col("visita_id","TEXT"),
                        col("created_by_user_id","TEXT"),
                        col("diabetes", bool("diabetes")),
                        col("dislipemia", bool("dislipemia")),
                        col("enf_cardiovascular", bool("enf_cardiovascular")),
                        col("enf_renal_cronica", bool("enf_renal_cronica")),
                        col("hta_previa", bool("hta_previa")),
                        col("tabaquismo", bool("tabaquismo")),
                        col("tratamiento_enf_cardiovascular", bool("tratamiento_enf_cardiovascular")),
                        col("tratamiento_enf_diabetes", bool("tratamiento_enf_diabetes")),
                        col("tratamiento_enf_dislipemia", bool("tratamiento_enf_dislipemia")),
                        col("tratamiento_enf_renal", bool("tratamiento_enf_renal")),
                        col("tratamiento_hta_previa", bool("tratamiento_hta_previa")),
                        col("desc_trat_enf_cardiovascular","TEXT"),
                        col("desc_trat_enf_dislipemia","TEXT"),
                        col("desc_trat_enf_renal" ,"TEXT"),
                        col("desc_trat_enf_diabetes","TEXT"),
                        col("desc_trat_hta_previa","TEXT"),
                        col("otros","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("persona_id","REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("visita_id","REFERENCES visitas(id) ON DELETE SET NULL")
                ),
                List.of(
                        idx("idx_ant_lm","last_modified"),
                        idx("idx_ant_sd","sql_deleted"),
                        idx("idx_ant_persona","persona_id")
                ),
                null,
                selectValuesSec("antecedentes", List.of(
                        "id","persona_id","visita_id","created_by_user_id",
                        "diabetes","dislipemia","enf_cardiovascular","enf_renal_cronica","hta_previa","tabaquismo",
                        "tratamiento_enf_cardiovascular","tratamiento_enf_diabetes","tratamiento_enf_dislipemia",
                        "tratamiento_enf_renal","tratamiento_hta_previa","desc_trat_enf_cardiovascular","desc_trat_enf_dislipemia",
                        "desc_trat_enf_renal",
                        "desc_trat_enf_diabetes","desc_trat_hta_previa",
                        "otros","last_modified","sql_deleted"
                ))
        ));

        // ---------- antecedente_has_medicacion_hta (con LM/SD) ----------
        tables.add(tableWithSchemaIdxValues(
                "antecedente_has_medicacion_hta",
                List.of(
                        col("antecedente_id","TEXT NOT NULL"),
                        col("medicacion_id","TEXT NOT NULL"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("antecedente_id","REFERENCES antecedentes(id) ON DELETE CASCADE"),
                        fk("medicacion_id","REFERENCES medicacion_hta(id) ON DELETE CASCADE")
                ),
                List.of(
                        idxUnique("uk_ant_medhta","antecedente_id,medicacion_id"),
                        idx("idx_ant_medhta_ant","antecedente_id"),
                        idx("idx_ant_medhta_med","medicacion_id"),
                        idx("idx_ant_medhta_lm","last_modified"),
                        idx("idx_ant_medhta_sd","sql_deleted")
                ),
                List.of( // <<--- TRIGGER CORRECTO
                        trigger(
                                "antecedente_has_medicacion_hta_trigger_last_modified",
                                "CREATE TRIGGER IF NOT EXISTS antecedente_has_medicacion_hta_trigger_last_modified " +
                                        "AFTER UPDATE ON antecedente_has_medicacion_hta " +
                                        "FOR EACH ROW WHEN NEW.last_modified <= OLD.last_modified " +
                                        "BEGIN " +
                                        "  UPDATE antecedente_has_medicacion_hta " +
                                        "     SET last_modified = CAST(strftime('%s','now') AS INTEGER) " +
                                        "   WHERE antecedente_id = NEW.antecedente_id " +
                                        "     AND medicacion_id  = NEW.medicacion_id; " +
                                        "END;"
                        )
                ),
                selectValuesSec("antecedente_has_medicacion_hta", List.of(
                        "antecedente_id","medicacion_id","last_modified","sql_deleted"
                ))
        ));

        // ---------- lab_tipos ----------
        tables.add(tableWithSchemaIdxValues(
                "lab_tipos",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("codigo","TEXT NOT NULL"),
                        col("nombre","TEXT NOT NULL"),
                        col("unidad_default","TEXT"),
                        col("ref_min","REAL"),
                        col("ref_max","REAL"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(
                        idxUnique("idx_labtip_codigo","codigo"),
                        idx("idx_labtip_lm","last_modified"),
                        idx("idx_labtip_sd","sql_deleted")
                ),
                null,
                selectValuesSec("lab_tipos", List.of(
                        "id","codigo","nombre","unidad_default","ref_min","ref_max","last_modified","sql_deleted"
                ))
        ));

        // ---------- lab_resultados ----------
        tables.add(tableWithSchemaIdxValues(
                "lab_resultados",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id","TEXT NOT NULL"),
                        col("visita_id","TEXT"),
                        col("tipo_id","TEXT NOT NULL"),
                        col("created_by_user_id","TEXT"),
                        col("fecha_realizado","TEXT NOT NULL"),
                        col("valor_num","REAL"),
                        col("valor_texto","TEXT"),
                        col("unidad","TEXT"),
                        col("laboratorio","TEXT"),
                        col("observaciones","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("persona_id","REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("visita_id","REFERENCES visitas(id) ON DELETE SET NULL"),
                        fk("tipo_id","REFERENCES lab_tipos(id) ON DELETE RESTRICT")
                ),
                List.of(
                        idx("idx_lr_lm","last_modified"),
                        idx("idx_lr_sd","sql_deleted"),
                        idx("idx_lr_persona","persona_id"),
                        idx("idx_lr_tipo","tipo_id")
                ),
                null,
                selectValuesSec("lab_resultados", List.of(
                        "id","persona_id","visita_id","tipo_id","created_by_user_id","fecha_realizado",
                        "valor_num","valor_texto","unidad","laboratorio","observaciones","last_modified","sql_deleted"
                ))
        ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("database", "frcv");
        body.put("version", 1);
        body.put("encrypted", false);
        body.put("mode", "full");
        body.put("tables", tables);

        return ResponseEntity.ok(body);
    }

    /* ===========================
       Helpers de DB / utilitarios
       =========================== */

    private boolean tableExists(String table) throws SQLException {
        try (Connection c = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getTables(c.getCatalog(), null, table, null)) {
                return rs.next();
            }
        }
    }

    private List<String> getTableColumns(String table) throws SQLException {
        List<String> cols = new ArrayList<>();
        try (Connection c = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getColumns(c.getCatalog(), null, table, null)) {
                List<Col> tmp = new ArrayList<>();
                while (rs.next()) {
                    tmp.add(new Col(rs.getString("COLUMN_NAME"), rs.getInt("ORDINAL_POSITION")));
                }
                tmp.sort(Comparator.comparingInt(a -> a.pos));
                for (Col col : tmp) cols.add(col.name);
            }
        }
        return cols;
    }

    private static class Col { final String name; final int pos; Col(String n,int p){name=n;pos=p;} }

    /* ---------- PK helpers (simple/compuesta) ---------- */

    private Map<String, Object> findByPk(String table, List<String> pkCols, List<Object> pkVals) {
        String where = "";
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < pkCols.size(); i++) {
            if (i > 0) where += " AND ";
            where += pkCols.get(i) + " = ?";
            args.add(pkVals.get(i));
        }
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT * FROM " + table + " WHERE " + where + " LIMIT 1", args.toArray());
        return list.isEmpty() ? null : list.get(0);
    }

    private void insertRow(String table, Map<String, Object> row) {
        List<String> cols = new ArrayList<>(row.keySet());
        String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "INSERT INTO " + table + " (" + String.join(",", cols) + ") VALUES (" + placeholders + ")";
        Object[] args = cols.stream().map(row::get).toArray();
        jdbc.update(sql, args);
    }

    private void updateRow(String table, Map<String, Object> row, List<String> pkCols, List<Object> pkVals) {
        String where = "";
        List<Object> whereArgs = new ArrayList<>();
        for (int i = 0; i < pkCols.size(); i++) {
            if (i > 0) where += " AND ";
            where += pkCols.get(i) + " = ?";
            whereArgs.add(pkVals.get(i));
        }
        Integer n = jdbc.queryForObject("SELECT COUNT(1) FROM " + table + " WHERE " + where, Integer.class, whereArgs.toArray());
        if (n == null || n == 0) { insertRow(table, row); return; }

        List<String> cols = new ArrayList<>(row.keySet());
        cols.removeAll(pkCols);
        if (cols.isEmpty()) return;

        String set = cols.stream().map(c -> c + "=?").collect(Collectors.joining(","));
        List<Object> args = cols.stream().map(row::get).collect(Collectors.toCollection(ArrayList::new));
        args.addAll(pkVals);

        String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
        jdbc.update(sql, args.toArray());
    }

    private Map<String, Object> zipRow(List<String> cols, List<Object> values) {
        Map<String, Object> map = new LinkedHashMap<>();
        int n = Math.min(cols.size(), values.size());
        for (int i = 0; i < n; i++) map.put(cols.get(i), values.get(i));
        return map;
    }

    /* ---------------- Tiempo / booleanos ---------------- */

    private long toEpochSeconds(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) {
            long x = n.longValue();
            return (x > 10_000_000_000L) ? (x / 1000L) : x;
        }
        try {
            long x = Long.parseLong(String.valueOf(v));
            return (x > 10_000_000_000L) ? (x / 1000L) : x;
        } catch (NumberFormatException ignore) {
            try {
                return Instant.parse(String.valueOf(v)).getEpochSecond();
            } catch (Exception e) {
                return 0L;
            }
        }
    }

    private boolean isBooleanishColumn(String c) {
        String s = c.toLowerCase(Locale.ROOT);
        return s.equals("sql_deleted") ||
                s.equals("asistencia") ||
                s.equals("accede_programa") ||
                s.equals("acepta_laboratotio") ||
                s.equals("fumador") ||
                s.equals("confirm_hta") ||
                s.equals("control_medicacion") ||
                s.equals("derivacion") ||
                s.equals("entrega_medicacion") ||
                s.equals("eventos") ||
                s.equals("conducta") ||
                s.equals("diabetes") ||
                s.equals("dislipemia") ||
                s.equals("enf_cardiovascular") ||
                s.equals("enf_renal_cronica") ||
                s.equals("hta_previa") ||
                s.equals("tabaquismo") ||
                s.equals("tratamiento_enf_cardiovascular") ||
                s.equals("tratamiento_enf_diabetes") ||
                s.equals("tratamiento_enf_dislipemia") ||
                s.equals("tratamiento_enf_renal") ||
                s.equals("tratamiento_hta_previa");
    }

    private Object boolAsInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return (n.intValue() != 0) ? 1 : 0;
        String s = String.valueOf(v).trim().toLowerCase();
        if ("true".equals(s) || "1".equals(s)) return 1;
        if ("false".equals(s) || "0".equals(s) || s.isEmpty()) return 0;
        return v;
    }

    /* ---------------- pull-full helpers ---------------- */

    private String bool0(String col) { return "INTEGER DEFAULT 0 CHECK (" + col + " IN (0, 1))"; }
    private String bool1(String col) { return "INTEGER DEFAULT 1 CHECK (" + col + " IN (0, 1))"; }
    private String bool(String col)  { return "INTEGER CHECK (" + col + " IN (0, 1))"; }

    private Map<String, Object> col(String name, String ddl) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("column", name);
        m.put("value", ddl);
        return m;
    }

    private Map<String, Object> fk(String colName, String fkDDL) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("foreignkey", colName);
        m.put("value", fkDDL);
        return m;
    }

    private Map<String, Object> idx(String name, String valueExpr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", valueExpr);
        return m;
    }

    private Map<String, Object> idxUnique(String name, String valueExpr) {
        Map<String, Object> m = idx(name, valueExpr);
        m.put("mode", "UNIQUE");
        return m;
    }

    private Map<String, Object> trigger(String name, String ddl) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", ddl); // el plugin acepta el SQL completo en "value"
        return m;
    }

    private Map<String, Object> tableWithSchemaIdxValues(
            String tableName,
            List<Map<String, Object>> schema,
            List<Map<String, Object>> indexes,
            List<Map<String, Object>> triggersOrExtras,
            List<List<Object>> values
    ) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("name", tableName);
        t.put("schema", schema);
        if (indexes != null && !indexes.isEmpty()) t.put("indexes", indexes);
        t.put("values", values);
        return t;
    }

    private List<List<Object>> selectValuesSec(String table, List<String> cols) {
        String sql = "SELECT " + String.join(",", cols) + " FROM " + table;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        List<List<Object>> values = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            List<Object> arr = new ArrayList<>(cols.size());
            for (String c : cols) {
                Object v = r.get(c);
                if (v instanceof Boolean b) {
                    v = b ? 1 : 0;
                } else if ("last_modified".equalsIgnoreCase(c)) {
                    v = toEpochSeconds(v);
                } else if (isBooleanishColumn(c)) {
                    v = boolAsInt(v);
                }
                arr.add(v);
            }
            values.add(arr);
        }
        return values;
    }
}
