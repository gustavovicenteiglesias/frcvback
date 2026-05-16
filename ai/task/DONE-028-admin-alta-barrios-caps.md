# DONE-028 - Alta administrativa de barrios y CAPS

## Estado

DONE

## Contexto

La administración de catálogos permite ver catálogos y activar/desactivar eventos y derivaciones. Falta permitir altas seguras en catálogos simples.

## Objetivo

Permitir que `ADMIN` agregue barrios y CAPS desde el tablero de administración.

## Alcance

- Crear barrio.
- Crear CAPS.
- Validar nombre no vacío.
- Evitar duplicados activos por nombre.
- Completar `id`, `last_modified` y `sql_deleted = 0`.
- Recargar catálogos luego del alta.

## Fuera de alcance

- Laboratorios, porque tienen campos particulares.
- Medicación HTA.
- Eventos y derivaciones.
- Edición de nombres.
- Borrado.

## Criterios de aceptación

- Solo `ADMIN`.
- Online.
- El alta queda sincronizable por importación full.
- No permite nombres vacíos.
- No duplica valores activos con el mismo nombre.
- El build pasa.

## Resultado

- Se agregó `POST /admin/catalogs/{catalog}`.
- Solo acepta:
  - `barrios`;
  - `caps`.
- Valida nombre obligatorio.
- Rechaza duplicados activos por nombre.
- Crea `id`, `last_modified` y `sql_deleted = 0`.
- La sección Catálogos permite elegir Barrio o CAPS, ingresar nombre y confirmar el alta.
- Se recarga la sección luego de crear.
- Se documentó en `docs/12-tablero-administracion-online.md`.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
