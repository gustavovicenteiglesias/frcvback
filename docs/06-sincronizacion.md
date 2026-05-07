# Sincronización

La sincronización conecta la base local SQLite con el backend Spring Boot y MySQL.

## Flujo simplificado

```mermaid
sequenceDiagram
  participant App as App Ionic
  participant SQLite as SQLite local
  participant Sync as SyncController
  participant MySQL as MySQL

  App->>SQLite: Guarda cambios locales
  App->>SQLite: Genera cambios pendientes
  App->>Sync: Push de cambios
  Sync->>MySQL: Persiste cambios
  App->>Sync: Pull o Pull full
  Sync->>MySQL: Consulta datos actualizados
  Sync->>App: Devuelve tablas
  App->>SQLite: Importa datos
```

## Tablas sincronizadas

El sistema sincroniza entidades principales:

- Viviendas.
- Personas.
- Responsables.
- Visitas.
- Controles domiciliarios.
- Controles consultorio.
- Antecedentes.
- Medicación HTA.
- Laboratorios.
- Catálogos.
- Eventos y derivaciones de consultorio.

## Baja lógica

La baja lógica se maneja con `sql_deleted`.

- `sql_deleted = 0`: registro activo.
- `sql_deleted = 1`: registro eliminado lógicamente.

Esto permite sincronizar eliminaciones sin borrar físicamente en todos los dispositivos de forma inmediata.

## Reglas técnicas

- No agregar tablas sin actualizar backend, SQLite y sync.
- Mantener `last_modified` para resolver sincronización.
- Evitar cambios destructivos en esquemas existentes.
- Mantener compatibilidad con trabajo offline.
