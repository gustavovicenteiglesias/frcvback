# TODO-006-dashboard-epidemiologico-medico

## Objetivo

Agregar una entrada de Dashboard Epidemiologico en la pantalla de seleccion de entidad, debajo de "Laboratorios del dia", visible solo para usuarios con rol MEDICO.

## Diagnostico

La pantalla actual muestra:
- Personas
- Viviendas
- Laboratorios del dia

La nueva opcion debe estar orientada a un/a profesional medico especialista epidemiologo/a, priorizando vigilancia poblacional, riesgo territorial, seguimiento y calidad de datos.

## Alcance UI

- Agregar un boton/tarjeta debajo de "Laboratorios del dia".
- Visible solo si el usuario tiene rol `MEDICO`.
- Texto sugerido:
  - Titulo: `Dashboard epidemiologico`
  - Subtitulo: `Indicadores poblacionales, riesgo y calidad de datos`
- Icono sugerido:
  - `analyticsOutline`, `pulseOutline`, `statsChartOutline` o similar.
- Navegar a una nueva ruta, por ejemplo:
  - `/dashboard-epidemiologico`

## Alcance Dashboard

Primera version recomendada:

- Indicadores generales:
  - personas cargadas
  - viviendas cargadas
  - personas en programa
  - controles domiciliarios
  - controles consultorio
  - laboratorios cargados

- Riesgo clinico:
  - HTA
  - DBT
  - dislipemia
  - ECV
  - ERC
  - alertas de TA/laboratorio

- Cohortes accionables:
  - HTA sin tratamiento
  - DBT sin tratamiento
  - dislipemia sin tratamiento
  - en programa sin laboratorio
  - acepta laboratorio sin resultado
  - sin control reciente

- Calidad de datos:
  - personas sin DNI
  - personas sin fecha de nacimiento
  - viviendas sin barrio/CAPS
  - controles sin TA
  - laboratorios incompletos

## Alcance Datos

- Reutilizar SQLite local.
- Crear queries en repositorio nuevo o existente, manteniendo separacion clara:
  - opcion sugerida: `src/data/dashboard-epidemiologico.repo.ts`
- No agregar dependencias nuevas.
- Mantener todos los conteos filtrando `sql_deleted=0`.

## Alcance Frontend

- Crear pagina:
  - `src/pages/DashboardEpidemiologico.tsx`
- Agregar ruta en `App.tsx`.
- Agregar tarjeta/boton en `SeleccionEntidad.tsx`.
- Usar `AuthService.getCurrentUser()` para validar rol `MEDICO`, consistente con el resto del proyecto.

## Mejoras opcionales

- Filtros por fecha desde/hasta.
- Filtro por barrio.
- Filtro por CAPS.
- Exportar CSV de cohortes.
- Semaforos por prioridad.

## Criterios de aceptacion

- Usuario MEDICO ve el boton debajo de Laboratorios del dia.
- Usuario sin MEDICO no ve el boton.
- Al tocar el boton navega al dashboard.
- El dashboard carga indicadores desde SQLite local.
- No rompe las pantallas existentes de seleccion, personas, viviendas ni laboratorios.

## Estado

DONE



