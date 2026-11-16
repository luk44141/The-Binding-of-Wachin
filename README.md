DOCUMENTACION
THE BINDING OF WACHIN

Esta documentacion explica como esta organizado el codigo, que responsabilidad tiene cada parte y como se relacionan las clases. El objetivo es entender la implementacion para poder modificarla, mantenerla y explicar sus decisiones tecnicas.

El desarrollo de The Binding of Wachin fue realizado con asistencia de herramientas de inteligencia artificial, pero la planificacion y el diseño de la estructura del proyecto fueron decisiones propias.

Yo me encargue de pensar como queria que funcionara el videojuego, definir su logica, organizar los paquetes, dividir las responsabilidades de las clases y establecer como debian relacionarse los distintos sistemas. Por ejemplo, planifique el funcionamiento general del combate y como debia resolverse el daño entre personajes y enemigos.
Utilice ChatGPT como apoyo para organizar esas ideas, definir soluciones tecnicas y plantear la logica de cada funcionalidad. Luego, Codex se encargo de generar e implementar gran parte del codigo siguiendo esas indicaciones y la estructura previamente definida.
Los recursos visuales, como los sprites, personajes, enemigos y proyectiles, fueron realizados manualmente, sin utilizar inteligencia artificial.

Por ejemplo un prompt directo a codex donde yo ya tengo definido como voy a implementarlo: 

Necesito crear la clase JugadorService.java dentro de la carpeta service, esta clase debe encargarse de administrar la logica relacionada con el jugador, separandola del controlador y del modelo, tiene que incluir los metodos necesarios para gestionar el movimiento, comprobar el estado del jugador y controlar sus acciones, mantene la estructura actual del proyecto, respetando la division de responsabilidades y reutilizando las clases existentes, no podes usar Jugador.java ni PartidaController.java para implementar esta logica, la idea es que la carpeta service pueda seguir ampliandose con nuevas clases encargadas de distintas funcionalidades

Ejemplo de un prompt a chatgpt para poder definir como implementar algo:

Quiero que cuando comience la partida se muestre en pantalla el sprite de Wachin que tengo guardado en la carpeta de assets, no tengo claro cual es la mejor manera de implementarlo, quiero que me expliques como podria cargar la imagen, mostrarla en pantalla y actualizar su posicion cuando el jugador se mueva, tambien quiero organizar correctamente la carpeta y en que clase conviene colocar cada responsabilidad para mantener separada la logica del jugador y lo visual

## 1. ESTRUCTURA GENERAL DEL PROYECTO

El código fuente principal se encuentra en:

```text
src/
├── main/
│   ├── java/com/wachin/
│   │   ├── GameApp.java                 ventana, escalado, F11, cambio menú/partida
│   │   ├── Main.java                    prueba manual de la conexión a MySQL
│   │   ├── ConfiguracionPartida.java    personaje y proyectil elegidos
│   │   ├── controller/
│   │   │   ├── PartidaController.java   orquesta la partida y ejecuta el ciclo de juego
│   │   │   ├── ContextoPartida.java     estado y servicios compartidos por los controladores
│   │   │   ├── JugadorController.java   entrada, movimiento y disparo con cadencia
│   │   │   ├── EnemigosController.java  persecución, dispersión, ataques y contacto
│   │   │   ├── InvocacionesController.java  movimiento y ataques de invocaciones
│   │   │   ├── CombateController.java   proyectiles, daño y muertes
│   │   │   ├── ObjetosController.java   objetos del suelo, cartas y recompensas
│   │   │   ├── SalaController.java      salas, puertas, transiciones y victoria
│   │   │   ├── GuardadoController.java  crear y restaurar el estado de la partida
│   │   │   ├── PausaController.java     menú de pausa, guardado y carga
│   │   │   ├── Retroalimentacion.java   partículas, sacudida y sonido por evento
│   │   │   └── MapaController.java      grafo de salas y progresión
│   │   ├── model/                       Entidad, Jugador, Enemigo, Jefe, Invocacion, Sala,
│   │   │                                Carta, ObjetoSuelo, Proyectil, EstadoPartida,
│   │   │                                EstadisticasPartida y enumeraciones
│   │   ├── service/                     Combate, Colision, EnemigoIA, Jefe, Invocacion,
│   │   │                                Juego, Jugador, Sala, Progresion, Recompensa,
│   │   │                                ObjetoSuelo, Carta, Mejoras, Partida,
│   │   │                                Ajustes y Sonido
│   │   ├── view/
│   │   │   ├── game/   PartidaView, HudView, EscenarioView, JugadorVisual, EnemigoVisual,
│   │   │   │           InvocacionVisual, ProyectilVisual, ObjetoSueloVisual, PuertaVisual,
│   │   │   │           SombraVisual, AssetVisual, CatalogoAssets, Dibujos
│   │   │   ├── fx/     CapaEfectos (partículas y textos), Iluminacion
│   │   │   ├── ui/     Tema, BarraRecurso, FondoAnimado, ContenedorEscalado,
│   │   │   │           PantallaModal, Pantallas, TarjetaCarta
│   │   │   └── menu/   MenuInicial
│   │   ├── interfaces/ Atacable
│   │   ├── exception/  excepciones del dominio
│   │   └── dao/        acceso a MySQL (ConexionBD, EntidadDAO, JugadorDAO, PartidaDAO)
│   └── resources/assets/
│       ├── ASSETS.md                    rutas y nombres exactos de cada recurso
│       ├── styles/wachin.css            hoja de estilos JavaFX del tema oscuro
│       └── images/                      player, enemies, bosses, summons, projectiles,
│                                        cards, pickups, environment...
└── test/java/com/wachin/                pruebas JUnit 4 de modelo, servicios, controlador y assets
```

Cada paquete agrupa clases que cumplen una funcion relacionada. Esto evita que todo el codigo este concentrado en una sola clase y permite modificar una parte del proyecto sin tener que reescribir las demas.

2) ARQUITECTURA DEL CODIGO

