# DONE-017 - Ordenar roles y permisos operativos

## Estado

DONE

## Contexto

El sistema ya utiliza roles `ADMIN`, `MEDICO` y `ENFERMERO`, pero el uso práctico mezcla dos conceptos distintos:

- rol de sistema o administración;
- rol sanitario/operativo dentro del circuito de carga, revisión clínica y análisis epidemiológico.

Además, con el cierre de trazabilidad de responsables y captura de GPS en visitas domiciliarias, los permisos deben acompañar la calidad del dato: quien carga debe quedar identificado, y quien accede a análisis clínico/epidemiológico debe tener rol apropiado.

## Problema

El rol `ADMIN` puede interpretarse como jerárquicamente superior, pero no necesariamente debería ver o ejecutar acciones clínicas sensibles si no tiene rol sanitario.

Actualmente varias pantallas usan directamente:

- `MEDICO` para dashboard, evolución y borrados;
- `ENFERMERO` para navegación/carga;
- `ADMIN` para administración general.

Hay que ordenar el criterio para que sea claro y mantenible, sin romper el login ni la sincronización.

## Objetivo

Definir y aplicar una política mínima de roles:

- `ADMIN`: administración técnica/sistema, no implica permisos clínicos por sí solo.
- `MEDICO`: revisión clínica, evolución, dashboard epidemiológico, borrado lógico y acciones sensibles.
- `ENFERMERO`: carga operativa, viviendas, personas, antecedentes y controles domiciliarios.

## Alcance

Frontend:

- Centralizar helpers de roles para evitar `roles.includes(...)` dispersos.
- Reemplazar usos directos en pantallas sensibles por helpers semánticos.
- Mantener el comportamiento actual donde ya es correcto: evolución, dashboard y borrados solo para `MEDICO`.
- Evitar que `ADMIN` herede permisos clínicos automáticamente.

Backend:

- Revisar si hay endpoints protegidos por roles.
- No cambiar esquema ni sync salvo que aparezca una dependencia real.
- Mantener login y JWT con los roles existentes.

Documentación:

- Actualizar documentación de roles y permisos.
- Dejar explícito que `ADMIN` no equivale a `MEDICO`.

## Plan

1. Relevar usos actuales de `ADMIN`, `MEDICO`, `ENFERMERO` en frontend y backend.
2. Crear helper central de permisos en frontend.
3. Reemplazar usos directos donde corresponda.
4. Actualizar documentación de roles.
5. Validar TypeScript y build.

## Criterios de aceptación

- Dashboard epidemiológico visible solo para usuarios con rol `MEDICO`.
- Evolución individual visible solo para usuarios con rol `MEDICO`.
- Borrado lógico visible solo para usuarios con rol `MEDICO`.
- `ADMIN` no ve acciones clínicas sensibles salvo que también tenga rol `MEDICO`.
- La carga operativa sigue disponible para los flujos existentes.
- No se modifica sync ni modelo de datos si no es necesario.

## Resultado

- Se creó un helper central de permisos en frontend.
- Se reemplazaron chequeos directos de roles en vistas sensibles por helpers semánticos.
- Se mantuvo `MEDICO` como único rol para dashboard, evolución y borrado lógico.
- Se dejó explícito que `ADMIN` no hereda permisos clínicos automáticamente.
- Se actualizó la documentación de roles.

## Validación

- `npx tsc --noEmit`
- `npm run build`
