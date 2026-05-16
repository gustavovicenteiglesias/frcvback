# DONE-019 - Ajuste de fórmula HTA/TA en dashboard

## Estado

DONE

## Contexto

La devolución epidemiológica indicó que el módulo de presión arterial estaba duplicando personas al contar por separado antecedente de HTA y TA alterada.

La regla correcta es jerárquica:

1. Primero se clasifican las personas con antecedente activo de HTA.
2. Esas personas ya quedan dentro del grupo HTA conocida.
3. Luego, entre quienes no tienen antecedente de HTA, se cuentan las personas con TA alterada.
4. Ese segundo grupo corresponde a captados o potenciales hipertensos nuevos.

## Cambio realizado

- `alertas-ta` deja de contar toda TA elevada y pasa a contar solo TA alterada en personas sin antecedente activo de HTA.
- Se agrega `hta-ta-total` como suma sin doble conteo:
  - antecedente de HTA;
  - TA alterada captada sin antecedente.
- El módulo de consultorio usa como denominador `hta-ta-total`.
- “Entraron al programa” en consultorio pasa a significar `entrega_medicacion = 1`.
- Se agrega “No aceptaron tratamiento” para personas del grupo HTA/TA alterada que fueron a consultorio pero no tienen entrega de medicación.
- Los modales de detalle reflejan la nueva fórmula.

## Archivos modificados

- `src/data/dashboard-epidemiologico.repo.ts`
- `src/pages/DashboardEpidemiologico.tsx`

## Validación

- `npx tsc --noEmit`
- `npm run build`

