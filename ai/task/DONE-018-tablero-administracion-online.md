# DONE-018 - Tablero de administración online

## Estado

DONE

## Contexto

Se necesita un tablero de administración visible solo para usuarios con rol `ADMIN` y disponible únicamente cuando hay conexión a internet. A diferencia de las pantallas operativas offline-first, este tablero debe consultar al backend como fuente principal.

## Problema

El tablero no puede construirse solo desde el frontend porque varias métricas dependen de datos centrales:

- usuarios y roles;
- responsables asociados a usuarios;
- estado de sincronización;
- calidad del dato global;
- auditoría de carga y borrados lógicos;
- catálogos globales.

Antes de diseñar la UI completa hay que relevar qué endpoints existen y cuáles faltan.

## Objetivo

Definir e iniciar el tablero de administración con una base técnica clara:

- visible solo para `ADMIN`;
- bloqueado si no hay conexión;
- apoyado en endpoints reales del backend;
- pensado como tablero técnico-operativo, no clínico.

## Alcance inicial

1. Relevar endpoints backend disponibles.
2. Identificar faltantes para:
   - gestión de usuarios;
   - roles;
   - responsables;
   - calidad del dato;
   - sincronización;
   - auditoría;
   - catálogos.
3. Documentar matriz de endpoints.
4. Proponer primer corte implementable.

## Alcance funcional esperado

- Gestión de usuarios y roles.
- Estado general de sincronización.
- Indicadores de calidad del dato.
- Auditoría básica.
- Resumen de catálogos.

## Restricciones

- No mostrar el tablero a perfiles no `ADMIN`.
- No mostrar el tablero si no hay conexión.
- No usar SQLite local como fuente principal del tablero.
- No crear endpoints innecesarios sin confirmar faltantes.
- No romper el flujo actual de importación full/sincronización.

## Criterios de aceptación

- Existe una tarea trazable para el tablero admin.
- Está documentado qué endpoints existen y cuáles faltan.
- Se define un primer corte viable de implementación.
- No se modifican permisos clínicos existentes.

## Resultado

### Backend

Se amplió `AdminController` con endpoints protegidos por `ADMIN`:

- `GET /admin/summary`
- `GET /admin/users`
- `GET /admin/roles`
- `PUT /admin/users/{userId}/roles`

El resumen incluye usuarios, responsables, roles, calidad del dato y tablas sincronizadas.

### Frontend

Se agregó pantalla `AdminDashboard` con:

- bloqueo por rol `ADMIN`;
- bloqueo visual si no hay conexión;
- indicadores de usuarios/responsables;
- indicadores de calidad del dato;
- listado de usuarios con edición de roles;
- resumen de tablas sincronizadas.

La entrada se muestra desde la selección de entidad solo para `ADMIN` y cuando hay conexión.

### Documentación

Se agregó `docs/12-tablero-administracion-online.md` y se enlazó desde el README.

## Validación

- Frontend: `npx tsc --noEmit`
- Frontend: `npm run build`
- Backend: `mvnw.cmd -q -DskipTests compile`

## Pendientes

- Auditoría detallada por usuario.
- Estado de última importación full por usuario/dispositivo.
- Vinculación usuario-responsable desde UI.
- Exportación de problemas de calidad del dato.
