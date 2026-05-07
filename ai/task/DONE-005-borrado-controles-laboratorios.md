# TODO-005-borrado-controles-laboratorios

## Objetivo

Revisar y completar el borrado logico de controles y resultados de laboratorio, manteniendo consistencia offline-first y sincronizacion.

## Diagnostico

Controles:
- Ya existe boton de borrado en `PersonaControles`.
- Usa `deleteControlYQuizasVisita(visitaId, tipo)`.
- Para domicilio marca `control_domicilio`.
- Para consultorio marca `control_consultorio`.
- Si la visita queda sin controles, antecedentes ni laboratorios, marca tambien la visita.

Riesgo detectado:
- Al borrar un control de consultorio no se marcan sus tablas hijas:
  - `control_consultorio_evento`
  - `control_consultorio_derivacion`
- Tampoco se reviso si `motivo_no_medicacion` debe marcarse cuando se borra el control.

Laboratorios:
- Existe `labResultadoSoftDelete(id)` en repo local.
- No se ve boton de borrado en `PersonaLaboratorios`.
- La funcion agrega outbox, pero el sync principal usa export parcial de SQLite; revisar si ese outbox tiene uso real o es residuo.

Backend:
- `ControlDomicilioServiceImpl.delete` hace baja directa del control.
- `ControlConsultorioServiceImpl.delete` hace baja directa del control, sin baja logica explicita de hijos.
- `LabResultadoServiceImpl.delete` hace baja directa del resultado.
- `SyncController` incluye tablas de controles, puentes y laboratorios, pero hay que validar cascadas ante borrados directos.

## Alcance Frontend

- Revisar `deleteControlYQuizasVisita`.
- Si se borra control de consultorio:
  - marcar `control_consultorio`
  - marcar `control_consultorio_evento`
  - marcar `control_consultorio_derivacion`
  - evaluar y definir `motivo_no_medicacion`
- Mantener la regla actual: si la visita queda sin controles, antecedentes ni laboratorios activos, marcar visita.
- Agregar boton de borrado para laboratorio en `PersonaLaboratorios`, solo para MEDICO.
- Confirmar antes de borrar laboratorio.
- Luego de borrar, refrescar lista.

## Alcance Backend / Sync

- Revisar si `ControlConsultorioServiceImpl.delete` debe marcar hijos logicos.
- Revisar si `SyncController` debe propagar baja desde `control_consultorio` hacia puentes cuando un control llega borrado por push.
- Revisar si borrar laboratorio debe afectar visita:
  - solo marcar `lab_resultados`, o
  - si era el unico contenido de la visita, marcar visita igual que controles.

## Preguntas de decision

- Si se borra un control de consultorio, `motivo_no_medicacion` debe borrarse siempre o conservarse como registro historico sincronizable?
- Si se borra un laboratorio asociado a una visita y la visita queda sin ningun contenido, se borra tambien la visita?
- El boton de borrar laboratorio debe estar en la lista o dentro del editor de laboratorio?

## Criterios de aceptacion

- Borrar control de domicilio mantiene comportamiento actual y sincroniza.
- Borrar control de consultorio tambien baja sus puentes asociados.
- Borrar laboratorio queda reflejado localmente y en sync.
- La visita no queda visible si no tiene contenido activo y se define esa regla.
- Solo usuarios MEDICO ven las acciones de borrado.

## Estado

DONE
