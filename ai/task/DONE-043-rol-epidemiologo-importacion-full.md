# DONE-043 - Rol EPIDEMIOLOGO e importacion full

## Objetivo

Permitir que el rol EPIDEMIOLOGO pueda descargar la base completa para consultar dashboard epidemiologico y mapa territorial, sin habilitar carga operativa ni exportacion/push.

## Cambios realizados

- Se agrego una regla especifica en `SecurityConfig` para `/api/sync/pull-full`.
- El endpoint queda habilitado para `ADMIN`, `MEDICO`, `ENFERMERO` y `EPIDEMIOLOGO`.
- La regla general `/api/**` sigue limitada a `ADMIN`, `MEDICO` y `ENFERMERO`.
- Por lo tanto, `EPIDEMIOLOGO` puede importar base completa, pero no puede hacer push ni usar endpoints operativos.

## Validacion

- Backend compila correctamente con `mvnw.cmd -q -DskipTests compile`.

## Estado

DONE