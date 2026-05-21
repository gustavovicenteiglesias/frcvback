package ar.edu.unsada.frcv.controller;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import ar.edu.unsada.frcv.security.JwtUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final JdbcTemplate jdbc;

    private static final List<String> VIVIENDAS_LEGACY_COLS = List.of(
            "id","barrios_id","caps_id","fecha","accedio","motivo","casa","manzana",
            "latitud","longitud","direccion","last_modified","sql_deleted"
    );

    private static final List<String> VIVIENDAS_COLS = List.of(
            "id","barrios_id","caps_id","fecha","accedio","motivo","casa","manzana",
            "latitud","longitud","direccion","last_modified","sql_deleted",
            "gps_accuracy","gps_captured_at","ubicacion_fuente","confianza_dato"
    );

    public SyncController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void ensureQualityColumnsOnStartup() {
        try {
            ensureViviendasQualityColumns();
        } catch (Exception e) {
            log.warn("No se pudieron asegurar las columnas de calidad de viviendas. Revisar migracion manual.", e);
        }
    }

    @PostMapping("/event")
    public ResponseEntity<Map<String, Object>> syncEvent(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        try {
            ensureSyncEventsTable();
            JwtUser jwtUser = jwtUser(authentication);
            jdbc.update("""
                    INSERT INTO sync_events
                    (id, user_id, email, device_id, platform, operation, result, message, app_version, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    UUID.randomUUID().toString(),
                    jwtUser == null ? null : jwtUser.getUserId(),
                    jwtUser == null ? null : jwtUser.getEmail(),
                    shortText(body.get("deviceId"), 120),
                    shortText(body.get("platform"), 40),
                    shortText(body.get("operation"), 40),
                    shortText(body.get("result"), 20),
                    shortText(body.get("message"), 240),
                    shortText(body.get("appVersion"), 40),
                    Instant.now().getEpochSecond()
            );
            return ResponseEntity.ok(Map.of("logged", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("logged", false));
        }
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
            // Catálogos consultorio
            "motivo_no_medicacion",
            "evento_catalogo",
            "derivacion_catalogo",
            "control_consultorio",
            // Tablas puente consultorio
            "control_consultorio_evento",
            "control_consultorio_derivacion",
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
        put("viviendas", VIVIENDAS_COLS);
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
        put("motivo_no_medicacion", List.of(
                "id","motivo","detalle","last_modified","sql_deleted"
        ));
        put("evento_catalogo", List.of(
                "id","nombre","activo","last_modified","sql_deleted"
        ));
        put("derivacion_catalogo", List.of(
                "id","nombre","activo","last_modified","sql_deleted"
        ));
        put("control_consultorio", List.of(
                "id","visita_id","created_by_user_id","motivo_no_medicacion_id","fecha","asistencia",
                "ta_sistolica","ta_diastolica",
                "peso","talla","imc",
                "circ_cintura","fumador",
                "confirm_hta","control_medicacion","derivacion","entrega_medicacion","eventos","conducta",
                "medicacion","observaciones","observaciones_derivacion","observaciones_eventos",
                "last_modified","sql_deleted"
        ));
        put("control_consultorio_evento", List.of(
                "id","control_consultorio_id","evento_id","detalle","last_modified","sql_deleted"
        ));
        put("control_consultorio_derivacion", List.of(
                "id","control_consultorio_id","derivacion_id","detalle","last_modified","sql_deleted"
        ));
        put("antecedentes", List.of(
                "id","persona_id","visita_id","created_by_user_id",
                "diabetes","dislipemia","enf_cardiovascular","enf_renal_cronica","hta_previa","tabaquismo",
                "tratamiento_enf_cardiovascular","tratamiento_enf_diabetes","tratamiento_enf_dislipemia",
                "tratamiento_enf_renal","tratamiento_hta_previa",
                "desc_trat_enf_cardiovascular","desc_trat_enf_dislipemia","desc_trat_enf_renal",
                "desc_trat_enf_diabetes","desc_trat_hta_previa",
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
        put("control_consultorio_evento", List.of("control_consultorio_id","evento_id"));
        put("control_consultorio_derivacion", List.of("control_consultorio_id","derivacion_id"));
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

    private void ensureSyncEventsTable() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS sync_events (
                  id VARCHAR(36) PRIMARY KEY,
                  user_id VARCHAR(36) NULL,
                  email VARCHAR(190) NULL,
                  device_id VARCHAR(120) NULL,
                  platform VARCHAR(40) NULL,
                  operation VARCHAR(40) NOT NULL,
                  result VARCHAR(20) NOT NULL,
                  message VARCHAR(240) NULL,
                  app_version VARCHAR(40) NULL,
                  created_at BIGINT NOT NULL,
                  INDEX idx_sync_events_created_at (created_at),
                  INDEX idx_sync_events_user_id (user_id),
                  INDEX idx_sync_events_result (result)
                )
                """);
    }

    private JwtUser jwtUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        Object principal = authentication.getPrincipal();
        return principal instanceof JwtUser jwtUser ? jwtUser : null;
    }

    private String shortText(Object value, int max) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        return text.length() <= max ? text : text.substring(0, max);
    }

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
    @Transactional
    public ResponseEntity<Map<String, Object>> push(@RequestBody Map<String, Object> partial) throws Exception {
        Object tablesObj = partial.get("tables");
        if (!(tablesObj instanceof List<?> rawTables)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido: falta 'tables'"));
        }

        // name -> rows(values[])
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
        List<Map<String, Object>> warnings = new ArrayList<>();

        // Para cascada targeteada: viviendas/personas que quedaron con sql_deleted=1 tras este push
        final Set<String> viviendasMarcadasBaja = new HashSet<>();
        final Set<String> personasMarcadasBaja = new HashSet<>();
        final Set<String> controlesConsultorioMarcadosBaja = new HashSet<>();

        for (String table : TABLE_ORDER) {
            if (!incoming.containsKey(table) || !tableExists(table)) continue;

            List<String> cols = MOBILE_COLS.getOrDefault(table, getTableColumns(table));
            boolean hasLm = cols.contains(LM);
            List<String> pkCols = pkFor(table);

            for (List<Object> rowArr : incoming.get(table)) {
                List<String> rowCols = cols;
                if ("viviendas".equals(table) && rowArr.size() == VIVIENDAS_LEGACY_COLS.size()) {
                    rowCols = VIVIENDAS_LEGACY_COLS;
                }

                if (rowArr.size() != rowCols.size()) {
                    skipped++;
                    Map<String, Object> warning = new LinkedHashMap<>();
                    warning.put("table", table);
                    warning.put("expected", cols.size());
                    warning.put("received", rowArr.size());
                    warning.put("message", "Fila salteada por cantidad de valores incompatible con MOBILE_COLS");
                    warnings.add(warning);
                    log.warn("Sync push: fila salteada en {} por cantidad de valores incompatible. Esperados={}, recibidos={}",
                            table, cols.size(), rowArr.size());
                    continue;
                }

                Map<String, Object> row = zipRow(rowCols, rowArr);
                try {

                // Normalizaciones
                for (String c : rowCols) {
                    Object v = row.get(c);
                    if (v instanceof Boolean b) row.put(c, b ? 1 : 0);
                    if (LM.equalsIgnoreCase(c)) row.put(c, toEpochSeconds(v));
                    if (isBooleanishColumn(c)) row.put(c, boolAsInt(v));
                }

                // PKs presentes
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

                // Si estamos procesando VIVIENDAS o PERSONAS, registrar las que quedan en baja lógica (=1)
                if ("viviendas".equals(table)) {
                    Object id = row.getOrDefault("id", null);
                    Object sd = row.getOrDefault("sql_deleted", 0);
                    if (id != null && Objects.equals(boolAsInt(sd), 1)) {
                        viviendasMarcadasBaja.add(String.valueOf(id));
                    }
                }

                if ("personas".equals(table)) {
                    Object id = row.getOrDefault("id", null);
                    Object sd = row.getOrDefault("sql_deleted", 0);
                    if (id != null && Objects.equals(boolAsInt(sd), 1)) {
                        personasMarcadasBaja.add(String.valueOf(id));
                    }
                }

                if ("control_consultorio".equals(table)) {
                    Object id = row.getOrDefault("id", null);
                    Object sd = row.getOrDefault("sql_deleted", 0);
                    if (id != null && Objects.equals(boolAsInt(sd), 1)) {
                        controlesConsultorioMarcadosBaja.add(String.valueOf(id));
                    }
                }
                } catch (Exception e) {
                    Object pkValue = row.get(pkCols.get(0));
                    throw new IllegalStateException(buildPushErrorMessage(table, pkValue, e), e);
                }
            }
        }

        if (!viviendasMarcadasBaja.isEmpty()) {
            personasMarcadasBaja.addAll(findPersonaIdsByViviendas(viviendasMarcadasBaja));
        }

        // Cascada de baja lógica SOLO para las personas marcadas en este push
        Map<String, Integer> cascaded = new LinkedHashMap<>(personasMarcadasBaja.isEmpty()
                ? Map.of("viviendas", 0, "personas", 0, "visitas", 0, "control_domicilio", 0, "control_consultorio", 0,
                "control_consultorio_evento", 0, "control_consultorio_derivacion", 0,
                "antecedentes", 0, "antecedente_has_medicacion_hta", 0, "lab_resultados", 0)
                : cascadeSoftDeleteForPersonas(personasMarcadasBaja, viviendasMarcadasBaja));

        if (!controlesConsultorioMarcadosBaja.isEmpty()) {
            Map<String, Integer> ccCascade = cascadeSoftDeleteForControlConsultorio(controlesConsultorioMarcadosBaja);
            ccCascade.forEach((k, v) -> cascaded.merge(k, v, Integer::sum));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("applied", applied);
        response.put("skipped", skipped);
        response.put("cascade_soft_deleted", cascaded);
        response.put("personas_borradas", personasMarcadasBaja.size());
        response.put("viviendas_borradas", viviendasMarcadasBaja.size());
        response.put("controles_consultorio_borrados", controlesConsultorioMarcadosBaja.size());
        if (!warnings.isEmpty()) response.put("warnings", warnings);
        return ResponseEntity.ok(response);
    }

    /* ===== Helpers específicos de este push ===== */

    private long nowSec() { return Instant.now().getEpochSecond(); }

    private String buildPushErrorMessage(String table, Object pkValue, Exception e) {
        Throwable root = rootCause(e);
        String detail = root == null ? e.getMessage() : root.getMessage();
        String column = inferSqlColumn(detail);
        StringBuilder message = new StringBuilder("Error sincronizando push");
        message.append(" tabla=").append(table);
        if (pkValue != null) message.append(" pk=").append(pkValue);
        if (column != null) message.append(" columna=").append(column);
        if (detail != null && !detail.isBlank()) {
            message.append(": ").append(detail);
        }
        return message.toString();
    }

    private Throwable rootCause(Throwable e) {
        Throwable current = e;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String inferSqlColumn(String message) {
        if (message == null) return null;
        String marker = "column '";
        int start = message.indexOf(marker);
        if (start < 0) return null;
        start += marker.length();
        int end = message.indexOf("'", start);
        if (end <= start) return null;
        return message.substring(start, end);
    }

    /**
     * Propaga sql_deleted = 1 desde PERSONAS a TODAS las tablas hijas/nietas,
     * limitando la cascada al conjunto recibido. Idempotente.
     */
    private Set<String> findPersonaIdsByViviendas(Set<String> viviendaIds) {
        if (viviendaIds == null || viviendaIds.isEmpty()) return Set.of();
        String placeholders = viviendaIds.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id FROM personas WHERE viviendas_id IN (" + placeholders + ")",
                viviendaIds.toArray()
        );
        return rows.stream()
                .map(r -> Objects.toString(r.get("id"), ""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private Map<String, Integer> cascadeSoftDeleteForPersonas(Set<String> personaIds, Set<String> viviendaIds) {
        long now = nowSec();
        Map<String, Integer> out = new LinkedHashMap<>();
        if (personaIds == null || personaIds.isEmpty()) return out;

        // Helpers para IN (?, ?, ?)
        String placeholders = personaIds.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>(personaIds);

        if (viviendaIds != null && !viviendaIds.isEmpty()) {
            String viviendaPlaceholders = viviendaIds.stream().map(x -> "?").collect(Collectors.joining(","));
            List<Object> argsViv = new ArrayList<>();
            argsViv.add(now);
            argsViv.addAll(viviendaIds);
            int viv = jdbc.update(
                    "UPDATE viviendas SET sql_deleted = 1, last_modified = ? " +
                            "WHERE sql_deleted = 0 AND id IN (" + viviendaPlaceholders + ")",
                    argsViv.toArray()
            );
            out.put("viviendas", viv);
        } else {
            out.put("viviendas", 0);
        }

        List<Object> argsP = new ArrayList<>();
        argsP.add(now);
        argsP.addAll(args);
        int p = jdbc.update(
                "UPDATE personas SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND id IN (" + placeholders + ")", argsP.toArray());
        out.put("personas", p);

        // 1) visitas de personas borradas
        List<Object> argsV = new ArrayList<>();
        argsV.add(now);
        argsV.addAll(args);
        int v = jdbc.update(
                "UPDATE visitas SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND persona_id IN (" + placeholders + ")", argsV.toArray());
        out.put("visitas", v);

        // 2) control_domicilio
        int cd = jdbc.update(
                "UPDATE control_domicilio SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND visita_id IN (SELECT id FROM visitas WHERE persona_id IN (" + placeholders + "))",
                argsV.toArray());
        out.put("control_domicilio", cd);

        // 3) control_consultorio
        int cc = jdbc.update(
                "UPDATE control_consultorio SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND visita_id IN (SELECT id FROM visitas WHERE persona_id IN (" + placeholders + "))",
                argsV.toArray());
        out.put("control_consultorio", cc);

        // 3b) control_consultorio_evento
        int cce = jdbc.update(
                "UPDATE control_consultorio_evento SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND control_consultorio_id IN (" +
                        "SELECT cc.id FROM control_consultorio cc JOIN visitas v ON v.id = cc.visita_id " +
                        "WHERE v.persona_id IN (" + placeholders + "))",
                argsV.toArray());
        out.put("control_consultorio_evento", cce);

        // 3c) control_consultorio_derivacion
        int ccd = jdbc.update(
                "UPDATE control_consultorio_derivacion SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND control_consultorio_id IN (" +
                        "SELECT cc.id FROM control_consultorio cc JOIN visitas v ON v.id = cc.visita_id " +
                        "WHERE v.persona_id IN (" + placeholders + "))",
                argsV.toArray());
        out.put("control_consultorio_derivacion", ccd);

        // 4) antecedentes
        List<Object> argsA = new ArrayList<>();
        argsA.add(now);
        argsA.addAll(args);
        int ant = jdbc.update(
                "UPDATE antecedentes SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND persona_id IN (" + placeholders + ")", argsA.toArray());
        out.put("antecedentes", ant);

        // 5) puente antecedente_has_medicacion_hta
        int am = jdbc.update(
                "UPDATE antecedente_has_medicacion_hta SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND antecedente_id IN (SELECT id FROM antecedentes WHERE persona_id IN (" + placeholders + "))",
                argsV.toArray());
        out.put("antecedente_has_medicacion_hta", am);

        // 6) lab_resultados
        List<Object> argsL = new ArrayList<>();
        argsL.add(now);
        argsL.addAll(args);
        int lr = jdbc.update(
                "UPDATE lab_resultados SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND persona_id IN (" + placeholders + ")", argsL.toArray());
        out.put("lab_resultados", lr);

        return out;
    }

    private Map<String, Integer> cascadeSoftDeleteForControlConsultorio(Set<String> controlIds) {
        long now = nowSec();
        Map<String, Integer> out = new LinkedHashMap<>();
        if (controlIds == null || controlIds.isEmpty()) return out;

        String placeholders = controlIds.stream().map(x -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>();
        args.add(now);
        args.addAll(controlIds);

        int motivo = jdbc.update(
                "UPDATE motivo_no_medicacion SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND id IN (" +
                        "SELECT motivo_no_medicacion_id FROM control_consultorio " +
                        "WHERE id IN (" + placeholders + ") AND motivo_no_medicacion_id IS NOT NULL)",
                args.toArray());
        out.put("motivo_no_medicacion", motivo);

        int eventos = jdbc.update(
                "UPDATE control_consultorio_evento SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND control_consultorio_id IN (" + placeholders + ")",
                args.toArray());
        out.put("control_consultorio_evento", eventos);

        int derivaciones = jdbc.update(
                "UPDATE control_consultorio_derivacion SET sql_deleted = 1, last_modified = ? " +
                        "WHERE sql_deleted = 0 AND control_consultorio_id IN (" + placeholders + ")",
                args.toArray());
        out.put("control_consultorio_derivacion", derivaciones);

        return out;
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
                        col("gps_accuracy","REAL"),
                        col("gps_captured_at","INTEGER"),
                        col("ubicacion_fuente","TEXT"),
                        col("confianza_dato","TEXT"),
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
                selectValuesSec("viviendas", VIVIENDAS_COLS)
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
                selectValuesSecSql("""
      SELECT v.id, v.persona_id, v.responsable_id, v.created_by_user_id, v.tipo, v.fecha,
             v.ubicacion_gps, v.observaciones, v.last_modified, v.sql_deleted
      FROM visitas v
      JOIN personas p ON p.id = v.persona_id AND p.sql_deleted = 0
      WHERE v.sql_deleted IN (0,1)
    """, List.of(
                        "id","persona_id","responsable_id","created_by_user_id","tipo","fecha",
                        "ubicacion_gps","observaciones","last_modified","sql_deleted"
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
                selectValuesSecSql("""
      SELECT cd.id, cd.visita_id, cd.created_by_user_id, cd.ta_sistolica, cd.ta_diastolica,
             cd.accede_programa, cd.acepta_laboratotio, cd.observaciones, cd.last_modified, cd.sql_deleted
      FROM control_domicilio cd
      JOIN visitas v   ON v.id = cd.visita_id    AND v.sql_deleted IN (0,1)
      JOIN personas p  ON p.id = v.persona_id    AND p.sql_deleted = 0
      WHERE cd.sql_deleted IN (0,1)
    """, List.of(
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
                        col("motivo_no_medicacion_id","TEXT"),
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
                        ,
                        fk("motivo_no_medicacion_id","REFERENCES motivo_no_medicacion(id) ON DELETE SET NULL")
                ),
                List.of(
                        idx("idx_cc_lm","last_modified"),
                        idx("idx_cc_sd","sql_deleted"),
                        idxUnique("idx_cc_visita","visita_id")
                ),
                null,
                selectValuesSecSql("""
      SELECT cc.id, cc.visita_id, cc.created_by_user_id, cc.fecha, cc.asistencia,
             cc.motivo_no_medicacion_id,
             cc.ta_sistolica, cc.ta_diastolica, cc.peso, cc.talla, cc.imc,
             cc.circ_cintura, cc.fumador, cc.confirm_hta, cc.control_medicacion,
             cc.derivacion, cc.entrega_medicacion, cc.eventos, cc.conducta,
             cc.medicacion, cc.observaciones, cc.observaciones_derivacion, cc.observaciones_eventos,
             cc.last_modified, cc.sql_deleted
      FROM control_consultorio cc
      JOIN visitas v   ON v.id = cc.visita_id    AND v.sql_deleted IN (0,1)
      JOIN personas p  ON p.id = v.persona_id    AND p.sql_deleted = 0
      WHERE cc.sql_deleted IN (0,1)
    """, List.of(
                        "id","visita_id","created_by_user_id","motivo_no_medicacion_id","fecha","asistencia",
                        "ta_sistolica","ta_diastolica","peso","talla","imc",
                        "circ_cintura","fumador","confirm_hta","control_medicacion","derivacion",
                        "entrega_medicacion","eventos","conducta",
                        "medicacion","observaciones","observaciones_derivacion","observaciones_eventos",
                        "last_modified","sql_deleted"
                ))
        ));

        // ---------- motivo_no_medicacion ----------
        tables.add(tableWithSchemaIdxValues(
                "motivo_no_medicacion",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("motivo","TEXT NOT NULL"),
                        col("detalle","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(
                        idx("idx_mnm_lm","last_modified"),
                        idx("idx_mnm_sd","sql_deleted")
                ),
                null,
                selectValuesSec("motivo_no_medicacion", List.of(
                        "id","motivo","detalle","last_modified","sql_deleted"
                ))
        ));

        // ---------- evento_catalogo ----------
        tables.add(tableWithSchemaIdxValues(
                "evento_catalogo",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("nombre","TEXT NOT NULL"),
                        col("activo", bool1("activo")),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(
                        idxUnique("uk_evento_nombre","nombre"),
                        idx("idx_evento_lm","last_modified"),
                        idx("idx_evento_sd","sql_deleted")
                ),
                null,
                selectValuesSec("evento_catalogo", List.of(
                        "id","nombre","activo","last_modified","sql_deleted"
                ))
        ));

        // ---------- derivacion_catalogo ----------
        tables.add(tableWithSchemaIdxValues(
                "derivacion_catalogo",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("nombre","TEXT NOT NULL"),
                        col("activo", bool1("activo")),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted"))
                ),
                List.of(
                        idxUnique("uk_derivacion_nombre","nombre"),
                        idx("idx_derivacion_lm","last_modified"),
                        idx("idx_derivacion_sd","sql_deleted")
                ),
                null,
                selectValuesSec("derivacion_catalogo", List.of(
                        "id","nombre","activo","last_modified","sql_deleted"
                ))
        ));

        // ---------- control_consultorio_evento ----------
        tables.add(tableWithSchemaIdxValues(
                "control_consultorio_evento",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("control_consultorio_id","TEXT NOT NULL"),
                        col("evento_id","TEXT NOT NULL"),
                        col("detalle","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("control_consultorio_id","REFERENCES control_consultorio(id) ON DELETE CASCADE"),
                        fk("evento_id","REFERENCES evento_catalogo(id) ON DELETE RESTRICT")
                ),
                List.of(
                        idxUnique("uk_cce_cc_evento","control_consultorio_id,evento_id"),
                        idx("idx_cce_cc","control_consultorio_id"),
                        idx("idx_cce_evento","evento_id"),
                        idx("idx_cce_lm","last_modified"),
                        idx("idx_cce_sd","sql_deleted")
                ),
                null,
                selectValuesSecSql("""
      SELECT cce.id, cce.control_consultorio_id, cce.evento_id, cce.detalle,
             cce.last_modified, cce.sql_deleted
      FROM control_consultorio_evento cce
      JOIN control_consultorio cc ON cc.id = cce.control_consultorio_id AND cc.sql_deleted IN (0,1)
      JOIN visitas v ON v.id = cc.visita_id AND v.sql_deleted IN (0,1)
      JOIN personas p ON p.id = v.persona_id AND p.sql_deleted = 0
      JOIN evento_catalogo ev ON ev.id = cce.evento_id AND ev.sql_deleted IN (0,1)
      WHERE cce.sql_deleted IN (0,1)
    """, List.of(
                        "id","control_consultorio_id","evento_id","detalle",
                        "last_modified","sql_deleted"
                ))
        ));

        // ---------- control_consultorio_derivacion ----------
        tables.add(tableWithSchemaIdxValues(
                "control_consultorio_derivacion",
                List.of(
                        col("id","TEXT PRIMARY KEY NOT NULL"),
                        col("control_consultorio_id","TEXT NOT NULL"),
                        col("derivacion_id","TEXT NOT NULL"),
                        col("detalle","TEXT"),
                        col("last_modified","INTEGER NOT NULL"),
                        col("sql_deleted", bool0("sql_deleted")),
                        fk("control_consultorio_id","REFERENCES control_consultorio(id) ON DELETE CASCADE"),
                        fk("derivacion_id","REFERENCES derivacion_catalogo(id) ON DELETE RESTRICT")
                ),
                List.of(
                        idxUnique("uk_ccd_cc_deriv","control_consultorio_id,derivacion_id"),
                        idx("idx_ccd_cc","control_consultorio_id"),
                        idx("idx_ccd_deriv","derivacion_id"),
                        idx("idx_ccd_lm","last_modified"),
                        idx("idx_ccd_sd","sql_deleted")
                ),
                null,
                selectValuesSecSql("""
      SELECT ccd.id, ccd.control_consultorio_id, ccd.derivacion_id, ccd.detalle,
             ccd.last_modified, ccd.sql_deleted
      FROM control_consultorio_derivacion ccd
      JOIN control_consultorio cc ON cc.id = ccd.control_consultorio_id AND cc.sql_deleted IN (0,1)
      JOIN visitas v ON v.id = cc.visita_id AND v.sql_deleted IN (0,1)
      JOIN personas p ON p.id = v.persona_id AND p.sql_deleted = 0
      JOIN derivacion_catalogo dc ON dc.id = ccd.derivacion_id AND dc.sql_deleted IN (0,1)
      WHERE ccd.sql_deleted IN (0,1)
    """, List.of(
                        "id","control_consultorio_id","derivacion_id","detalle",
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
                selectValuesSecSql("""
      SELECT a.id, a.persona_id, a.visita_id, a.created_by_user_id,
             a.diabetes, a.dislipemia, a.enf_cardiovascular, a.enf_renal_cronica, a.hta_previa, a.tabaquismo,
             a.tratamiento_enf_cardiovascular, a.tratamiento_enf_diabetes, a.tratamiento_enf_dislipemia,
             a.tratamiento_enf_renal, a.tratamiento_hta_previa,
             a.desc_trat_enf_cardiovascular, a.desc_trat_enf_dislipemia, a.desc_trat_enf_renal,
             a.desc_trat_enf_diabetes, a.desc_trat_hta_previa, a.otros,
             a.last_modified, a.sql_deleted
      FROM antecedentes a
      JOIN personas p ON p.id = a.persona_id AND p.sql_deleted = 0
      WHERE a.sql_deleted IN (0,1)
    """, List.of(
                        "id","persona_id","visita_id","created_by_user_id",
                        "diabetes","dislipemia","enf_cardiovascular","enf_renal_cronica","hta_previa","tabaquismo",
                        "tratamiento_enf_cardiovascular","tratamiento_enf_diabetes","tratamiento_enf_dislipemia",
                        "tratamiento_enf_renal","tratamiento_hta_previa",
                        "desc_trat_enf_cardiovascular","desc_trat_enf_dislipemia","desc_trat_enf_renal",
                        "desc_trat_enf_diabetes","desc_trat_hta_previa",
                        "otros","last_modified","sql_deleted"
                ))
        ));


        // ---------- antecedente_has_medicacion_hta ----------
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
                List.of(
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
                selectValuesSecSql("""
      SELECT am.antecedente_id, am.medicacion_id, am.last_modified, am.sql_deleted
      FROM antecedente_has_medicacion_hta am
      JOIN antecedentes a ON a.id = am.antecedente_id AND a.sql_deleted IN (0,1)
      JOIN personas    p ON p.id = a.persona_id    AND p.sql_deleted = 0
      JOIN medicacion_hta m ON m.id = am.medicacion_id AND m.sql_deleted = 0
      WHERE am.sql_deleted IN (0,1)
    """, List.of(
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
                selectValuesSecSql("""
      SELECT lr.id, lr.persona_id, lr.visita_id, lr.tipo_id, lr.created_by_user_id, lr.fecha_realizado,
             lr.valor_num, lr.valor_texto, lr.unidad, lr.laboratorio, lr.observaciones,
             lr.last_modified, lr.sql_deleted
      FROM lab_resultados lr
      JOIN personas p ON p.id = lr.persona_id AND p.sql_deleted = 0
      JOIN lab_tipos t ON t.id = lr.tipo_id AND t.sql_deleted IN (0,1)
      LEFT JOIN visitas v ON v.id = lr.visita_id
      WHERE lr.sql_deleted IN (0,1)
        AND (lr.visita_id IS NULL OR v.sql_deleted IN (0,1))
    """, List.of(
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

    private void ensureViviendasQualityColumns() throws SQLException {
        if (!tableExists("viviendas")) return;
        addColumnIfMissing("viviendas", "gps_accuracy", "DOUBLE NULL");
        addColumnIfMissing("viviendas", "gps_captured_at", "BIGINT NULL");
        addColumnIfMissing("viviendas", "ubicacion_fuente", "VARCHAR(20) NULL");
        addColumnIfMissing("viviendas", "confianza_dato", "VARCHAR(20) NULL");
    }

    private void addColumnIfMissing(String table, String column, String definition) throws SQLException {
        if (columnExists(table, column)) return;
        jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private boolean columnExists(String table, String column) throws SQLException {
        try (Connection c = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getColumns(c.getCatalog(), null, table, column)) {
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
                s.equals("activo") ||
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
    private List<List<Object>> selectValuesSecSql(String sql, List<String> cols, Object... params) {
        List<Map<String, Object>> rows = (params == null || params.length == 0)
                ? jdbc.queryForList(sql)
                : jdbc.queryForList(sql, params);
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
