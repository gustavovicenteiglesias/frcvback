# Roles y permisos

El sistema utiliza roles para mostrar u ocultar funcionalidades sensibles. La regla principal es separar el rol técnico del rol sanitario: `ADMIN` no equivale a `MEDICO` por defecto.

## Roles principales

- `ADMIN`: administración técnica del sistema, sincronización, configuración y soporte.
- `MEDICO`: revisión clínica, seguimiento terapéutico, análisis epidemiológico y acciones sensibles sobre datos clínicos.
- `ENFERMERO`: carga operativa en territorio, viviendas, personas, antecedentes y controles domiciliarios.

## Funcionalidades por rol

| Funcionalidad | ADMIN | MEDICO | ENFERMERO |
| --- | --- | --- | --- |
| Ingresar al panel de trabajo | Sí | Sí | Sí |
| Cargar viviendas/personas | Sí | Sí | Sí |
| Cargar controles domiciliarios | Sí | Sí | Sí |
| Cargar controles de consultorio | Según flujo | Sí | Según flujo |
| Ver Dashboard epidemiológico | No por defecto | Sí | No |
| Ver Evolución individual | No por defecto | Sí | No |
| Borrar personas/controles/laboratorios | No por defecto | Sí | No |
| Importación full / sincronización | Sí | Sí | Sí |

## Criterio de seguridad funcional

Las vistas de análisis clínico y epidemiológico se restringen a `MEDICO` porque pueden contener información sensible o interpretaciones clínicas. Si una persona administradora también debe revisar información clínica, debe tener además el rol `MEDICO`.

## Criterio de calidad del dato

La carga operativa debe quedar asociada al usuario real y al responsable sanitario correspondiente. En visitas domiciliarias, además, se intenta registrar la ubicación GPS de la visita para distinguir datos tomados en territorio de datos cargados posteriormente.

La importación full queda disponible para todos los perfiles porque es el mecanismo vigente de sincronización de la base local. No se considera una acción clínica sensible, sino una operación necesaria para trabajar con datos actualizados.

## Restricciones implementadas recientemente

- Dashboard epidemiológico visible solo para `MEDICO`.
- Evolución individual visible solo para `MEDICO`.
- Borrado lógico visible solo para `MEDICO`.
- `ADMIN` no hereda permisos clínicos automáticamente.
- Acceso directo a evolución redirige si el usuario no tiene rol `MEDICO`.
