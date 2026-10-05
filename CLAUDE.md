# DnD App – Android

App Android (Jetpack Compose) que funciona como character sheet de D&D para uso personal. Es el cliente del server DnDApp, que vive en **otro repo** ([DnD-Server](https://github.com/FedericoValsagna/DnD-Server), Kotlin + Spring Boot + Postgres, corre en WSL en la misma PC). La app no tiene lógica de reglas propia: los valores derivados (modificadores, bonus de competencia, etc.) los calcula el server.

## Stack

- Kotlin, Jetpack Compose + Material 3, AGP 9 (Kotlin integrado en AGP: no hay plugin `kotlin-android`)
- `compileSdk`/`targetSdk` 37, `minSdk` 36 (solo un dispositivo propio, no hace falta soportar versiones viejas)
- Versiones de dependencias en `gradle/libs.versions.toml` (version catalog). Nunca hardcodear versiones en `build.gradle.kts`.

## Comandos

En Windows (PowerShell), desde la raíz del proyecto:

```powershell
.\gradlew assembleDebug            # compila el APK de debug
.\gradlew installDebug             # instala en el emulador/dispositivo conectado
.\gradlew testDebugUnitTest        # unit tests (JVM)
.\gradlew connectedDebugAndroidTest  # tests instrumentados (requiere emulador/dispositivo)
.\gradlew lint                     # Android lint
```

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
- Levantar el server de dev: en WSL, desde `~/Proyectos/DyDApp/server`, `make dev` (queda en `localhost:8081`).
- Debug de red: en debug, OkHttp loguea cada request/respuesta/error en Logcat con el tag `DnDHttp`.
  También sirve el Network Inspector de Android Studio (App Inspection).

## Contrato de la API

Todo bajo `/api/v1`. JSON. Endpoints actuales:

| Método | Ruta | Respuesta |
|---|---|---|
| `POST` | `/api/v1/characters` | `201` + personaje |
| `GET` | `/api/v1/characters` | `200` + lista de personajes (ordenada por nombre, sin paginar; `[]` si no hay) |
| `GET` | `/api/v1/characters/{id}` | `200` + personaje / `404` |

Request de creación:
```json
{ "name": "Tordek", "level": 5,
  "abilityScores": { "strength": 16, "dexterity": 12, "constitution": 15,
                     "intelligence": 10, "wisdom": 13, "charisma": 8 } }
```
Respuesta (personaje):
```json
{ "id": "uuid", "name": "Tordek", "level": 5, "proficiencyBonus": 3,
  "abilities": { "STRENGTH": { "score": 16, "modifier": 3 }, "...": "una entrada por cada atributo" } }
```
Validaciones: `name` 1–100 caracteres, `level` 1–20, atributos 1–30.
Errores: `application/problem+json` (RFC 9457) con `status`, `title` y `detail`. `400` por validación, `404` si no existe.

Fuentes de verdad del contrato, en el repo del server:
- Spec OpenAPI: con el server de dev corriendo, `make openapi` (en el server) lo guarda en `build/openapi.json`; también en `http://localhost:8081/v3/api-docs` y Swagger UI en `/swagger-ui.html`.
- Colección de Postman en `postman/collections/DnDApp`.
- Si el contrato de acá y el del server no coinciden, manda el del server: actualizar esta sección.

## Testing

- **ViewModels y repositorios**: unit tests en JVM (`src/test`), con fakes de los repositorios / del cliente HTTP en vez de mocks. Acá van la mayoría de los tests.
- **Mappers DTO <-> dominio**: unit tests, incluyendo deserializar JSON real de la API.
- **UI**: tests de Compose (`src/androidTest`) para las pantallas principales: que el estado se muestre bien y que los eventos lleguen al ViewModel.
- Todo cambio de comportamiento viene con su test. Nombres descriptivos en backticks: ``fun `shows error when character is not found`()``.

## Convenciones

- Código e identificadores en inglés; textos de UI en `strings.xml` (nunca strings hardcodeados en Composables); commits y docs pueden ir en español.
- Commits chicos y enfocados.
