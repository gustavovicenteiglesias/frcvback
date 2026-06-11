# DONE-040 - Mapa riesgo: control territorial por barrio GeoJSON

## Objetivo

Usar el GeoJSON de barrios con centroides y poligonos para que `/mapa-riesgo-territorial` no use GPS fuera del barrio declarado y pueda ubicar por barrio cuando la direccion no resuelve.

## Implementado

- Se incorporo el GeoJSON local de barrios como capa de datos del frontend.
- Se agrego una utilidad territorial para obtener barrio por `barrio_id`, validar punto dentro de poligono y validar distancia al centroide cuando no hay poligono.
- El mapa trae ahora `barrios_id` desde la consulta para evitar depender del texto del nombre del barrio.
- Regla de ubicacion actual:
  1. Direccion resuelta por Georef.
  2. GPS aceptado solo si no cae fuera del barrio declarado.
  3. Si el GPS cae fuera o no hay GPS usable, se ubica por centroide de barrio.
  4. Si no hay barrio usable, queda sin ubicar.
- Se subio la version de cache local a `v3` para probar sin fallas viejas.
- No se modifica la vivienda, no se escriben coordenadas y no se toca sync.

## Verificacion

- `npm run build` ejecutado correctamente.
- Advertencia conocida de Vite por tamano de chunks, sin falla de compilacion.
