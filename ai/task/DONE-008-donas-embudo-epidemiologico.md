# TODO-008-donas-embudo-epidemiologico

## Objetivo

Sumar donas epidemiologicas al Dashboard epidemiologico para mostrar embudos de visita, deteccion de riesgo y aceptacion de tratamiento/programa.

## Diagnostico

El dashboard ya muestra indicadores y graficos generales. Falta visualizar, para medicos/as y epidemiologos/as, la relacion entre personas visitadas, aceptacion del programa/tratamiento, deteccion de factores de riesgo y aceptacion entre personas con riesgo detectado.

## Alcance UI

- Agregar donas en Dashboard epidemiologico.
- Mantener comportamiento auditable: al click debe mostrar formula y detalle cuando aplique.
- No agregar dependencias nuevas.

## Definiciones operativas

- Persona visitada: persona activa con al menos una visita activa.
- Acepto tratamiento/programa: persona visitada con al menos un control domiciliario activo con accede_programa = 1.
- Riesgo detectado: persona visitada con TA elevada en control activo o antecedente activo de HTA/DBT/dislipemia/ECV/ERC/tabaquismo.
- Riesgo detectado y acepto: persona visitada con riesgo detectado y accede_programa = 1.

## Graficos

- Dona: personas visitadas que aceptaron tratamiento/programa vs no aceptaron/no consta.
- Dona: personas visitadas con riesgo detectado vs sin riesgo detectado/no registrado.
- Dona: entre personas con riesgo detectado, aceptaron tratamiento/programa vs no aceptaron/no consta.

## Criterios de aceptacion

- Las donas se calculan desde SQLite local.
- Los conteos excluyen sql_deleted = 0 segun corresponda.
- Las donas abren formula y detalle al tocar los segmentos.
- TypeScript compila sin errores.

## Estado

DONE

## Verificacion

- npx tsc --noEmit: OK.
- npm run build: bloqueado por spawn EPERM dentro del sandbox; no se ejecuto fuera del sandbox por rechazo de aprobacion.


