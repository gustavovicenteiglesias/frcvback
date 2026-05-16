# DONE-027 - Activar/desactivar catálogos seguros

## Estado

DONE

## Contexto

La administración de catálogos ya muestra una vista de solo lectura. Al revisar los modelos, solo algunos catálogos tienen columna `activo`: eventos de consultorio y derivaciones de consultorio.

## Objetivo

Permitir que `ADMIN` active o desactive valores de catálogos que ya tienen soporte explícito para estado activo/inactivo.

## Alcance

- Eventos de consultorio.
- Derivaciones de consultorio.
- Actualizar `activo` y `last_modified`.
- Mantener `sql_deleted` sin cambios.
- Confirmar la acción desde UI.

## Fuera de alcance

- Crear nuevos valores.
- Editar nombres.
- Borrar valores.
- Activar/desactivar catálogos sin columna `activo`.

## Criterios de aceptación

- Solo `ADMIN`.
- Online.
- La app móvil recibe el cambio en la próxima importación full.
- No se rompen registros históricos.
- El build pasa.

## Resultado

- Se agregó `PUT /admin/catalogs/{catalog}/{id}/active`.
- Solo acepta:
  - `evento_catalogo`;
  - `derivacion_catalogo`.
- La UI muestra un `toggle` solo para catálogos con columna `activo`.
- La acción pide confirmación.
- Se actualiza `activo` y `last_modified`.
- No se modifica `sql_deleted`.
- Se documentó en `docs/12-tablero-administracion-online.md`.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