El proyecto utiliza una organizacion basada en responsabilidades. Las clases no deberian encargarse de absolutamente todo, sino trabajar en conjunto.

Model: representa los objetos del sistema. Por ejemplo, Jugador almacena la vida, el dano, la experiencia y la energia. No necesita saber como se dibuja el personaje en pantalla.
View: construye la interfaz visual. Por ejemplo, PartidaView crea el escenario, muestra al jugador, agrega enemigos y actualiza los textos del HUD.
Controller: recibe las acciones del usuario y coordina lo que sucede. PartidaController interpreta el teclado, actualiza las posiciones, comprueba colisiones y solicita cambios a la vista.
Service: agrupa operaciones y reglas de logica. Por ejemplo, CombateService ejecuta ataques y comprueba si una entidad sigue viva.
Interfaces: establecen metodos que una clase debe implementar. Atacable define las operaciones relacionadas con atacar y recibir dano.
Exceptions: representan errores especificos del sistema, como intentar crear una entidad con vida invalida.

La relacion general se puede entender asi:

             GameApp
                |
                v
        PartidaController
          /     |      \
         v      v       v
       View   Services  Model
                |        |
                v        v
             Operaciones y datos

No significa que todas las clases se comuniquen directamente entre si. El controlador es el principal coordinador de la partida y utiliza los servicios para ejecutar operaciones.

3) PAQUETE MODEL: LOS OBJETOS DEL JUEGO

El paquete model contiene las clases que representan los elementos logicos del juego. Estas clases almacenan informacion y definen comportamientos propios de cada objeto.
La diferencia principal entre model y view es que el modelo representa lo que un objeto es y como funciona, mientras que la vista representa como se ve.
Por ejemplo, el jugador tiene una cantidad de vida dentro de Jugador. La barra de vida que aparece en pantalla es responsabilidad de PartidaView.

3.1) Entidad.java
Entidad es la clase base del sistema de entidades. Esta declarada como abstract, por lo que no se puede crear directamente un objeto de tipo Entidad.
Su objetivo es reunir los atributos y comportamientos que comparten los personajes, enemigos, jefes e invocaciones.

Sus atributos principales son:
- id: identificador de la entidad.
- nombre: nombre del objeto.
- vida: cantidad de vida actual.
- vidaMaxima: limite de vida.
- dano: cantidad de dano que puede provocar.
- tipo: categoria de la entidad.

El constructor valida los datos antes de asignarlos. Por ejemplo, no permite crear una entidad con vida maxima menor o igual a cero, dano negativo o nombre vacio.

Metodos importantes:

getVida(), getNombre(), getDano() y los demas getters permiten consultar los atributos.

setNombre() y setDano() modifican ciertos datos, pero primero comprueban que los valores sean validos.

recibirDano(int dano) resta vida a la entidad. Tambien evita que un dano negativo funcione como curacion y evita que la vida quede por debajo de cero.

curar(int cantidad) recupera vida, pero no permite superar la vida maxima.

Una caracteristica importante es que Entidad implementa la interfaz Atacable. Por eso, todas sus clases derivadas heredan la obligacion de cumplir ese contrato.

3.2) Jugador.java
Jugador representa al personaje controlado por el usuario. Hereda de Entidad, por lo que no necesita volver a declarar atributos como nombre, vida y dano.

Agrega atributos propios:
- experiencia: puntos obtenidos durante la partida.
- nivel: nivel actual del jugador.
- monedas: cantidad de monedas.
- energia: energia disponible.
- energiaMaxima: limite de energia.

Metodos importantes:

ganarExperiencia(int cantidad) suma experiencia cuando la cantidad recibida es positiva.

subirNivel(int experienciaNecesaria) comprueba si el jugador tiene suficiente experiencia. Si la tiene, aumenta el nivel y descuenta la experiencia utilizada.

gastarEnergia(int cantidad) comprueba que haya energia suficiente antes de descontarla.

recuperarEnergia(int cantidad) recupera energia sin superar el maximo permitido.

atacar(Entidad objetivo) ejecuta un ataque contra otra entidad y utiliza recibirDano() para aplicar el dano.

3.3) Enemigo.java
Enemigo representa a los enemigos normales. Tambien hereda de Entidad.

Agrega dos atributos propios:
- tipoEnemigo: identifica la clase o categoria del enemigo.
- velocidad: determina que tan rapido puede desplazarse.

Su metodo atacar(Entidad objetivo) aplica el dano del enemigo sobre el objetivo.

La velocidad y el movimiento visual se utilizan desde otras clases. Por ejemplo, EnemigoVisual consulta la velocidad del modelo para calcular el desplazamiento del enemigo.

3.4) Jefe.java
Jefe hereda de Enemigo. Esto significa que tambien hereda los atributos y comportamientos de Entidad.

Agrega informacion especifica de los jefes:
- nivelJefe: nivel del jefe.
- ataqueEspecial: nombre o identificador del ataque especial.
- danoAdicional: dano extra aplicado por su habilidad especial.

Su metodo ataqueEspecial(Entidad objetivo) aplica el dano normal del jefe mas el dano adicional.

La diferencia entre un enemigo normal y un jefe no esta en que sean objetos completamente separados: el jefe reutiliza la estructura de Enemigo y agrega sus propias caracteristicas.

3.5) Invocacion.java
Invocacion representa una entidad invocada. Tambien hereda de Entidad.

Agrega dos atributos:
- costoEnergia: energia necesaria para utilizarla.
- duracion: tiempo o duracion asignada a la invocacion.

Su metodo atacar(Entidad objetivo) aplica el dano que tiene configurado.

De esta manera, una invocacion puede compartir las operaciones generales de una entidad, pero conservar datos especificos de su funcionamiento.

3.6) Sala.java
Sala representa una sala del juego desde el punto de vista logico.

Almacena:
- numero: identificador de la sala.
- dificultad: dificultad asignada.
- enemigos: lista de enemigos que contiene.

Su constructor valida que el numero y la dificultad sean positivos.

