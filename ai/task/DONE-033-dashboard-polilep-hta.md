# DONE-033 - Dashboard POLILEP, HTA y seguimiento

Estado: DONE
Fecha: 2026-06-04

## Objetivo

Ajustar el lenguaje y los indicadores del dashboard para diferenciar antecedente declarado, hallazgo detectado y tratamiento efectivo.

## Cambios realizados

- Se reemplaza "Entraron al programa" por "En tratamiento POLILEP".
- El bloque de presion arterial queda como "Domicilio - Presion arterial".
- "TA alterada captada" pasa a mostrarse como "TA alta detectada".
- Se agrega el indicador "HTA/TA alta sin consultorio".
- Se quitan los rotulos "Modulo 1", "Modulo 2", "Modulo 3" y "Modulo 4".
- La seccion "Cohortes accionables" pasa a llamarse "Personas con antecedentes sin tratamiento".
- Se ocultan temporalmente indicadores que confundian la lectura operativa:
  - En programa sin laboratorio.
  - Acepta laboratorio sin resultado.
  - Sin control reciente.

## Pendiente

- Confirmar como representar "No aplica" en visitas antes de agregarlo.
- Agregar visualizaciones de motivos de no entrega de medicacion.
- Agregar visualizaciones de eventos reportados.

## Verificacion

`npm run build` ejecutado correctamente.

