# DONE-030 - Calidad del dato territorial visible y auditable

## Estado

DONE

## Contexto

Se agregaron campos de calidad territorial en viviendas:

- `gps_accuracy`
- `gps_captured_at`
- `ubicacion_fuente`
- `confianza_dato`

El sync ya debe conservar esos campos y el `pull-full` debe mandarlos en schema y valores. Falta convertir esa informacion en controles visibles, filtros y auditoria operativa.

## Objetivo

Hacer que la calidad del dato territorial sea visible para usuarios y administracion, evitando que coordenadas dudosas se mezclen sin advertencia con datos confiables.

## Alcance

- Mostrar semaforo de confianza en detalle/listado de viviendas cuando corresponda.
- Mostrar advertencias por vivienda con dato territorial pendiente o no usable.
- Auditar coordenadas repetidas en muchas viviendas.
- Auditar viviendas sin barrio, sin direccion valida, sin coordenadas o con `S/N`.
- Auditar precision GPS baja cuando `gps_accuracy` supere el umbral definido.
- Auditar fuente de ubicacion: `gps`, `manual`, `estimada`, `sin_dato`.
- Permitir filtrar/exportar datos territoriales por confianza: `alta`, `media`, `baja`, `no_usable`.
- En mapas y reportes, diferenciar visualmente datos confiables de datos dudosos.

## Fuera de alcance

- Borrar datos de baja confianza automaticamente.
- Cambiar reglas clinicas o epidemiologicas.
- Reprocesar masivamente direcciones historicas sin validacion previa.

## Criterios de aceptacion

- Una vivienda con baja confianza o no usable queda claramente marcada en UI.
- Los reportes/mapas pueden excluir o diferenciar datos `baja` y `no_usable`.
- Administracion puede listar casos accionables de mala calidad territorial.
- Las coordenadas repetidas quedan disponibles para auditoria.
- Los casos `S/N` quedan identificados para seguimiento desde origen.
- El sync sigue funcionando con dispositivos viejos y nuevos.

## Notas

- El dato de baja confianza no se elimina: puede servir para auditoria y mejora del proceso.
- El criterio debe evitar falsa confianza en mapas territoriales.

## Resultado

- El listado de viviendas muestra semaforo de confianza, fuente de ubicacion y precision GPS cuando existe.
- El listado de viviendas permite filtrar por `alta`, `media`, `baja` y `no_usable`.
- La exportacion CSV de viviendas incluye los campos territoriales de calidad y respeta el filtro de confianza.
- El tablero admin agrega indicadores exportables para:
  - viviendas de baja confianza;
  - viviendas no usables;
  - coordenadas repetidas en varias viviendas;
  - direcciones `S/N`;
  - GPS con precision mayor a 100 metros;
  - fuente de ubicacion `sin_dato` o `estimada`.
- El mapa territorial incorpora `gps_accuracy`, `ubicacion_fuente` y `confianza_dato` en los puntos y popups.

## Validacion

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
