# DECISIONES DE ARQUITECTURA Y DISEÑO

## Modelo de datos

### 1. MotivoNoMedicacion
Relación:
- ManyToOne con ControlConsultorio

Justificación:
- selección simple

---

### 2. EventoConsultorio
Relación:
- ManyToMany con ControlConsultorio

Justificación:
- selección múltiple explícita

---

### 3. DerivacionConsultorio
Relación:
- ManyToMany con ControlConsultorio

Justificación:
- selección múltiple explícita

---

## Persistencia dual

El sistema es offline-first:

- SQLite = base operativa local
- MySQL = base central

Toda tabla nueva debe existir en:
- backend (JPA)
- SQLite (db.ts)

---

## Sincronización

Debe actualizarse:

- SyncController (backend)
- sync.ts (frontend)

Regla:
NUNCA agregar entidades sin incluirlas en sync

---

## Estrategia de migración

No eliminar inmediatamente:

- eventos (Boolean)
- derivacion (Boolean)

Se permite coexistencia temporal.

---

## UI

- Motivo → select simple
- Eventos → multiselect
- Derivaciones → multiselect

---

## Restricciones técnicas

- NO romper sync existente
- NO cambiar endpoints actuales sin necesidad
- NO hardcodear listas en frontend (deben venir del backend o ser consistentes con SQLite)