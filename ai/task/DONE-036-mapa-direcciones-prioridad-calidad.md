# DONE-036 - Mapa por direcciones con prioridad de calidad

Estado: DONE
Fecha: 2026-06-04

## Objetivo

Consolidar el mapa por direcciones para que marque viviendas priorizando direccion normalizada y dejando visible la fuente del punto.

## Criterio acordado

1. Si hay direccion geocodificable, marcar por direccion.
2. Si la direccion no resuelve y hay GPS, marcar por GPS como respaldo.
3. Si no hay numero pero hay barrio, permitir aproximacion al centro del barrio calculado desde viviendas con GPS.
4. Si no hay direccion usable, GPS ni barrio suficiente, dejar sin ubicar.
5. Mostrar siempre la fuente para evitar falsa confianza.

## Plan

1. Revisar el mapa actual y eliminar logica duplicada/confusa.
2. Centralizar la decision de fuente del punto.
3. Ajustar textos para que deje de figurar como prueba si queda operativo.
4. Verificar build.
5. Documentar y pasar a DONE.

## Compatibilidad

No requiere schema, sync ni backend.

## Resultado

- El mapa por direcciones toma viviendas activas, no solo accedidas.
- La decision de punto se centralizo con prioridad: direccion Georef, aproximacion por barrio para direcciones sin numero/fallidas, GPS de respaldo y sin ubicar.
- El dashboard enlaza como Mapa territorial por direccion.
- El mapa muestra conteos separados por direccion, GPS, aproximacion por barrio y sin ubicar.
- No se modificaron schema, sync ni backend.

## Verificacion

- npm run build OK.

