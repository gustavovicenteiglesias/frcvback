# TODO-001-control-consultorio-modelado

## Objetivo

Extender el modelo de ControlConsultorio para soportar:

- MotivoNoMedicacion
- EventoConsultorio (multiple)
- DerivacionConsultorio (multiple)

---

## Alcance

Backend:

- Crear entidades nuevas
- Crear relaciones
- Crear repositorios
- Modificar ControlConsultorio

---

## Impacto

- Base MySQL
- DTOs (si aplica)
- SyncController (NO implementar aún)

---

## Exclusiones

NO tocar aún:

- db.ts
- sync.ts
- UI

---

## Resultado esperado

Modelo backend listo para persistir:

- 1 motivo
- N eventos
- N derivaciones