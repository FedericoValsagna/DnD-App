# DnD App – Android

App Android (Jetpack Compose) que funciona como character sheet de D&D para uso personal. Es el cliente del server DnDApp, que vive en **otro repo** ([DnD-Server](https://github.com/FedericoValsagna/DnD-Server), Kotlin + Spring Boot + Postgres, corre en WSL en la misma PC). La app no tiene lógica de reglas propia: los valores derivados (modificadores, bonus de competencia, etc.) los calcula el server.

## Stack

- Kotlin, Jetpack Compose + Material 3, AGP 9 (Kotlin integrado en AGP: no hay plugin `kotlin-android`)
- `compileSdk`/`targetSdk` 37, `minSdk` 36 (solo un dispositivo propio, no hace falta soportar versiones viejas)
- Versiones de dependencias en `gradle/libs.versions.toml` (version catalog). Nunca hardcodear versiones en `build.gradle.kts`.

## Comandos

Todo pasa por el `Makefile` (como en el server). `make` sin argumentos lista los targets. Los principales:

```
make run           # instala la app de debug en el dispositivo conectado y la abre
make logs-http     # logs de red (requests, respuestas, errores) del dispositivo
make server-check  # verifica que el server de dev responda en dndapp.devBaseUrl
make build         # build completo: debug + release, tests, coverage, lint de Android, ktlint y detekt
make test          # todos los tests, incluidos los de Compose (T='*Patron*' para filtrar)
make lint          # ktlint + detekt + lint de Android
make format        # autoformatea con ktlint
make coverage      # reporte HTML de coverage (Kover) y verificación del mínimo (80%)
```

- `make` es GNU make para Windows (`winget install ezwinports.make`); funciona desde PowerShell o Git Bash.
  Las recetas corren con las herramientas de Git for Windows (`C:/Program Files/Git`), no con cmd.exe.
- `JAVA_HOME`: si no está definido, el Makefile usa el JDK de Android Studio.
- Estilo: ktlint (`.editorconfig`) y detekt (`config/detekt/detekt.yml`), ambos con las reglas de Compose
  (`io.nlopez.compose.rules`). Antes de commitear: `make format` y `make lint`.

## Arquitectura

MVVM con flujo de datos unidireccional, en capas por paquete dentro de `com.valsagnapps.dndapp`:

```
ui/          # Composables por pantalla + su ViewModel. El ViewModel expone un StateFlow<UiState> inmutable
             # y recibe eventos; los Composables no tienen lógica ni llaman a la red.
domain/      # Modelos de la app (Character, AbilityScores, ...). Kotlin puro, sin Android ni DTOs.
data/        # Repositorios (interfaces + implementaciones), cliente HTTP, DTOs y mappers DTO <-> dominio.
```

Reglas:
- Los DTOs de la API no salen de `data/`; la UI trabaja con modelos de dominio.
- Los Composables reciben estado y lambdas (state hoisting); los que son de pantalla completa toman el ViewModel, los internos no.
- Cada pantalla tiene `@Preview` con datos de ejemplo.
- Errores de red/servidor se modelan en el `UiState` (cargando / contenido / error), nunca se tiran excepciones hasta la UI.

## Conexión con el server

- Online-only: sin cache offline por ahora (decisión explícita).
- La URL base sale de `BuildConfig`, configurada por build type; nunca hardcodeada en el código:
- Dev y prod corren en la misma PC (WSL) y se exponen al tailnet con Tailscale Serve, que hace de proxy HTTPS
  a `localhost` (el server de dev escucha solo en `127.0.0.1:8081`, no es alcanzable por la IP de Tailscale):
  - **dev** → `https://<pc>.<tailnet>.ts.net:8443`, con `tailscale serve --bg --https=8443 http://localhost:8081`.
  - **prod** → `https://<pc>.<tailnet>.ts.net` (puerto 443).
- Las URLs van en `local.properties` (gitignored), no en el repo:
  - `dndapp.devBaseUrl` → build **debug**. Si falta, usa `http://10.0.2.2:8081/` (el localhost de la PC visto
    desde el emulador; un celular real no lo alcanza). El `network_security_config` de debug permite HTTP sin TLS
    **solo** para `10.0.2.2`.
  - `dndapp.prodBaseUrl` → build **release**.
- El celular tiene que estar conectado a Tailscale.
- No hay autenticación: el límite de seguridad es Tailscale (solo dispositivos del tailnet llegan al server).
- Levantar el server de dev: en WSL, desde `server/` (en la carpeta del proyecto), `make dev` (queda en `localhost:8081`).
- Debug de red: en debug, OkHttp loguea cada request/respuesta/error en Logcat con el tag `DnDHttp`.
  También sirve el Network Inspector de Android Studio (App Inspection).

## Contrato de la API

Todo bajo `/api/v1`. JSON. Endpoints actuales:

| Método | Ruta | Respuesta |
|---|---|---|
| `POST` | `/api/v1/characters` | `201` + personaje |
| `GET` | `/api/v1/characters` | `200` + lista de personajes (ordenada por nombre, sin paginar; `[]` si no hay) |
| `GET` | `/api/v1/characters/{id}` | `200` + personaje / `404` |
| `PUT` | `/api/v1/characters/{id}/skills` | `200` + personaje / `404`. **Reemplaza** todas las competencias: las skills que no vienen quedan en `NONE` |
| `PUT` | `/api/v1/characters/{id}/classes` | `200` + personaje / `404`. **Reemplaza** todas las clases: `{ "classes": [ ... ] }` |
| `PUT` | `/api/v1/characters/{id}/hit-points` | `200` + personaje / `404`. `{ "maxHitPoints": 47 }` |

Request de creación:
```json
{ "name": "Tordek",
  "classes": [{ "class": "FIGHTER", "level": 5 }],
  "maxHitPoints": 44,
  "abilityScores": { "strength": 16, "dexterity": 12, "constitution": 15,
                     "intelligence": 10, "wisdom": 13, "charisma": 8 },
  "skills": { "ATHLETICS": "PROFICIENT", "PERCEPTION": "EXPERTISE" } }
```
`skills` es opcional (las que no vienen quedan en `NONE`). El `PUT .../skills` recibe `{ "skills": { ... } }` con el mismo formato.
Competencias: `NONE`, `PROFICIENT`, `EXPERTISE`. Skills: las 18 de 5e en mayúsculas con `_` (`ANIMAL_HANDLING`, `SLEIGHT_OF_HAND`, ...).
Clases (PHB 2014): `BARBARIAN`, `BARD`, `CLERIC`, `DRUID`, `FIGHTER`, `MONK`, `PALADIN`, `RANGER`, `ROGUE`, `SORCERER`, `WARLOCK`, `WIZARD`.
`classes` es una lista para soportar multiclase (sin clases repetidas, la suma de niveles es el `level`, 1–20). La primera es la clase inicial, la que da las salvaciones. La app hoy maneja una sola clase.
`maxHitPoints` (1–999) lo carga el jugador: el server no lo calcula.

Respuesta (personaje):
```json
{ "id": "uuid", "name": "Tordek", "level": 5, "proficiencyBonus": 3,
  "classes": [{ "class": "FIGHTER", "level": 5, "hitDie": 10 }],
  "maxHitPoints": 44,
  "hitDice": [{ "die": 10, "count": 5 }],
  "abilities": { "STRENGTH": { "score": 16, "modifier": 3 }, "...": "una entrada por cada atributo" },
  "savingThrows": { "STRENGTH": { "proficiency": "PROFICIENT", "bonus": 6 }, "...": "los 6 atributos" },
  "skills": { "ATHLETICS": { "ability": "STRENGTH", "proficiency": "PROFICIENT", "bonus": 6 },
              "...": "las 18 skills" },
  "passivePerception": 17 }
```
El `bonus` de cada skill y salvación, `hitDice` (agrupados por dado, de mayor a menor) y `passivePerception` los calcula el server.
La app tolera que falten `skills`, `passivePerception`, `classes`, `maxHitPoints`, `hitDice` y `savingThrows` (server viejo): los muestra con `—`. Clases que no conoce (libros nuevos) se ignoran.
Validaciones: `name` 1–100 caracteres, al menos una clase, niveles 1–20 (también la suma), `maxHitPoints` 1–999, atributos 1–30, clases, skills y competencias conocidas (si no, `400`).
Errores: `application/problem+json` (RFC 9457) con `status`, `title` y `detail`. `400` por validación, `404` si no existe.

Fuentes de verdad del contrato, en el repo del server:
- Spec OpenAPI: con el server de dev corriendo, `make openapi` (en el server) lo guarda en `build/openapi.json`; también en `http://localhost:8081/v3/api-docs` y Swagger UI en `/swagger-ui.html`.
- Colección de Postman en `postman/collections/DnDApp`.
- Si el contrato de acá y el del server no coinciden, manda el del server: actualizar esta sección.

## Testing

- **ViewModels y repositorios**: unit tests en JVM (`src/test`), con fakes de los repositorios / del cliente HTTP en vez de mocks. Acá van la mayoría de los tests.
- **Mappers DTO <-> dominio**: unit tests, incluyendo deserializar JSON real de la API.
- **UI**: tests de Compose para las pantallas principales (que el estado se muestre bien y que los eventos lleguen
  al ViewModel). Corren en la JVM con Robolectric (`src/test`, `@RunWith(AndroidJUnit4::class)`), así que no
  necesitan dispositivo y corren en CI. Usar `androidx.compose.ui.test.junit4.v2.createComposeRule` y
  `performScrollTo()` antes de interactuar con lo que puede quedar fuera de la pantalla (la de Robolectric es chica).
  Robolectric corre con SDK 36 (`src/test/resources/robolectric.properties`) hasta que soporte el 37.
- Todo cambio de comportamiento viene con su test. Nombres descriptivos en backticks: ``fun `shows error when character is not found`()``.
- Coverage con Kover, mínimo 80% (`make coverage`; `make build` y el CI fallan por debajo). Se excluye solo lo que
  no tiene lógica (arranque, wiring, tema, previews): lo demás se testea.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): en cada PR y push a `main`, lint, tests, coverage y build completo.
  Dependabot abre PRs semanales de versiones (Gradle y actions).
- **Release** (`.github/workflows/release.yml`): cuando el CI pasa en `main`, compila el APK de release firmado
  y lo publica como GitHub Release `v1.0.<n>` (`n` = número de corrida, que también es el `versionCode`).
  En el celular, [Obtainium](https://github.com/ImranR98/Obtainium) sigue los Releases del repo y avisa/instala.
- Debug y release conviven en el celular: debug es `com.valsagnapps.dndapp.debug` ("DnD App (dev)", contra dev,
  firmada con la clave de debug de la PC) y release es `com.valsagnapps.dndapp` ("DnD App", contra prod).
- No es Play Store: la app se instala por fuera (sideload). Todos los APKs deben firmarse con **la misma clave**,
  si no Android no deja actualizar encima. El keystore tiene backup fuera de GitHub y nunca va al repo.
- Secrets de Actions que usa el release (si falta alguno, el release se saltea con un aviso):
  `DNDAPP_KEYSTORE_BASE64` (el `.jks` en base64), `DNDAPP_KEYSTORE_PASSWORD`, `DNDAPP_KEY_ALIAS`,
  `DNDAPP_KEY_PASSWORD` y `DNDAPP_PROD_BASE_URL`.
- Para compilar el release firmado en la PC, las mismas claves van en `local.properties`: `dndapp.keystore.file`,
  `dndapp.keystore.password`, `dndapp.key.alias`, `dndapp.key.password` (y `dndapp.prodBaseUrl`).
- El hostname de prod queda dentro del APK público: es aceptado, el límite de seguridad es Tailscale.

## Convenciones

- Código e identificadores en inglés; textos de UI en `strings.xml` (nunca strings hardcodeados en Composables); commits y docs pueden ir en español.
- Los términos de D&D van en inglés en la UI (atributos, skills, Level, Proficiency Bonus, Passive Perception, ...); el resto de la UI (botones, errores, mensajes) en castellano.
- Commits chicos y enfocados.
