# 🚢 Batalla Naval (Battleship)

**Batalla Naval** es una recreación digital completa del clásico juego de estrategia de combate naval, desarrollado usando **JavaFX** y **Maven**.  
Este proyecto fue creado como parte de un curso universitario de programación y demuestra principios avanzados de **programación orientada a objetos**, **patrones de diseño**, **implementación de IA** e **interfaces gráficas** en Java.

---

## 🎯 Introducción

Batalla Naval es un juego de estrategia por turnos donde los jugadores colocan estratégicamente su flota naval en una cuadrícula y se turnan para disparar y hundir los barcos del oponente. Esta versión digital cuenta con un oponente de IA inteligente, animaciones fluidas, guardado persistente de partidas y seguimiento completo de estadísticas.

El proyecto demuestra prácticas profesionales de diseño de software incluyendo **arquitectura MVC**, **patrón Singleton**, **patrón Adapter**, **patrón Strategy** (para la IA), y **gestión robusta de estados** con serialización.

---

## 🧩 Objetivos

- Desarrollar un juego de Batalla Naval completamente jugable y visualmente pulido usando JavaFX.  
- Implementar un **oponente de IA avanzado** con modos de caza y objetivo usando estrategias basadas en probabilidad.  
- Aplicar conceptos de **programación orientada a objetos**: abstracción, herencia, polimorfismo y encapsulación.  
- Crear una **arquitectura en capas** separando presentación, lógica de negocio y persistencia de datos.  
- Proporcionar una **experiencia de usuario fluida** a través de vistas FXML, estilos CSS, fondos de video y animaciones.  
- Manejar la persistencia del estado del juego con **mecanismos de persistencia duales** (serialización binaria y CSV).  
- Implementar **manejo integral de excepciones** y mecanismos de seguridad.

---

## 🎮 Características

### Jugabilidad Principal
- **Fase de Colocación de Barcos**: Colocación interactiva de barcos con rotación (tecla R o Espacio)
- **Combate por Turnos**: Disparos estratégicos con retroalimentación visual (aciertos, fallos, explosiones)
- **Oponente IA Inteligente**: IA avanzada con dos modos:
  - **Modo CAZA**: Objetivo basado en probabilidad usando mapas de calor
  - **Modo OBJETIVO**: Persecución inteligente de barcos después de anotar aciertos
- **Composición de la Flota**:
  - 1× Portaaviones (4 celdas)
  - 2× Submarinos (3 celdas cada uno)
  - 3× Destructores (2 celdas cada uno)
  - 4× Fragatas (1 celda cada una)

### Interfaz de Usuario
- **Interfaz JavaFX Dinámica** con transiciones fluidas entre pantallas:
  - **Menú Principal** — Iniciar nuevo juego, continuar partida guardada o salir
  - **Tablero de Juego** — Vista de doble tablero (jugador y enemigo) con actualizaciones en tiempo real
  - **Pantalla de Victoria/Derrota** — Visualización animada del resultado
  - **Pantalla de Estadísticas** — Resultados detallados de la partida y rendimiento del jugador
- **Fondos de Video**: Videos de fondo en bucle para atmósfera inmersiva
- **Efectos de Partículas**: Animaciones de explosión en clics de botones y destrucción de barcos
- **Renderizado de Barcos con Sprites**: Rotación automática y manejo de orientación

### Persistencia y Estadísticas
- **Guardado del Estado del Juego**: Auto-guardado al salir, guardado manual durante el juego
- **Guardados Basados en Nombre de Usuario**: Cada jugador tiene su propio archivo de guardado
- **Seguimiento de Estadísticas del Jugador**:
  - Partidas jugadas y ganadas
  - Total de disparos realizados y porcentaje de precisión
  - Ratios de aciertos/fallos
- **Tabla de Clasificación Basada en CSV**: Datos persistentes de jugadores con rankings

### Características Avanzadas
- **Ejecución Multi-hilo de la IA**: Previene congelamiento de la UI durante cálculos de IA
- **Arquitectura de Máquina de Estados**: Gestión robusta del estado del juego (SETUP → PLAYING → FINISHED)
- **Seguimiento de Historial de Disparos**: Previene disparos duplicados y valida movimientos
- **Detección de Hundimiento de Barcos**: Identificación automática de barcos destruidos usando BFS
- **Marcado de Celdas Adyacentes**: La IA marca celdas imposibles alrededor de barcos hundidos

---

