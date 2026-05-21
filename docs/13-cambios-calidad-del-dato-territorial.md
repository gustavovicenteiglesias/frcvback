# Cambios implementados para calidad del dato territorial

## Principio

La calidad del dato no quedo tratada como una recomendacion informal, sino como una regla del producto.

El sistema ahora intenta impedir, advertir o dejar visible cuando un dato territorial puede comprometer el analisis clinico, epidemiologico o comunitario.

## Cambios de modelo de datos

En `viviendas` se agregaron campos para no guardar solo latitud y longitud, sino tambien contexto de confianza:

| Campo | Uso |
|---|---|
| `gps_accuracy` | Precision informada por el dispositivo, en metros. |
| `gps_captured_at` | Momento en que fue capturada la ubicacion. |
| `ubicacion_fuente` | Origen de la ubicacion: `gps`, `manual`, `estimada`, `recuperada`, `sin_dato`. |
| `confianza_dato` | Semaforo: `alta`, `media`, `baja`, `no_usable`. |

Esto permite diferenciar:

- una coordenada GPS real tomada en terreno;
- una coordenada cargada manualmente;
- una coordenada estimada;
- un dato sin fuente;
- un dato no usable para mapas o analisis.

## Cambios de sincronizacion

El backend y el frontend quedaron alineados para sincronizar esos campos nuevos.

Se ajusto:

- `POST /api/sync/push` para aceptar `viviendas` en formato viejo de 13 columnas y nuevo de 17 columnas;
- `GET /api/sync/pull-full` para mandar el schema completo de `viviendas`;
- exportacion parcial del frontend para enviar las 17 columnas;
- creacion defensiva de columnas nuevas en backend si faltan;
- entidad `Viviendas` y servicio REST para conservar los campos nuevos.

Esto evita que el push falle por desalineacion de columnas y que luego aparezcan errores de FK en `personas.viviendas_id`.

## Reglas de direccion

Se incorporaron normas minimas para direccion:

| Caso | Regla |
|---|---|
| Domicilio con numeracion | Debe terminar con numero. Ejemplo: `San Martin 253`. |
| Domicilio sin numeracion | Debe terminar con `S/N`. Ejemplo: `A Brown S/N`. |
| Esquina/interseccion | Se acepta como `Calle y Calle`, `Calle esq Calle`, `Calle esquina Calle` o `Calle / Calle`. |

Las intersecciones son validas para ubicar un punto territorial, pero no identifican una vivienda exacta. Por eso quedan como confianza media aunque la coordenada sea buena.

## Normalizacion de calles

Se incorporo un catalogo local de calles del partido de San Antonio de Areco para sugerencias sin depender siempre de internet.

Tambien se agrego una correccion territorial local:

| Dato de base | Correccion operativa |
|---|---|
| `JOSE LUIS BALARZA` | `JOSE LUIS GALARZA` |

La correccion se hizo porque fue verificada territorialmente y en Google Maps.

Para intersecciones, el sistema ahora sugiere cada calle por separado y reconstruye la direccion completa.

Ejemplo:

```text
Sils y Peron
```

Puede sugerir:

```text
Primera calle: ...
Segunda calle: ...
```

Y luego reconstruir:

```text
Calle normalizada y Otra calle normalizada
```

## Reglas de vivienda accedida

Cuando una vivienda figura como accedida:

- la direccion es obligatoria;
- el barrio es obligatorio;
- la direccion debe tener numero, `S/N` o forma de interseccion;
- el sistema marca dato territorial pendiente si falta informacion minima;
- no se permite agregar personas a una vivienda accedida con dato territorial incompleto.

## Visibilidad del semaforo

La calidad territorial ahora se muestra en:

- formulario de vivienda;
- detalle de vivienda;
- listado de viviendas;
- mapa territorial;
- exportacion CSV de viviendas.

El listado permite filtrar por:

- alta confianza;
- confianza media;
- baja confianza;
- no usable.

## Auditoria en administracion

El tablero admin incorporo indicadores y exportaciones CSV para:

- viviendas sin GPS;
- viviendas de baja confianza;
- viviendas no usables;
- coordenadas repetidas en varias viviendas;
- direcciones `S/N`;
- GPS con precision mayor a 100 metros;
- fuente de ubicacion `sin_dato` o `estimada`;
- visitas domiciliarias sin GPS;
- visitas con misma fecha y misma coordenada de vivienda;
- personas sin DNI;
- usuarios sin rol;
- responsables genericos o sin usuario.

## Mapa territorial

El mapa territorial incorpora:

- precision calculada;
- fuente de ubicacion;
- confianza del dato;
- advertencia de coordenadas repetidas;
- exclusion de puntos no usables.

El objetivo es evitar que el mapa muestre como exacto algo que fue estimado, incompleto o dudoso.

## Control de acceso relacionado

Se cerro el flujo de usuario sin rol:

- un usuario nuevo puede autenticarse y quedar guardado en la base;
- no recibe automaticamente rol operativo;
- si no tiene rol, ve una pantalla de acceso pendiente;
- no puede usar rutas operativas;
- no puede importar ni exportar;
- backend protege `/api/**` para roles `ADMIN`, `MEDICO` o `ENFERMERO`.

## Resultado

El sistema ahora cuida mejor el paso de dato a informacion.

Una vivienda mal ubicada ya no queda escondida en una tabla como si fuera confiable. Queda marcada, filtrable y exportable para auditoria.

