# DONE-038 - Mapa riesgo: geocodificacion completa cacheada

## Objetivo

Permitir que `/mapa-riesgo-territorial` revise todas las direcciones pendientes contra Georef cuando el usuario lo solicite, guardando el resultado solamente en localStorage y mostrando progreso.

## Implementado

- Se reemplazo la consulta por lotes de 20 por un boton `Revisar todas`.
- Se agrego barra de progreso con cantidad revisada sobre total.
- Las coordenadas resueltas por Georef se guardan solo en localStorage del navegador.
- Las fallas tambien se guardan localmente para no repetir consultas fallidas en cada entrada.
- Se agrego reintento de fallidas: limpia la cache local de esas viviendas y vuelve a consultarlas.
- Se mantiene el limite institucional: el mapa no modifica viviendas, no guarda latitud/longitud en SQLite ni cambia sync.

## Verificacion

- `npm run build` ejecutado correctamente.
- Advertencia conocida de Vite por tamano de chunks, sin falla de compilacion.
