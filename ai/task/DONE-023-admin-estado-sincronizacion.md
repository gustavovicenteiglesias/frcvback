# DONE-023 - Estado de sincronización por usuario o dispositivo

## Estado

DONE

## Contexto

La app usa importación full como mecanismo vigente de sincronización. Hoy no quedaba una auditoría central simple de cuándo sincronizó cada usuario o dispositivo ni de errores ocurridos.

## Objetivo

Registrar y mostrar el estado de sincronización para que administración pueda detectar usuarios sin actualizar o con errores.

## Alcance

- Registrar usuario y dispositivo.
- Registrar tipo de operación: importación full y push/export.
- Registrar fecha/hora.
- Registrar resultado: OK/error.
- Registrar mensaje de error resumido.
- Mostrar en tablero admin últimos sync, usuarios sin sync reciente y errores recientes.

## Restricciones

- No bloquear la importación full por no poder registrar el evento.
- No guardar datos clínicos en el log de errores.
- Mantener compatibilidad con Android y web.

## Resultado

- Se agregó la tabla operativa `sync_events` mediante creación segura `CREATE TABLE IF NOT EXISTS`.
- Se agregó `POST /api/sync/event` para registrar eventos de sincronización.
- La app registra `push` e `import_full`, tanto exitosos como fallidos.
- Se guarda usuario autenticado, dispositivo local, plataforma, resultado, mensaje técnico corto y fecha/hora.
- El registro de evento no bloquea la sincronización si falla.
- Se agregó `GET /admin/sync-status` para administración.
- El tablero admin muestra eventos recientes, errores recientes, usuarios sin sync OK en siete días y último evento por usuario/dispositivo.
- Se documentó en `docs/12-tablero-administracion-online.md`.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