El metodo getEnemigos() devuelve una lista no modificable desde afuera. Esto evita que otra clase pueda alterar directamente la coleccion interna de enemigos mediante el getter.

4) PAQUETE CONTROLLER: COORDINACION DEL JUEGO
El controlador conecta los datos del modelo, la logica de los servicios y los elementos visuales.

No es el encargado de dibujar cada elemento desde cero. Su funcion es decidir cuando deben crearse, moverse, eliminarse o actualizarse.

4.1) PartidaController.java y los controladores de la partida
PartidaController dejo de concentrar toda la logica. Ahora solo orquesta una partida: crea o restaura el estado,
ejecuta el ciclo del juego (AnimationTimer), recibe el teclado y decide cuando se muestra la derrota o la victoria.
Cada responsabilidad vive en una clase propia del paquete controller, que comparten un ContextoPartida (posicion
del jugador, listas de entidades visuales, banderas de estado y servicios):

- JugadorController: teclas presionadas, movimiento (WASD), disparo en una sola direccion con las flechas y
  cadencia de 0,24 s (mantener la flecha dispara de forma continua), invulnerabilidad.
- EnemigosController: persecucion con dispersion, tiradores que mantienen la distancia, ataques y avisos de los
  jefes, dano por contacto. Los enemigos se bloquean entre si y se empujan suavemente para rodear al jugador.
- InvocacionesController: movimiento suavizado y ataques de las invocaciones. No colisionan con nada.
- CombateController: proyectiles del jugador y hostiles, dano, muerte de enemigos, experiencia y drops.
- ObjetosController: recogida de objetos del suelo, seleccion de cartas, reemplazo de invocaciones y recompensas de jefe.
- SalaController: preparar y limpiar salas, puertas, transiciones con fundido y deteccion de sala completada.
- GuardadoController y PausaController: instantanea y restauracion del estado, y menu de pausa.
- Retroalimentacion: un unico lugar que traduce eventos (disparo, impacto, muerte...) en particulas, sacudida y sonido.

Metodos publicos de PartidaController: iniciar(), detener(), alPresionarTecla(KeyCode) y alLiberarTecla(KeyCode).
Sus salidas hacia GameApp se agrupan en el record PartidaController.Acciones (reintentar, cargar, volver al menu).

4.2) MapaController.java
MapaController administra la estructura de salas y el progreso del jugador por el mapa.

Utiliza una estructura de tipo grafo: cada sala es un nodo y las puertas representan las conexiones entre ellas.

La clase interna SalaNodo guarda informacion de cada sala, como su identificador, profundidad, mapa, si es una sala de jefe y las salidas disponibles.

Las conexiones se almacenan en un EnumMap, utilizando las direcciones definidas en PuertaVisual.Direccion.

Metodos importantes:

iniciar() crea la primera sala y la establece como sala actual.

entrarPor() intenta ingresar a una sala mediante una direccion. Si no existe una salida o la sala ya fue visitada, no permite el ingreso.

prepararSalidas() genera las conexiones de cada sala. Las salas normales pueden tener dos salidas y las salas cercanas al jefe pueden tener una.

obtenerJefe() reutiliza el nodo de jefe correspondiente a cada mapa.

opuesta() calcula la direccion contraria a una puerta. Por ejemplo, si se ingresa por arriba, la puerta de entrada queda abajo.

La clase utiliza HashSet para registrar salas visitadas y HashMap para almacenar los jefes asociados a cada mapa.

5) PAQUETE SERVICE: LOGICA Y OPERACIONES
Los servicios agrupan operaciones que no necesitan estar dentro de una clase del modelo o del controlador. Esto permite mantener responsabilidades separadas.

5.1) CombateService.java

Se ocupa de ejecutar ataques y comprobar el estado de las entidades.

atacar(Entidad atacante, Entidad objetivo) verifica que ambos objetos existan y que el dano no sea negativo. Luego ejecuta el metodo atacar() del atacante.

estaVivo(Entidad entidad) devuelve true cuando la entidad existe y tiene vida mayor a cero.

estaDerrotado(Entidad entidad) devuelve true cuando la entidad no existe o tiene vida menor o igual a cero.

Un detalle importante es que el servicio recibe objetos del tipo Entidad, no necesita recibir por separado un Jugador, un Enemigo o un Jefe. Esto permite aprovechar la herencia y el polimorfismo.

5.2) JuegoService.java
Mantiene el estado logico principal de la partida.

Almacena una referencia al jugador, una lista de enemigos, una lista de invocaciones activas y una referencia al jefe.

iniciarPartida() establece el jugador y limpia las colecciones para comenzar una nueva partida.

agregarEnemigo() agrega un enemigo a la lista. Si el objeto recibido es un jefe, tambien actualiza la referencia al jefe.

derrotarEnemigo() elimina al enemigo derrotado y entrega experiencia al jugador. La cantidad depende de si se trata de un jefe o de un enemigo normal.

jugadorEstaVivo() comprueba si el jugador existe y conserva vida.

5.3) JugadorService.java
Agrupa operaciones relacionadas con los recursos del jugador.

ganarExperiencia() delega la suma de experiencia al objeto Jugador.

subirNivel() solicita al jugador que suba de nivel y propaga la excepcion si no tiene suficiente experiencia.

gastarEnergia() y recuperarEnergia() delegan las operaciones de energia.

usarInvocacion() comprueba que existan el jugador y la invocacion, y descuenta la energia necesaria.

5.4) ColisionService.java
Calcula las colisiones entre elementos del juego.

colisionaProyectilEnemigo() comprueba si el recorrido de un proyectil se cruza con el recorrido de un enemigo.

colisionaJugadorEnemigo() comprueba si el jugador entra en contacto con un enemigo.

segmentosSeCruzan() realiza el calculo matematico de la distancia entre recorridos y determina si estan lo suficientemente cerca como para considerarlos una colision.

distancia() calcula la distancia entre dos puntos utilizando Math.hypot().