## 🧱 Estructura del Proyecto
```
BattleShip-main/
│
├── src/main/java/com/example/battleship/
│   ├── Controllers/                    # Controladores JavaFX
│   │   ├── GameController.java         # Controlador principal del juego
│   │   ├── MainMenuController.java     # Navegación del menú
│   │   ├── StatsController.java        # Visualización de estadísticas
│   │   └── VictoryController.java      # Pantalla de fin de juego
│   │
│   ├── Model/
│   │   ├── AI/
│   │   │   └── SmartAI.java            # IA avanzada con modos caza/objetivo
│   │   ├── Board/
│   │   │   ├── Board.java              # Implementación del tablero con HashMap
│   │   │   ├── BoardAdapter.java       # Clase base del patrón Adapter
│   │   │   └── IBoard.java             # Interfaz del tablero
│   │   ├── Coordinates/
│   │   │   └── Coordinates.java        # Generación de coordenadas aleatorias
│   │   ├── Exceptions/
│   │   │   ├── InvalidPositionException.java
│   │   │   ├── InvalidShotException.java
│   │   │   ├── InvalidGameStateException.java
│   │   │   ├── GameSaveException.java
│   │   │   └── GameLoadException.java
│   │   ├── Game/
│   │   │   ├── Game.java               # Motor principal del juego
│   │   │   ├── GameAdapter.java        # Patrón Adapter
│   │   │   ├── GameState.java          # Estado del juego serializable
│   │   │   └── IGame.java              # Interfaz del juego
│   │   ├── Player/
│   │   │   ├── Human.java              # Jugador humano
│   │   │   ├── Machine.java            # Jugador IA
│   │   │   ├── PlayerAdapter.java      # Patrón Adapter
│   │   │   ├── PlayerData.java         # Persistencia de estadísticas
│   │   │   └── IPlayer.java            # Interfaz del jugador
│   │   ├── Serializable/
│   │   │   ├── SerializableFileHandler.java  # Persistencia binaria
│   │   │   └── ISerializableFileHandler.java
│   │   ├── Ship/
│   │   │   ├── AircraftCarrier.java    # Barco de 4 celdas
│   │   │   ├── Submarine.java          # Barco de 3 celdas
│   │   │   ├── Destroyer.java          # Barco de 2 celdas
│   │   │   ├── Frigate.java            # Barco de 1 celda
│   │   │   ├── ShipAdapter.java        # Patrón Adapter
│   │   │   └── IShip.java              # Interfaz del barco
│   │   ├── TextFile/
│   │   │   ├── PlaneTextFileHandler.java  # Persistencia CSV
│   │   │   └── IPlaneTextFileHandler.java
│   │   └── Utils/
│   │       └── SpriteSheet.java        # Corte y rotación de sprites
│   │
│   ├── Views/                          # Gestión de stages
│   │   ├── GameView.java               # Ventana del juego (Singleton)
│   │   ├── MainMenuView.java           # Ventana del menú (Singleton)
│   │   ├── StatsView.java              # Ventana de estadísticas
│   │   └── VictoryView.java            # Ventana de victoria (Singleton)
│   │
│   └── MainApplication.java            # Punto de entrada de la aplicación
│
├── src/main/resources/
│   ├── Battleship-Images/              # Sprites de barcos, marcadores
│   ├── Battleship-Videos/              # Videos de fondo
│   ├── *.fxml                          # Definiciones de layout UI
│   └── Styles.css                      # Estilos de la UI
│
├── src/test/java/                      # Pruebas unitarias
│   ├── BoardTest.java
│   ├── GameTest.java
│   └── FrigateTest.java
│
├── data/
│   └── player_data.txt                 # CSV de estadísticas de jugadores
│
├── pom.xml                             # Configuración de Maven
└── module-info.java                    # Descriptor de módulo Java
```

---

## ⚙️ Tecnologías Utilizadas

- **Java 17+**  
- **JavaFX 21** para la interfaz gráfica de usuario  
- **FXML** para layout declarativo de UI  
- **CSS** para estilos y efectos visuales  
- **Maven** para gestión de dependencias  
- **JUnit 5** para pruebas unitarias  
- **Serialización de Java** para persistencia del estado del juego  
- **Multi-threading** (ExecutorService) para cálculos de IA  
- **IntelliJ IDEA** (IDE recomendado)

---

## 🚀 Cómo Ejecutar el Proyecto

### 🧩 Requisitos

- **Java JDK 17** o superior  
- **Apache Maven 3.8+**  
- **JavaFX SDK 21** (gestionado por Maven)  
- 4GB RAM mínimo (para reproducción de video)

### ▶️ Pasos para Ejecutar

1. **Clonar el repositorio**:
```bash
   git clone https://github.com/tu-usuario/battleship-game.git
   cd battleship-game
```

2. **Compilar el proyecto**:
```bash
   mvn clean install
```

3. **Ejecutar la aplicación**:
```bash
   mvn javafx:run
```

4. **Ejecutar pruebas**:
```bash
   mvn test
```

### 🎮 Instrucciones de Juego

