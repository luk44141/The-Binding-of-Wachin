# The Binding of Wachin

Desarrolladores: 

- Luca Caprio
- Thiago Llano

Videojuego desarrollado en Java como proyecto integrador de Programacion Orientada a Objetos.

## Descripcion

The Binding of Wachin es un videojuego de accion y exploracion inspirado en los roguelike. El jugador controla a Wachin, un personaje que debe atravesar distintas salas, enfrentarse a enemigos y derrotar jefes para avanzar.

Durante la partida, el jugador puede combatir utilizando proyectiles, obtener mejoras y conseguir invocaciones que lo ayudan durante el recorrido. La dificultad aumenta progresivamente a medida que avanza por las salas.

## Caracteristicas principales

- Movimiento del personaje y sistema de combate.
- Disparo de proyectiles en distintas direcciones.
- Enemigos con diferentes comportamientos.
- Jefes con mecanicas y ataques propios.
- Sistema de salas conectadas mediante puertas.
- Progresion de dificultad durante la partida.
- Invocaciones que ayudan al jugador en combate.
- Sistema de vida y dano.
- Menu principal y menu de pausa.
- Seleccion de configuraciones para el personaje y sus proyectiles.
- Interfaz visual con informacion de la partida.
- Persistencia de datos mediante MySQL.

## Tecnologias utilizadas

- **Java 25:** lenguaje principal del proyecto.
- **JavaFX:** desarrollo de la interfaz grafica y los elementos visuales.
- **MySQL:** almacenamiento persistente de datos.
- **Maven:** administracion de dependencias y ejecucion del proyecto.
- **Git y GitHub:** control de versiones y trabajo colaborativo.

## Arquitectura del proyecto

El proyecto esta organizado en diferentes paquetes para separar responsabilidades y facilitar el mantenimiento del codigo.

| Paquete | Responsabilidad |
|---|---|
| `model` | Contiene las clases que representan los objetos y entidades del juego. |
| `dao` | Se encarga de las operaciones de acceso a la base de datos. |
| `service` | Contiene la logica y las reglas principales del juego. |
| `controller` | Coordina las acciones del usuario con la logica del programa. |
| `view` | Contiene las pantallas y los elementos visuales de JavaFX. |
| `exception` | Contiene las excepciones propias del proyecto. |
| `interfaces` | Define contratos que pueden implementar distintas clases. |

Esta separacion permite mantener el codigo organizado y evita concentrar toda la funcionalidad en una sola clase.

## Base de datos

El proyecto utiliza MySQL para almacenar informacion de manera persistente.

El acceso a los datos se realiza mediante clases DAO, que separan las consultas y operaciones SQL del resto de la aplicacion.

La conexion se administra desde `ConexionBD`, mientras que los DAO correspondientes se encargan de las operaciones sobre las distintas entidades.

Para ejecutar el proyecto correctamente, es necesario contar con MySQL instalado y configurar la conexion de acuerdo con los datos de la base de datos utilizada.

## Requisitos

- JDK 25.
- Maven.
- MySQL Server.
- Una base de datos configurada para el proyecto.

## Ejecucion

1. Clonar el repositorio:

```bash
git clone https://github.com/luk44141/The-Binding-of-Wachin.git
```

2. Entrar en la carpeta del proyecto:

```bash
cd The-Binding-of-Wachin
```

3. Configurar la conexion a MySQL y verificar que el servidor este iniciado.

4. Ejecutar el proyecto:

```bash
mvn clean javafx:run
```

## Controles

Los controles de movimiento y combate se utilizan durante la partida. La tecla `ESC` permite abrir el menu de pausa.

## Proyecto integrador

Desarrollado como proyecto academico para aplicar conceptos de Programacion Orientada a Objetos, interfaces, separacion por capas, persistencia de datos y desarrollo de interfaces graficas con JavaFX.