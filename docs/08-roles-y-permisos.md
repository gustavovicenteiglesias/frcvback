# Roles y permisos

El sistema utiliza roles para mostrar u ocultar funcionalidades sensibles.

## Roles principales

- `ADMIN`
- `MEDICO`
- `ENFERMERO`

## Funcionalidades por rol

| Funcionalidad | ADMIN | MEDICO | ENFERMERO |
| --- | --- | --- | --- |
| Ingresar al panel de trabajo | Sí | Sí | Sí |
| Cargar viviendas/personas | Sí | Sí | Sí |
| Cargar controles domiciliarios | Sí | Sí | Sí |
| Cargar controles de consultorio | Según flujo | Sí | Según flujo |
| Ver Dashboard epidemiológico | No por defecto | Sí | No |
| Ver Evolución individual | No por defecto | Sí | No |
| Borrar personas/controles/laboratorios | Según implementación | Sí | No |
| Importación full | Sí | No | No |

## Criterio de seguridad funcional

Las vistas de análisis clínico y epidemiológico se restringen a `MEDICO` porque pueden contener información sensible o interpretaciones clínicas.

## Restricciones implementadas recientemente

- Dashboard epidemiológico visible solo para `MEDICO`.
- Evolución individual visible solo para `MEDICO`.
- Acceso directo a evolución redirige si el usuario no tiene rol `MEDICO`.
