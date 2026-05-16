# DONE-025 - Mejorar UI/UX del tablero de administración

## Estado

DONE

## Contexto

El tablero de administración creció con varias secciones útiles: estado del sistema, calidad del dato, auditoría de carga, estado de sincronización, usuarios/roles y tablas sincronizadas. Funcionalmente está bien, pero en móvil obliga a hacer demasiado scroll.

## Objetivo

Reorganizar el tablero admin para que sea más navegable, especialmente en móvil, sin cambiar endpoints ni reglas de negocio.

## Alcance

- Separar el contenido en secciones navegables con tabs/segmentos.
- Mantener:
  - resumen;
  - calidad del dato;
  - auditoría;
  - sincronización;
  - usuarios y roles;
  - tablas sincronizadas.
- Reducir scroll inicial.
- Mantener exportaciones CSV y filtros.
- No cambiar permisos ni endpoints.

## Criterios de aceptación

- El usuario admin puede cambiar de sección sin recorrer toda la página.
- La primera vista queda compacta y entendible.
- Las listas largas quedan dentro de su sección.
- El diseño funciona en móvil y desktop.
- No se rompe el build del frontend.

## Resultado

- Se agregó navegación por secciones con `IonSegment`:
  - Resumen;
  - Calidad;
  - Auditoría;
  - Sync;
  - Usuarios;
  - Tablas.
- Cada bloque largo se renderiza solo cuando la sección está activa.
- El segmento queda visible arriba para cambiar de sección sin volver al inicio.
- Se mantuvieron filtros, exportaciones CSV y acciones existentes.

## Validación

- Frontend: `npm run build`.
