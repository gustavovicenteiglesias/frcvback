# Sync de antecedentes: orden de columnas y caso de validación

## Problema detectado

En `POST /api/sync/push`, el backend recibe exportaciones parciales de Capacitor SQLite en formato array de arrays. Para interpretar cada posición del array usa `MOBILE_COLS`.

Se detectó un error:

```text
Data truncated for column 'desc_trat_hta_previa' at row 1
```

La causa fue una desalineación en el orden de columnas de `antecedentes`: el backend estaba leyendo `HIPOGLUCEMIANTES_ORALES` como `desc_trat_hta_previa`, cuando corresponde a `desc_trat_enf_diabetes`.

## Orden esperado para antecedentes

```text
id
persona_id
visita_id
created_by_user_id
diabetes
dislipemia
enf_cardiovascular
enf_renal_cronica
hta_previa
tabaquismo
tratamiento_enf_cardiovascular
tratamiento_enf_diabetes
tratamiento_enf_dislipemia
tratamiento_enf_renal
tratamiento_hta_previa
desc_trat_enf_cardiovascular
desc_trat_enf_dislipemia
desc_trat_enf_renal
desc_trat_enf_diabetes
desc_trat_hta_previa
otros
last_modified
sql_deleted
```

Este orden debe mantenerse igual en:

- `MOBILE_COLS` de `SyncController`
- esquema SQLite de `antecedentes`
- `pull-full`
- exportaciones parciales de Capacitor SQLite

## Caso problemático usado como referencia

```json
[
  "b1221e38-eb67-4f81-a5f5-7cac69aef4a5",
  "a13a32ec-2b0f-4a1a-8153-3ff68de261d6",
  null,
  null,
  1,
  1,
  0,
  0,
  1,
  0,
  null,
  1,
  1,
  null,
  1,
  null,
  "HIPOGLUCEMIANTES_ORALES",
  null,
  null,
  null,
  "valsartan",
  1778849891,
  1
]
```

Con el orden corregido:

- `HIPOGLUCEMIANTES_ORALES` queda en `desc_trat_enf_dislipemia` para ese array concreto, porque ocupa la posición 17.
- `desc_trat_enf_diabetes` queda en `null`, porque ocupa la posición 19.
- `desc_trat_hta_previa` queda en `null`, porque ocupa la posición 20.
- `valsartan` queda en `otros`, porque ocupa la posición 21.

Observación importante: si el dato clínico correcto era que `HIPOGLUCEMIANTES_ORALES` estuviera en diabetes, entonces la exportación de origen debió ubicarlo en la posición 19. El arreglo evita que se inserte en HTA, pero no inventa un reordenamiento de datos ya exportados.

## Validación defensiva agregada

Antes de insertar o actualizar una fila, `push` verifica que la cantidad de valores recibida coincida con la cantidad de columnas esperada en `MOBILE_COLS`.

Si hay diferencia:

- la fila se saltea;
- se incrementa `skipped`;
- se agrega un warning con tabla, cantidad esperada y cantidad recibida;
- se registra un warning en logs.

## Mejora de diagnóstico

Si una fila falla durante el upsert, el error ahora agrega contexto:

- tabla procesada;
- PK de la fila cuando está disponible;
- columna inferida si el mensaje SQL la informa.

Esto permite que el próximo error no quede solamente como `Data truncated`, sino asociado a una fila y tabla concretas.

## SQL sugerido para evaluar aparte

No se aplicó ninguna migración automática. Si se decide permitir texto libre en campos descriptivos de antecedentes, evaluar una migración controlada como:

```sql
ALTER TABLE antecedentes
  MODIFY desc_trat_enf_cardiovascular TEXT NULL,
  MODIFY desc_trat_enf_dislipemia TEXT NULL,
  MODIFY desc_trat_enf_renal TEXT NULL,
  MODIFY desc_trat_enf_diabetes TEXT NULL,
  MODIFY desc_trat_hta_previa TEXT NULL,
  MODIFY otros TEXT NULL;
```

Esta decisión debe revisarse clínicamente porque algunos campos hoy funcionan como valores controlados.
