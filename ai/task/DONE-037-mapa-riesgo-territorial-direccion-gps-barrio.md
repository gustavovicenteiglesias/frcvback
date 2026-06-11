# DONE-037 - Mapa de riesgo territorial por direccion, GPS y barrio

## Objetivo

Ajustar `/mapa-riesgo-territorial` para que, sin modificar la base de datos, pueda mostrar viviendas de riesgo priorizando ubicacion por direccion normalizada, usando GPS como respaldo y aproximando por barrio cuando no haya otro dato ubicable.

## Alcance

- Solo frontend.
- Sin cambios de schema.
- Sin cambios de sync.
- Sin escritura de coordenadas en viviendas.
- Compatible con dispositivos Android que sigan cargando datos con versiones anteriores.

## Implementado

- Se agrego una capa visual de geocodificacion con Georef para el mapa de riesgo territorial.
- La prioridad de ubicacion del punto queda asi:
  1. Direccion resuelta por Georef.
  2. Direccion aproximada o S/N resuelta/estimada como dato territorial de confianza media.
  3. GPS registrado como respaldo.
  4. Barrio aproximado cuando hay centro de barrio calculado desde viviendas con GPS.
  5. Sin ubicar cuando no hay direccion, GPS ni barrio usable.
- La consulta a Georef queda cacheada solamente en el navegador para acelerar la visualizacion.
- Se agregaron contadores por fuente de punto: direccion, aproximacion por barrio, GPS y sin ubicar.
- Se agrego control para consultar direcciones por lote, reintentar fallidas y limpiar cache local del mapa.
- El detalle del punto muestra fuente original, fuente usada para mapear, direccion normalizada y coordenadas visuales.

## Verificacion

- `npm run build` ejecutado correctamente.
- Advertencia conocida de Vite por tamano de chunks, sin falla de compilacion.