El servicio guarda los calculos de colision en una clase independiente para que el controlador no tenga que contener todas las operaciones matematicas.

5.5) SalaService.java
Se encarga de generar salas, enemigos y jefes.

generarSala() crea una sala con una cantidad de enemigos determinada por la dificultad y la profundidad.

generarJefe() construye un jefe con vida, dano, velocidad y dano adicional segun el mapa.

crearEnemigo() genera enemigos normales con atributos que aumentan segun el nivel.

esSalaJefe() determina si una sala corresponde a un jefe segun su numero.

obtenerNumeroMapa() calcula a que mapa pertenece una sala.

Este servicio se ocupa de generar los objetos logicos. La representacion visual de esos enemigos se crea posteriormente desde el controlador y las clases de view.

6) PAQUETE VIEW: REPRESENTACION VISUAL
El paquete view contiene las clases que construyen y administran lo que aparece en pantalla. En JavaFX, la interfaz
es un arbol de nodos; mover un nodo o cambiar sus propiedades actualiza lo que se ve.

Principio de diseno: todo lo que pueda dibujarse por codigo tiene una alternativa visual de calidad cuando falta el PNG.
Los sprites existentes siguen funcionando; si falta uno, se usa la ilustracion generada por Dibujos.

6.1) PartidaView.java
Compone las capas de la partida (de abajo hacia arriba): escenario, objetos del suelo, entidades ordenadas por
profundidad (viewOrder segun la coordenada Y), luz del jugador, proyectiles, efectos, vineta, destello de dano, HUD,
fundido de transiciones y pantalla modal. Ofrece sacudir(), transicionar(), actualizarHUD() y mostrarPantalla().

6.2) AssetVisual.java, CatalogoAssets.java y Dibujos.java
CatalogoAssets concentra las rutas (la primera de cada lista es el nombre documentado en ASSETS.md). AssetVisual carga
cada imagen una sola vez con cache y devuelve el sprite real o la ilustracion procedural. Dibujos genera, con Canvas,
los personajes, jefes, invocaciones, proyectiles e iconos que no tienen sprite propio.

6.3) EscenarioView.java
Pinta el piso, las paredes, la decoracion, las antorchas y las puertas. El fondo se compone una vez por mapa, tipo de
sala y variante y se guarda en cache. Las salas de jefe usan una variante roja con circulo de runas.

6.4) Visuales de entidades
JugadorVisual, EnemigoVisual, InvocacionVisual, ProyectilVisual, ObjetoSueloVisual y PuertaVisual muestran su modelo,
con sombra y animaciones procedurales (caminar, disparar, destello al recibir dano, aparicion). EnemigoVisual tambien
dibuja los avisos de ataque de los jefes. Ninguna contiene reglas de juego.

6.5) HudView.java y el paquete ui
HudView muestra vida, experiencia, nivel, invocaciones con vida y energia, cartas, proyectil, progreso del mapa, barra
de jefe, avisos y carteles. El paquete ui contiene el tema (Tema + wachin.css), BarraRecurso, PantallaModal, Pantallas
(pausa, ajustes, guardado, cartas, derrota, victoria...), TarjetaCarta, FondoAnimado y ContenedorEscalado.

6.6) MenuInicial.java
Menu principal: eleccion de personaje y de proyectil inicial, jugar, cargar partida (desde MySQL), ajustes y salir.
Recibe sus operaciones mediante el record MenuInicial.Acciones.

6.7) CapaEfectos.java e Iluminacion.java (paquete fx)
CapaEfectos dibuja particulas, anillos y textos flotantes sobre un unico Canvas con un maximo de particulas; su cantidad
se escala con el ajuste de calidad. Iluminacion aporta la vineta, la luz que sigue al jugador y el destello de dano.

7) COMO SE DIBUJA Y ACTUALIZA EL JUEGO EN PANTALLA
Esta es una de las partes mas importantes para entender el funcionamiento tecnico del proyecto.

JavaFX no dibuja automaticamente los personajes a partir de las clases del modelo. Para mostrar un objeto, se necesita crear un nodo visual y agregarlo a un contenedor que pertenezca a la escena.

7.1) Creacion de la ventana
El punto de entrada es GameApp.java, que extiende la clase Application de JavaFX.

Cuando se ejecuta el programa, JavaFX llama al metodo start(Stage stage).

Dentro de ese metodo se configura la ventana, se crea el menu inicial y se establece una Scene.

Los conceptos principales son:

Stage: representa la ventana de la aplicacion.

Scene: representa el contenido que se muestra dentro de la ventana.

Node: es la clase base de los elementos visuales de JavaFX.

Pane: es un contenedor que permite agregar y posicionar nodos.

En este proyecto, PartidaView utiliza un Pane como raiz de la pantalla de juego.

7.2) Carga de imagenes
Cuando se necesita mostrar al jugador, un enemigo o un proyectil, se utiliza AssetVisual.crear().

El proceso es:
1. Se recibe la ruta del archivo de imagen.
2. Se busca el recurso dentro de resources.
3. Se crea un objeto Image a partir del archivo.
4. Se crea un ImageView para mostrar esa imagen.
5. Se configura su ancho, alto y posicion.
6. Se devuelve el nodo para agregarlo a la pantalla.

Por ejemplo, cuando PartidaController crea al jugador, utiliza la ruta que viene de ConfiguracionPartida. Luego agrega el resultado a la vista mediante agregarJugador().

7.3) Posicionamiento de los elementos
Los elementos visuales tienen coordenadas dentro del escenario.

En este proyecto, el jugador se representa mediante las variables jugadorX y jugadorY del controlador.

Cuando cambia su posicion, se llama a AssetVisual.moverAlCentro() para actualizar las coordenadas del nodo visual.

La idea es mantener sincronizada la posicion logica del personaje con su representacion en pantalla.

7.4) AnimationTimer: el ciclo del juego
El movimiento no se realiza una sola vez. El juego necesita actualizarse continuamente mientras la partida esta activa.
Para eso se utiliza AnimationTimer, una clase de JavaFX que ejecuta repetidamente el metodo handle(long ahora).
Dentro de PartidaController, este ciclo actualiza el estado del juego.

