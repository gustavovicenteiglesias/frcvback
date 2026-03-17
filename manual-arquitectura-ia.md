# Manual de Arquitectura Context-First para IA

## ¿Qué es esto?
Es un sistema de carpetas y archivos estandarizado para trabajar con cualquier agente de IA (Antigravity, Cursor, Windsurf, Claude, etc.) sin depender de su memoria interna. Optimiza el consumo de tokens y permite cambiar de herramienta en cualquier momento sin perder el progreso.

## Estructura del Boilerplate
Añade esto a la raíz de cualquier proyecto nuevo:
├── AGENTS.md                 # (Fijo) El "portero". Obliga a la IA a leer la carpeta /ai.
└── ai/                       # (Dinámico) La memoria del proyecto.
    ├── workflow.md           # (Fijo) Reglas de comportamiento y kanban de la IA.
    ├── context.md            # (Dinámico) De qué trata el proyecto y su estado actual.
    ├── decisions.md          # (Dinámico) Reglas técnicas (stack, UI, convenciones).
    ├── project-map.md        # (Dinámico) Mapa de archivos clave para no escanear todo el disco.
    ├── checkpoints/          # (Dinámico) Fotos del estado al terminar hitos grandes.
    └── tasks/                # (Dinámico) Tablero Kanban de tareas.
        └── template.md       # (Fijo) La estructura que deben tener las tareas.

## Flujo de Trabajo (El "Prompt de Arranque")
1. Llenas los archivos de la carpeta `/ai` con la información del nuevo proyecto.
2. Creas una o más tareas en `/ai/tasks/` con el prefijo `TODO-`.
3. Abres tu asistente de IA y tu único mensaje inicial debe ser:
   > *"Lee AGENTS.md en la raíz del proyecto y obedece sus instrucciones para comenzar."*
4. La IA leerá las reglas, agarrará la primera tarea `TODO-`, la cambiará a `DOING-` y comenzará a trabajar.

## Convención de Nombres (Nomenclatura)
El estándar recomendado es: [ID]-[ESTADO]-[descripcion-corta].md

Los estados permitidos deben ser explícitos:

TODO: Tarea pendiente.

DOING: La IA está trabajando en esto actualmente.

BLOCKED: Depende de algo externo o requiere respuesta tuya.

DONE: Tarea terminada.