# Mapa por direcciones y calidad territorial

Fecha: 2026-06-04

## Objetivo

Mostrar viviendas en mapa priorizando la ubicacion por direccion normalizada y dejando visible la fuente del punto para evitar falsa confianza.

## Regla aplicada

1. Si Georef resuelve la direccion, el punto se marca por direccion.
2. Si la direccion no resuelve y es una direccion aproximada o sin numero, se puede ubicar cerca del centro del barrio calculado desde viviendas con GPS valido.
3. Si no hay resolucion por direccion pero la vivienda tiene GPS valido, se usa GPS como respaldo.
4. Si no hay direccion usable, GPS ni barrio suficiente, queda sin ubicar.

## Lectura de confianza

- Direccion con numero: mayor confianza territorial para inspeccion del mapa.
- Direccion aproximada: util para ver distribucion barrial, no identifica puerta exacta.
- GPS de respaldo: punto cargado en la vivienda, pero usado despues de intentar direccion.
- Sin ubicar: requiere correccion de dato de origen.

## Compatibilidad

No se modificaron tablas, sync, migraciones ni backend. El cambio es de consulta local, visualizacion y criterio de presentacion.
