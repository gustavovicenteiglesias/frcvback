# DONE-035 - Dashboard visitas domiciliarias: No aplica

Estado: DONE
Fecha: 2026-06-04

## Objetivo

Agregar la categoria No aplica al bloque de visitas domiciliarias del dashboard epidemiologico.

## Diagnostico

El dato ya existe y es compatible:

- Backend: enum EstadoAcceso incluye NO_APLICA.
- MySQL: viviendas.motivo permite enum('ACCEDIO','AUSENTE','NO_APLICA','RECHAZO').
- SQLite: viviendas.motivo permite 'ACCEDIO','AUSENTE','RECHAZO','NO_APLICA'.
- Front: ViviendaForm ya ofrece No aplica y guarda NO_APLICA.

## Plan

1. Contar viviendas activas con motivo = 'NO_APLICA'.
2. Mostrar la categoria en el modulo de visitas domiciliarias.
3. Agregar detalle clickeable del indicador.
4. Verificar build.
5. Pasar la tarea a DONE.

## Compatibilidad

No requiere schema, migracion, sync ni backend.

## Resultado

- Se agrego el conteo de viviendas con motivo = 'NO_APLICA'.
- Se muestra la categoria No aplica en visitas domiciliarias del dashboard.
- El indicador tiene detalle clickeable por vivienda.
- No se modificaron schema, sync ni backend.

## Verificacion

- npm run build OK.

