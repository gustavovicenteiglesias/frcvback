# DONE-041 - Dashboard: KPI cobertura programa

## Objetivo

Agregar al dashboard epidemiologico un KPI de cobertura del programa con denominador fijo 2275 y numerador de personas en programa (`control_domicilio.accede_programa = 1`).

## Implementado

- Se agrego una tarjeta KPI de cobertura en el dashboard epidemiologico.
- Numerador: indicador existente `programa`, personas activas con al menos un control domiciliario activo donde `accede_programa = 1`.
- Denominador fijo: 2275.
- Se muestra porcentaje redondeado y relacion numerador/denominador.
- Se agrego nota pequeña aclarando que 2275 corresponde a mayores de 40 de los barrios definidos por el usuario.
- La tarjeta es clickeable y abre el detalle existente de personas en programa.

## Verificacion

- `npm run build` ejecutado correctamente.
- Advertencia conocida de Vite por tamano de chunks, sin falla de compilacion.
