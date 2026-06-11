# Operación del agente sanitario

Esta guía describe el uso esperado de la aplicación para agentes sanitarios o personal de enfermería que realiza carga territorial.

## Objetivo operativo

Registrar viviendas, personas y controles domiciliarios durante el trabajo de campo, incluso sin conexión.

## Flujo general

El trabajo del agente sanitario se organiza en cuatro momentos principales: inicio, sincronización, carga territorial y cierre del lote de trabajo.

| Momento                   | Acción principal                                                                                                            | Recomendación operativa                                                                                                                                             |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1. Inicio                 | Ingresar a la app y revisar la pantalla Home.                                                                               | Antes de comenzar una jornada, verificar si hay datos pendientes o si corresponde actualizar la información.                                                        |
| 2. Sincronización inicial | Exportar si existen cambios locales pendientes e importar datos actualizados.                                               | Si aparece el botón **Exportar**, primero exportar. Luego importar. Si no aparece, igualmente importar si hay conexión para traer datos cargados por otros agentes. |
| 3. Carga territorial      | Seleccionar **Viviendas** o **Personas**, registrar o buscar vivienda, cargar persona, antecedentes y control domiciliario. | Asociar correctamente cada persona a su vivienda y registrar los datos del control aunque los valores sean normales.                                                |
| 4. Cierre del lote        | Guardar localmente y exportar al finalizar la ronda o bloque de carga.                                                      | No es necesario exportar después de cada cambio pequeño. Conviene trabajar por bloques y exportar el lote al terminar.                                              |

En resumen: **importar antes de empezar**, **trabajar localmente durante la carga** y **exportar al finalizar el lote**.

## Trabajo colaborativo y sincronización

La app no es una libreta individual: es una herramienta de trabajo colaborativo.

Cada agente carga datos en su dispositivo, pero esos datos forman parte de una misma base de trabajo. Por eso, antes de empezar una jornada, una ronda o una actualización importante, hay que verificar si corresponde importar datos del servidor.

### Regla práctica

* Si el botón **Exportar** aparece, primero hay datos locales pendientes de enviar. En ese caso, exportar antes de importar.
* Si el botón **Exportar** no aparece, no significa que ya esté todo actualizado. Puede significar que este dispositivo no tiene cambios locales pendientes. En ese caso, si hay conexión, importar para traer las cargas realizadas por otros agentes.
* Antes de salir a trabajar o antes de actualizar datos de personas ya cargadas, importar para trabajar con la información más reciente disponible.
* Al terminar una ronda de carga, exportar el lote de datos cargados.
* Si después de importar se corrigen o agregan datos, exportar nuevamente al terminar ese bloque de trabajo.

No se recomienda exportar después de cada cambio pequeño. La idea no es hacer una exportación por cada persona editada, sino trabajar por bloques: cargar una ronda, revisar que esté completa y exportar el lote.

### Ejemplos

* Empiezo el día sin cambios locales pendientes: importo, trabajo y al terminar la ronda exporto.
* Termino mi ronda en terreno: exporto para que el resto del equipo pueda ver esos datos.
* Necesito actualizar una vivienda o persona ya existente: primero importo, hago la corrección y luego exporto el lote corregido.
* Si trabajé sin conexión, sigo cargando localmente y exporto cuando vuelva a tener conexión.

El objetivo es evitar dos problemas: trabajar con información vieja y dejar datos cargados solo en un dispositivo. En un operativo colaborativo, cada carga tiene valor para todo el equipo.

## Datos principales

| Vivienda                      | Persona             | Control domiciliario |
| ----------------------------- | ------------------- | -------------------- |
| Barrio                        | DNI                 | Fecha de visita      |
| CAPS                          | Apellido y nombre   | TA sistólica         |
| Dirección                     | Sexo                | TA diastólica        |
| Manzana                       | Fecha de nacimiento | Accede al programa   |
| Casa                          | Teléfono            | Acepta laboratorio   |
| Fecha                         | Cobertura de salud  | Observaciones        |
| Motivo de acceso o no acceso  | Vivienda asociada   |                      |
| Ubicación, si está disponible |                     |                      |

## Recomendaciones de carga

* Cargar DNI siempre que sea posible.
* Completar fecha de nacimiento para permitir análisis por edad.
* Asociar la persona a una vivienda.
* Registrar TA aunque sea normal; no registrar solo los valores alterados.
* Usar observaciones para aclarar situaciones de rechazo, ausencia o dudas.

## Calidad de datos

El dashboard epidemiológico permite detectar:

* Personas sin DNI.
* Personas sin fecha de nacimiento.
* Viviendas sin barrio o CAPS.
* Controles sin TA.
* Laboratorios incompletos.

Estos indicadores ayudan a mejorar el trabajo territorial y evitar planillas incompletas.
