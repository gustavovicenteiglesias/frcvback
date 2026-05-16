# DONE-020 - Administración segura de usuarios y roles

## Estado

DONE

## Contexto

El tablero de administración ya permite listar usuarios y modificar roles. Falta cerrar controles de seguridad mínimos para evitar errores operativos.

## Objetivo

Completar la gestión administrativa de usuarios y roles sin comprometer el acceso al sistema.

## Alcance

- Confirmar cambios de roles con un modal claro.
- Evitar que un administrador se quite a sí mismo el rol `ADMIN`.
- Permitir activar/desactivar usuarios si el backend lo soporta o agregar endpoint mínimo.
- Mostrar último login y responsable asociado de forma clara.
- Agregar búsqueda/filtro por email, nombre, rol y estado.
- Validar que los roles disponibles vengan del backend.

## Fuera de alcance

- Crear nuevos códigos de rol desde la app.
- Cambiar reglas clínicas: `MEDICO` sigue siendo rol clínico sensible.
- Modificar sincronización.

## Criterios de aceptación

- Un admin puede cambiar roles de otros usuarios con confirmación.
- Un admin no puede dejarse a sí mismo sin rol `ADMIN`.
- Los usuarios inactivos quedan visibles como inactivos.
- La pantalla permite buscar y filtrar usuarios.
- Se mantiene acceso solo online y solo `ADMIN`.

## Resultado

- Se agregó confirmación antes de modificar roles.
- Se agregó defensa en backend y frontend para impedir que un admin se quite su propio rol `ADMIN`.
- Se agregó endpoint para activar/desactivar usuarios.
- Se agregó defensa en backend y frontend para impedir que un admin se desactive a sí mismo.
- Se agregaron filtros por texto, rol y estado.
- Se muestra último login, responsable vinculado, estado e indicador de usuario actual.

## Validación

- Frontend: `npx tsc --noEmit`
- Frontend: `npm run build`
- Backend: `mvnw.cmd -q -DskipTests compile`