El orden general es:
1. Si la partida esta pausada, no avanza; si esta terminada o en transicion, solo se animan los efectos.
2. Calcula el tiempo transcurrido desde la ultima actualizacion (limitado a 0,05 s).
3. JugadorController: invulnerabilidad, cadencia, movimiento y disparo.
4. SalaController: comprueba si el jugador cruzo una puerta abierta (inicia el fundido de transicion).
5. EnemigosController: movimiento, dispersion, ataques de jefes y dano por contacto.
6. InvocacionesController y ObjetosController: invocaciones y recogida de objetos.
7. CombateController: proyectiles hostiles y del jugador, impactos, muertes y drops.
8. verificarEstado(): derrota, sala completada, carta de las salas pares y victoria de la sala 20.
9. Actualiza el HUD y las animaciones de la vista.

7.5) Delta time: movimiento independiente de los FPS

El juego utiliza una variable llamada deltaTiempo.

Representa el tiempo transcurrido entre una actualizacion y la siguiente, expresado en segundos.

En lugar de mover al personaje una cantidad fija de pixeles por actualizacion, se multiplica la velocidad por el tiempo transcurrido.

Formula:
desplazamiento = velocidad x deltaTiempo

Por ejemplo, si el jugador tiene una velocidad de 225 unidades por segundo y transcurrieron 0,02 segundos:

225 x 0,02 = 4,5

El jugador se desplaza 4,5 unidades en ese intervalo.

Esto permite que la velocidad sea mas consistente aunque cambie la cantidad de actualizaciones por segundo.

7.6) Entrada del teclado
GameApp recibe los eventos de teclado de JavaFX.
Cuando se presiona una tecla, se llama a teclaPresionada() y luego a manejarTecla() cuando corresponde.
Cuando se suelta, se llama a teclaLiberada().
El controlador utiliza un HashSet<KeyCode> para guardar las teclas que permanecen presionadas. Durante cada actualizacion del juego consulta ese conjunto para calcular el movimiento.
De esta manera, mantener presionada una tecla permite un movimiento continuo sin necesitar que JavaFX genere un nuevo evento de presion constantemente.

7.7) Colisiones
Las colisiones se calculan desde ColisionService.
El controlador le proporciona las posiciones del jugador, los proyectiles y los enemigos. El servicio realiza los calculos y devuelve un valor booleano.
Si el resultado es true, el controlador ejecuta la accion correspondiente. Por ejemplo, si un proyectil impacta contra un enemigo, se llama al servicio de combate para aplicar el dano.
El uso de las posiciones anteriores y actuales permite tener en cuenta el recorrido de los objetos durante una actualizacion, en lugar de comparar solamente sus posiciones finales.

7.8) HUD y barras de vida

El HUD es la informacion que aparece en pantalla durante la partida.

PartidaView contiene los textos y rectangulos necesarios para mostrar vida, experiencia, nivel, cantidad de enemigos, mapa y sala.

PartidaController obtiene los valores del modelo y los envia a actualizarHUD().
Las barras de vida se actualizan calculando la proporcion entre la vida actual y la vida maxima:
porcentaje = vida actual / vida maxima
Ese porcentaje se utiliza para modificar el ancho del rectangulo que representa la barra.

En EnemigoVisual, cada enemigo tiene su propia barra. El jefe tambien cuenta con una barra general en PartidaView.

8) PROGRAMACION ORIENTADA A OBJETOS APLICADA AL PROYECTO

Esta seccion explica los conceptos principales de POO utilizando clases reales del codigo.

8.1) Herencia: reutilizar atributos y metodos

La herencia permite crear una clase nueva a partir de otra existente. La clase derivada recibe sus atributos y metodos heredables, y puede agregar funcionalidades propias o modificar comportamientos.

En Java se utiliza la palabra extends.

En el proyecto, la estructura principal es:

             Entidad (abstract)
                  |
        ┌─────────┼──────────┐
        |         |          |
     Jugador   Enemigo   Invocacion
                  |
                 Jefe

Entidad contiene los atributos comunes: nombre, vida, vida maxima y dano.

Jugador, Enemigo e Invocacion heredan esos atributos y agregan sus propios datos.

Jefe hereda de Enemigo, por lo que tambien recibe todo lo que Enemigo heredo de Entidad.

Ejemplo:

public class Jugador extends Entidad {
    private int experiencia;
    private int nivel;
}

Esta declaracion significa que Jugador es una clase derivada de Entidad.

No necesita volver a declarar vida, nombre o dano, porque esos atributos ya pertenecen a la clase padre.

Para que sirve: evita repetir codigo. Si todas las entidades necesitan recibir dano, ese comportamiento se puede definir una sola vez en Entidad y reutilizar en las clases derivadas.

8.2) Abstraccion: definir una estructura comun

La abstraccion permite representar las caracteristicas esenciales de un conjunto de objetos sin tener que desarrollar todos sus detalles en un unico lugar.

En el proyecto se aplica mediante:

public abstract class Entidad implements Atacable

La palabra abstract indica que Entidad es una clase abstracta.

No se puede crear directamente una instancia de ella:

Entidad entidad = new Entidad(...); // No permitido

En cambio, se crean objetos de sus clases concretas:

Jugador jugador = new Jugador(...);
Enemigo enemigo = new Enemigo(...);

La clase abstracta establece una estructura comun, pero cada tipo de entidad puede tener sus propias caracteristicas.

Diferencia entre abstraccion y herencia: la abstraccion define que elementos comunes deberian existir; la herencia permite que otras clases reutilicen esa estructura.

8.3) Interfaces: establecer contratos

Una interfaz define un conjunto de metodos que las clases que la implementan deben proporcionar.

En Java se utiliza la palabra implements.

En el proyecto existe la interfaz Atacable:

