# Mapa territorial y calidad geográfica

## Propósito

El mapa territorial incorporado al dashboard epidemiológico permite visualizar viviendas con al menos una persona con tensión arterial alta registrada o antecedente de hipertensión arterial.

La pantalla tiene dos objetivos:

1. apoyar la lectura territorial del riesgo;
2. auditar la calidad de la carga geográfica realizada en terreno.

En este proyecto, el mapa no debe entenderse solo como una visualización. También es una herramienta de control de calidad del operativo.

## Mapa que quedó activo

El mapa activo es el mapa basado en coordenadas cargadas en la vivienda.

Características:

- usa Leaflet y OpenStreetMap;
- no requiere API key;
- muestra puntos de viviendas con riesgo cardiovascular relevante;
- diferencia la precisión de coordenadas;
- descarta coordenadas fuera del rango esperable de Argentina;
- separa visualmente viviendas con coordenadas repetidas para que no queden ocultas;
- conserva el detalle de vivienda, barrio, CAPS, riesgo, TA alta y antecedente de HTA.

Este mapa quedó expuesto desde el dashboard como:

`Mapa territorial HTA / TA alta`

## Lectura epidemiológica

El mapa permite observar concentración espacial de riesgo.

Puede ayudar a responder preguntas como:

- ¿hay barrios con mayor concentración de personas con TA alta?
- ¿hay zonas con mayor antecedente de HTA?
- ¿hay CAPS con carga territorial más intensa?
- ¿hay sectores donde conviene reforzar seguimiento?

La lectura debe hacerse con cuidado: el punto representa la coordenada cargada para la vivienda, no necesariamente una validación catastral exacta.

## Lectura de calidad de datos

El mapa mostró un hallazgo importante: varias viviendas tienen coordenadas repetidas o coordenadas de baja precisión.

Esto puede indicar:

- carga posterior fuera del domicilio;
- uso de coordenadas aproximadas;
- coordenadas tomadas desde un lugar común;
- copia de coordenadas;
- problemas de permisos o señal GPS;
- falta de comprensión del valor del dato territorial.

El mapa permite ver estos problemas de manera rápida y objetiva.

## Problemas encontrados

### Coordenadas imposibles

Aparecieron valores como `62, 62`, que son coordenadas inválidas para Argentina.

Decisión tomada:

- coordenadas fuera del rango argentino se consideran inválidas;
- no se dibujan en el mapa principal;
- se contabilizan dentro de casos sin coordenada válida.

### Coordenadas repetidas

Muchas viviendas compartían exactamente la misma latitud y longitud.

Problema:

- Leaflet dibuja los puntos uno encima de otro;
- visualmente parece que hay pocas viviendas;
- se pierde información.

Decisión tomada:

- mantener la coordenada original como dato;
- separar visualmente los puntos repetidos con un pequeño desplazamiento;
- conservar el aviso de coordenada compartida.

### Precisión baja

Coordenadas con pocos decimales pueden ubicar una vivienda de forma demasiado general.

Ejemplo:

- 4 decimales pueden acercar a una zona;
- no garantizan ubicación exacta de domicilio.

Decisión tomada:

- clasificar precisión;
- mostrar la calidad de la coordenada;
- no ocultar el dato si está dentro de rango, pero sí marcarlo como dudoso.

### Mapa por domicilio

Se probó una pantalla alternativa basada en dirección y casa.

Problema:

- muchas direcciones del operativo no son direcciones postales normalizadas;
- aparecen registros como `5 Alborada`, `casa 104`, `orofino y petrili`;
- OpenStreetMap o Georef no siempre pueden resolver esos textos como calles y alturas;
- el resultado era parcial y podía generar falsa confianza.

Decisión tomada:

- ocultar el botón del mapa alternativo;
- conservar el aprendizaje técnico;
- no exponer una visualización que no aporta confianza suficiente.

## Georef

Se identificó Georef Argentina como herramienta oficial útil para futuras etapas.

Usos posibles:

- geocodificación inversa de coordenadas válidas;
- recuperación de localidad, departamento, municipio y provincia;
- normalización de unidades territoriales;
- eventual vínculo con radios o fracciones censales si la API lo permite en la versión utilizada.

Georef puede ser útil, pero no resuelve por sí solo direcciones operativas o barriales mal estructuradas.

## Conclusión

El mapa territorial que quedó activo es útil porque muestra dos cosas al mismo tiempo:

- distribución territorial del riesgo;
- calidad real de la carga geográfica.

Para investigación, el dato geográfico no debe usarse sin declarar su nivel de confianza.

La decisión correcta es mantener el mapa de coordenadas cargadas como evidencia y avanzar en reglas de calidad de dato antes de prometer mapas más sofisticados.
