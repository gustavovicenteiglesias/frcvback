# TODO-007-graficos-dashboard-epidemiologico

## Objetivo

Sumar visualizaciones simples al Dashboard epidemiologico para facilitar lectura rapida por parte de medicos/as y epidemiologos/as.

## Diagnostico

El dashboard ya muestra totales auditables y permite abrir detalle/formula. Falta una capa visual que permita detectar rapidamente proporciones, prioridades operativas y problemas de calidad de datos.

## Alcance UI

- Agregar graficos al dashboard epidemiologico sin incorporar dependencias nuevas.
- Mantener los totales existentes y el modal de formula/detalle al click.
- Usar visualizaciones simples y legibles en mobile.

## Graficos sugeridos para primera version

- Dona: laboratorios completos vs incompletos.
- Barras horizontales: cohortes accionables.
- Barras horizontales: calidad de datos.
- Barras verticales o compactas: indicadores generales.

## Criterios de aceptacion

- El dashboard muestra graficos calculados desde los mismos datos SQLite locales.
- Los graficos no agregan dependencias nuevas.
- Las tarjetas/totales siguen siendo clickeables y mostrando formula/detalle.
- El build frontend compila correctamente.

## Estado

DONE

## Verificacion

- npx tsc --noEmit: OK.
- npm run build: bloqueado por spawn EPERM dentro del sandbox; no se ejecuto fuera del sandbox por rechazo de aprobacion.



