# Recursos del juego

Todos los recursos se leen desde el classpath, es decir, desde `src/main/resources/`. Las rutas de esta
guía son relativas a `src/main/resources/assets/images/` salvo que se indique otra cosa.

## Cómo se resuelve cada imagen

`CatalogoAssets` (paquete `com.wachin.view.game`) es la única fuente de verdad de las rutas y `AssetVisual`
las carga **una sola vez** y las guarda en caché. Para cada elemento se prueba, en orden:

1. El archivo documentado en esta guía (la primera ruta de cada fila).
2. Alternativas indicadas en la columna *Respaldo* (nombres históricos o el sprite equivalente del mundo).
3. Una ilustración dibujada por código (`Dibujos`, `EscenarioView`, `PuertaVisual`).

**Ningún PNG es imprescindible**: si un archivo falta, el juego sigue funcionando con su alternativa dibujada.
Si un archivo existe pero no se puede leer, se registra una advertencia en el log una sola vez y se usa la alternativa.
El test `CatalogoAssetsTest` comprueba que toda ruta que el código puede pedir aparece en este documento.

Convenciones para el arte nuevo:

- Formato PNG con transparencia (RGBA). Se dibuja **sin suavizado**, así que el pixel art se ve nítido.
- Personajes, enemigos y jefes: vista frontal, centrados. El juego los refleja horizontalmente según hacia dónde van.
- Proyectiles: **apuntando hacia arriba**; el juego los rota según la dirección del disparo.
- Cualquier tamaño sirve; se escala conservando la proporción dentro del recuadro indicado.

## Jugador, enemigos y jefes

| Ruta | Estado | Recuadro en juego | Respaldo si falta |
|---|---|---|---|
| `player/wachin.png` | incluido | 64×64 | criatura rosada dibujada |
| `player/wachina.png` | incluido | 64×64 | igual, con moño |
| `enemies/poli.png` | incluido | 68×68 | policía azul dibujado |
| `enemies/ranged/tirador.png` | falta | 68×68 | guardia con pistola dibujado |
| `bosses/palinni.png` | incluido (jefe de la sala 5) | 112×112 | dibujado |
| `bosses/emisor_rayos.png` | incluido (jefe de la sala 10) | 112×112 | dibujado |
| `bosses/boss_2/boss.png` | falta (jefe de la sala 15) | 112×112 | golem artillero dibujado |
| `bosses/boss_3/boss.png` | falta (jefe de la sala 20 y de cada 20 salas) | 138×138 | demonio con cuernos dibujado |

Las carpetas `enemies/melee/` y `bosses/boss_1/` están reservadas para variantes y animaciones futuras.

## Invocaciones

| Ruta | Estado | Recuadro | Respaldo |
|---|---|---|---|
| `summons/worm/worm.png` | falta | 46×46 | gusano segmentado dibujado |
| `summons/bat/bat.png` | falta | 46×46 | murciélago dibujado |
| `summons/robot/robot.png` | falta | 46×46 | robot dibujado |

## Proyectiles

| Ruta | Estado | Recuadro | Respaldo |
|---|---|---|---|
| `projectiles/lagrima.png` | incluido | 28×28 | orbe cian |
| `projectiles/gota.png` | incluido | 28×28 | orbe dorado |
| `projectiles/enemies/tirador.png` | falta | 24×24 | orbe naranja |
| `projectiles/enemies/boss.png` | falta | 24×24 | orbe del color de cada jefe |
| `projectiles/summons/robot/laser.png` | falta | 30×30 | rayo cian alargado |

## Cartas

Nombre del archivo = nombre de `CartaTipo` en minúscula. El mismo icono se usa en el HUD, la pantalla de
elección, la consulta de cartas y el objeto del suelo.

| Ruta | Carta | Estado | Respaldo |
|---|---|---|---|
| `cards/dano.png` | Fuerza | falta | espada dibujada |
| `cards/velocidad.png` | Agilidad | falta | rayo dibujado |
| `cards/vida_maxima.png` | Vitalidad | falta | corazón con cruz dibujado |
| `cards/dano_invocaciones.png` | Vínculo | falta | huella dibujada |
| `cards/segundo_espacio_invocacion.png` | Pacto | falta | anillos dibujados |
| `cards/seleccion_cartas.png` | Mazo de cartas (objeto del suelo) | falta | mazo dibujado |

