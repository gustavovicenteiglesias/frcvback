# TODO-004-borrado-logico-viviendas-personas

## Objetivo

Agregar borrado logico para viviendas y personas, disponible solo para usuarios con rol MEDICO.

Rol objetivo:
- MEDICO
- id de referencia informado: 22222222-2222-2222-2222-222222222222

## Diagnostico

Actualmente existen funciones locales:
- `personaSoftDelete(id)`
- `viviendaSoftDelete(id)`

Pero ambas hacen baja directa sobre la entidad principal y no garantizan cascada local completa.

En backend `SyncController` ya propaga baja logica cuando una persona llega con `sql_deleted=1`, pero:
- no hay cascada equivalente desde viviendas
- la cascada actual de personas debe revisarse para limitar updates al conjunto recibido
- los endpoints DELETE de persona/vivienda hacen baja directa sin cascada

## Alcance Frontend

- Mostrar accion de borrado para viviendas y personas solo si el usuario tiene rol MEDICO.
- Agregar confirmacion antes de borrar.
- Implementar baja logica local en cascada:
  - vivienda -> personas de la vivienda
  - persona -> visitas
  - visitas -> control_domicilio
  - visitas -> control_consultorio
  - control_consultorio -> control_consultorio_evento
  - control_consultorio -> control_consultorio_derivacion
  - persona -> antecedentes
  - antecedentes -> antecedente_has_medicacion_hta
  - persona -> lab_resultados
- Usar un mismo `last_modified` para todo el lote local.
- Refrescar listados/detalles luego de borrar.

## Alcance Backend / Sync

- Detectar viviendas marcadas como `sql_deleted=1` en `/api/sync/push`.
- Derivar desde viviendas borradas a sus personas y reutilizar/ajustar la cascada de personas.
- Revisar `cascadeSoftDeleteForPersonas` para evitar updates globales sobre registros ya borrados fuera del conjunto recibido.
- Confirmar que pull/push devuelvan todos los cambios necesarios para que otros dispositivos reciban la baja.

## Alcance SQLite / Sync Frontend

- Verificar que todas las tablas afectadas exporten cambios en `exportPartialJson`.
- Agregar `viviendas` al calculo de `setSyncDateFromMaxLastModified` si corresponde.
- Confirmar que los listados ya filtran `sql_deleted=0`.

## UI sugerida

- En detalle de persona/vivienda: boton visible de borrar.
- En listados: accion secundaria con icono de borrar si no genera riesgo de toque accidental.
- Mensaje para vivienda: "Tambien se borraran sus integrantes y datos dependientes".
- Mensaje para persona: "Tambien se borraran visitas, controles, antecedentes y laboratorios".

## Criterios de aceptacion

- Un MEDICO puede borrar logicamente una persona y desaparece de listados.
- Un MEDICO puede borrar logicamente una vivienda y desaparecen vivienda e integrantes de listados.
- Las tablas dependientes quedan con `sql_deleted=1` y `last_modified` actualizado.
- El push sincroniza la cascada al backend.
- Un pull posterior replica las bajas a otro dispositivo.
- Usuarios sin rol MEDICO no ven ni ejecutan la accion desde UI.

## Estado

DONE
