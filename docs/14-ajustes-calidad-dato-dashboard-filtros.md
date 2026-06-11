# Calidad del dato: ajustes de filtros y dashboard

Fecha: 2026-06-04

## Alcance

Se aplicaron cambios de front y consultas locales sin modificar schema, sincronizacion ni contrato con backend, para mantener compatibilidad con dispositivos Android ya instalados.

## Personas

- El buscador de personas contempla barrio y CAPS asociados a la vivienda.
- El filtro "Con antecedentes" pasa a representar "Con antecedente HTA".
- Se elimina de la pantalla el filtro confuso "No acepta pero en tratamiento".
- Se agrega el filtro "En tratamiento POLILEP", calculado por controles de consultorio con `entrega_medicacion = 1`.
- Se agrega el filtro/indicador "TA alta detectada", para personas sin antecedente HTA declarado y con TA elevada registrada.
- En el listado de controles se muestra la fecha sanitaria del control cuando existe, usando la fecha de visita solo como respaldo.

## Viviendas

- La pantalla conserva filtro por barrio.
- Se elimina de la vista el filtro "Con tratamiento".
- "Solo accedidas" queda desactivado por defecto.
- El contador distingue viviendas visibles segun filtros y total general cargado.

## Dashboard epidemiologico

- Se reemplaza "Entraron al programa" por "En tratamiento POLILEP".
- El bloque de presion arterial queda rotulado como "Domicilio - Presion arterial".
- "TA alterada captada" se muestra como "TA alta detectada".
- Se agrega el indicador "HTA/TA alta sin consultorio".
- La seccion "Cohortes accionables" pasa a llamarse "Personas con antecedentes sin tratamiento".
- Se ocultan de esa seccion indicadores que confundian la lectura operativa: en programa sin laboratorio, acepta laboratorio sin resultado y sin control reciente.

## Pendiente deliberado

- No se agrego categoria "No aplica" en visitas porque falta confirmar que valor de base representa esa situacion.
- No se hicieron cambios de compatibilidad, migraciones ni nuevas columnas.
- Motivos de no entrega de medicacion y eventos reportados quedan para una segunda pasada de dashboard.

## Actualizacion 2026-06-04 - Visitas No aplica

Se incorpora la categoria No aplica al bloque de visitas domiciliarias del dashboard. El dato proviene de viviendas.motivo = 'NO_APLICA', ya contemplado por backend, MySQL, SQLite y el formulario de vivienda. No requiere cambios de compatibilidad, schema ni sync.


## KPI de cobertura del programa

Se agrego al dashboard epidemiologico un KPI de cobertura del programa.

- Numerador: personas activas con al menos un control domiciliario activo donde `accede_programa = 1`.
- Denominador fijo: 2275.
- Formula visual: `personas en programa / 2275`.
- El porcentaje se muestra redondeado.

Aclaracion institucional del denominador: 2275 corresponde a mayores de 40 de los barrios Canuglio, Don Pancho, Amespil, Alborada, Duggan, Municipal, Barrio Prado, Ex feria, Bomberos, 23 de octubre, Esperanza, Alberdi, Orofino, Plan Federal, Minca, 18 viviendas, Manuela y Cementerio.