## Objetos del suelo

Cada objeto busca primero su icono propio y, si no existe, el sprite equivalente del mundo, de modo que
la gota, el gusano o una carta se ven bien sin crear nada nuevo. El nombre es el código del objeto en minúscula.

| Ruta propia (opcional) | Se usa como respaldo |
|---|---|
| `pickups/projectiles/gota.png`, `pickups/projectiles/lagrima.png` | `projectiles/gota.png`, `projectiles/lagrima.png` |
| `pickups/summons/gusano.png`, `murcielago.png`, `robot.png` | el sprite de la invocación y, si falta, el dibujado |
| `pickups/cards/<carta>.png` (por ejemplo `dano.png`, `seleccion_cartas.png`) | `cards/<carta>.png` |
| `pickups/rewards/aumento_vida_maxima_25.png` | `rewards/aumento_vida_maxima_25.png`; si falta, corazón dorado dibujado |

## Escenario

| Ruta | Estado | Uso | Respaldo |
|---|---|---|---|
| `environment/floors/floor.png` | incluido | piso de los mapas impares (se repite en baldosas de 128 px) | losas dibujadas |
| `environment/floors/floor_alt.png` | incluido | piso de los mapas pares (variante marrón) | `floor.png` |
| `environment/walls/wall.png` | incluido | paredes (se repite en baldosas de 100 px) | ladrillos dibujados |
| `environment/doors/closed.png` | incluido | puerta de sala normal (se recorta el recuadro 64,32 → 192,256 del PNG de 256×256) | puerta de hierro dibujada |
| `environment/doors/open.png` | falta | puerta abierta de sala normal | umbral con portal cian dibujado |
| `environment/doors/boss_closed.png` | falta | puerta cerrada de sala de jefe | `closed.png` teñida de dorado |
| `environment/doors/boss_open.png` | falta | puerta abierta de sala de jefe | umbral con portal dorado dibujado |

El piso se tiñe según el mapa y las salas de jefe usan una variante roja con un círculo de runas; todo eso se
compone por código, no requiere archivos.

### Nombres anteriores

En este proyecto los archivos se llamaban `floors.png`, `ReColorFloors.png`, `walls.png` y `doors.png`, pero el
código buscaba otros nombres, por lo que nunca se cargaban. Se renombraron a los nombres de esta guía
(`floor.png`, `floor_alt.png`, `wall.png`, `closed.png`). Si algún día vuelven a aparecer los nombres antiguos
en una carpeta, el cargador los acepta como respaldo.

`bosses/emisor_rayos.png` se reemplazó por una versión con **fondo transparente** (el original era un RGB de
1024×1024 con fondo blanco sólido, que se dibujaba como un cuadrado blanco) y se redujo a 256×256.

## Efectos e interfaz

Las partículas, destellos, sombras, iluminación, barras, paneles y botones se generan por código y con la
hoja de estilos JavaFX `assets/styles/wachin.css`. Las carpetas `effects/` y `ui/` quedan reservadas por si se
agregan recursos propios más adelante.

## Sonido (opcional)

No se necesita ningún archivo: los efectos se sintetizan en memoria con `javax.sound.sampled`. Para usar sonidos
propios, colocá archivos WAV en `src/main/resources/assets/audio/` con estos nombres (reemplazan al efecto sintetizado):

`disparo.wav`, `impacto.wav`, `muerte.wav`, `danio.wav`, `recoger.wav`, `puerta.wav`, `jefe.wav`,
`nivel.wav`, `click.wav`, `carta.wav`, `derrota.wav`, `victoria.wav`.

Si el equipo no tiene salida de audio, el juego lo registra una vez en el log y continúa sin sonido.
El volumen se ajusta en **Ajustes** (menú principal o pausa).
