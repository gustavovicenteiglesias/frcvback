# TODO-014-mapa-viviendas-ta-hta

## Objetivo

Agregar una primera visualizacion territorial de viviendas con personas con TA alta o antecedente de HTA, usando coordenadas locales sin incorporar dependencias externas de mapas.

## Diagnostico

El dashboard epidemiologico necesita una lectura territorial del riesgo. Las viviendas tienen latitud/longitud, pero la precision puede variar por cantidad de decimales o por datos cargados como centroide/default. Se debe mostrar la precision de la coordenada para evitar interpretaciones incorrectas.

## Alcance primera version

- Crear repositorio SQLite para puntos territoriales.
- Crear pagina/componente de mapa territorial en el frontend.
- Agregar acceso desde Dashboard epidemiologico.
- Mostrar viviendas con al menos una persona con TA alta o antecedente HTA.
- Usar SVG/plano relativo, sin Google Maps, Leaflet ni nuevas dependencias.
- Clasificar precision de coordenadas: alta, media, baja/dudosa.
- Mostrar detalle al tocar un punto.
- Restringir uso al flujo del Dashboard epidemiologico, ya visible solo para MEDICO.

## Criterios de aceptacion

- El mapa muestra puntos con coordenadas validas.
- Los puntos se calculan desde SQLite local y excluyen sql_deleted = 0.
- Se ve la cantidad de viviendas mapeadas y las dudosas/sin coordenadas.
- Al tocar un punto se muestra detalle de vivienda y cantidad de personas con riesgo.
- TypeScript compila.

## Estado

DONE

## Verificacion

- npx tsc --noEmit: OK.
- npm run build: OK ejecutado fuera del sandbox tras error de acceso a vite.config.ts en sandbox.


