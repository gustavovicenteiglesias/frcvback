package ar.edu.unsada.frcv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final JdbcTemplate jdbc;

    public SyncController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Orden de importación para respetar FKs (padres → hijos)
    private static final List<String> TABLE_ORDER = List.of(
            "personas",
            "responsables",
            "visitas",
            "control_domicilio",
            "control_consultorio",
            "antecedentes",
            "lab_tipos",
            "lab_resultados",
            "outbox" // opcional
    );

    // Orden de columnas "móvil" EXACTO según el JSON del front (arrays de arrays)
    private static final Map<String, List<String>> MOBILE_COLS = new LinkedHashMap<>() {{
        put("outbox", List.of(
                "id","table","entity_id","op","payload","last_modified","sql_deleted","status"
        ));
        put("personas", List.of(
                "id","dni","apellido","nombre","sexo","fecha_nac","telefono","direccion","last_modified","sql_deleted"
        ));
        put("responsables", List.of(
                "id","user_id","nombre","matricula","email","activo","last_modified","sql_deleted"
        ));
        put("visitas", List.of(
                "id","persona_id","responsable_id","created_by_user_id","tipo","fecha","ubicacion_gps","observaciones","last_modified","sql_deleted"
        ));
        put("control_domicilio", List.of(
                "id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica","frecuencia_cardiaca","peso",
                "talla","imc","riesgo_cv","derivar_a_consultorio","observaciones","last_modified","sql_deleted"
        ));
        put("control_consultorio", List.of(
                "id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica","medicacion_json","conducta","last_modified","sql_deleted"
        ));
        // ⚠️ Ajustado según tu dump: created_by_user_id ANTES de fecha_realizado
        put("lab_resultados", List.of(
                "id","persona_id","visita_id","tipo_id","created_by_user_id","fecha_realizado",
                "valor_num","valor_texto","unidad","laboratorio","observaciones","last_modified","sql_deleted"
        ));
        // Antecedentes: mantuve el orden tal como viene en tu dump
        put("antecedentes", List.of(
                "id",
                "persona_id",
                "visita_id",
                "created_by_user_id",
                "valid_from",
                "valid_to",
                "is_current",
                "tabaquismo",
                "ex_tabaquista",
                "diabetes",
                "dislipemia",
                "hta_previa",
                "enf_cardiovascular",
                "enf_renal_cronica",
                "fam_cvd_precoz",
                "alcohol_riesgo",
                "actividad_fisica_baja",
                "obesidad",
                "otros",
                "last_modified",
                "sql_deleted"
        ));

        put("lab_tipos", List.of(
                "id","codigo","nombre","unidad_default","ref_min","ref_max","last_modified","sql_deleted"
        ));
    }};

    private static final String PK = "id";
    private static final String LM = "last_modified";

    // ---------------- PULL ----------------
    @GetMapping("/pull")
    public ResponseEntity<Map<String, Object>> pull(@RequestParam(required = false) String since) throws Exception {
        long cutoff = parseSince(since);
        List<Map<String, Object>> tables = new ArrayList<>();

        for (String table : TABLE_ORDER) {
            if (!tableExists(table)) continue;

            // Usamos orden móvil si está definido; sino, el físico de la DB
            List<String> cols = MOBILE_COLS.getOrDefault(table, getTableColumns(table));
            boolean hasLm = cols.contains(LM);

            String sql = hasLm
                    ? "SELECT * FROM " + table + " WHERE " + LM + " > ?"
                    : "SELECT * FROM " + table;

            List<Map<String, Object>> rows = hasLm
                    ? jdbc.queryForList(sql, cutoff)
                    : jdbc.queryForList(sql);

            // Convertimos a arrays en el orden esperado por el front
            List<List<Object>> values = new ArrayList<>(rows.size());
            for (Map<String, Object> r : rows) {
                List<Object> arr = new ArrayList<>(cols.size());
                for (String c : cols) arr.add(r.get(c));
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

    // ---------------- PUSH ----------------
    @PostMapping("/push")
    public ResponseEntity<Map<String, Object>> push(@RequestBody Map<String, Object> partial) throws Exception {
        Object tablesObj = partial.get("tables");
        if (!(tablesObj instanceof List<?> rawTables)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido: falta 'tables'"));
        }

        // Parse entrante: tabla -> filas (arrays)
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

            // Orden móvil si existe; si no, orden físico de DB
            List<String> cols = MOBILE_COLS.getOrDefault(table, getTableColumns(table));
            boolean hasLm = cols.contains(LM);

            for (List<Object> rowArr : incoming.get(table)) {
                Map<String, Object> row = zipRow(cols, rowArr);
                Object idVal = row.get(PK);
                if (idVal == null) { skipped++; continue; }

                Map<String, Object> existing = findById(table, PK, idVal);
                if (existing == null) {
                    insertRow(table, row);
                    applied++;
                } else if (!hasLm) {
                    updateRow(table, row, PK, idVal);
                    applied++;
                } else {
                    long inLm = toEpochMillis(row.get(LM));
                    long exLm = toEpochMillis(existing.get(LM));
                    if (inLm > exLm) {
                        updateRow(table, row, PK, idVal);
                        applied++;
                    } else {
                        skipped++;
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("applied", applied, "skipped", skipped));
    }

    // ---------------- Helpers ----------------

    private boolean tableExists(String table) throws SQLException {
        try (Connection c = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getTables(c.getCatalog(), null, table, null)) {
                return rs.next();
            }
        }
    }

    /** Columnas físicas en orden (ORDINAL_POSITION) */
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

    private static class Col { final String name; final int pos; Col(String n, int p){name=n;pos=p;} }

    private Map<String, Object> findById(String table, String pkCol, Object idVal) {
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT * FROM " + table + " WHERE " + pkCol + " = ? LIMIT 1", idVal);
        return list.isEmpty() ? null : list.get(0);
    }

    private void insertRow(String table, Map<String, Object> row) {
        List<String> cols = new ArrayList<>(row.keySet());
        String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "INSERT INTO " + table + " (" + String.join(",", cols) + ") VALUES (" + placeholders + ")";
        Object[] args = cols.stream().map(row::get).toArray();
        jdbc.update(sql, args);
    }

    private void updateRow(String table, Map<String, Object> row, String pkCol, Object idVal) {
        Integer n = jdbc.queryForObject("SELECT COUNT(1) FROM " + table + " WHERE " + pkCol + " = ?",
                Integer.class, idVal);
        if (n == null || n == 0) { insertRow(table, row); return; }

        List<String> cols = new ArrayList<>(row.keySet());
        cols.remove(pkCol);
        if (cols.isEmpty()) return;

        String set = cols.stream().map(c -> c + "=?").collect(Collectors.joining(","));
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + pkCol + " = ?";
        List<Object> args = cols.stream().map(row::get).collect(Collectors.toCollection(ArrayList::new));
        args.add(idVal);
        jdbc.update(sql, args.toArray());
    }

    private Map<String, Object> zipRow(List<String> cols, List<Object> values) {
        Map<String, Object> map = new LinkedHashMap<>();
        int n = Math.min(cols.size(), values.size());
        for (int i = 0; i < n; i++) map.put(cols.get(i), values.get(i));
        return map;
    }

    private long parseSince(String since) {
        if (since == null || since.isBlank()) return 0L;
        try {
            long n = Long.parseLong(since.trim());
            return n < 10_000_000_000L ? n * 1000L : n; // s → ms
        } catch (NumberFormatException ignore) {}
        try { return Instant.parse(since.trim()).toEpochMilli(); }
        catch (DateTimeParseException ignore) {}
        return 0L;
    }

    private long toEpochMillis(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n)
            return n.longValue() < 10_000_000_000L ? n.longValue() * 1000L : n.longValue();
        try { return parseSince(String.valueOf(val)); } catch (Exception e) { return 0L; }
    }
    // ============================
// GET /api/sync/pull-full
// Formato EXACTO como doc del plugin
// ============================
    @GetMapping("/pull-full")
    public ResponseEntity<Map<String, Object>> pullFull() throws Exception {
        List<Map<String, Object>> tables = new ArrayList<>();

        // ---------- personas ----------
        tables.add(tableWithSchemaIdxValues(
                "personas",
                List.of(
                        col("id",            "TEXT PRIMARY KEY NOT NULL"),
                        col("dni",           "TEXT"),
                        col("apellido",      "TEXT NOT NULL"),
                        col("nombre",        "TEXT NOT NULL"),
                        col("sexo",          "TEXT"),
                        col("fecha_nac",     "TEXT"),
                        col("telefono",      "TEXT"),
                        col("direccion",     "TEXT"),
                        col("last_modified", "INTEGER NOT NULL"),
                        col("sql_deleted",   bool0("sql_deleted"))
                ),
                List.of(
                        idx("idx_personas_lm", "last_modified"),
                        idx("idx_personas_sd", "sql_deleted")
                ),
                null, // sin FKs
                selectValuesSec("personas",
                        List.of("id","dni","apellido","nombre","sexo","fecha_nac","telefono","direccion","last_modified","sql_deleted"))
        ));

        // ---------- responsables ----------
        tables.add(tableWithSchemaIdxValues(
                "responsables",
                List.of(
                        col("id",            "TEXT PRIMARY KEY NOT NULL"),
                        col("user_id",       "TEXT"),
                        col("nombre",        "TEXT NOT NULL"),
                        col("matricula",     "TEXT"),
                        col("email",         "TEXT"),
                        col("activo",        bool1("activo")),
                        col("last_modified", "INTEGER NOT NULL"),
                        col("sql_deleted",   bool0("sql_deleted"))

                ),
                List.of(
                        idx("idx_resp_lm", "last_modified"),
                        idx("idx_resp_sd", "sql_deleted")
                ),
                null,
                selectValuesSec("responsables",
                        List.of("id","user_id","nombre","matricula","email","activo","last_modified","sql_deleted"))
        ));

        // ---------- visitas ----------
        tables.add(tableWithSchemaIdxValues(
                "visitas",
                List.of(
                        col("id",               "TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id",       "TEXT NOT NULL"),
                        col("responsable_id",   "TEXT NOT NULL"),
                        col("created_by_user_id","TEXT"),
                        col("tipo",             "TEXT NOT NULL"),
                        col("fecha",            "TEXT NOT NULL"),
                        col("ubicacion_gps",    "TEXT"),
                        col("observaciones",    "TEXT"),
                        col("last_modified",    "INTEGER NOT NULL"),
                        col("sql_deleted",   bool0("sql_deleted")),
                        fk("persona_id",        "REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("responsable_id",    "REFERENCES responsables(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_visitas_lm",      "last_modified"),
                        idx("idx_visitas_sd",      "sql_deleted"),
                        idx("idx_visitas_persona", "persona_id"),
                        idx("idx_visitas_resp",    "responsable_id")
                ),
                null,
                selectValuesSec("visitas",
                        List.of("id","persona_id","responsable_id","created_by_user_id","tipo","fecha","ubicacion_gps","observaciones","last_modified","sql_deleted"))
        ));

        // ---------- control_domicilio ----------
        tables.add(tableWithSchemaIdxValues(
                "control_domicilio",
                List.of(
                        col("id",                    "TEXT PRIMARY KEY NOT NULL"),
                        col("visita_id",             "TEXT NOT NULL"),
                        col("created_by_user_id",    "TEXT"),
                        col("ta_sistolica",          "INTEGER"),
                        col("ta_diastolica",         "INTEGER"),
                        col("frecuencia_cardiaca",   "INTEGER"),
                        col("peso",                  "REAL"),
                        col("talla",                 "REAL"),
                        col("imc",                   "REAL"),
                        col("riesgo_cv",             "INTEGER"),
                        col("derivar_a_consultorio", bool("derivar_a_consultorio")),
                        col("observaciones",         "TEXT"),
                        col("last_modified",         "INTEGER NOT NULL"),
                        col("sql_deleted",           bool0("sql_deleted")),
                        fk("visita_id",              "REFERENCES visitas(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_cd_lm",     "last_modified"),
                        idx("idx_cd_sd",     "sql_deleted"),
                        idxUnique("idx_cd_visita", "visita_id") // UNIQUE por visita
                ),
                null,
                selectValuesSec("control_domicilio",
                        List.of("id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica","frecuencia_cardiaca","peso","talla","imc","riesgo_cv","derivar_a_consultorio","observaciones","last_modified","sql_deleted"))
        ));

        // ---------- control_consultorio ----------
        tables.add(tableWithSchemaIdxValues(
                "control_consultorio",
                List.of(
                        col("id",                 "TEXT PRIMARY KEY NOT NULL"),
                        col("visita_id",          "TEXT NOT NULL"),
                        col("created_by_user_id", "TEXT"),
                        col("ta_sistolica",       "INTEGER"),
                        col("ta_diastolica",      "INTEGER"),
                        col("medicacion_json",    "TEXT"),
                        col("conducta",           "TEXT"),
                        col("last_modified",      "INTEGER NOT NULL"),
                        col("sql_deleted",           bool0("sql_deleted")),
                        fk("visita_id",           "REFERENCES visitas(id) ON DELETE CASCADE")
                ),
                List.of(
                        idx("idx_cc_lm",     "last_modified"),
                        idx("idx_cc_sd",     "sql_deleted"),
                        idxUnique("idx_cc_visita", "visita_id")
                ),
                null,
                selectValuesSec("control_consultorio",
                        List.of("id","visita_id","created_by_user_id","ta_sistolica","ta_diastolica","medicacion_json","conducta","last_modified","sql_deleted"))
        ));

        // ---------- antecedentes ----------
        tables.add(tableWithSchemaIdxValues(
                "antecedentes",
                List.of(
                        col("id",                     "TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id",             "TEXT NOT NULL"),
                        col("visita_id",              "TEXT"),
                        col("created_by_user_id",     "TEXT"),
                        col("valid_from",             "TEXT NOT NULL"),
                        col("valid_to",               "TEXT"),
                        col("is_current",            bool1("is_current")),
                        col("tabaquismo",            bool("tabaquismo")),
                        col("ex_tabaquista",         bool("ex_tabaquista")),
                        col("diabetes",              bool("diabetes")),
                        col("dislipemia",            bool("dislipemia")),
                        col("hta_previa",            bool("hta_previa")),
                        col("enf_cardiovascular",    bool("enf_cardiovascular")),
                        col("enf_renal_cronica",     bool("enf_renal_cronica")),
                        col("fam_cvd_precoz",        bool("fam_cvd_precoz")),
                        col("alcohol_riesgo",        bool("alcohol_riesgo")),
                        col("actividad_fisica_baja", bool("actividad_fisica_baja")),
                        col("obesidad",              bool("obesidad")),
                        col("otros",                  "TEXT"),
                        col("last_modified",          "INTEGER NOT NULL"),
                        col("sql_deleted",           bool0("sql_deleted")),
                        fk("persona_id",              "REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("visita_id",               "REFERENCES visitas(id) ON DELETE SET NULL")
                ),
                List.of(
                        idx("idx_ant_lm",     "last_modified"),
                        idx("idx_ant_sd",     "sql_deleted"),
                        idx("idx_ant_persona","persona_id")
                ),
                null,
                selectValuesSec("antecedentes",
                        List.of("id","persona_id","visita_id","created_by_user_id","valid_from","valid_to","is_current","tabaquismo","ex_tabaquista","diabetes","dislipemia","hta_previa","enf_cardiovascular","enf_renal_cronica","fam_cvd_precoz","alcohol_riesgo","actividad_fisica_baja","obesidad","otros","last_modified","sql_deleted"))
        ));

        // ---------- lab_tipos ----------
        tables.add(tableWithSchemaIdxValues(
                "lab_tipos",
                List.of(
                        col("id",            "TEXT PRIMARY KEY NOT NULL"),
                        col("codigo",        "TEXT NOT NULL"),
                        col("nombre",        "TEXT NOT NULL"),
                        col("unidad_default","TEXT"),
                        col("ref_min",       "REAL"),
                        col("ref_max",       "REAL"),
                        col("last_modified", "INTEGER NOT NULL"),
                        col("sql_deleted",           bool0("sql_deleted"))
                ),
                List.of(
                        idxUnique("idx_labtip_codigo", "codigo"),
                        idx("idx_labtip_lm", "last_modified"),
                        idx("idx_labtip_sd", "sql_deleted")
                ),
                null,
                selectValuesSec("lab_tipos",
                        List.of("id","codigo","nombre","unidad_default","ref_min","ref_max","last_modified","sql_deleted"))
        ));

        // ---------- lab_resultados ----------
        tables.add(tableWithSchemaIdxValues(
                "lab_resultados",
                List.of(
                        col("id",                 "TEXT PRIMARY KEY NOT NULL"),
                        col("persona_id",         "TEXT NOT NULL"),
                        col("visita_id",          "TEXT"),
                        col("tipo_id",            "TEXT NOT NULL"),
                        col("created_by_user_id", "TEXT"),
                        col("fecha_realizado",    "TEXT NOT NULL"),
                        col("valor_num",          "REAL"),
                        col("valor_texto",        "TEXT"),
                        col("unidad",             "TEXT"),
                        col("laboratorio",        "TEXT"),
                        col("observaciones",      "TEXT"),
                        col("last_modified",      "INTEGER NOT NULL"),
                        col("sql_deleted",           bool0("sql_deleted")),
                        fk("persona_id",          "REFERENCES personas(id) ON DELETE CASCADE"),
                        fk("visita_id",           "REFERENCES visitas(id) ON DELETE SET NULL"),
                        fk("tipo_id",             "REFERENCES lab_tipos(id) ON DELETE RESTRICT")
                ),
                List.of(
                        idx("idx_lr_lm",     "last_modified"),
                        idx("idx_lr_sd",     "sql_deleted"),
                        idx("idx_lr_persona","persona_id"),
                        idx("idx_lr_tipo",   "tipo_id")
                ),
                null,
                selectValuesSec("lab_resultados",
                        List.of("id","persona_id","visita_id","tipo_id","created_by_user_id","fecha_realizado","valor_num","valor_texto","unidad","laboratorio","observaciones","last_modified","sql_deleted"))
        ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("database", "frcv");
        body.put("version", 1);
        body.put("encrypted", false);
        body.put("mode", "full");
        body.put("tables", tables);

        return ResponseEntity.ok(body);
    }

    /* ================= Helpers ================= */

    // === helpers de tipo booleano (0/1) ===
    private String bool0(String col) { return "INTEGER DEFAULT 0 CHECK (" + col + " IN (0, 1))"; }
    private String bool1(String col) { return "INTEGER DEFAULT 1 CHECK (" + col + " IN (0, 1))"; }
    private String bool (String col) { return "INTEGER CHECK (" + col + " IN (0, 1))"; }


    private Map<String,Object> col(String name, String ddl) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("column", name);
        m.put("value", ddl); // DDL completo por columna
        return m;
    }

    private Map<String,Object> fk(String colName, String fkDDL) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("foreignkey", colName);
        m.put("value", fkDDL);
        return m;
    }

    private Map<String,Object> idx(String name, String valueExpr) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", valueExpr); // puede ser "col", "col DESC", "col1, col2"
        return m;
    }

    private Map<String,Object> idxUnique(String name, String valueExpr) {
        Map<String,Object> m = idx(name, valueExpr);
        m.put("mode", "UNIQUE");
        return m;
    }

    /** Construye el objeto {name, schema, indexes, values} */
    private Map<String,Object> tableWithSchemaIdxValues(
            String tableName,
            List<Map<String,Object>> schema,
            List<Map<String,Object>> indexes,
            List<Map<String,Object>> triggersOrExtras, // no usado, reservado
            List<List<Object>> values
    ) {
        Map<String,Object> t = new LinkedHashMap<>();
        t.put("name", tableName);
        t.put("schema", schema);
        if (indexes != null && !indexes.isEmpty()) t.put("indexes", indexes);
        t.put("values", values);
        return t;
    }

    /** Devuelve values en el orden de cols, normalizando booleans→0/1 y last_modified→segundos */
    private List<List<Object>> selectValuesSec(String table, List<String> cols) {
        String sql = "SELECT " + String.join(",", cols) + " FROM " + table;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        List<List<Object>> values = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            List<Object> arr = new ArrayList<>(cols.size());
            for (String c : cols) {
                Object v = r.get(c);
                if (v instanceof Boolean b) {
                    v = b ? 1 : 0; // boolean → 0/1
                } else if ("last_modified".equalsIgnoreCase(c)) {
                    v = toEpochSeconds(v); // normaliza a segundos
                } else if ("sql_deleted".equalsIgnoreCase(c) || "is_current".equalsIgnoreCase(c)
                        || "derivar_a_consultorio".equalsIgnoreCase(c)
                        || "tabaquismo".equalsIgnoreCase(c) || "ex_tabaquista".equalsIgnoreCase(c)
                        || "diabetes".equalsIgnoreCase(c) || "dislipemia".equalsIgnoreCase(c)
                        || "hta_previa".equalsIgnoreCase(c) || "enf_cardiovascular".equalsIgnoreCase(c)
                        || "enf_renal_cronica".equalsIgnoreCase(c) || "fam_cvd_precoz".equalsIgnoreCase(c)
                        || "alcohol_riesgo".equalsIgnoreCase(c) || "actividad_fisica_baja".equalsIgnoreCase(c)
                        || "obesidad".equalsIgnoreCase(c)) {
                    v = boolAsInt(v); // 0/1 si viniera como boolean/STRING
                }
                arr.add(v);
            }
            values.add(arr);
        }
        return values;
    }

    private Object boolAsInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return (n.intValue() != 0) ? 1 : 0;
        String s = String.valueOf(v).trim().toLowerCase();
        if ("true".equals(s) || "1".equals(s)) return 1;
        if ("false".equals(s) || "0".equals(s) || s.isEmpty()) return 0;
        // fallback: no tocar
        return v;
    }

    private long toEpochSeconds(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) {
            long x = n.longValue();
            // si parece milisegundos, convertimos a segundos
            return (x > 10_000_000_000L) ? (x / 1000L) : x;
        }
        try {
            long x = Long.parseLong(String.valueOf(v));
            return (x > 10_000_000_000L) ? (x / 1000L) : x;
        } catch (NumberFormatException ignore) {
            // si viene ISO-8601
            try {
                return java.time.Instant.parse(String.valueOf(v)).getEpochSecond();
            } catch (Exception e) {
                return 0L;
            }
        }
    }

}
