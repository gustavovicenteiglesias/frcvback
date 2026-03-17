# AGENTS.md - Protocolo de Asistencia de IA

## Propósito
Este archivo define las reglas estrictas de inicialización para cualquier agente de IA que intervenga en este repositorio. 

## 🛑 REGLA CERO: AHORRO DE TOKENS
Para optimizar el contexto, NUNCA realices un escaneo global del código fuente al iniciar. Toda la información que necesitas para entender el proyecto ya está procesada y resumida en la carpeta `/ai`.

## Secuencia de Lectura Obligatoria (Boot Sequence)
Al iniciar una nueva sesión, debes leer silenciosamente estos archivos en el siguiente orden antes de interactuar con el usuario o proponer código:
1. `ai/workflow.md` (Cómo debes trabajar, interactuar y gestionar tareas)
2. `ai/context.md` (De qué trata el proyecto y su estado actual)
3. `ai/decisions.md` (Reglas de arquitectura que no puedes romper)
4. `ai/project-map.md` (Dónde están las cosas)

Una vez leídos, ejecuta la primera tarea pendiente según las reglas de `workflow.md`.