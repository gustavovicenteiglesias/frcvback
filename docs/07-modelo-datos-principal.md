# Modelo de datos principal

Este documento resume las entidades principales del sistema.

```mermaid
erDiagram
  VIVIENDA ||--o{ PERSONA : contiene
  PERSONA ||--o{ VISITA : tiene
  VISITA ||--o| CONTROL_DOMICILIO : registra
  VISITA ||--o| CONTROL_CONSULTORIO : registra
  PERSONA ||--o{ ANTECEDENTE : tiene
  PERSONA ||--o{ LAB_RESULTADO : tiene
  LAB_TIPO ||--o{ LAB_RESULTADO : clasifica
  CONTROL_CONSULTORIO ||--o{ CONTROL_CONSULTORIO_EVENTO : tiene
  CONTROL_CONSULTORIO ||--o{ CONTROL_CONSULTORIO_DERIVACION : tiene
  EVENTO_CATALOGO ||--o{ CONTROL_CONSULTORIO_EVENTO : clasifica
  DERIVACION_CATALOGO ||--o{ CONTROL_CONSULTORIO_DERIVACION : clasifica
```

## Vivienda

Representa el punto territorial donde vive una o más personas.

Campos relevantes:

- Barrio.
- CAPS.
- Dirección.
- Manzana.
- Casa.
- Fecha.
- Acceso o motivo de no acceso.

## Persona

Representa a una persona registrada en el sistema.

Campos relevantes:

- DNI.
- Apellido.
- Nombre.
- Sexo.
- Fecha de nacimiento.
- Teléfono.
- Cobertura.
- Vivienda asociada.

## Visita

Representa un contacto con la persona.

Tipos:

- Domicilio.
- Consultorio.

## Control domiciliario

Registra datos de campo:

- TA.
- Acceso al programa.
- Aceptación de laboratorio.
- Observaciones.

## Control consultorio

Registra seguimiento médico:

- TA.
- Peso, talla, IMC.
- Medicación.
- Motivo de no medicación.
- Eventos.
- Derivaciones.
- Observaciones.

## Laboratorio

Registra resultados por tipo:

- Colesterol.
- Triglicéridos.
- Glucemia.
- Creatinina.
- Filtrado glomerular.
- Otros tipos configurados.