1. **Pantalla de Inicio**: Ingresa tu nombre de usuario y haz clic en "Jugar" para un nuevo juego o "Continuar" para reanudar
2. **Colocación de Barcos**:
   - Mueve el mouse para previsualizar la posición del barco
   - Presiona **R** o **Espacio** para rotar barcos
   - Haz clic para colocar (los barcos se colocan secuencialmente del más grande al más pequeño)
3. **Fase de Combate**:
   - Haz clic en las celdas del tablero enemigo para disparar
   - Azul = Agua (fallo), Rojo = Acierto, Calavera = Barco hundido
   - La IA toma su turno después de que falles o cuando las reglas del juego lo dicten
4. **Final del Juego**: Ve las estadísticas y regresa al menú principal

---

## 🧠 Arquitectura y Patrones de Diseño

### MVC (Modelo-Vista-Controlador)

| Capa | Responsabilidad |
|-------|----------------|
| **Modelo** | Lógica del juego, IA, estado del tablero, barcos, jugadores, persistencia |
| **Vista** | Layouts FXML, gestión de stages, presentación visual |
| **Controlador** | Manejo de entrada del usuario, actualizaciones de vista, coordinación del flujo del juego |

### Patrones de Diseño Implementados

#### 1. **Patrón Singleton**
- `GameView`, `MainMenuView`, `VictoryView`
- Asegura una única instancia de cada ventana
- Inicialización lazy thread-safe usando el idioma Holder

#### 2. **Patrón Adapter**
- `BoardAdapter`, `GameAdapter`, `PlayerAdapter`, `ShipAdapter`
- Proporciona implementaciones predeterminadas para interfaces
- Permite sobrescritura selectiva de métodos

#### 3. **Patrón Strategy**
- `SmartAI` con cambio de modo (HUNT/TARGET)
- Encapsula algoritmos de IA
- Mapas de calor de probabilidad vs. persecución dirigida

#### 4. **Patrón State**
- Enum `GameState` (SETUP, PLAYING, FINISHED)
- Controla transiciones y acciones válidas

#### 5. **Método Factory**
- Creación de barcos en `Game.createShip()`
- Generación de coordenadas en `Coordinates`

### Principios de Diseño Clave

- **Separación de Responsabilidades**: Límites claros entre capas
- **Encapsulación**: Campos privados con acceso controlado
- **Segregación de Interfaces**: Interfaces enfocadas (IBoard, IShip, IPlayer)
- **Inversión de Dependencias**: Los controladores dependen de abstracciones
- **Responsabilidad Única**: Cada clase tiene un propósito claro

---

## 🤖 Implementación de IA

### Arquitectura de SmartAI

La IA utiliza una estrategia sofisticada de dos modos:

#### Modo CAZA (Sin Aciertos Activos)
- **Mapa de Calor de Probabilidad**: Calcula la probabilidad de colocación para cada celda
- **Ponderado por Barcos Restantes**: Los barcos más grandes aumentan las puntuaciones de las celdas
- **Optimización en Tablero de Ajedrez**: Prioriza patrones de alta probabilidad
- **Selección Aleatoria**: Entre celdas con puntuaciones iguales para imprevisibilidad

#### Modo OBJETIVO (Aciertos Activos Detectados)
- **Agrupación de Aciertos**: Usa BFS para identificar barcos separados
- **Detección de Orientación**: Determina alineación horizontal/vertical
- **Objetivo en Extremos**: Se enfoca en los extremos del barco
- **Cola de Prioridad**: Puntúa celdas por probabilidad (200 para extremos, 100 para adyacentes)

#### Características Avanzadas
- **Seguimiento Multi-Barco**: Maneja múltiples barcos dañados simultáneamente
- **Detección de Barcos Hundidos**: BFS para identificar aciertos conectados
- **Marcado de Celdas Adyacentes**: Marca celdas imposibles alrededor de barcos hundidos
- **Prevención de Duplicados**: Mantiene historial de disparos
- **Mecanismos de Respaldo**: Manejo elegante de casos límite

---

## 🎨 Características de Interfaz de Usuario

### Componentes Visuales
- **Sistema de Doble Canvas**: Renderizado separado para tableros de jugador/enemigo
- **Barcos Basados en Sprites**: Imágenes realistas de barcos con rotación automática
- **Superposición de Cuadrícula**: Cuadrícula semi-transparente para apuntado preciso
- **Sistema de Animaciones**:
  - Partículas de explosión en clics de botones
  - Efectos de sacudida de botones
  - Retrasos de transición suaves

### Integración de Medios
- **Fondos de Video en Bucle**: Diferentes videos por pantalla
- **Optimización de Precarga**: Previene tartamudeo inicial
- **Control de Volumen**: Niveles de audio configurables
- **Respaldo Elegante**: Fondos de color sólido si el video falla

