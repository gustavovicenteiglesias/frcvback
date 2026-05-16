# DOING-021 - Exportar calidad del dato desde administración

## Estado

DONE

## Contexto

El tablero admin muestra indicadores de calidad del dato, pero todavía no permite descargar los casos para auditoría o devolución al equipo.

## Objetivo

Permitir que administración exporte listados accionables de problemas de calidad del dato.

## Alcance

Agregar exportaciones CSV para:

- Visitas domiciliarias sin GPS de visita.
- Visitas/control domiciliario con misma fecha y mismo GPS de vivienda.
- Personas sin DNI.
- Viviendas sin coordenadas.
- Controles domiciliarios sin TA.
- Controles de consultorio sin TA.
- Laboratorios sin valor.
- Responsables por defecto o sin usuario asociado.
- Usuarios sin rol.

## Reglas

- Debe estar disponible solo para `ADMIN`.
- Debe funcionar online contra backend.
- Los CSV deben incluir columnas útiles para corregir: identificador, persona, vivienda, fecha, responsable, usuario cuando exista, motivo del problema.
- Evitar exponer más datos sensibles de los necesarios.

## Criterios de aceptación

- Cada indicador de calidad puede exportar su detalle.
- Los archivos tienen encabezados claros en español.
- Los totales del tablero coinciden con las filas exportadas.
- La exportación no depende de SQLite local.

## Resultado

- Se agregó `GET /admin/quality/{type}` para devolver detalles exportables por problema de calidad.
- El tablero de administración muestra botones CSV por indicador.
- Se separaron controles domiciliarios sin TA y controles de consultorio sin TA para que el total sea trazable.
- Se agregó el indicador de grupos con misma fecha y mismo GPS de vivienda en controles domiciliarios.
- Se documentó el flujo en `docs/12-tablero-administracion-online.md`.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
