# Resumen para revisión de últimos cambios

Hola, comparto un resumen breve de los últimos cambios realizados en la aplicación FRCV para que puedan revisarlos funcionalmente.

## Objetivo general

La app quedó más orientada al uso real de cada rol: agente sanitario, médico/a y perfil epidemiológico. Además de la carga de datos, ahora suma herramientas para seguimiento clínico, análisis poblacional, calidad de datos y exportaciones.

## Cambios principales

### Dashboard epidemiológico

- Se agregó un **Dashboard epidemiológico** visible solo para usuarios con rol `MEDICO`.
- Incluye indicadores generales, riesgo clínico, cohortes accionables y calidad de datos.
- Se agregaron gráficos de barras y donas para lectura rápida.
- Cada indicador permite ver:
  - fórmula de cálculo;
  - detalle de registros incluidos;
  - exportación CSV del detalle.

### Evolución individual de persona

- Se agregó la pestaña **Evolución** en el detalle de persona, visible solo para rol `MEDICO`.
- Muestra:
  - último control;
  - última TA;
  - último laboratorio;
  - estado en programa;
  - aceptación de laboratorio;
  - medicación registrada;
  - comparación entre primer y último valor;
  - timeline de visitas y laboratorios.

### Exportaciones

- Se agregó exportación CSV desde los modales del dashboard.
- Se creó una pantalla de **Exportación personalizada** tipo sábana epidemiológica.
- Permite exportar por:
  - viviendas;
  - personas;
  - controles;
  - laboratorios;
  - búsqueda por DNI.
- Incluye rango de fechas y accesos rápidos como hoy, últimos 7 días, últimos 30 días, este mes o todo.

### Lista de personas

- Se mejoró el rendimiento de `/personas`.
- Ahora carga de a 10 registros con scroll infinito.
- Se mantienen búsqueda, filtros y exportación.

### Documentación

- Se creó un `README.md` general.
- Se agregó carpeta `docs/` con documentación funcional y técnica.
- La documentación está organizada por roles y procesos:
  - operación del agente sanitario;
  - operación médica;
  - dashboard epidemiológico;
  - exportaciones;
  - sincronización;
  - modelo de datos;
  - roles y permisos.

Me gustó mucho cómo quedó la explicación del uso de la app por roles. Los diagramas ayudan a entender rápidamente el circuito del agente sanitario, el flujo médico, la sincronización offline-first y el sentido del dashboard epidemiológico.

## Puntos sugeridos para revisar

1. Ingresar con usuario médico y verificar que aparezcan:
   - Dashboard epidemiológico;
   - pestaña Evolución en persona.

2. Ingresar con usuario no médico y verificar que:
   - no aparezca Dashboard epidemiológico;
   - no aparezca Evolución.

3. Revisar el dashboard:
   - abrir indicadores;
   - validar fórmula y detalle;
   - probar exportación CSV.

4. Revisar una persona con varios controles y laboratorios:
   - verificar la pestaña Evolución;
   - revisar primer vs último valor;
   - revisar timeline.

5. Probar exportación personalizada:
   - por viviendas;
   - por personas;
   - por controles;
   - por laboratorios;
   - por DNI;
   - con y sin rango de fechas.

6. Revisar `/personas`:
   - scroll infinito;
   - filtros;
   - búsqueda;
   - navegación al detalle.

## Comentario final

La aplicación quedó más completa no solo como sistema de carga, sino también como herramienta de seguimiento clínico y análisis epidemiológico. La separación por roles y la documentación con diagramas hacen mucho más claro cómo se espera que cada perfil use el sistema.
