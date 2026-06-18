# Ajustes de dashboard, mapa territorial y rol epidemiologia

Fecha: 2026-06-18

## Alcance

Se aplicaron cambios principalmente de frontend para mejorar la lectura epidemiologica del tablero, recuperar el mapa territorial como herramienta de analisis visual y crear un perfil de acceso limitado para usuarios que solo deben consultar indicadores.

No se modifico el schema local de SQLite ni el contrato de sincronizacion offline. Los cambios son compatibles con dispositivos Android que sigan cargando datos con versiones anteriores.

## Dashboard epidemiologico

Se ajusto el tablero para que la lectura sea mas clara para el equipo medico y epidemiologico.

Cambios principales:

- Se mantuvo el KPI de cobertura de personas en programa con denominador institucional fijo de 2275.
- El numerador de cobertura se calcula por personas activas con al menos una TA registrada en controles activos, sea domiciliario o de consultorio.
- Se mantiene la aclaracion del denominador como poblacion mayor de 40 de los barrios definidos.
- En el bloque de visitas domiciliarias se muestra el total de viviendas.
- El bloque de presion arterial queda como lectura domiciliaria/territorial del total evaluado.
- La seccion de antecedentes queda separada de hallazgos: `TA alta detectada` no se muestra como antecedente de riesgo cardiovascular porque no es un antecedente declarado, sino un hallazgo de control.
- En consultorio se agrego lectura de motivos para personas que fueron al consultorio pero no recibieron tratamiento POLILEP.
- Cuando falta motivo de no entrega de medicacion, se muestra como `Sin motivo registrado` para no inventar una causa.

## Mapa territorial

Se restablecio el acceso al mapa `/mapa-riesgo-territorial` desde el dashboard epidemiologico.

Objetivo del mapa:

- Mostrar territorialmente las viviendas y personas evaluadas.
- Ayudar a medicos y epidemiologia a ver distribucion de visitas, acceso domiciliario y presion arterial.
- No funcionar como pantalla de carga ni de correccion de viviendas.
- No escribir coordenadas ni modificar datos originales.

Criterio visual:

- La vista prioriza lectura simple por colores y capas.
- El mapa informa si un punto proviene de direccion, GPS o aproximacion por barrio.
- Cuando la ubicacion es aproximada, se evita dar falsa precision.
- El mapa puede requerir internet para resolver direcciones con servicios externos.

Se incorporaron vistas orientadas al uso sanitario:

- Visitas domiciliarias: accedio, ausente, rechazo y no aplica.
- Presion arterial: HTA/TA alterada, antecedente de HTA, TA alta detectada y normal/sin alerta.

## Boton Mapa territorial

El boton `Mapa territorial` del dashboard se bloquea apenas se presiona.

Motivo:

- Evitar doble navegacion si el usuario toca varias veces.
- Dar una senal visual de que el mapa se esta abriendo.
- Reducir errores de experiencia cuando el mapa tarda en cargar capas, cache o georreferenciacion.

Comportamiento:

- Al presionar, el boton queda deshabilitado.
- Muestra spinner y texto `Abriendo mapa...`.
- Luego navega a `/mapa-riesgo-territorial`.

## Rol EPIDEMIOLOGO

Se agrego soporte frontend para el rol `EPIDEMIOLOGO`.

Este rol esta pensado para usuarios que deben consultar informacion epidemiologica sin operar carga de datos.

Permisos esperados:

| Pantalla / funcion | Acceso |
|---|---|
| Home | Si |
| Menu principal | Si |
| Dashboard epidemiologico | Si |
| Mapa territorial | Si |
| Personas | No |
| Viviendas | No |
| Laboratorios del dia | No |
| Administracion | No |
| Importar base completa | Si |
| Exportar / push | No |
| Exportacion personalizada | No |

Notas tecnicas:

- El frontend reconoce `EPIDEMIOLOGO` y tambien formatos equivalentes como `ROLE_EPIDEMIOLOGO`.
- Se normalizan roles aunque lleguen como texto, objeto con `code`, objeto con `authority` o dentro del JWT.
- El backend debe tener creado el rol y debe incluirlo en el JWT del login.
- El backend autoriza a `EPIDEMIOLOGO` solamente para `/api/sync/pull-full`, no para `/api/sync/push`.

SQL orientativo para crear el rol si no existe:

```sql
INSERT INTO roles (sql_deleted, last_modified, id, code, descripcion)
VALUES (b'0', UNIX_TIMESTAMP(), UUID(), 'EPIDEMIOLOGO', 'Acceso solo a dashboard epidemiologico y mapa territorial');
```

## Calidad del dato

Estos cambios sostienen la misma linea institucional ya documentada: mostrar informacion util sin ocultar la calidad del dato que la sostiene.

Puntos importantes:

- El mapa no corrige datos automaticamente.
- La aproximacion por barrio se usa solo para visualizacion.
- Las direcciones y coordenadas siguen siendo responsabilidad de la carga territorial.
- Las categorias del dashboard deben representar conceptos sanitarios claros: antecedente, hallazgo, consulta, tratamiento y motivo no son lo mismo.

## Verificacion tecnica

- Cambios implementados en frontend.
- Build frontend correcto con `npm run build`.
- Backend compila correctamente con `mvnw.cmd -q -DskipTests compile`.
- No requiere migraciones SQLite.
- No requiere cambios de sincronizacion offline.
- Requiere que produccion tenga el rol `EPIDEMIOLOGO` creado y asignable desde administracion.
- Backend autoriza `/api/sync/pull-full` para `EPIDEMIOLOGO`, manteniendo `/api/sync/push` y el resto de `/api/**` restringidos a roles operativos.