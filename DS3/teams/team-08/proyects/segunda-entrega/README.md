# Hospital Universitario Nacional de Colombia — Asignación prioritaria de camas UCI

Proyecto semestral de Estructuras de Datos (UNAL, 2026-2) — **Entrega 2**.
Evolución de la Entrega 1 («Residencias UNAL»): **misma arquitectura** (ABB propio + HashMap + listas), aplicada a triaje de camas UCI y ampliada con login, interfaz gráfica, validación de campos, persistencia y pruebas.

## Integrantes
- Daniel Poveda
- Daniel Morales
- John Diaz


## Problema
Una UCI tiene pocas camas y más pacientes críticos que cupos. Cada paciente tiene un **índice de gravedad (0–100)**: a menor índice, mayor urgencia. El sistema ordena a los pacientes por prioridad, asigna las N camas a los más urgentes y mantiene la lista de espera, aun cuando el estado clínico cambia (empeoramiento/mejoría) o hay altas.

## Requisitos Java y ejecución
JDK 17 o superior (probado con 21 y 25). Sin Maven ni librerías externas. Desde la carpeta del proyecto:

| Acción | Windows | Linux/Mac |
|---|---|---|
| Interfaz gráfica | `ejecutar.bat` | `./ejecutar.sh` |
| Versión consola | `ejecutar-consola.bat` | `./ejecutar.sh --consola` |
| Pruebas | `pruebas.bat` | `./pruebas.sh` |
| Tiempos (benchmark) | `java -cp out uci.Benchmark` | igual |
| Restaurar datos de ejemplo | `restaurar-datos.bat` | `./restaurar-datos.sh` |

Manual: `javac -encoding UTF-8 -d out src/uci/*.java src/uci/*/*.java pruebas/uci/*.java` y `java -cp out uci.Main`.
**Ejecute siempre desde la carpeta que contiene `data/`.**

## Credenciales de ejemplo
**Funcionarios** (ID + clave; las claves se guardan como `salt$SHA-256`, no en texto plano):

| ID | Clave | Nombre | Cargo |
|---|---|---|---|
| 1010101010 | Uci#2026a | Laura Camila Gómez Rivera | Médica intensivista |
| 1020202020 | Hun2026*b | Andrés Felipe Moreno Díaz | Coordinador de la UCI |
| 1030303030 | Cuidado7!c | Marta Lucía Pardo Rojas | Enfermera jefe |
| 1040404040 | Turno2026d | Julián David Ortiz Peña | Jefe de turno |
| 1050505050 | Admis#2026e | Sandra Milena Herrera Cruz | Auxiliar de admisiones |

**Pacientes:** 50 inventados en `data/pacientes.csv`; entran solo con su ID. Ejemplos: `6739875326` (índice 5, con cama UCI-01) y `257151433` (índice 30, en espera). Con 12 camas hay 12 con cama y 38 en espera. Hay empates de índice (15, 25, 30, 40…) para probar el desempate. Los datos son ficticios.

## Funcionalidades
- **Login general:** paciente = solo ID; funcionario = ID + clave (3 intentos, luego bloqueo en la sesión). El ID debe existir en la base de datos correspondiente.
- **Paciente:** consulta su estado, si tiene cama UCI, cuál y dónde, su posición y el índice. No puede modificar nada.
- **Funcionario:** registrar, buscar por ID, actualizar índice, dar de alta, definir camas, ejecutar asignación, listas con cama / en espera.
- **Validación de campos:** ID solo dígitos (5–12); nombre y diagnóstico solo letras y espacios; índice solo dígitos 0–100; camas 1–30. En la GUI el carácter inválido ni siquiera se escribe (`DocumentFilter`); además se valida al enviar.
- **Bases de datos:** `data/*.csv` + `config.properties`; cada operación consulta/guarda ahí (escritura segura con archivo temporal). Si el ID no existe o está repetido se informa.
- **Interfaz gráfica** azul y blanco (Swing) y versión de **consola** con el mismo login.

## Estructuras de datos y complejidad
| Estructura | Clave | Uso | Costo |
|---|---|---|---|
| `ArbolBinarioBusqueda` (propio, genérico) | (índice, ID) | lista de prioridad ordenada | insertar/eliminar/buscar O(h); inorden O(n) |
| `HashMap` | ID | existencia y acceso por ID; funcionarios | O(1) promedio |
| `Queue` (`ArrayDeque`) | — | camas libres al asignar | O(1) por cama |
| `ArrayList` | — | listados devueltos | O(1) acceso |

*h* = altura del árbol: O(log n) con datos aleatorios, O(n) si llegan ordenados.

| Operación | Costo |
|---|---|
| Registrar | O(h) (+ O(1) hash) |
| Buscar por ID / validar existencia | O(1) promedio |
| Actualizar índice | O(h): sacar con clave vieja, cambiar, reinsertar |
| Dar de alta | O(h) |
| Listar ordenado | O(n) |
| Asignar N camas | O(n) |
| Posición de un paciente | O(n) |

