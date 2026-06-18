# DONE-042 - Programa por TA domiciliaria en dashboard y personas

Estado: DONE
Fecha: 2026-06-11

## Objetivo

Ajustar el concepto operativo de "persona en programa" para que no dependa del checkbox `accede_programa`, sino de una evidencia registrada: al menos una TA tomada en control domiciliario activo.

## Cambios realizados

- Dashboard epidemiologico: el KPI de cobertura usa como numerador las personas con control domiciliario activo y TA sistolica o diastolica registrada.
- Dashboard epidemiologico: el indicador general `programa` se renombro visualmente a "Personas en programa" y su detalle lista las personas con TA domiciliaria registrada.
- Pantalla `/personas`: el filtro visible "Solo en programa" conserva la etiqueta solicitada, pero internamente filtra por TA tomada en domicilio.
- Reporte desde filtros de personas: mantiene el mismo criterio que la pantalla para no exportar una poblacion distinta.
- Dashboard: el modulo de visitas domiciliarias muestra el total de viviendas del bloque.
- Dashboard: la seccion de riesgo clinico pasa a titularse "Antecedentes de riesgo cardiovascular".

## Verificacion

- `npm run build` ejecutado correctamente.

### Ajuste posterior: TA registrada en cualquier control

Se corrigio el alcance del criterio para evitar discrepancias entre el KPI de cobertura y el bloque de presion arterial del dashboard. El criterio final de "persona en programa" para dashboard, filtro `/personas` y exportacion desde filtros es: persona activa con al menos una TA sistolica o diastolica registrada en un control activo, sea domiciliario o de consultorio.

Motivo: el conteo anterior tomaba solo control domiciliario y dejaba fuera personas que ya tenian TA registrada en consultorio, produciendo diferencia contra el total evaluado de presion arterial.
