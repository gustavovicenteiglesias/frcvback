# TODO-013-rediseño-modular-dashboard-epidemiologico

## Objetivo

Rediseñar la parte gráfica del Dashboard epidemiológico como un tablero modular tipo informe, inspirado en módulos operativos y clínicos, cuidando compatibilidad con modo claro y modo oscuro.

## Diagnóstico

El dashboard actual contiene indicadores, donas y barras, pero la lectura visual puede mejorar si se organiza en módulos epidemiológicos: visitas domiciliarias, presión arterial, consultorio/seguimiento y laboratorio. El diseño debe ser claro, responsive y auditable.

## Alcance

- Rediseñar la sección gráfica de DashboardEpidemiologico.
- Usar módulos grandes con bordes suaves y estructura tipo informe.
- Mantener detalle al click, fórmula y exportación CSV.
- Usar variables de Ionic y estilos compatibles con dark mode.
- No agregar dependencias nuevas.
- Agregar métricas SQLite necesarias para los módulos.

## Módulos esperados

- Módulo 1: Visitas domiciliarias.
- Módulo 2: Presión arterial.
- Módulo 3: Consultorio / seguimiento.
- Módulo 4: Laboratorio.

## Criterios de aceptación

- El dashboard muestra los módulos visuales en grilla de 2 columnas en pantallas amplias y 1 columna en móvil.
- El diseño se adapta al modo oscuro.
- Los indicadores principales siguen siendo clickeables y auditables.
- TypeScript compila.

## Estado

DONE

## Verificacion

- npx tsc --noEmit: OK.
- npm run build: OK ejecutado fuera del sandbox tras error de acceso a vite.config.ts en sandbox.


