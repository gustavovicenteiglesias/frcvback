# TODO-022 - Auditoría de carga y responsables

## Estado

DONE

## Contexto

Ya se corrigió la creación de visitas para asociar `responsable_id` y `created_by_user_id` al usuario real cuando es posible. Falta convertir esa trazabilidad en una vista administrativa útil.

## Objetivo

Crear una vista de auditoría que permita revisar actividad por usuario/responsable y detectar patrones de carga problemáticos.

## Alcance

- Conteos por usuario/responsable:
  - visitas creadas;
  - controles domiciliarios;
  - controles consultorio;
  - laboratorios;
  - antecedentes;
  - borrados lógicos si puede inferirse.
- Indicadores de calidad por responsable:
  - visitas domiciliarias sin GPS;
  - uso de responsable por defecto;
  - coordenadas repetidas mismo día;
  - controles sin TA.
- Filtros por rango de fechas.
- Vista de detalle y exportación CSV.

## Consideraciones

El sistema tiene datos históricos con `created_by_user_id = NULL` y responsable por defecto. La auditoría debe separar:

- datos trazables;
- datos sin usuario;
- datos históricos con responsable genérico.

## Criterios de aceptación

- Admin puede ver actividad por usuario/responsable.
- Admin puede filtrar por fechas.
- Los datos sin trazabilidad se muestran como categoría explícita.
- La vista sirve para devolución operativa, no para sanción automática.

## Resultado

- Se agregó `GET /admin/audit` con filtros `from` y `to` en formato `YYYY-MM-DD`.
- La auditoría consolida actividad por usuario/responsable:
  - visitas;
  - controles domiciliarios;
  - controles de consultorio;
  - laboratorios;
  - antecedentes;
  - borrados lógicos inferidos.
- Se muestran categorías de trazabilidad: `trazable`, `sin_usuario`, `sin_responsable`, `sin_usuario_y_responsable` y `responsable_generico`.
- Se incorporaron señales de calidad operativa:
  - visitas domiciliarias sin GPS;
  - coordenadas repetidas el mismo día;
  - controles sin TA;
  - responsable por defecto.
- El tablero admin muestra la sección “Auditoría de carga” con filtros de fecha y exportación CSV.
- Se documentó en `docs/12-tablero-administracion-online.md`.

## Validación

- Backend: `.\mvnw.cmd -q -DskipTests compile`.
- Frontend: `npm run build`.
