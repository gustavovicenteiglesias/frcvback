# Línea de calidad del dato

## Principio

El dato no es un trámite administrativo.

En este sistema, cada dato cargado puede transformarse en información clínica, conocimiento epidemiológico y decisiones sanitarias. Si el dato nace mal, todo el proceso posterior pierde fuerza.

La calidad del dato debe ser una regla de trabajo, no una sugerencia.

## Por qué importa

Un dato mal cargado puede producir:

- pérdida de tiempo del equipo que analiza;
- decisiones clínicas incompletas;
- mapas territoriales incorrectos;
- subregistro de riesgo;
- dificultad para justificar recursos;
- pérdida de valor de una intervención comunitaria;
- resultados de investigación débiles;
- daño a la confianza institucional.

Una coordenada repetida, una dirección incompleta o una visita cargada fuera del domicilio no son detalles menores. Son señales de que el proceso de producción del dato necesita límites.

## Línea institucional

El sistema debe ayudar a hacer bien las cosas.

Pero también debe poner límites.

No alcanza con capacitar y explicar. El software tiene que impedir, advertir o dejar visible aquello que compromete el valor del dato.

La regla debe ser clara:

> Si el dato es necesario para una decisión clínica, territorial o epidemiológica, la carga debe tener controles de calidad.

## Límites que debería incorporar el software

### Coordenadas

El sistema debería:

- advertir si la coordenada está fuera del rango esperado;
- advertir si la precisión es baja;
- advertir si la misma coordenada se repite en muchas viviendas;
- registrar si la ubicación fue tomada en el momento o cargada manualmente;
- registrar precisión del GPS cuando el dispositivo la provea;
- diferenciar GPS real de dato estimado o recuperado.

### Domicilio

El sistema debería:

- separar localidad, barrio, calle, número, manzana y casa;
- evitar que todo quede mezclado en un único campo de texto;
- sugerir normalización cuando haya conexión;
- permitir guardar observaciones territoriales sin mezclarlas con dirección postal;
- marcar domicilios incompletos como dato pendiente.

### Visitas

El sistema debería:

- registrar fecha y hora real de carga;
- registrar ubicación de la carga cuando corresponda;
- advertir si muchas visitas se cargan desde el mismo lugar;
- permitir auditoría por equipo o agente sanitario;
- diferenciar carga en terreno de carga diferida.

### Investigación

Para investigación, el sistema debería:

- conservar el dato original;
- conservar la fuente de corrección o estimación;
- documentar el nivel de confianza;
- permitir excluir datos dudosos de ciertos análisis;
- permitir analizarlos como problema de calidad.

## Semáforo de confianza

Se propone clasificar ciertos datos con semáforo:

- **Alta confianza**: dato completo, consistente y tomado en terreno.
- **Confianza media**: dato usable, pero con alguna limitación documentada.
- **Baja confianza**: dato incompleto, aproximado o contradictorio.
- **No usable**: dato inválido para análisis específico.

El dato de baja confianza no necesariamente se elimina. Puede servir para auditoría, seguimiento o mejora del proceso. Pero no debe mezclarse sin advertencia con datos confiables.

## Criterio de producto

Antes de sumar una funcionalidad nueva, hay que preguntarse:

- ¿qué decisión permite tomar?
- ¿qué dato necesita?
- ¿qué calidad mínima requiere ese dato?
- ¿qué pasa si el dato está mal?
- ¿cómo se lo mostramos al usuario sin generar falsa confianza?

Si una funcionalidad depende de datos débiles, el sistema debe mostrar esa debilidad.

## Frase guía

Dato, información, conocimiento y sabiduría no son lo mismo.

El sistema debe cuidar el paso de uno a otro.

Un dato mal cargado no solo ensucia una tabla. Puede impedir que una comunidad sea vista correctamente.

Por eso, la calidad del dato es parte de la ética del trabajo sanitario y de la investigación.
