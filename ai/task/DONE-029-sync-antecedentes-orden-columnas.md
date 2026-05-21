# DONE-029 - Corregir orden de columnas en sync de antecedentes

## Contexto
Se detectó un error en `POST /api/sync/push` al procesar exportaciones parciales de Capacitor SQLite:

`Data truncated for column 'desc_trat_hta_previa' at row 1`

El caso observado indica que el backend interpreta valores de `antecedentes` con un orden distinto al exportado por SQLite / pull-full. En particular, `HIPOGLUCEMIANTES_ORALES` debe mapear a `desc_trat_enf_diabetes` y no a `desc_trat_hta_previa`.

## Objetivo
Corregir de forma quirúrgica el flujo de sincronización del backend para que `MOBILE_COLS`, `pull-full` y `push` usen el mismo orden lógico de columnas en `antecedentes`, sin cambiar el contrato JSON ni modificar el frontend.

## Alcance
- Revisar `SyncController`, especialmente `TABLE_ORDER`, `MOBILE_COLS`, `pull`, `push`, `pullFull`, `selectValuesSecSql`, `zipRow`, `insertRow` y `updateRow`.
- Corregir el orden de `MOBILE_COLS` para `antecedentes`.
- Verificar que `pull-full` exporte `antecedentes` en el mismo orden.
- Revisar otros `MOBILE_COLS` y corregir solo si hay evidencia de desalineación.
- Agregar validación defensiva de cantidad de columnas recibidas.
- Mejorar el mensaje de error de `push` con tabla, PK y columna inferida cuando sea posible.
- Documentar el caso problemático y dejar SQL sugerido para convertir campos descriptivos a `TEXT` si se decide hacerlo más adelante.

## Restricciones
- No refactorizar el sync completo.
- No cambiar nombres de tablas ni columnas.
- No cambiar el contrato JSON actual.
- No modificar frontend.
- No tocar seguridad ni autenticación.
- Mantener `last_modified` y `sql_deleted`.

## Validación
- Compilar backend.
- Dejar documentado el array problemático y el mapeo esperado.
