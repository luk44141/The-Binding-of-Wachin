# Recursos visuales

Los recursos se cargan desde `src/main/resources/assets/images/`. Las rutas se resuelven en el classpath. Conserva los PNG ya existentes en `player/`, `enemies/`, `bosses/` y `projectiles/`.

## Rutas preparadas

- `player/`: `wachin.png`, `wachina.png` (existentes).
- `enemies/`: `poli.png` (existente); el tirador usa `enemies/ranged/tirador.png`. `melee/` queda preparada para variantes futuras.
- `bosses/`: el jefe de sala 10 conserva `bosses/palinni.png`; los otros usan `bosses/boss_2/boss.png` y `bosses/boss_3/boss.png`. `boss_1/` queda disponible para futuras animaciones sin mover el sprite existente.
- `summons/worm/worm.png`, `summons/bat/bat.png` y `summons/robot/robot.png`: Gusano, Murciélago y Robot.
- `projectiles/`: `lagrima.png` y `gota.png` existentes; `projectiles/enemies/tirador.png` para el tirador, `projectiles/enemies/boss.png` para jefes y `projectiles/summons/robot/laser.png` para el láser del Robot.
- `cards/`: iconos de carta nombrados en minúscula según `CartaTipo` (por ejemplo, `dano.png`, `velocidad.png`, `vida_maxima.png` y `dano_invocaciones.png`). La selección muestra un bloque provisional mientras falte el PNG.
- `pickups/projectiles/`, `pickups/summons/`, `pickups/cards/`, `pickups/rewards/`: iconos del suelo nombrados en minúscula según el código del objeto, por ejemplo `gota.png`, `gusano.png`, `dano.png` y `aumento_vida_maxima_25.png`.
- `rewards/`: ilustraciones de recompensas especiales que no sean iconos del suelo.
- `environment/walls/wall.png`, `environment/floors/floor.png` y `environment/doors/closed.png` / `open.png`: piezas del escenario. Si faltan, se muestran bloques geométricos.
- `effects/` y `ui/`: efectos y recursos de interfaz.

La carga de imágenes usa la ruta del recurso como configuración. Cuando no encuentra un PNG, `AssetVisual` permite representar la entidad con una figura geométrica; no se requieren sprites para que su lógica funcione. Para mantener el reemplazo directo, conserva el nombre/ruta documentado o actualiza únicamente la ruta configurada del tipo de recurso.
