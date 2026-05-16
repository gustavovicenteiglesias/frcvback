package ar.edu.unsada.frcv.controller;

import jakarta.transaction.Transactional;
import ar.edu.unsada.frcv.security.JwtUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> adminPing() {
        return Map.of("ok","admin-only");
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> summary() {
        Map<String, Object> out = new LinkedHashMap<>();

        out.put("usuarios", Map.of(
                "activos", count("""
                        SELECT COUNT(*)
                        FROM users
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND COALESCE(activo, 1) = 1
                        """),
                "sinRoles", count("""
                        SELECT COUNT(*)
                        FROM users u
                        WHERE COALESCE(u.sql_deleted, 0) = 0
                          AND NOT EXISTS (
                              SELECT 1
                              FROM user_roles ur
                              WHERE ur.user_id = u.id
                                AND COALESCE(ur.sql_deleted, 0) = 0
                          )
                        """)
        ));

        out.put("roles", queryList("""
                SELECT r.code AS codigo, COUNT(ur.user_id) AS cantidad
                FROM roles r
                LEFT JOIN user_roles ur
                  ON ur.role_id = r.id
                 AND COALESCE(ur.sql_deleted, 0) = 0
                WHERE COALESCE(r.sql_deleted, 0) = 0
                GROUP BY r.code
                ORDER BY r.code
                """));

        out.put("responsables", Map.of(
                "activos", count("""
                        SELECT COUNT(*)
                        FROM responsables
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND COALESCE(activo, 1) = 1
                        """),
                "sinUsuario", count("""
                        SELECT COUNT(*)
                        FROM responsables
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND user_id IS NULL
                        """),
                "porDefecto", count("""
                        SELECT COUNT(*)
                        FROM responsables
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND LOWER(nombre) LIKE '%defecto%'
                        """)
        ));

        out.put("calidadDato", Map.ofEntries(
                Map.entry("visitasDomicilio", count("""
                        SELECT COUNT(*)
                        FROM visitas
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND tipo = 'domicilio'
                        """)),
                Map.entry("visitasDomicilioSinGps", count("""
                        SELECT COUNT(*)
                        FROM visitas
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND tipo = 'domicilio'
                          AND (ubicacion_gps IS NULL OR TRIM(ubicacion_gps) = '')
                        """)),
                Map.entry("visitasMismaFechaGpsVivienda", count("""
                        SELECT COUNT(*)
                        FROM (
                            SELECT DATE(v.fecha), viv.latitud, viv.longitud
                            FROM visitas v
                            JOIN control_domicilio cd ON cd.visita_id = v.id AND COALESCE(cd.sql_deleted, 0) = 0
                            JOIN personas p ON p.id = v.persona_id AND COALESCE(p.sql_deleted, 0) = 0
                            JOIN viviendas viv ON viv.id = p.viviendas_id AND COALESCE(viv.sql_deleted, 0) = 0
                            WHERE COALESCE(v.sql_deleted, 0) = 0
                              AND v.tipo = 'domicilio'
                              AND viv.latitud IS NOT NULL AND TRIM(viv.latitud) <> ''
                              AND viv.longitud IS NOT NULL AND TRIM(viv.longitud) <> ''
                            GROUP BY DATE(v.fecha), viv.latitud, viv.longitud
                            HAVING COUNT(DISTINCT v.id) > 1
                        ) repetidos
                        """)),
                Map.entry("personasSinDni", count("""
                        SELECT COUNT(*)
                        FROM personas
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND (dni IS NULL OR TRIM(dni) = '')
                        """)),
                Map.entry("viviendasSinGps", count("""
                        SELECT COUNT(*)
                        FROM viviendas
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND (latitud IS NULL OR TRIM(latitud) = ''
                           OR longitud IS NULL OR TRIM(longitud) = '')
                        """)),
                Map.entry("controlesDomicilioSinTa", count("""
                        SELECT COUNT(*)
                        FROM control_domicilio
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND ta_sistolica IS NULL
                          AND ta_diastolica IS NULL
                        """)),
                Map.entry("controlesConsultorioSinTa", count("""
                        SELECT COUNT(*)
                        FROM control_consultorio
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND ta_sistolica IS NULL
                          AND ta_diastolica IS NULL
                        """)),
                Map.entry("laboratoriosSinValor", count("""
                        SELECT COUNT(*)
                        FROM lab_resultados
                        WHERE COALESCE(sql_deleted, 0) = 0
                          AND valor_num IS NULL
                          AND (valor_texto IS NULL OR TRIM(valor_texto) = '')
                        """))
        ));

        out.put("tablas", tableSummary());
        return out;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> users() {
        List<Map<String, Object>> users = queryList("""
                SELECT u.id, u.email, u.nombre, u.activo, u.ultimo_login, u.last_modified,
                       r.nombre AS responsable_nombre,
                       r.id AS responsable_id
                FROM users u
                LEFT JOIN responsables r
                  ON r.user_id = u.id
                 AND COALESCE(r.sql_deleted, 0) = 0
                WHERE COALESCE(u.sql_deleted, 0) = 0
                ORDER BY u.email
                """);

        for (Map<String, Object> user : users) {
            user.put("roles", queryList("""
                    SELECT ro.code
                    FROM user_roles ur
                    JOIN roles ro ON ro.id = ur.role_id
                    WHERE ur.user_id = ?
                      AND COALESCE(ur.sql_deleted, 0) = 0
                      AND COALESCE(ro.sql_deleted, 0) = 0
                    ORDER BY ro.code
                    """, user.get("id")).stream().map(row -> row.get("code")).toList());
        }

        return users;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> roles() {
        return queryList("""
                SELECT id, code, descripcion
                FROM roles
                WHERE COALESCE(sql_deleted, 0) = 0
                ORDER BY code
                """);
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> audit(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        String fromDate = cleanDate(from);
        String toDate = cleanDate(to);
        if ((from != null && fromDate == null && !from.isBlank()) || (to != null && toDate == null && !to.isBlank())) {
            return ResponseEntity.badRequest().body(List.of(Map.of(
                    "problema", "Rango de fechas inválido. Usar formato YYYY-MM-DD"
            )));
        }

        String visitasDate = dateCondition("v.fecha", fromDate, toDate);
        String consultorioDate = dateCondition("COALESCE(cc.fecha, v.fecha)", fromDate, toDate);
        String laboratorioDate = dateCondition("lr.fecha_realizado", fromDate, toDate);
        String antecedenteDate = dateCondition("COALESCE(v.fecha, FROM_UNIXTIME(a.last_modified))", fromDate, toDate);
        String deletedDate = dateCondition("FROM_UNIXTIME(last_modified)", fromDate, toDate);

        String sql = """
                SELECT
                  usuario_id,
                  usuario,
                  responsable_id,
                  responsable,
                  CASE
                    WHEN usuario_id IS NULL AND responsable_id IS NULL THEN 'sin_usuario_y_responsable'
                    WHEN usuario_id IS NULL THEN 'sin_usuario'
                    WHEN responsable_id IS NULL THEN 'sin_responsable'
                    WHEN LOWER(COALESCE(responsable, '')) LIKE '%%defecto%%' THEN 'responsable_generico'
                    ELSE 'trazable'
                  END AS trazabilidad,
                  SUM(visitas) AS visitas,
                  SUM(controles_domicilio) AS controles_domicilio,
                  SUM(controles_consultorio) AS controles_consultorio,
                  SUM(laboratorios) AS laboratorios,
                  SUM(antecedentes) AS antecedentes,
                  SUM(borrados_logicos) AS borrados_logicos,
                  SUM(visitas_domicilio_sin_gps) AS visitas_domicilio_sin_gps,
                  SUM(coordenadas_repetidas_mismo_dia) AS coordenadas_repetidas_mismo_dia,
                  SUM(controles_sin_ta) AS controles_sin_ta,
                  CASE WHEN LOWER(COALESCE(responsable, '')) LIKE '%%defecto%%' THEN 1 ELSE 0 END AS responsable_por_defecto,
                  SUM(visitas_domicilio_sin_gps) + SUM(coordenadas_repetidas_mismo_dia) + SUM(controles_sin_ta)
                    + CASE WHEN LOWER(COALESCE(responsable, '')) LIKE '%%defecto%%' THEN 1 ELSE 0 END AS problemas_calidad
                FROM (
                  SELECT
                    v.created_by_user_id AS usuario_id,
                    u.email AS usuario,
                    v.responsable_id,
                    r.nombre AS responsable,
                    COUNT(*) AS visitas,
                    0 AS controles_domicilio,
                    0 AS controles_consultorio,
                    0 AS laboratorios,
                    0 AS antecedentes,
                    0 AS borrados_logicos,
                    SUM(CASE WHEN v.tipo = 'domicilio'
                              AND (v.ubicacion_gps IS NULL OR TRIM(v.ubicacion_gps) = '')
                             THEN 1 ELSE 0 END) AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    0 AS controles_sin_ta
                  FROM visitas v
                  LEFT JOIN users u ON u.id = v.created_by_user_id
                  LEFT JOIN responsables r ON r.id = v.responsable_id
                  WHERE COALESCE(v.sql_deleted, 0) = 0
                  %s
                  GROUP BY v.created_by_user_id, u.email, v.responsable_id, r.nombre

                  UNION ALL

                  SELECT
                    COALESCE(cd.created_by_user_id, v.created_by_user_id) AS usuario_id,
                    u.email AS usuario,
                    v.responsable_id,
                    r.nombre AS responsable,
                    0 AS visitas,
                    COUNT(*) AS controles_domicilio,
                    0 AS controles_consultorio,
                    0 AS laboratorios,
                    0 AS antecedentes,
                    0 AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    SUM(CASE WHEN cd.ta_sistolica IS NULL AND cd.ta_diastolica IS NULL THEN 1 ELSE 0 END) AS controles_sin_ta
                  FROM control_domicilio cd
                  LEFT JOIN visitas v ON v.id = cd.visita_id
                  LEFT JOIN users u ON u.id = COALESCE(cd.created_by_user_id, v.created_by_user_id)
                  LEFT JOIN responsables r ON r.id = v.responsable_id
                  WHERE COALESCE(cd.sql_deleted, 0) = 0
                  %s
                  GROUP BY COALESCE(cd.created_by_user_id, v.created_by_user_id), u.email, v.responsable_id, r.nombre

                  UNION ALL

                  SELECT
                    COALESCE(cc.created_by_user_id, v.created_by_user_id) AS usuario_id,
                    u.email AS usuario,
                    v.responsable_id,
                    r.nombre AS responsable,
                    0 AS visitas,
                    0 AS controles_domicilio,
                    COUNT(*) AS controles_consultorio,
                    0 AS laboratorios,
                    0 AS antecedentes,
                    0 AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    SUM(CASE WHEN cc.ta_sistolica IS NULL AND cc.ta_diastolica IS NULL THEN 1 ELSE 0 END) AS controles_sin_ta
                  FROM control_consultorio cc
                  LEFT JOIN visitas v ON v.id = cc.visita_id
                  LEFT JOIN users u ON u.id = COALESCE(cc.created_by_user_id, v.created_by_user_id)
                  LEFT JOIN responsables r ON r.id = v.responsable_id
                  WHERE COALESCE(cc.sql_deleted, 0) = 0
                  %s
                  GROUP BY COALESCE(cc.created_by_user_id, v.created_by_user_id), u.email, v.responsable_id, r.nombre

                  UNION ALL

                  SELECT
                    lr.created_by_user_id AS usuario_id,
                    u.email AS usuario,
                    v.responsable_id,
                    r.nombre AS responsable,
                    0 AS visitas,
                    0 AS controles_domicilio,
                    0 AS controles_consultorio,
                    COUNT(*) AS laboratorios,
                    0 AS antecedentes,
                    0 AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    0 AS controles_sin_ta
                  FROM lab_resultados lr
                  LEFT JOIN visitas v ON v.id = lr.visita_id
                  LEFT JOIN users u ON u.id = lr.created_by_user_id
                  LEFT JOIN responsables r ON r.id = v.responsable_id
                  WHERE COALESCE(lr.sql_deleted, 0) = 0
                  %s
                  GROUP BY lr.created_by_user_id, u.email, v.responsable_id, r.nombre

                  UNION ALL

                  SELECT
                    a.created_by_user_id AS usuario_id,
                    u.email AS usuario,
                    v.responsable_id,
                    r.nombre AS responsable,
                    0 AS visitas,
                    0 AS controles_domicilio,
                    0 AS controles_consultorio,
                    0 AS laboratorios,
                    COUNT(*) AS antecedentes,
                    0 AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    0 AS controles_sin_ta
                  FROM antecedentes a
                  LEFT JOIN visitas v ON v.id = a.visita_id
                  LEFT JOIN users u ON u.id = a.created_by_user_id
                  LEFT JOIN responsables r ON r.id = v.responsable_id
                  WHERE COALESCE(a.sql_deleted, 0) = 0
                  %s
                  GROUP BY a.created_by_user_id, u.email, v.responsable_id, r.nombre

                  UNION ALL

                  SELECT
                    NULL AS usuario_id,
                    NULL AS usuario,
                    NULL AS responsable_id,
                    NULL AS responsable,
                    0 AS visitas,
                    0 AS controles_domicilio,
                    0 AS controles_consultorio,
                    0 AS laboratorios,
                    0 AS antecedentes,
                    SUM(cantidad) AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    0 AS coordenadas_repetidas_mismo_dia,
                    0 AS controles_sin_ta
                  FROM (
                    SELECT COUNT(*) AS cantidad FROM visitas WHERE COALESCE(sql_deleted, 0) = 1 %s
                    UNION ALL SELECT COUNT(*) AS cantidad FROM control_domicilio WHERE COALESCE(sql_deleted, 0) = 1 %s
                    UNION ALL SELECT COUNT(*) AS cantidad FROM control_consultorio WHERE COALESCE(sql_deleted, 0) = 1 %s
                    UNION ALL SELECT COUNT(*) AS cantidad FROM lab_resultados WHERE COALESCE(sql_deleted, 0) = 1 %s
                    UNION ALL SELECT COUNT(*) AS cantidad FROM antecedentes WHERE COALESCE(sql_deleted, 0) = 1 %s
                  ) borrados

                  UNION ALL

                  SELECT
                    rep.usuario_id,
                    rep.usuario,
                    rep.responsable_id,
                    rep.responsable,
                    0 AS visitas,
                    0 AS controles_domicilio,
                    0 AS controles_consultorio,
                    0 AS laboratorios,
                    0 AS antecedentes,
                    0 AS borrados_logicos,
                    0 AS visitas_domicilio_sin_gps,
                    COUNT(*) AS coordenadas_repetidas_mismo_dia,
                    0 AS controles_sin_ta
                  FROM (
                    SELECT
                      COALESCE(cd.created_by_user_id, v.created_by_user_id) AS usuario_id,
                      u.email AS usuario,
                      v.responsable_id,
                      r.nombre AS responsable,
                      DATE(v.fecha) AS fecha,
                      viv.latitud,
                      viv.longitud
                    FROM visitas v
                    JOIN control_domicilio cd ON cd.visita_id = v.id AND COALESCE(cd.sql_deleted, 0) = 0
                    JOIN personas p ON p.id = v.persona_id AND COALESCE(p.sql_deleted, 0) = 0
                    JOIN viviendas viv ON viv.id = p.viviendas_id AND COALESCE(viv.sql_deleted, 0) = 0
                    LEFT JOIN users u ON u.id = COALESCE(cd.created_by_user_id, v.created_by_user_id)
                    LEFT JOIN responsables r ON r.id = v.responsable_id
                    WHERE COALESCE(v.sql_deleted, 0) = 0
                      AND v.tipo = 'domicilio'
                      AND viv.latitud IS NOT NULL AND TRIM(viv.latitud) <> ''
                      AND viv.longitud IS NOT NULL AND TRIM(viv.longitud) <> ''
                    %s
                    GROUP BY COALESCE(cd.created_by_user_id, v.created_by_user_id), u.email, v.responsable_id, r.nombre,
                             DATE(v.fecha), viv.latitud, viv.longitud
                    HAVING COUNT(DISTINCT v.id) > 1
                  ) rep
                  GROUP BY rep.usuario_id, rep.usuario, rep.responsable_id, rep.responsable
                ) actividad
                GROUP BY usuario_id, usuario, responsable_id, responsable
                HAVING SUM(visitas) + SUM(controles_domicilio) + SUM(controles_consultorio)
                     + SUM(laboratorios) + SUM(antecedentes) + SUM(borrados_logicos)
                     + SUM(visitas_domicilio_sin_gps) + SUM(coordenadas_repetidas_mismo_dia)
                     + SUM(controles_sin_ta) > 0
                ORDER BY problemas_calidad DESC,
                         SUM(visitas) + SUM(controles_domicilio) + SUM(controles_consultorio)
                         + SUM(laboratorios) + SUM(antecedentes) DESC
                LIMIT 500
                """.formatted(
                visitasDate,
                visitasDate,
                consultorioDate,
                laboratorioDate,
                antecedenteDate,
                deletedDate,
                deletedDate,
                deletedDate,
                deletedDate,
                deletedDate,
                visitasDate
        );

        return ResponseEntity.ok(queryList(sql));
    }

    @GetMapping("/sync-status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> syncStatus() {
        ensureSyncEventsTable();
        Map<String, Object> out = new LinkedHashMap<>();
        long sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60).getEpochSecond();

        out.put("ultimosEventos", queryList("""
                SELECT id, user_id, email, device_id, platform, operation, result, message, app_version, created_at
                FROM sync_events
                ORDER BY created_at DESC
                LIMIT 80
                """));

        out.put("ultimosPorUsuario", queryList("""
                SELECT
                  COALESCE(se.email, u.email, 'sin usuario') AS usuario,
                  COALESCE(se.user_id, u.id) AS user_id,
                  se.device_id,
                  se.platform,
                  se.operation,
                  se.result,
                  se.message,
                  se.created_at
                FROM (
                  SELECT COALESCE(user_id, CONCAT('device:', COALESCE(device_id, 'sin-device'))) AS clave,
                         MAX(created_at) AS max_created_at
                  FROM sync_events
                  GROUP BY COALESCE(user_id, CONCAT('device:', COALESCE(device_id, 'sin-device')))
                ) last_event
                JOIN sync_events se
                  ON COALESCE(se.user_id, CONCAT('device:', COALESCE(se.device_id, 'sin-device'))) = last_event.clave
                 AND se.created_at = last_event.max_created_at
                LEFT JOIN users u ON u.id = se.user_id
                ORDER BY se.created_at DESC
                LIMIT 80
                """));

        out.put("erroresRecientes", queryList("""
                SELECT id, user_id, email, device_id, platform, operation, result, message, app_version, created_at
                FROM sync_events
                WHERE result = 'ERROR'
                ORDER BY created_at DESC
                LIMIT 40
                """));

        out.put("usuariosSinSyncReciente", queryList("""
                SELECT u.id AS user_id, u.email, u.nombre, MAX(se.created_at) AS ultimo_sync_ok
                FROM users u
                LEFT JOIN sync_events se
                  ON se.user_id = u.id
                 AND se.result = 'OK'
                WHERE COALESCE(u.sql_deleted, 0) = 0
                  AND COALESCE(u.activo, 1) = 1
                GROUP BY u.id, u.email, u.nombre
                HAVING ultimo_sync_ok IS NULL OR ultimo_sync_ok < ?
                ORDER BY ultimo_sync_ok ASC
                LIMIT 80
                """, sevenDaysAgo));

        return out;
    }

    @GetMapping("/catalogs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> catalogs() {
        List<Map<String, Object>> out = new ArrayList<>();

        out.add(catalog("barrios", "Barrios", """
                SELECT id, nombre, NULL AS codigo, NULL AS detalle, NULL AS activo,
                       last_modified, sql_deleted
                FROM barrios
                ORDER BY COALESCE(sql_deleted, 0), nombre
                """));
        out.add(catalog("caps", "CAPS", """
                SELECT id, nombre, NULL AS codigo, NULL AS detalle, NULL AS activo,
                       last_modified, sql_deleted
                FROM caps
                ORDER BY COALESCE(sql_deleted, 0), nombre
                """));
        out.add(catalog("lab_tipos", "Tipos de laboratorio", """
                SELECT id, nombre, codigo,
                       CONCAT('Unidad: ', COALESCE(unidad_default, '-'),
                              ' | Ref.: ', COALESCE(CAST(ref_min AS CHAR), '-'),
                              ' - ', COALESCE(CAST(ref_max AS CHAR), '-')) AS detalle,
                       NULL AS activo, last_modified, sql_deleted
                FROM lab_tipos
                ORDER BY COALESCE(sql_deleted, 0), nombre
                """));
        out.add(catalog("medicacion_hta", "Medicación HTA", """
                SELECT id, nombre, NULL AS codigo, NULL AS detalle, NULL AS activo,
                       last_modified, sql_deleted
                FROM medicacion_hta
                ORDER BY COALESCE(sql_deleted, 0), nombre
                """));
        out.add(catalog("evento_catalogo", "Eventos de consultorio", """
                SELECT id, nombre, NULL AS codigo, NULL AS detalle, activo,
                       last_modified, sql_deleted
                FROM evento_catalogo
                ORDER BY COALESCE(sql_deleted, 0), COALESCE(activo, 1) DESC, nombre
                """));
        out.add(catalog("derivacion_catalogo", "Derivaciones de consultorio", """
                SELECT id, nombre, NULL AS codigo, NULL AS detalle, activo,
                       last_modified, sql_deleted
                FROM derivacion_catalogo
                ORDER BY COALESCE(sql_deleted, 0), COALESCE(activo, 1) DESC, nombre
                """));
        return out;
    }

    @PutMapping("/catalogs/{catalog}/{id}/active")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCatalogActive(
            @PathVariable String catalog,
            @PathVariable String id,
            @RequestBody UpdateActiveRequest request
    ) {
        String table = switch (catalog) {
            case "evento_catalogo" -> "evento_catalogo";
            case "derivacion_catalogo" -> "derivacion_catalogo";
            default -> null;
        };

        if (table == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Este catálogo no permite activar/desactivar desde administración"
            ));
        }

        int updated = jdbc.update("""
                UPDATE %s
                   SET activo = ?,
                       last_modified = ?
                 WHERE id = ?
                   AND COALESCE(sql_deleted, 0) = 0
                """.formatted(table), Boolean.TRUE.equals(request.activo()), Instant.now().getEpochSecond(), id);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "catalog", catalog,
                "id", id,
                "activo", Boolean.TRUE.equals(request.activo())
        ));
    }

    @PostMapping("/catalogs/{catalog}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCatalogItem(
            @PathVariable String catalog,
            @RequestBody CreateCatalogItemRequest request
    ) {
        String table = switch (catalog) {
            case "barrios" -> "barrios";
            case "caps" -> "caps";
            default -> null;
        };

        if (table == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Este catálogo no permite altas simples desde administración"
            ));
        }

        String nombre = request.nombre() == null ? "" : request.nombre().trim();
        if (nombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "El nombre es obligatorio"
            ));
        }

        long duplicates = count("""
                SELECT COUNT(*)
                FROM %s
                WHERE COALESCE(sql_deleted, 0) = 0
                  AND LOWER(TRIM(nombre)) = LOWER(TRIM(?))
                """.formatted(table), nombre);

        if (duplicates > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Ya existe un valor activo con ese nombre"
            ));
        }

        String id = UUID.randomUUID().toString();
        long now = Instant.now().getEpochSecond();
        jdbc.update("""
                INSERT INTO %s (id, nombre, last_modified, sql_deleted)
                VALUES (?, ?, ?, 0)
                """.formatted(table), id, nombre, now);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "catalog", catalog,
                "id", id,
                "nombre", nombre
        ));
    }

    @GetMapping("/quality/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> qualityDetail(@PathVariable String type) {
        return switch (type) {
            case "visitas-domicilio-sin-gps" -> ResponseEntity.ok(queryList("""
                    SELECT
                      v.id AS visita_id,
                      v.fecha,
                      p.id AS persona_id,
                      p.apellido,
                      p.nombre,
                      p.dni,
                      r.nombre AS responsable,
                      u.email AS usuario,
                      viv.id AS vivienda_id,
                      viv.direccion,
                      viv.casa,
                      viv.manzana,
                      viv.latitud AS vivienda_latitud,
                      viv.longitud AS vivienda_longitud,
                      'Visita domiciliaria sin GPS de visita' AS problema
                    FROM visitas v
                    JOIN personas p ON p.id = v.persona_id AND COALESCE(p.sql_deleted, 0) = 0
                    LEFT JOIN responsables r ON r.id = v.responsable_id
                    LEFT JOIN users u ON u.id = v.created_by_user_id
                    LEFT JOIN viviendas viv ON viv.id = p.viviendas_id
                    WHERE COALESCE(v.sql_deleted, 0) = 0
                      AND v.tipo = 'domicilio'
                      AND (v.ubicacion_gps IS NULL OR TRIM(v.ubicacion_gps) = '')
                    ORDER BY v.fecha DESC, p.apellido, p.nombre
                    LIMIT 500
                    """));
            case "visitas-misma-fecha-gps-vivienda" -> ResponseEntity.ok(queryList("""
                    SELECT
                      DATE(v.fecha) AS fecha,
                      viv.latitud,
                      viv.longitud,
                      COUNT(DISTINCT v.id) AS cantidad_visitas,
                      COUNT(DISTINCT viv.id) AS cantidad_viviendas,
                      GROUP_CONCAT(DISTINCT CONCAT(COALESCE(viv.direccion, ''), ' ', COALESCE(viv.casa, '')) SEPARATOR ' | ') AS direcciones,
                      GROUP_CONCAT(DISTINCT CONCAT(COALESCE(p.apellido, ''), ', ', COALESCE(p.nombre, '')) SEPARATOR ' | ') AS personas,
                      'Misma fecha y mismo GPS de vivienda en controles domiciliarios' AS problema
                    FROM visitas v
                    JOIN control_domicilio cd ON cd.visita_id = v.id AND COALESCE(cd.sql_deleted, 0) = 0
                    JOIN personas p ON p.id = v.persona_id AND COALESCE(p.sql_deleted, 0) = 0
                    JOIN viviendas viv ON viv.id = p.viviendas_id AND COALESCE(viv.sql_deleted, 0) = 0
                    WHERE COALESCE(v.sql_deleted, 0) = 0
                      AND v.tipo = 'domicilio'
                      AND viv.latitud IS NOT NULL AND TRIM(viv.latitud) <> ''
                      AND viv.longitud IS NOT NULL AND TRIM(viv.longitud) <> ''
                    GROUP BY DATE(v.fecha), viv.latitud, viv.longitud
                    HAVING COUNT(DISTINCT v.id) > 1
                    ORDER BY cantidad_visitas DESC, fecha DESC
                    LIMIT 500
                    """));
            case "personas-sin-dni" -> ResponseEntity.ok(queryList("""
                    SELECT
                      p.id AS persona_id,
                      p.apellido,
                      p.nombre,
                      p.fecha_nac,
                      p.telefono,
                      viv.id AS vivienda_id,
                      viv.direccion,
                      viv.casa,
                      'Persona activa sin DNI' AS problema
                    FROM personas p
                    LEFT JOIN viviendas viv ON viv.id = p.viviendas_id
                    WHERE COALESCE(p.sql_deleted, 0) = 0
                      AND (p.dni IS NULL OR TRIM(p.dni) = '')
                    ORDER BY p.apellido, p.nombre
                    LIMIT 500
                    """));
            case "viviendas-sin-gps" -> ResponseEntity.ok(queryList("""
                    SELECT
                      viv.id AS vivienda_id,
                      viv.fecha,
                      viv.direccion,
                      viv.casa,
                      viv.manzana,
                      viv.latitud,
                      viv.longitud,
                      viv.motivo,
                      'Vivienda activa sin coordenada completa' AS problema
                    FROM viviendas viv
                    WHERE COALESCE(viv.sql_deleted, 0) = 0
                      AND (viv.latitud IS NULL OR TRIM(viv.latitud) = ''
                       OR viv.longitud IS NULL OR TRIM(viv.longitud) = '')
                    ORDER BY viv.fecha DESC, viv.direccion
                    LIMIT 500
                    """));
            case "controles-domicilio-sin-ta" -> ResponseEntity.ok(queryList("""
                    SELECT
                      cd.id AS control_id,
                      v.id AS visita_id,
                      v.fecha,
                      p.id AS persona_id,
                      p.apellido,
                      p.nombre,
                      p.dni,
                      r.nombre AS responsable,
                      u.email AS usuario,
                      'Control domiciliario sin TA' AS problema
                    FROM control_domicilio cd
                    LEFT JOIN visitas v ON v.id = cd.visita_id
                    LEFT JOIN personas p ON p.id = v.persona_id
                    LEFT JOIN responsables r ON r.id = v.responsable_id
                    LEFT JOIN users u ON u.id = cd.created_by_user_id
                    WHERE COALESCE(cd.sql_deleted, 0) = 0
                      AND cd.ta_sistolica IS NULL
                      AND cd.ta_diastolica IS NULL
                    ORDER BY v.fecha DESC, p.apellido, p.nombre
                    LIMIT 500
                    """));
            case "controles-consultorio-sin-ta" -> ResponseEntity.ok(queryList("""
                    SELECT
                      cc.id AS control_id,
                      v.id AS visita_id,
                      COALESCE(cc.fecha, v.fecha) AS fecha,
                      p.id AS persona_id,
                      p.apellido,
                      p.nombre,
                      p.dni,
                      r.nombre AS responsable,
                      u.email AS usuario,
                      'Control consultorio sin TA' AS problema
                    FROM control_consultorio cc
                    LEFT JOIN visitas v ON v.id = cc.visita_id
                    LEFT JOIN personas p ON p.id = v.persona_id
                    LEFT JOIN responsables r ON r.id = v.responsable_id
                    LEFT JOIN users u ON u.id = cc.created_by_user_id
                    WHERE COALESCE(cc.sql_deleted, 0) = 0
                      AND cc.ta_sistolica IS NULL
                      AND cc.ta_diastolica IS NULL
                    ORDER BY COALESCE(cc.fecha, v.fecha) DESC, p.apellido, p.nombre
                    LIMIT 500
                    """));
            case "laboratorios-sin-valor" -> ResponseEntity.ok(queryList("""
                    SELECT
                      lr.id AS laboratorio_id,
                      lr.fecha_realizado,
                      p.id AS persona_id,
                      p.apellido,
                      p.nombre,
                      p.dni,
                      lt.nombre AS tipo_laboratorio,
                      lr.laboratorio,
                      u.email AS usuario,
                      'Laboratorio sin valor numérico ni texto' AS problema
                    FROM lab_resultados lr
                    LEFT JOIN personas p ON p.id = lr.persona_id
                    LEFT JOIN lab_tipos lt ON lt.id = lr.tipo_id
                    LEFT JOIN users u ON u.id = lr.created_by_user_id
                    WHERE COALESCE(lr.sql_deleted, 0) = 0
                      AND lr.valor_num IS NULL
                      AND (lr.valor_texto IS NULL OR TRIM(lr.valor_texto) = '')
                    ORDER BY lr.fecha_realizado DESC, p.apellido, p.nombre
                    LIMIT 500
                    """));
            case "responsables-sin-usuario" -> ResponseEntity.ok(queryList("""
                    SELECT
                      r.id AS responsable_id,
                      r.nombre,
                      r.email,
                      r.matricula,
                      r.activo,
                      'Responsable sin usuario asociado' AS problema
                    FROM responsables r
                    WHERE COALESCE(r.sql_deleted, 0) = 0
                      AND r.user_id IS NULL
                    ORDER BY r.nombre
                    LIMIT 500
                    """));
            case "responsables-por-defecto" -> ResponseEntity.ok(queryList("""
                    SELECT
                      r.id AS responsable_id,
                      r.nombre,
                      r.email,
                      r.matricula,
                      r.activo,
                      'Responsable por defecto o genérico' AS problema
                    FROM responsables r
                    WHERE COALESCE(r.sql_deleted, 0) = 0
                      AND LOWER(r.nombre) LIKE '%defecto%'
                    ORDER BY r.nombre
                    LIMIT 500
                    """));
            case "usuarios-sin-rol" -> ResponseEntity.ok(queryList("""
                    SELECT
                      u.id AS usuario_id,
                      u.email,
                      u.nombre,
                      u.activo,
                      u.ultimo_login,
                      'Usuario activo sin roles asignados' AS problema
                    FROM users u
                    WHERE COALESCE(u.sql_deleted, 0) = 0
                      AND NOT EXISTS (
                        SELECT 1
                        FROM user_roles ur
                        WHERE ur.user_id = u.id
                          AND COALESCE(ur.sql_deleted, 0) = 0
                      )
                    ORDER BY u.email
                    LIMIT 500
                    """));
            default -> ResponseEntity.notFound().build();
        };
    }

    @PutMapping("/users/{userId}/roles")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserRoles(
            @PathVariable String userId,
            @RequestBody UpdateRolesRequest request,
            Authentication authentication
    ) {
        Integer exists = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM users
                WHERE id = ?
                  AND COALESCE(sql_deleted, 0) = 0
                """, Integer.class, userId);

        if (exists == null || exists == 0) {
            return ResponseEntity.notFound().build();
        }

        long now = Instant.now().getEpochSecond();
        List<String> requestedRoles = request.roles() == null ? List.of() : request.roles();

        if (isCurrentUser(authentication, userId) && requestedRoles.stream().noneMatch("ADMIN"::equals)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "No podés quitarte tu propio rol ADMIN"
            ));
        }

        jdbc.update("""
                UPDATE user_roles
                   SET sql_deleted = 1,
                       last_modified = ?
                 WHERE user_id = ?
                   AND COALESCE(sql_deleted, 0) = 0
                """, now, userId);

        for (String roleCode : requestedRoles) {
            List<Map<String, Object>> roles = queryList("""
                    SELECT id
                    FROM roles
                    WHERE code = ?
                      AND COALESCE(sql_deleted, 0) = 0
                    LIMIT 1
                    """, roleCode);

            if (roles.isEmpty()) continue;

            String roleId = String.valueOf(roles.get(0).get("id"));
            List<Map<String, Object>> existing = queryList("""
                    SELECT id
                    FROM user_roles
                    WHERE user_id = ?
                      AND role_id = ?
                    LIMIT 1
                    """, userId, roleId);

            if (existing.isEmpty()) {
                jdbc.update("""
                        INSERT INTO user_roles
                        (id, user_id, role_id, last_modified, sql_deleted)
                        VALUES (?, ?, ?, ?, 0)
                        """, UUID.randomUUID().toString(), userId, roleId, now);
            } else {
                jdbc.update("""
                        UPDATE user_roles
                           SET sql_deleted = 0,
                               last_modified = ?
                         WHERE id = ?
                        """, now, existing.get(0).get("id"));
            }
        }

        return ResponseEntity.ok(Map.of("ok", true, "userId", userId));
    }

    @PutMapping("/users/{userId}/active")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserActive(
            @PathVariable String userId,
            @RequestBody UpdateActiveRequest request,
            Authentication authentication
    ) {
        if (isCurrentUser(authentication, userId) && !Boolean.TRUE.equals(request.activo())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "No podés desactivar tu propio usuario"
            ));
        }

        Integer updated = jdbc.update("""
                UPDATE users
                   SET activo = ?,
                       last_modified = ?
                 WHERE id = ?
                   AND COALESCE(sql_deleted, 0) = 0
                """, Boolean.TRUE.equals(request.activo()), Instant.now().getEpochSecond(), userId);

        if (updated == null || updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("ok", true, "userId", userId, "activo", Boolean.TRUE.equals(request.activo())));
    }

    private List<Map<String, Object>> tableSummary() {
        List<String> tables = List.of(
                "users", "roles", "user_roles", "responsables", "viviendas", "personas",
                "visitas", "control_domicilio", "control_consultorio", "antecedentes",
                "lab_tipos", "lab_resultados", "barrios", "caps", "medicacion_hta"
        );
        List<Map<String, Object>> rows = new ArrayList<>();

        for (String table : tables) {
            rows.add(Map.of(
                    "tabla", table,
                    "activos", count("SELECT COUNT(*) FROM " + table + " WHERE COALESCE(sql_deleted, 0) = 0"),
                    "borrados", count("SELECT COUNT(*) FROM " + table + " WHERE COALESCE(sql_deleted, 0) = 1"),
                    "ultimoCambio", scalar("SELECT MAX(last_modified) FROM " + table)
            ));
        }

        return rows;
    }

    private Map<String, Object> catalog(String key, String label, String sql) {
        List<Map<String, Object>> rows = queryList(sql);
        long active = rows.stream()
                .filter(row -> !isTruthy(row.get("sql_deleted")))
                .filter(row -> row.get("activo") == null || isTruthy(row.get("activo")))
                .count();
        long inactive = rows.stream()
                .filter(row -> !isTruthy(row.get("sql_deleted")))
                .filter(row -> row.get("activo") != null && !isTruthy(row.get("activo")))
                .count();
        long deleted = rows.stream()
                .filter(row -> isTruthy(row.get("sql_deleted")))
                .count();
        Object lastModified = rows.stream()
                .map(row -> row.get("last_modified"))
                .filter(Objects::nonNull)
                .max((a, b) -> Long.compare(asLong(a), asLong(b)))
                .orElse(null);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("key", key);
        out.put("label", label);
        out.put("activos", active);
        out.put("inactivos", inactive);
        out.put("borrados", deleted);
        out.put("ultimoCambio", lastModified);
        out.put("items", rows);
        return out;
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString());
    }

    private long asLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private String cleanDate(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        return trimmed.matches("\\d{4}-\\d{2}-\\d{2}") ? trimmed : null;
    }

    private String dateCondition(String expression, String from, String to) {
        StringBuilder where = new StringBuilder();
        if (from != null) {
            where.append(" AND DATE(").append(expression).append(") >= '").append(from).append("'");
        }
        if (to != null) {
            where.append(" AND DATE(").append(expression).append(") <= '").append(to).append("'");
        }
        return where.toString();
    }

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

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private Object scalar(String sql, Object... args) {
        return jdbc.queryForObject(sql, Object.class, args);
    }

    private List<Map<String, Object>> queryList(String sql, Object... args) {
        return jdbc.queryForList(sql, args);
    }

    private boolean isCurrentUser(Authentication authentication, String userId) {
        if (authentication == null || authentication.getPrincipal() == null) return false;
        Object principal = authentication.getPrincipal();
        return principal instanceof JwtUser jwtUser && userId.equals(jwtUser.getUserId());
    }

    public record UpdateRolesRequest(List<String> roles) {}
    public record UpdateActiveRequest(Boolean activo) {}
    public record CreateCatalogItemRequest(String nombre) {}
}
