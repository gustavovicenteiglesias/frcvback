# TODO-010-exportacion-personalizada-sabana-epidemiologica

## Objetivo

Crear una primera pantalla de exportacion personalizada tipo sabana epidemiologica desde SQLite local.

## Diagnostico

Los equipos medicos/epidemiologicos necesitan exportar viviendas, personas, controles y laboratorios con relaciones aplanadas para analisis en planilla. La jerarquia elegida define que entidad se repite en la sabana.

## Alcance primera version

- Crear pantalla /exportacion-personalizada.
- Agregar acceso desde Dashboard epidemiologico.
- Permitir entidad raiz: viviendas, personas, controles, laboratorios y busqueda por DNI.
- Permitir rango de fechas desde/hasta.
- Exportar CSV sin dependencias nuevas.
- Usar columnas amplias fijas en esta primera version.

## Reglas operativas

- Raiz viviendas: una fila por vivienda + persona + visita/control/laboratorio relacionado.
- Raiz personas: una fila por persona + visita/control/laboratorio relacionado.
- Raiz controles: una fila por control con persona/vivienda/labs del dia si existen.
- Raiz laboratorios: una fila por resultado con persona/vivienda/visita si existe.
- DNI: devuelve historia relacionada de esa persona, con rango opcional.

## Criterios de aceptacion

- El usuario puede elegir tipo de sabana y rango de fechas.
- El usuario puede exportar CSV.
- La exportacion usa SQLite local y excluye sql_deleted = 0.
- TypeScript compila.

## Estado

DOING
