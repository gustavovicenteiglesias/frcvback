# DONE-016 - Responsable y usuario real en visitas

## Problema

Las visitas se estaban creando con el último responsable activo o con `Responsable por defecto`.

Esto impedía auditar correctamente quién cargó cada visita, control, antecedente o laboratorio.

## Evidencia

En el dump revisado:

- 1191 visitas totales.
- 1181 visitas activas.
- 2 responsables.
- 850 visitas apuntaban a `Responsable por defecto`.
- 341 visitas apuntaban a `resp-1`.
- 1191 visitas tenían `created_by_user_id = NULL`.

## Diagnóstico

El backend ya devuelve `userId` en el login y el modelo ya permite relacionar `responsables.user_id` con `users.id`.

El problema principal estaba en el frontend:

- se buscaba un responsable genérico;
- no se usaba el usuario logueado;
- no se completaba `created_by_user_id`.

## Implementado en frontend

- Se agregó helper central para obtener o crear el responsable del usuario actual.
- Se prioriza responsable por `user_id`.
- Si no existe, se busca por email y se vincula al usuario actual.
- Si no existe nada, se crea responsable con `id = userId`, `user_id = userId`, nombre y email del login.
- Las nuevas visitas guardan:
  - `responsable_id` del usuario actual;
  - `created_by_user_id` del usuario actual.
- Controles, antecedentes nuevos y laboratorios también completan `created_by_user_id`.

## Back

No requirió cambio inmediato porque:

- `AuthService` ya devuelve `userId`;
- `Responsable` ya tiene relación con `UserEntity`;
- `Visita` ya tiene `responsable` y `createdBy`;
- sync ya contempla `responsables.user_id` y `visitas.created_by_user_id`.

## Pendiente recomendado

- Evaluar migración histórica para reasignar visitas cargadas con responsable default cuando pueda inferirse el usuario real.
- Agregar indicadores de auditoría por responsable/agente.
- Evitar que el sistema permita crear visitas sin usuario logueado en producción.
