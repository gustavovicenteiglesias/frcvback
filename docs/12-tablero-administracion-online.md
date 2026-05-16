# Tablero de administración online

El tablero de administración es una vista técnica-operativa, no clínica. Está disponible solo para usuarios con rol `ADMIN` y requiere conexión a internet porque consulta al backend como fuente principal.

## Endpoints implementados

| Endpoint | Método | Rol | Uso |
| --- | --- | --- | --- |
| `/admin/ping` | GET | `ADMIN` | Verificación básica de acceso admin. |
| `/admin/summary` | GET | `ADMIN` | Resumen de usuarios, responsables, roles, calidad del dato y tablas sincronizadas. |
| `/admin/users` | GET | `ADMIN` | Listado de usuarios con roles y responsable vinculado. |
| `/admin/roles` | GET | `ADMIN` | Catálogo de roles disponibles. |
| `/admin/audit` | GET | `ADMIN` | Auditoría de actividad por usuario/responsable con filtros de fecha. |
| `/admin/sync-status` | GET | `ADMIN` | Estado de sincronización por usuario o dispositivo. |
| `/admin/catalogs` | GET | `ADMIN` | Vista de solo lectura de catálogos operativos. |
| `/admin/quality/{type}` | GET | `ADMIN` | Detalle exportable de un problema de calidad del dato. |
| `/admin/users/{userId}/roles` | PUT | `ADMIN` | Actualización de roles existentes para un usuario. |
| `/admin/users/{userId}/active` | PUT | `ADMIN` | Activación o desactivación de usuarios. |
| `/api/sync/event` | POST | Usuario autenticado | Registro liviano de eventos de sincronización. |

## Indicadores incluidos

### Usuarios y responsables

- Usuarios activos.
- Usuarios sin roles.
- Responsables activos.
- Responsables sin usuario.
- Responsables por defecto.

### Calidad del dato

- Visitas domiciliarias.
- Visitas domiciliarias sin GPS de visita.
- Visitas o controles domiciliarios con misma fecha y mismo GPS de vivienda.
- Personas sin DNI.
- Viviendas sin coordenadas.
- Controles domiciliarios sin TA.
- Controles de consultorio sin TA.
- Laboratorios sin valor.
- Responsables sin usuario asociado.
- Responsables por defecto.
- Usuarios sin rol.

Cada indicador puede exportar un CSV desde la tarjeta correspondiente. La exportación consulta el backend y devuelve filas accionables, con identificadores, persona, vivienda, fecha, responsable, usuario y motivo del problema cuando esos datos existen. Esto permite revisar el caso puntual y corregir el circuito de carga, no solo mirar un total agregado.

### Tablas sincronizadas

El tablero muestra cantidad de registros activos, borrados lógicos y último cambio por tabla principal.

### Estado de sincronización

La app registra eventos de exportación parcial e importación full en `sync_events`. Cada evento guarda usuario autenticado, dispositivo local, plataforma, operación, resultado, mensaje corto y fecha/hora. Si el registro del evento falla, la sincronización no se bloquea.

El tablero muestra últimos eventos, errores recientes y usuarios activos sin sincronización exitosa en los últimos siete días. Esta información sirve para detectar equipos que no están actualizando la base local o usuarios con fallas repetidas.

### Catálogos operativos

El tablero muestra una vista de solo lectura de los catálogos centrales: barrios, CAPS, tipos de laboratorio, medicación HTA, eventos de consultorio y derivaciones.

La primera etapa permitió revisar valores existentes, activos, inactivos, borrados lógicos y último cambio. La etapa siguiente habilita activar/desactivar solo en catálogos que ya tienen columna `activo`: eventos de consultorio y derivaciones.

También se permite el alta simple de barrios y CAPS. Son catálogos con estructura mínima (`id`, `nombre`, `last_modified`, `sql_deleted`), por eso se pueden crear desde administración sin afectar campos clínicos ni rangos técnicos. El sistema valida nombre obligatorio y evita duplicados activos por nombre.

`motivo_no_medicacion` no se trata como catálogo administrativo en esta pantalla porque contiene motivos registrados con detalle operativo y puede tener valores repetidos por uso clínico. Si se analiza, debe hacerse como dato de consultorio/auditoría, no como catálogo maestro.

### Auditoría de carga

La auditoría agrupa actividad por usuario y responsable. Incluye visitas, controles domiciliarios, controles de consultorio, laboratorios, antecedentes y borrados lógicos inferidos. También muestra señales de calidad por responsable: visitas domiciliarias sin GPS, coordenadas repetidas el mismo día, controles sin TA y uso de responsable genérico.

La vista permite filtrar por rango de fechas y exportar el resultado a CSV. Los registros históricos sin `created_by_user_id` se muestran como `sin_usuario`; no se mezclan con datos trazables. Esta separación es importante para no atribuir carga antigua o incompleta a una persona específica sin evidencia.

## Decisiones

- El tablero no usa SQLite local como fuente principal.
- El acceso se bloquea si el usuario no es `ADMIN`.
- La entrada desde la app se muestra solo si hay conexión.
- `ADMIN` no hereda permisos clínicos: este tablero no habilita dashboard epidemiológico ni evolución individual.
- La actualización de roles usa roles ya existentes; no crea nuevos códigos de rol.
- La auditoría sirve para devolución operativa y mejora del dato; no aplica sanciones automáticas.
- El log de sincronización no guarda datos clínicos, solo metadatos técnicos mínimos.
- La administración de catálogos comienza como solo lectura; edición y altas requieren una etapa posterior con reglas específicas.
- Solo se permite activar/desactivar valores de catálogos con soporte explícito de `activo`; no se modifica `sql_deleted`.
- Solo se permite crear barrios y CAPS desde esta pantalla; laboratorios quedan fuera porque tienen campos particulares.

## Pendientes posibles

- Agregar auditoría detallada de cambios sensibles.
- Vincular usuarios y responsables desde la UI.