### Diseño Responsivo
- **Tamaños de Canvas Fijos**: 364×301 píxeles (cuadrícula 10×10)
- **Dimensiones de Celda**: 36.4×30.1 píxeles por celda
- **Estilo Consistente**: Tema basado en CSS en todas las pantallas

---

## 💾 Persistencia de Datos

### Serialización Binaria (Guardados de Juego)
- **Formato**: Serialización de Objetos Java (archivos .dat)
- **Patrón de Nombre de Archivo**: `game_save_[nombre_usuario].dat`
- **Datos Almacenados**:
  - Estados de ambos tableros
  - Todas las posiciones de barcos y conteos de aciertos
  - Historiales de disparos
  - Turno del jugador actual
  - Fase del juego

### Persistencia CSV (Estadísticas)
- **Formato**: Texto plano CSV
- **Nombre de Archivo**: `player_data.txt`
- **Campos**: `nombre,partidasJugadas,partidasGanadas,disparosTotales,aciertosTotales`
- **Características**:
  - Búsqueda de jugador insensible a mayúsculas
  - Actualizaciones atómicas (reescritura completa del archivo)
  - Cálculo de precisión

---

## 🧪 Pruebas Unitarias

### Cobertura Actual de Pruebas

**BoardTest.java**
- ✅ Inicialización del tablero
- ✅ Operaciones get/set de celdas
- ✅ Manejo de celdas inválidas

**GameTest.java**
- ✅ Inicialización de jugadores
- ✅ Colocación válida de barcos
- ✅ Validación de límites
- ✅ Detección de colisiones
- ✅ Extracción de coordenadas
- ✅ Avance de turnos

**FrigateTest.java**
- ✅ Verificación de tamaño de barco
- ✅ Registro de aciertos
- ✅ Lógica de hundimiento
- ✅ Almacenamiento de posición/dirección

### Ejecutar Pruebas
```bash
mvn test
mvn test -Dtest=BoardTest
mvn test -Dtest=GameTest#testPlayersInitialized
```

### Mejoras Necesarias en las Pruebas
1. Agregar pruebas para comportamiento de IA
2. Probar serialización/deserialización de estado del juego
3. Probar escenarios multi-hilo
4. Agregar pruebas de integración para flujo completo del juego
5. Probar rutas de manejo de excepciones

---

## 📊 Estadísticas del Juego

### Métricas Rastreadas
- **Partidas Jugadas**: Total de partidas completadas
- **Partidas Ganadas**: Contador de victorias
- **Disparos Totales**: Disparos acumulados en todas las partidas
- **Aciertos Totales**: Aciertos exitosos
- **Precisión**: Calculada como `(aciertos / disparos) × 100`
- **Barcos Hundidos**: Seguimiento por partida

### Sistema de Clasificación
```java
// Obtener los 10 mejores jugadores por victorias y precisión
List topPlayers = PlayerData.getTopPlayers(10);
```

---

## 🔧 Configuración

### Constantes del Juego (en GameController)
```java
private final int WIDTH_CELL = 364 / 10;   // Ancho de celda: 36.4px
private final int HEIGHT_CELL = 301 / 10;  // Alto de celda: 30.1px
private final int SIZE = 10;                // Tamaño del tablero: 10×10
private final int[] fleet = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};
```

### Parámetros de IA (en SmartAI)
```java
private int[] remainingShips = {4, 3, 2, 1}; // [fragatas, destructores, subs, portaaviones]
```


## 📚 Documentación

La documentación JavaDoc completa está disponible en la carpeta `/JavaDoc`.

### Interfaces Clave
- `IBoard` - Contrato de operaciones del tablero
- `IGame` - Contrato del controlador del juego
- `IPlayer` - Contrato de comportamiento del jugador
- `IShip` - Contrato de propiedades del barco

### Clases Clave
- `Game` - Motor principal del juego
- `SmartAI` - Oponente inteligente
- `Board` - Cuadrícula basada en HashMap
- `GameController` - Coordinación de UI
- `SerializableFileHandler` - Persistencia

---

## 👥 Autores

**Equipo de Desarrollo**  
*Proyecto Universitario - Curso de Programación Orientada a Eventos*
---

## 🎯 Objetivos del Proyecto Alcanzados

✅ Juego de Batalla Naval completamente funcional  
✅ Oponente de IA avanzado  
✅ Guardados persistentes de juego  
✅ UI/UX profesional  
✅ Estadísticas completas  
✅ Arquitectura limpia  
✅ Documentación extensa  
✅ Cobertura de pruebas unitarias  
✅ Manejo de excepciones  
✅ Soporte multi-hilo  

---

**¡Disfruta jugando Batalla Naval! ¡Que tus disparos sean precisos y tu flota victoriosa! 🚢⚓**
