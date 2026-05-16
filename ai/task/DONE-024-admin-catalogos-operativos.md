# DONE-024 - Administración de catálogos operativos

## Estado

DONE

## Contexto

La app usa catálogos que impactan la carga y la sincronización: barrios, CAPS, tipos de laboratorio, medicación HTA, eventos, derivaciones y motivos.

## Objetivo

Evaluar e implementar una administración segura de catálogos desde el tablero admin.

## Alcance de esta etapa

Primera etapa de solo lectura:

- Barrios.
- CAPS.
- Tipos de laboratorio.
- Medicación HTA.
- Eventos de consultorio.
- Derivaciones de consultorio.

No se habilitaron edición, creación ni borrado desde la UI.

## Resultado

- Se agregó `GET /admin/catalogs`.
- El tablero admin suma la sección `Catálogos`.
- Cada catálogo muestra:
  - activos;
  - inactivos cuando existe `activo`;
  - borrados lógicos;
  - último cambio;
  - listado de valores.
- Se excluyó `motivo_no_medicacion` porque en la base real funciona como dato operativo con detalles y repetidos, no como catálogo maestro administrable.
- Se documentó en `docs/12-tablero-administracion-online.md`.

## Decisiones

- La primera etapa es de observación y diagnóstico.
- No se modifican catálogos hasta definir reglas por tabla.
- No se borra físicamente ningún valor.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
