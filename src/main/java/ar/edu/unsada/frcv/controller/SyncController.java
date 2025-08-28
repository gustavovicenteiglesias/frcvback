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
}
