# TODO-011-vista-evolucion-persona

## Objetivo

Crear una vista de evolucion individual de persona orientada al medico de consultorio y al seguimiento del tratamiento/farmacos.

## Diagnostico

La informacion de controles y laboratorios existe, pero esta distribuida en pestañas. Para evaluar evolucion clinica hay que comparar manualmente fechas, TA, laboratorios, medicacion y programa. Se necesita una pantalla longitudinal que resuma primer valor, ultimo valor, cambios y eventos relevantes.

## Alcance primera version

- Crear pagina /persona/:id/evolucion.
- Agregar acceso desde el detalle de persona.
- Crear repositorio SQLite src/data/persona-evolucion.repo.ts.
- Mostrar resumen actual: ultimo control, ultima TA, ultimo laboratorio, en programa, alertas.
- Mostrar comparacion primer vs ultimo valor para TA y laboratorios principales.
- Mostrar timeline unificado de visitas, controles y laboratorios.
- No agregar dependencias nuevas.

## Criterios de aceptacion

- Desde una persona se puede abrir la vista Evolucion.
- La vista carga datos desde SQLite local.
- Se excluyen registros con sql_deleted = 0 segun corresponda.
- TypeScript compila.

## Estado

DONE

## Verificacion

- npx tsc --noEmit: OK.
- npm run build: OK ejecutado fuera del sandbox tras spawn EPERM interno.


