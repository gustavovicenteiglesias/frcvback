# DONE-031 - Control de acceso para usuarios sin rol

## Estado

DONE

## Contexto

Actualmente todos los roles pueden importar y exportar desde la pantalla `/home`. Ademas, la app permite que una persona se autentique y quede creada en la base por el flujo de autousuario.

El riesgo es que alguien descubra el link, inicie sesion y termine con acceso operativo si el sistema le asigna rol automaticamente o si el frontend no bloquea correctamente a usuarios sin permisos.

## Objetivo

Permitir que el usuario autenticado quede registrado en la base, pero sin otorgarle permisos operativos hasta que un administrador le asigne un rol.

## Alcance

- Revisar el flujo de login/autousuario para que no asigne roles automaticamente.
- Definir estado inicial seguro para usuarios nuevos:
  - opcion preferida: usuario sin rol;
  - opcion alternativa: rol `INVITADO` sin permisos operativos.
- Bloquear navegacion operativa en frontend para usuarios sin rol o con rol `INVITADO`.
- Mostrar una pantalla clara: "Comunicate con el administrador del sistema para habilitar tu acceso".
- Impedir importar/exportar si el usuario no tiene rol autorizado.
- Revisar permisos de importacion/exportacion en `/home`.
- Asegurar que backend rechace operaciones sensibles aunque el frontend falle.
- Mantener visible en administracion el listado de usuarios sin rol o invitados para asignacion manual.

## Fuera de alcance

- Alta manual de usuarios antes del primer login.
- Integracion con otro proveedor de identidad.
- Cambiar roles clinicos existentes.

## Criterios de aceptacion

- Un usuario nuevo autenticado queda guardado en base, pero sin permisos operativos.
- Un usuario sin rol o `INVITADO` ve solo la pantalla de espera/contacto con administrador.
- Un usuario sin rol o `INVITADO` no puede importar, exportar, sincronizar ni acceder a pantallas clinicas.
- El backend valida permisos y no depende solo del frontend.
- Administracion puede identificar facilmente usuarios pendientes de rol.
- Los roles existentes siguen funcionando sin cambios inesperados.

## Notas

- Si se crea `INVITADO`, debe ser un rol explicito sin permisos de negocio.
- La regla principal es: autenticacion no equivale a autorizacion.

## Resultado

- Se elimino la asignacion automatica de rol `ENFERMERO` en el login/autousuario.
- Los usuarios nuevos quedan guardados en base con `roles: []` hasta asignacion administrativa.
- Se restringio `/api/**` en backend a `ADMIN`, `MEDICO` o `ENFERMERO`.
- Se agrego una pantalla de acceso pendiente para usuarios autenticados sin rol operativo.
- Se agrego guarda de rutas operativas en frontend para bloquear acceso directo por URL.
- En `/home`, usuarios sin rol no ven continuar, importar ni exportar.

## Validacion

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