**Por qué un ABB y no un heap:** el heap da el mínimo, pero no permite listar ordenado ni quitar/reubicar a un paciente cualquiera sin estructuras auxiliares. **Por qué (índice, ID):** el índice se repite; el ID desempata y la clave es única.
**Cambio respecto a la Entrega 1:** se agrega la cola de camas libres, la persistencia, el login y la GUI; el ABB pasó a ser genérico (`ArbolBinarioBusqueda<T>`) con inserción, inorden y altura **iterativos**.
**Limitación conocida:** el ABB no se balancea. Mejora prevista para la entrega final: AVL.

## Analogía Residencias UNAL → Camas UCI
| Residencias UNAL | Camas UCI | Nota |
|---|---|---|
| Estudiante | Paciente | + diagnóstico y cama |
| puntaje socioeconómico | índice de gravedad | menor = más prioridad |
| cupos N | camas disponibles N | 1–30 |
| `Estudiante.compareTo` (puntaje, ID) | `Paciente.compareTo` (índice, ID) | desempate por ID |
| `SistemaResidencias` | `SistemaUCI` | misma lógica |
| `ArbolBinarioBusqueda` / `Nodo` | igual (genéricos) | |
| modificar puntaje | actualizar índice (cambio de signos vitales) | O(h) |
| eliminar estudiante | alta médica | O(h) |
| asignar cupos | asignar camas | O(n) |
| `Main` | `Main` + GUI + consola | |

`compareTo`: `Integer.compare(indiceGravedad)`; si es 0, `id.compareTo(otro.id)`.

## Caso de uso (verificado ejecutando el código)
Pacientes: Ana Torres 35, Carlos Pérez 12, Laura Gómez 48, Juan Díaz 12, Valentina Ruiz 60, Andrés Mora 22.
1. **Orden:** Carlos(12), Juan(12, empate: gana el ID menor), Andrés(22), Ana(35), Laura(48), Valentina(60). Altura del ABB = 4.
2. **3 camas:** UCI-01 Carlos, UCI-02 Juan, UCI-03 Andrés; el resto en espera.
3. **Valentina empeora 60 → 8:** pasa de la posición 6 a la 1 y recibe la UCI-03; Andrés sale a lista de espera.
4. **Alta de Carlos:** libera la UCI-01, que ocupa Andrés. Ana queda 4.ª de 5, en espera.

## Pruebas y resultados preliminares
- `PruebasSistema`: **85 pruebas** (árbol, validaciones, registro/alta/modificación, asignación, seguridad, persistencia y base de ejemplo).
- `PruebasInterfaz`: **21 pruebas** de GUI sin pantalla (filtros de teclado, login, registro, gestión y asignación).
- Nota: las pruebas se ejecutaron en un entorno con 1 CPU; **ejecútenlas en su computador** y reporten sus propios números.

Benchmark (`uci.Benchmark`, una sola corrida, JVM 21, 1 CPU; los tiempos varían entre equipos):

| Datos | n | Altura ABB | log2(n) | Insertar n (ms) | Inorden (ms) |
|---|---:|---:|---:|---:|---:|
| Aleatorios | 1 000 | 21 | 10,0 | 0,7 | 0,09 |
| Aleatorios | 10 000 | 29 | 13,3 | 13,4 | 1,08 |
| Aleatorios | 100 000 | 37 | 16,6 | 232,4 | 18,18 |
| Ya ordenados | 1 000 | 1 000 | 10,0 | 1,2 | 0,01 |
| Ya ordenados | 5 000 | 5 000 | 12,3 | 27,9 | 0,02 |
| Ya ordenados | 20 000 | 20 000 | 14,3 | 883,2 | 0,19 |

Con datos aleatorios la altura crece como O(log n) (algo por encima de log2 n); con datos ordenados el árbol degenera en una lista (altura = n) y insertar n datos pasa a costar O(n²), lo que justifica balancearlo (AVL) en la entrega final.

## Estado y pendiente
Implementado: todo lo anterior. Aproximadamente 80 % del proyecto. Pendiente: árbol balanceado (AVL), reportes/estadísticas, historial de cambios por paciente.

## Estructura
```
src/uci/{Main}  modelo/ estructuras/ servicio/ persistencia/ ui/
pruebas/uci/    PruebasSistema, PruebasInterfaz, Benchmark
data/           pacientes.csv, funcionarios.csv, config.properties
data-inicial/   copia de respaldo de la base de ejemplo
```

## Herramientas y declaración de ayuda
- Java (JDK), Swing, `HashMap`, `ArrayDeque` y `ArrayList` de la biblioteca estándar.
- **Asistencia de IA:** se utilizó un asistente de inteligencia artificial (Claude, de Anthropic) como apoyo para diseñar y redactar el código, las pruebas y esta documentación. El equipo revisó, ejecutó y probó el resultado, y todos los integrantes deben poder explicar su funcionamiento.

Aviso: proyecto académico con datos ficticios; no es un sistema clínico real.
