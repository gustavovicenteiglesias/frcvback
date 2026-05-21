# Reunion con agentes: calidad de carga territorial

Material para reunion del martes 26 de mayo de 2026.

## Idea central

La app no es un tramite administrativo.

Cada vivienda cargada puede convertirse en:

- mapa territorial;
- seguimiento comunitario;
- decision sanitaria;
- justificacion de recursos;
- investigacion;
- auditoria del trabajo en campo.

Si la vivienda nace mal cargada, todo lo que viene despues queda debilitado.

## Regla principal

La vivienda debe quedar perfectamente marcada cuando se accede.

Se puede entender que algunos datos clinicos o de primera entrevista se apoyen transitoriamente en cuaderno si la situacion del domicilio lo exige. Pero la vivienda no deberia quedar para despues.

La vivienda es el ancla territorial del sistema.

Si se carga mal la vivienda, despues no sabemos donde esta la persona, que barrio cubrimos, que zona quedo sin visitar ni donde hay riesgo concentrado.

Hay otro punto clave: la vivienda suele ser el unico dato que no volvemos a relevar.

Al paciente probablemente lo volvamos a ver en una visita, un control, una consulta o un laboratorio. La vivienda, en cambio, muchas veces se carga una sola vez y queda como referencia territorial para todo lo que venga despues.

Por eso, si la vivienda queda mal marcada en el primer contacto, el error puede acompanar durante todo el seguimiento.

## App nativa, no cuaderno

La app nativa tiene que usarse en terreno porque puede registrar cosas que el cuaderno no registra:

| La app puede registrar | El cuaderno no garantiza |
|---|---|
| Coordenada GPS | Ubicacion real del domicilio |
| Precision del GPS | Si la ubicacion es confiable o dudosa |
| Fecha y hora real de carga | Si se cargo en el momento o despues |
| Barrio y CAPS estructurados | Que el barrio no quede mezclado en el apellido o direccion |
| Semaforo de confianza | Si el dato sirve para mapa o solo para auditoria |

El cuaderno puede ayudar como apoyo, pero no puede reemplazar la carga territorial correcta.

## Horrores, no errores

Estos casos no son detalles menores. Son patrones de carga que destruyen valor del dato.

| Horror de carga | Que provoca | Norma esperada |
|---|---|---|
| Poner el barrio al lado del apellido de la persona | Ensucia el dato personal y dificulta busquedas reales | El barrio va en el campo barrio de la vivienda |
| Cargar una vivienda como `BALVE` o texto incompleto | No sirve para ubicar ni auditar | Direccion clara, barrio y CAPS |
| Cargar `Orofino 1326` pero en casa escribir `1326 oro fino` | Duplica informacion y genera contradiccion | Direccion: `Rodolfo Orofino 1326`; casa solo si aplica |
| Cargar `Avenida siempre viva` sin numero | No permite ubicar vivienda exacta | Terminar con numero o usar `S/N` si no tiene numeracion |
| Usar `S/N` sin criterio | Puede esconder falta de dato | `S/N` solo si realmente no hay numeracion |
| Cargar esquina como si fuera domicilio exacto | El punto sirve para mapa, pero no identifica vivienda | Usar formato `Calle y Calle`; queda confianza media |
| Repetir la misma coordenada en muchas viviendas | Genera mapas falsos | Tomar GPS real en cada vivienda |
| Cargar visitas desde el mismo lugar | Oculta si la visita fue en terreno o diferida | Usar app en domicilio cuando corresponda |
| Dejar vivienda sin barrio | Rompe analisis territorial | Barrio requerido cuando la vivienda accede |
| Dejar vivienda sin CAPS | Dificulta seguimiento operativo | CAPS debe seleccionarse cuando corresponda |
| Cargar coordenada manual sin aclarar fuente | Parece GPS real pero no lo es | Fuente: GPS, manual, estimada, recuperada o sin dato |
| Cargar persona sin DNI sin motivo operativo | Dificulta seguimiento y evita cruces | Completar DNI cuando sea posible |

## Normas de direccion

| Situacion | Como cargar |
|---|---|
| Calle con altura | `San Martin 253` |
| Calle sin numeracion real | `A Brown S/N` |
| Esquina o interseccion | `Duggan y Rivadavia` |
| Calle mal escrita | Usar sugerencia de la app |
| Barrio | No va en apellido, nombre ni direccion |

## Semaforo de confianza

| Semaforo | Que significa | Uso |
|---|---|---|
| Alta confianza | Dato completo, consistente y tomado en terreno | Mapas y analisis |
| Confianza media | Dato usable, pero con limitacion documentada | Mapas con advertencia |
| Baja confianza | Dato incompleto, aproximado o dudoso | Auditoria y correccion |
| No usable | Dato invalido para analisis territorial | No mezclar en mapas |

Una esquina queda como confianza media porque ubica una zona, pero no una vivienda exacta.

Una coordenada sin fuente o estimada no debe mezclarse como si fuera GPS real.

## Que cambio en la app

La app ahora:

- exige direccion cuando la vivienda accede;
- exige barrio cuando la vivienda accede;
- acepta direccion con numero, `S/N` o interseccion;
- sugiere nombres de calles;
- marca intersecciones como confianza media;
- guarda precision GPS;
- guarda fuente de ubicacion;
- muestra semaforo de confianza;
- permite auditar viviendas dudosas;
- exporta casos problematicos desde administracion.

## Mensaje para el equipo

No se busca castigar la carga.

Se busca que el trabajo territorial sea visible y defendible.

Si una vivienda fue visitada, tiene que quedar bien ubicada. Si no queda bien ubicada, el sistema puede mostrar actividad, pero no puede mostrar territorio con confianza.

La calidad del dato es parte del cuidado sanitario.

Una vivienda mal cargada no solo ensucia una tabla. Puede hacer que una comunidad no sea vista correctamente.
