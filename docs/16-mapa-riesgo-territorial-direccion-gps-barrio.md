# Mapa de riesgo territorial: direccion, GPS y barrio

## Que cambia

El mapa `/mapa-riesgo-territorial` deja de depender exclusivamente de las coordenadas GPS guardadas en la vivienda. Ahora puede usar la direccion normalizada como primera fuente de ubicacion visual, y deja GPS o barrio como respaldo.

Este cambio es solo de lectura y visualizacion: no corrige viviendas, no pisa latitud/longitud y no cambia datos de sincronizacion.

## Prioridad de ubicacion

| Prioridad | Fuente | Uso en el mapa | Lectura de calidad |
|---|---|---|---|
| 1 | Direccion resuelta por Georef | Punto principal | Mejor lectura territorial disponible para esta vista |
| 2 | Direccion aproximada / S/N | Punto aproximado | Sirve para mapa, no identifica puerta exacta |
| 3 | GPS registrado | Respaldo cuando la direccion no esta resuelta | Depende de precision y confianza original |
| 4 | Barrio | Centro aproximado del barrio, con desplazamiento visual | Solo orientativo |
| 5 | Sin dato ubicable | No se dibuja | Requiere mejora de carga |

## Por que importa

Con mejores direcciones cargadas, el mapa de riesgo puede mostrar mejor la distribucion territorial de viviendas con personas con TA alta o antecedente de HTA. Tambien permite ver rapidamente que parte del mapa depende todavia de datos debiles: GPS de baja precision, direcciones incompletas o barrios sin coordenadas suficientes.

## Limite institucional

El mapa puede ayudar a leer el territorio, pero no debe dar falsa precision. Por eso cada punto informa de donde salio: direccion, aproximacion por barrio o GPS. Si el dato es aproximado, queda visible como tal.

## Verificacion tecnica

- Cambio implementado en frontend.
- No requiere migraciones.
- No altera export/import ni sync.
- Build de frontend correcto con `npm run build`.

## Revision completa cacheada

El mapa permite ejecutar una revision completa de direcciones pendientes con el boton `Revisar todas`. El proceso muestra progreso por cantidad de direcciones revisadas.

Los resultados se guardan solo en `localStorage` del navegador:

- si Georef encuentra coordenadas, el mapa las reutiliza en futuras entradas;
- si Georef falla, la falla queda cacheada para no repetir la misma consulta cada vez;
- el boton `Reintentar fallidas` limpia esas fallas locales y vuelve a consultarlas;
- el boton `Limpiar Georef del mapa` borra toda la cache local de esta visualizacion.

Este mecanismo no corrige la vivienda ni escribe coordenadas en la base. Sirve para lectura territorial del riesgo y para detectar donde conviene mejorar la calidad de la carga.

## Abreviaturas de calles

Se detecto que algunas direcciones cargadas con abreviaturas no se resolvian aunque fueran ubicables por otros servicios. Ejemplo: `Gral Paz` frente a la calle oficial local `PAZ GRL`.

Para evitar que esos casos caigan directamente a aproximacion por barrio, la busqueda de calles ahora considera equivalentes `Gral`, `Grl` y `General`. Ademas se actualizo la version de cache local del mapa para no reutilizar fallas anteriores del normalizador.

## Control territorial con barrios GeoJSON

Se incorporo una capa local inicial de barrios con centroides y algunos poligonos manuales. Esta capa no es oficial y no corrige viviendas, pero permite mejorar la visualizacion del mapa de riesgo territorial.

La regla del mapa queda:

1. Si la direccion resuelve por Georef, se usa la direccion.
2. Si la direccion no resuelve, el GPS solo se usa si no cae fuera del barrio declarado.
3. Si el GPS cae fuera del barrio, se descarta para esta vista y se usa el centroide del barrio.
4. Si no hay barrio territorial usable, la vivienda queda sin ubicar.

Este criterio evita que un GPS tomado desde otro lugar desplace una vivienda fuera del barrio real. Tambien hace visible el problema de calidad: el mapa informa cuando el punto mostrado es una aproximacion por barrio.
