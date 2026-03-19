# AGENTS.md

Antes de realizar cualquier acción, debes leer obligatoriamente:

- ai/context.md
- ai/decisions.md
- ai/project-map.md
- ai/workflow.md
- ai/tasks/

## Regla principal
NO empieces a codear directamente.

Primero:
1. Identificar la tarea en /ai/tasks/ con estado TODO
2. Cambiarla a DOING
3. Explicar el plan antes de implementar

## Alcance actual
El foco está en:
- Extender ControlConsultorio
- Mantener sincronización MySQL ↔ SQLite
- No romper la lógica offline-first

## Restricciones críticas
- NO modificar estructura base del proyecto
- NO romper sync existente
- NO inventar endpoints nuevos innecesarios
- TODO cambio debe reflejarse en:
  - Backend (Spring)
  - SQLite (db.ts)
  - Sync (sync.ts + SyncController)