public interface Atacable {
    void atacar(Entidad objetivo) throws Exception;
    void recibirDano(int dano) throws Exception;
}

Esto establece que un objeto que implemente Atacable debe tener esos dos metodos.

Entidad implementa esa interfaz:

public abstract class Entidad implements Atacable

Por lo tanto, las clases que heredan de Entidad tambien reciben ese contrato.

Jugador, Enemigo e Invocacion implementan su propia version de atacar(). En cambio, recibirDano() ya esta implementado en Entidad y se hereda.

Para que sirve: permite establecer comportamientos comunes sin obligar a que todas las clases tengan la misma estructura interna.

Una interfaz define que operaciones deben existir, pero no necesariamente como se realizan.

8.4) Polimorfismo: un mismo tipo, diferentes comportamientos

El polimorfismo permite trabajar con objetos de diferentes clases mediante un tipo comun.

En este proyecto, CombateService recibe objetos de tipo Entidad:

public void atacar(Entidad atacante, Entidad objetivo)

Esto permite enviarle distintos tipos de objetos:

combateService.atacar(jugador, enemigo);
combateService.atacar(enemigo, jugador);
combateService.atacar(jefe, jugador);

Aunque el parametro es de tipo Entidad, el metodo que se ejecuta depende del objeto real que se esta utilizando.

Por ejemplo, si el atacante es un Jugador, se ejecuta el atacar() de Jugador. Si es un Enemigo, se ejecuta el de Enemigo.

Esto ocurre porque las clases derivadas sobrescriben el metodo heredado o implementado.

Diferencia entre herencia y polimorfismo: la herencia permite reutilizar y extender clases. El polimorfismo permite utilizar objetos de distintas clases mediante un tipo comun, respetando el comportamiento de cada uno.

8.5) Encapsulamiento: proteger los datos

El encapsulamiento consiste en restringir el acceso directo a los atributos de una clase y controlar como se consultan o modifican.

En el proyecto, los atributos generalmente se declaran como private.

Por ejemplo:

private int vida;
private int vidaMaxima;

Esto impide que otra clase modifique directamente esos valores.

Para consultar la vida se utiliza:

public int getVida() {
    return vida;
}

Para modificar ciertos atributos se utilizan metodos que realizan validaciones.

Por ejemplo, setDano() comprueba que el valor no sea negativo antes de asignarlo.

Esto permite proteger el estado interno de los objetos y evitar valores incorrectos.

8.6) Composicion y asociaciones: relacionar objetos

Los objetos pueden relacionarse entre si para formar sistemas mas complejos.

En el proyecto, PartidaController mantiene referencias a distintos servicios y a la vista. Tambien contiene listas de proyectiles y enemigos visuales.

Sala contiene una lista de objetos Enemigo.

JuegoService mantiene una lista de enemigos y otra de invocaciones.

Estas relaciones permiten que cada clase trabaje con objetos de otras clases sin tener que concentrar toda la funcionalidad en un unico lugar.

8.7) Enumeraciones (enum)

Un enum permite definir un conjunto cerrado de valores posibles.

En el proyecto se utiliza en ConfiguracionPartida:

public enum Personaje {
    WACHIN(...),
    WACHINA(...)
}

Y tambien en TipoProyectil:

public enum TipoProyectil {
    LAGRIMA(...),
    GOTA(...)
}

Tambien se utiliza PuertaVisual.Direccion para definir las cuatro direcciones posibles.

Esto evita utilizar cadenas de texto arbitrarias y reduce errores al seleccionar opciones.

8.8) Colecciones de objetos

El proyecto utiliza distintas colecciones de Java para administrar grupos de elementos.

ArrayList: permite almacenar listas de objetos, como los proyectiles y enemigos visuales.

HashSet: almacena elementos sin repetirlos. Se utiliza para registrar teclas presionadas y salas visitadas.

HashMap: permite relacionar claves con valores. En MapaController se utiliza para asociar mapas con sus respectivos jefes.

EnumMap: es una implementacion de mapa especialmente pensada para utilizar enumeraciones como claves. Se usa para relacionar las direcciones de las puertas con las salas de destino.

Estas estructuras permiten organizar los datos y acceder a ellos de acuerdo con las necesidades de cada clase.

9) PAQUETE EXCEPTION: MANEJO DE ERRORES

El paquete exception contiene excepciones personalizadas para representar situaciones invalidas del sistema.

En Java, una excepcion permite interrumpir el flujo normal de una operacion cuando ocurre un problema y comunicar que algo no se pudo realizar correctamente.

El proyecto define excepciones como:

- DanoInvalidoException: representa un dano que no cumple las condiciones permitidas.
- NombreInvalidoException: se utiliza cuando un nombre no es valido.
- VidaInvalidaException: representa valores de vida incorrectos.
- VidaMaximaException: se utiliza cuando una curacion supera la vida maxima.
- EnergiaInsuficienteException: indica que el jugador no tiene energia suficiente.
- ExperienciaInsuficienteException: indica que no tiene experiencia suficiente para subir de nivel.
- InvocacionNoDisponibleException: representa una invocacion que no puede utilizarse.

Por ejemplo, DanoInvalidoException extiende de Exception:

public class DanoInvalidoException extends Exception {
    public DanoInvalidoException(String mensaje) {
        super(mensaje);
    }
}

super(mensaje) llama al constructor de la clase padre y guarda el mensaje que describe el error.

Cuando una funcion puede generar una excepcion comprobada, puede declararla mediante throws. La clase que la utiliza debe manejarla mediante try-catch o propagarla.

Esto permite distinguir errores especificos y evitar que las operaciones continuen con datos invalidos.

10) FLUJO INTERNO DE EJECUCION

Esta seccion explica como se conectan las clases desde que se inicia el programa hasta que termina una partida.

10.1) Inicio de la aplicacion

La ejecucion comienza en GameApp.main():

public static void main(String[] args) {
    launch(args);
}

launch() inicia el entorno de JavaFX. Luego JavaFX ejecuta el metodo start().

