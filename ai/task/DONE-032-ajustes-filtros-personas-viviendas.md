# DONE-032 - Ajustes de filtros en personas y viviendas

Estado: DONE
Fecha: 2026-06-04

## Objetivo

Ajustar los filtros principales de personas y viviendas para que reflejen mejor el circuito real del programa y no generen categorias confusas.

## Cambios realizados

- Personas:
  - El buscador permite encontrar por barrio y CAPS asociados a la vivienda.
  - El filtro "Con antecedentes" se redefine como "Con antecedente HTA".
  - Se agrega "En tratamiento POLILEP".
  - Se agrega "TA alta detectada".
  - Se quita de la pantalla "No acepta pero en tratamiento".
  - El export de personas usa los mismos filtros que la pantalla.
- Viviendas:
  - "Solo accedidas" queda desactivado por defecto.
  - Se quita el filtro "Con tratamiento".
  - El contador diferencia viviendas visibles por filtros y total general cargado.

## Compatibilidad

No se modificaron schema, sincronizacion, migraciones ni contrato con backend.

## Verificacion

`npm run build` ejecutado correctamente.

