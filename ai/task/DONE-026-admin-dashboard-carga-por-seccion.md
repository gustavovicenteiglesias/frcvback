# DONE-026 - Carga por sección en tablero de administración

## Estado

DONE

## Contexto

El tablero de administración ya está separado por secciones, pero sigue consultando todos los datos al cargar la pantalla. Algunas secciones pueden ser pesadas: auditoría, sincronización, usuarios y catálogos.

## Objetivo

Reducir carga inicial y mejorar la experiencia en móvil cargando datos según la sección activa.

## Alcance

- Mantener las mismas secciones.
- Cargar `summary` para resumen, calidad y tablas.
- Cargar usuarios/roles solo en la sección usuarios.
- Cargar auditoría solo en la sección auditoría.
- Cargar estado de sync solo en la sección sync.
- Cargar catálogos solo en la sección catálogos.
- Mantener refresco manual.

## Criterios de aceptación

- La primera carga del tablero es más liviana.
- Al cambiar de sección se cargan los datos necesarios.
- El botón refrescar actualiza la sección activa.
- No se rompen filtros ni exportaciones.
- El build del frontend pasa.

## Resultado

- La carga inicial consulta solo el resumen.
- Cada sección carga sus datos al entrar:
  - usuarios/roles;
  - auditoría;
  - sincronización;
  - catálogos.
- El botón refrescar actualiza la sección activa.
- El filtro de auditoría se aplica con el botón `Aplicar`, sin consultar por cada cambio de fecha.
- Se agregó indicador visual de actualización por sección.

## Validación

- Frontend: `npm run build`.
