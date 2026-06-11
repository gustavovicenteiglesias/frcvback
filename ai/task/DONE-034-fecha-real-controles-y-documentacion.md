# DONE-034 - Fecha real de controles y cierre documental

Estado: DONE
Fecha: 2026-06-04

## Objetivo

Mostrar en controles la fecha sanitaria real cuando existe y dejar documentados los cambios de calidad del dato realizados.

## Cambios realizados

- El listado de controles usa `control_consultorio.fecha` cuando existe.
- La fecha de visita queda como respaldo.
- Se elimino un `console.log` de depuracion del listado de controles.
- Se agrego documentacion de alcance en `/docs/2026-06-04-calidad-dato-dashboard-filtros.md`.

## Compatibilidad

El cambio solo agrega una columna calculada en la consulta local (`fecha_control`) y no cambia tablas ni sync.

## Verificacion

`npm run build` ejecutado correctamente.