Dentro de start() se crea el menu inicial y se muestra la ventana.

10.2) Seleccion de configuracion

El usuario selecciona un personaje y un tipo de proyectil en MenuInicial.

Cuando presiona jugar, el menu crea un objeto ConfiguracionPartida.

Ese objeto almacena las opciones seleccionadas y permite obtener las rutas de las imagenes correspondientes.

10.3) Creacion de la partida

GameApp recibe la configuracion y ejecuta iniciarPartida().

Ese metodo crea una instancia de PartidaView y otra de PartidaController.

Despues crea una nueva Scene, establece los eventos del teclado y comienza la partida llamando a partidaController.iniciar().

10.4) Creacion del jugador y la primera sala

PartidaController.iniciar() llama a crearJugador().

El jugador se crea como un objeto de tipo Jugador y se registra en JuegoService.

Luego se crea su representacion visual y se agrega a PartidaView.

Despues se inicia el mapa mediante MapaController.iniciar() y se prepara la primera sala.

10.5) Actualizacion de la partida

Una vez iniciada, AnimationTimer ejecuta continuamente el ciclo de actualizacion.

El controlador lee las teclas, calcula movimientos, actualiza enemigos y proyectiles, comprueba colisiones y actualiza la informacion visual.

Los servicios realizan operaciones especificas cuando el controlador las necesita.

10.6) Combate

Cuando un proyectil colisiona con un enemigo, PartidaController llama a atacarEnemigo().

Ese metodo obtiene el objeto Enemigo asociado al elemento visual y utiliza CombateService para ejecutar el ataque.

Si el enemigo pierde toda su vida, se elimina su representacion visual y JuegoService registra su derrota y entrega experiencia.

10.7) Cambio de sala

Cuando todos los enemigos fueron derrotados, verificarEstado() detecta que la lista de enemigos visuales esta vacia.

En ese momento se habilitan las puertas.

Si el jugador llega a una salida valida, MapaController determina la sala de destino y PartidaController prepara el nuevo escenario.

10.8) Fin de la partida

Cuando el jugador pierde toda su vida, verificarEstado() detecta que ya no esta vivo.

El controlador detiene el ciclo de actualizacion y ejecuta la funcion que recibe mediante alPerder.

GameApp utiliza esa funcion para mostrar el menu de reintento, conservando la configuracion seleccionada anteriormente.

11) REFERENCIA RAPIDA DE CLASES Y METODOS IMPORTANTES

GameApp: iniciar JavaFX y administrar las escenas.
ConfiguracionPartida: guardar las opciones elegidas.
Entidad: definir los datos y comportamientos comunes.
Jugador: administrar los atributos y acciones del jugador.
Enemigo: representar enemigos normales.
Jefe: agregar caracteristicas especiales a los enemigos.
Invocacion: representar entidades invocadas.
Sala: almacenar los enemigos y la dificultad de una sala.
PartidaController: coordinar la ejecucion de la partida.
MapaController: administrar las conexiones entre salas.
CombateService: ejecutar ataques y comprobar derrotas.
ColisionService: calcular colisiones.
JuegoService: mantener el estado logico de la partida.
JugadorService: administrar experiencia y energia.
SalaService: generar salas, enemigos y jefes.
PartidaView: construir y actualizar la pantalla.
AssetVisual: cargar imagenes y crear nodos visuales.
EnemigoVisual: administrar movimiento y representacion de enemigos.
Proyectil: administrar movimiento y datos de los proyectiles.
PuertaVisual: representar las puertas.
MenuInicial: construir el menu y recibir las opciones.

12) DEPENDENCIAS Y CONFIGURACION

El proyecto utiliza Maven para administrar las dependencias, configurar la compilacion y ejecutar la aplicacion.

La configuracion se encuentra en pom.xml.

Java: el proyecto establece Java 25 mediante la propiedad maven.compiler.release.

JavaFX Controls: proporciona componentes de interfaz como botones, listas desplegables y otros controles.

JavaFX Graphics: proporciona las herramientas graficas necesarias para trabajar con escenas, nodos, imagenes, formas y animaciones.

MySQL Connector/J: permite la conexion con MySQL mediante JDBC. El proyecto tambien contiene clases DAO para operaciones de acceso a datos, aunque la logica principal de la partida se encuentra separada de esa capa.

JUnit: se incluye como dependencia para realizar pruebas automatizadas.

Para ejecutar el proyecto desde una terminal ubicada en la carpeta que contiene pom.xml, se utiliza:

mvn clean javafx:run

clean elimina los archivos generados por compilaciones anteriores. javafx:run ejecuta la aplicacion utilizando la configuracion de JavaFX definida en Maven.

11) ESTADO DE IMPLEMENTACION: LO DOCUMENTADO FRENTE A LO REAL

Comparacion de CosasAgregar.txt, este README y ASSETS.md con el codigo (actualizado el 13/11/2026).

Implementado:
- Sprites de paredes, piso y puerta, con variante de sala de jefe (piso rojo con runas, puerta dorada y archivos opcionales boss_*).
- Jefes: Palinni (sala 5), Emisor de rayos (sala 10), Artillero (sala 15) y jefe con cuernos (sala 20; vuelve cada 20 salas desde la 40).
- Disparo en una sola direccion (flechas), ahora con cadencia y disparo continuo al mantener la flecha.
- Enemigos de dos tipos: policia (cuerpo a cuerpo) y tirador (a distancia, retrocede si te acercas).
- Invocaciones Gusano (tanquea y protege), Murcielago (cuerpo a cuerpo) y Robot (laser).
- Recompensas de jefe: sala 10 aumenta la vida maxima (+25%); sala 15 ofrece elegir una carta.
- Menu de pausa con cartas, invocaciones, guardar y cargar partida (3 espacios en MySQL), ajustes.
- Colisiones: el jugador y los enemigos no atraviesan a los enemigos; los enemigos se dispersan y rodean al jugador.
- Animaciones de caminar y disparar (procedurales, sin hojas de sprites).
- Invocaciones sin colision y con movimiento suavizado.
- Los jefes sueltan una invocacion con 75% de probabilidad.
- Seleccion de proyectil inicial en el menu (estaba documentada en este README pero no existia).

