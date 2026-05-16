# TODO-015 - Mapa de análisis territorial con Georef y confianza de ubicación

## Objetivo

Construir una vista de mapa orientada a investigación epidemiológica y social, no a dirección postal exacta.

La pantalla debe permitir visualizar concentración territorial de viviendas/personas con TA alta o antecedente de HTA usando distintas fuentes de ubicación:

- GPS real cargado en la vivienda.
- Ubicación territorial recuperada o normalizada con Georef cuando haya conexión.
- Estimación local por barrio, CAPS, manzana/casa o centroides de viviendas cercanas ya geolocalizadas.
- Casos no ubicables para revisión de calidad de datos.

## Contexto

El mapa de coordenadas cargadas ya sirve para auditoría de carga de los agentes sanitarios. Debe conservarse.

El intento de mapa por domicilio exacto no alcanza porque muchas viviendas tienen direcciones operativas o barriales, por ejemplo `5 Alborada`, `casa 104`, `orofino y petrili`, que no necesariamente existen como dirección postal normalizable en OpenStreetMap.

Para investigación, lo importante es poder ubicar patrones espaciales con niveles explícitos de confianza.

## Criterios

- Mantener el mapa actual de coordenadas sin reemplazarlo.
- Reconvertir o reemplazar la pantalla de mapa por domicilio por una pantalla de análisis territorial.
- Usar Georef Argentina cuando haya conexión para:
  - geocodificación inversa de coordenadas válidas;
  - normalización de localidad/departamento/municipio;
  - eventual búsqueda de direcciones cuando el texto sea suficientemente postal.
- Cachear resultados para uso posterior sin conexión.
- Marcar visualmente fuente/confianza:
  - GPS real;
  - Georef;
  - estimado local;
  - pendiente/no ubicable.
- Evitar prometer exactitud cuando el dato solo permite aproximación.

## Notas

Georef API v2:

- Base: `https://apis.datos.gob.ar/georef/api/v2.0`
- Endpoints relevantes:
  - `/ubicacion`
  - `/direcciones`
  - `/localidades`
  - `/calles`

## Implementado

- Se conserva el mapa de coordenadas cargadas como herramienta de auditoría.
- Se reconvirtió el segundo mapa en una vista de análisis territorial.
- La vista clasifica puntos por fuente/confianza:
  - GPS real;
  - estimado por barrio;
  - estimado por CAPS;
  - sin ubicar.
- Los puntos estimados se calculan desde centroides de viviendas de riesgo con GPS válido en el mismo barrio o CAPS.
- Se agregó enriquecimiento opcional con Georef para recuperar contexto territorial cuando hay conexión.
- Los resultados de Georef se cachean en `localStorage`.
- Se validó con `npx tsc --noEmit` y `npm run build`.

## Estado

DONE
