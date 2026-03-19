# MAPA DEL PROYECTO

## Backend

src/main/java/ar/edu/unsada/frcv/

### Modelos
- models/ControlConsultorio.java

### Controllers
- controller/ControlConsultorioController.java
- controller/SyncController.java

### Services
- service/ControlConsultorioService.java
- service/impl/ControlConsultorioServiceImpl.java

### Repositories
- repository/ControlConsultorioRepository.java

---

## Frontend

appfrcv-vite/src/

### Base local
- data/db.ts

### Sync
- data/sync.ts

### UI
- pages/ (pantallas)
- components/ (componentes)

---

## Archivos críticos

### db.ts
Define esquema SQLite

### sync.ts
Define sincronización

### SyncController
Orquesta sync backend

---

## Nuevos archivos esperados

### Backend
- MotivoNoMedicacion.java
- EventoConsultorio.java
- DerivacionConsultorio.java

### Repositories
- MotivoNoMedicacionRepository
- EventoConsultorioRepository
- DerivacionConsultorioRepository

---

## Restricciones

- No escanear todo el proyecto
- Usar este mapa como referencia