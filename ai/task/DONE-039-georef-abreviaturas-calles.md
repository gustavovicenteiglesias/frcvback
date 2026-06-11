# DONE-039 - Georef: abreviaturas de calles

## Objetivo

Mejorar la normalizacion de direcciones para que abreviaturas habituales como `Gral.` puedan resolverse antes de caer a aproximacion por barrio.

## Diagnostico

El caso `Gral Paz 829/839` caia como `Aprox. barrio` porque la base local de calles tiene la calle como `PAZ GRL`. La busqueda local no consideraba equivalentes `Gral`, `Grl` y `General`, por lo que no encontraba la calle oficial antes de consultar Georef.

## Implementado

- En el normalizador de texto de busqueda de calles se unifican `gral`, `grl` y `general` como `general`.
- Se subio la version de cache local del mapa de riesgo territorial para no reutilizar fallas de Georef guardadas antes de este ajuste.
- No se modifica la vivienda, no se escriben coordenadas y no se toca sync.

## Verificacion

- `npm run build` ejecutado correctamente.
- Advertencia conocida de Vite por tamano de chunks, sin falla de compilacion.