Corregido durante esta revision:
- La recompensa de la sala 15 no existia: ahora ese jefe ofrece la eleccion de carta.
- El jefe con cuernos aparecia una sola vez: ahora regresa cada 20 salas.
- Las rutas de piso, pared y puerta no coincidian con los archivos, asi que nunca se cargaban. Ver ASSETS.md.
- emisor_rayos.png tenia fondo blanco solido (cuadrado blanco en pantalla).
- Cada proyectil volvia a leer su PNG del disco: ahora las imagenes se cargan una sola vez.
- La pantalla de victoria era inalcanzable (esCierreDeProgresion siempre devolvia false): ahora aparece al vencer
  al jefe de la sala 20 y permite seguir explorando o volver al menu.
- Tres pruebas existentes tenian expectativas desactualizadas respecto al codigo (jefe de la sala 10, intervalo del
  tirador y recompensa de la sala 15) y se actualizaron.

Incompleto o dependiente de recursos externos:
- Sprites propios de: tirador, jefes de las salas 15 y 20, invocaciones, proyectiles hostiles, cartas, puerta abierta y
  puertas de jefe. Hasta que existan se muestra una ilustracion dibujada por codigo (ver ASSETS.md).
- Guardar y cargar requiere MySQL (ver the_binding_of_wachin.sql y ConexionBD). Sin base de datos el juego muestra el
  error en pantalla y sigue funcionando.
- La energia del jugador existe en el modelo pero ninguna mecanica la consume, por eso el HUD solo muestra las barras
  de vida y experiencia del jugador y las de vida y energia de cada invocacion.
- "La gota esta bugeada" (CosasAgregar.txt) no indica cual es el fallo. Se reviso la carga, la rotacion, la hitbox, el
  objeto del suelo y el cambio de proyectil de la gota y no se encontro un error de logica; si persiste, conviene
  describir como se manifiesta.

12) EFECTOS, ANIMACIONES Y AJUSTES

- Particulas, anillos y textos de dano: CapaEfectos (un solo Canvas, maximo de particulas acotado).
- Sacudida de camara, destello rojo al recibir dano, fundido negro al cambiar de sala y vineta: PartidaView e Iluminacion.
- Aparicion de enemigos y jefes con animacion; los jefes muestran un cartel y no atacan mientras aparecen.
- Avisos de ataque de los jefes (carril de embestida, disparo y laser, anillo radial y zonas de impacto).
- Sonido: SonidoService sintetiza los efectos en memoria; si existe assets/audio/<efecto>.wav se usa ese archivo.
- Ajustes (AjustesService, guardados con java.util.prefs): volumen, calidad de efectos, sacudida y numeros de dano.
- La ventana es redimensionable (ContenedorEscalado mantiene la proporcion) y F11 alterna la pantalla completa.

13) PRUEBAS

Las pruebas JUnit 4 estan en src/test/java y cubren modelo, servicios, MapaController, el catalogo de assets (incluida
la coherencia con ASSETS.md y la transparencia del jefe emisor), los ajustes y el limite de FPS. Se ejecutan con:

mvn test

El codigo de vistas y controladores de partida depende de JavaFX y se verifico de forma manual, no con pruebas automaticas.

14) RENDIMIENTO Y LIMITE DE FPS

JavaFX limita la animacion a 60 fps por defecto. GameApp aplica al arrancar las propiedades del limite elegido en
Ajustes (AjustesService.propiedadesDeRendimiento) sin pisar las que se pasen con -D:

- 60 / 144 / 240 FPS: javafx.animation.pulse=N (con VSync, la velocidad real no supera la frecuencia del monitor).
  El valor por defecto es 240: se adapta al monitor, sea de 60, 144 o 240 Hz.
- SIN LIMITE: javafx.animation.fullspeed=true y prism.vsync=false (puede haber tearing y usa toda la CPU/GPU).

El cambio de limite se aplica al reiniciar el juego. F3 muestra u oculta un contador de FPS en pantalla.

Que se optimizo (medido con un renderizador por software, donde cada mejora se nota mucho mas que con GPU):
- Los textos del HUD usaban contorno nativo (StrokeType.OUTSIDE), que cuesta cerca de un tercio del fotograma. Ahora
  EtiquetaContorno dibuja una sombra con un segundo texto y todo el HUD estatico se guarda como bitmap (cache).
- Las barras (BarraRecurso) ya no repintan mientras estan en reposo.
- Sombras, brillos, halos, auras y portales usan imagenes compartidas pregeneradas (Brillo) en lugar de degradados
  radiales y mezclas ADD por nodo. Se quitaron los DropShadow del juego.
- La vineta de pantalla se pinta una vez en el fondo de cada sala en lugar de cubrir la pantalla en cada fotograma.
- El fondo del menu es una imagen fija y solo las brasas se animan.

Resultado en el entorno de pruebas (sin GPU, sin limite de fotogramas): de ~5 fps a ~35 fps en la misma partida. Con una GPU
real el margen es mucho mayor, pero esa cifra no se pudo medir aqui. Si el juego sigue lento en tu equipo, ejecutalo con
-Dprism.verbose=true: si imprime "sw" como pipeline, JavaFX esta usando renderizado por software (sin aceleracion).

NOTA TECNICA SOBRE LA IMPLEMENTACION ACTUAL (sirve para mantener la documentacion actualizada)

Esta documentacion describe el estado del proyecto tras la revision del 13/11/2026. Las secciones 3, 5, 8 y 10 conservan
su explicacion original; donde nombran PartidaController, PartidaView o Proyectil como unicos responsables, la
responsabilidad esta ahora repartida segun las secciones 1, 4.1 y 6. El paquete DAO solo se usa para guardar y cargar
partidas y para la conexion con MySQL; no interviene en el combate ni en el movimiento.
