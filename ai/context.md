# CONTEXTO DEL PROYECTO

## Proyecto
FRCV - Sistema de seguimiento de pacientes cardiovasculares con arquitectura offline-first (SQLite local + sincronización contra backend Spring/MySQL).

## Stack Principal
- Frontend: Ionic + React + TypeScript + SQLite
- Backend: Spring Boot
- DB/Otros: MySQL + SQLite sincronizado

## Estado y Foco Actual
El objetivo actual es extender el módulo ControlConsultorio para soportar:

1. Motivo por el cual NO recibe medicación
2. Eventos clínicos (selección múltiple)
3. Derivaciones (selección múltiple)

## Requerimiento funcional

### Motivo no medicación (simple)
- Contraindicación a la formulación
- HTA severa
- Sigue con su médico/a
- Otro

### Eventos (múltiple)
- muerte
- infarto
- stroke
- revascularización
- otro

### Derivaciones (múltiple)
- infarto
- stroke
- revascularización
- otro

## Estado actual del sistema

Backend actualizado:
- ControlConsultorio mantiene booleanos/observaciones legacy.
- Relaciones nuevas: `MotivoNoMedicacion` (1:1) y tablas dinámicas para eventos/derivaciones vía catálogos (`EventoConsultorio`, `DerivacionConsultorio`) + tablas intermedias (`control_consultorio_evento`, `control_consultorio_derivacion`) con `detalle` opcional.
- Servicio asigna UUID/lastModified/sqlDeleted a hijos y reemplaza sets en update; catálogos se gestionan aparte (no cascada).

Pendientes:
- SyncController ajustado (catálogos consultorio + puentes). Falta reflejo en frontend (db.ts/sync.ts/ UI).
Update SyncController (MySQL ↔ SQLite):
- Añadidos catálogos `motivo_no_medicacion`, `evento_catalogo`, `derivacion_catalogo`.
- Añadidas tablas puente `control_consultorio_evento`, `control_consultorio_derivacion` a pull/push y pull-full (DDL, índices, LM/SD).
- Cascada de baja lógica incluye puentes; booleano `activo` normalizado.

## Problema actual

El modelo actual no permite:
- múltiples eventos
- múltiples derivaciones
- categorización estructurada

## Objetivo técnico

Pasar de:
Boolean + observación

A:
modelo relacional con entidades + relaciones

## Ubicación del proyecto

- Backend:
C:\Users\Gustavo\OneDrive\Documentos\spring\frcv

- Frontend:
C:\Users\Gustavo\OneDrive\Documentos\Ionic\appfrcv-vite

## Componentes críticos

- db.ts → estructura SQLite
- sync.ts → sincronización
- SyncController → backend sync

## Restricción principal

TODO cambio debe mantenerse consistente en:

- MySQL
- SQLite
- Sync
- UI
