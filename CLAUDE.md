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
- **Diseño**: el design system está en claude.ai (link en `../docs/features/diseno.md`). Tema papel claro y oscuro según el sistema, sin color dinámico (`ui/theme`). Los estilos propios de la hoja, en Literata 600, están en `SheetType`; el resto usa la escala de Material 3. Diseñar por rol del `ColorScheme`, nunca por hex.
- **La hoja es de solo lectura por defecto**: lo editable (nombre, clase, Max HP, atributos, skills) y borrar el personaje y las ayudas de armado del personaje (sugerencias de clase ✦, "elegí N") aparecen solo en **modo edición** (menú ⋮ del encabezado, `isEditing` en el `UiState`). Guardar no sale del modo edición. Más adelante, cuando haya trasfondo y raza, la creación pasa a un asistente aparte.
- La hoja no tiene barra de arriba: arriba va **`CharacterHeader`** (nombre, HP actual/máximo con los temporales al lado, y menú ⋮ con Short rest y Long rest, deshabilitados hasta que existan, Editar y, en modo edición, Borrar), igual en todas las pantallas. Abajo, la **barra inferior** (`SheetDestination`: hoy Combate, Skills y Personaje; Spells y Slots se suman con sus features, cinco como máximo). La hoja abre en Combate. Las pantallas no llevan título. Se vuelve a la lista con el retroceso del sistema.
- **Combate** (`CombatScreen`): AC e Init en `StatCell` (Spell DC y Spell Atk llegan con spellcasting). Debajo, las acciones (`CombatActionsList`, la única parte con scroll); con 0 HP las reemplazan los death saves (`DeathSavesBox`: tres puntos de éxitos y tres de fallos que se tocan para marcar y desmarcar, como los slots) y, si está estable o muerto, un recuadro que lo explica (muerto, con Revivir).
- **Acciones de combate** (`SheetCombat`, `viewModel.combat`): la hoja de HP, los death saves y revivir funcionan **fuera del modo edición** (se usan en la mesa). Tocar el HP del encabezado abre `HitPointsSheet` (hoja desde abajo: Daño, Curar o Temporales, teclado propio, Crítico solo con 0 HP y cómo queda antes de aplicar, con `previewHitPoints`; la regla la aplica el server). Con el personaje muerto el HP no se toca. Siguen el mismo patrón que los diálogos de edición: `CombatDialog` + `CombatInput` + `CombatDialog.changedBy`, y los callbacks en `CombatActions`. Viven en una clase aparte porque el ViewModel está en el límite de funciones de detekt. Usan `CombatRepository`, aparte de `CharacterRepository` por lo mismo.
- **Acciones y recursos**: la lista sale de `Character.combatActionList()` (dominio, testeable): las features con `actionType` agrupadas en Action, Bonus Action y Reaction, y al final **Otros**, con la feature que da cada recurso que ninguna acción gasta (Action Surge, Arcane Recovery). Las pasivas no van. Cada fila muestra el nombre y los usos del recurso (`Character.resourceOf`): `UsePips` si el server dice `PIPS` (tocar un punto lleno gasta uno y uno vacío lo recupera, y se guarda al instante con `onResourceUsedChange`) o un `ValueChip` "quedan/máximo" si dice `POOL`. Tocar la fila abre `FeatureSheet` (`CombatDialog.Feature`, con el recurso como estaba al abrir): el texto, el tipo y la recarga; los puntos se tocan igual que en la fila y un pool se gasta o recupera con el `Keypad` de la hoja de HP, mostrando cómo queda. La app no valida contra las reglas: manda cuántos usos quedan gastados (`PUT .../resources/{id}`) y muestra lo que vuelve. Un recurso sin límite (Rage al 20) no viene en `resources`: la fila queda sin usos.
- **Skills** (`SkillsScreen`), sin scroll: Saving Throws arriba, una caja por atributo (score, modificador y sus skills) en dos columnas, Passive Perception abajo. El aviso del modo edición ocupa el lugar de Passive Perception para que nada se mueva.
- **Personaje** (`CharacterScreen`, en `CharacterTab.kt`): el bloque de clase fijo (clases con su nivel, PB, Max HP, Hit Dice, Speed) y **pestañas internas** (`CharacterTab`: Proficiencies, Features), cada una con su scroll. Lo que se usa poco va como pestaña nueva acá, no como destino de la barra.
- **Features**: cada una con nombre, nivel y el resumen del server; el texto SRD (solo las que lo tienen) se abre con "Ver texto SRD". Con varias clases, agrupadas bajo el nombre de cada una. Una clase sin features cargadas en el server muestra un aviso.
- Los diálogos de edición de la hoja: `SheetEdit` (sealed, en `SheetEdit.kt`) guarda lo que se está editando; se abren con `onStartEdit(SheetEditTarget)` y cada cambio de un campo llega al ViewModel como un `SheetEditInput` por `onEditInput` (la lógica de aplicarlo es `SheetEdit.changedBy`, testeable sin ViewModel). Para un campo nuevo: un `SheetEditTarget`, un `SheetEdit`, un `SheetEditInput` y su callback en `SheetEditActions`.
- **Dónde se edita cada cosa** (modo edición): el nombre, tocándolo en el encabezado; los atributos, tocando el encabezado de cualquier caja de Skills (abre el diálogo con los seis); las skills, tocando su fila; la clase, el Max HP y la Speed base, en el bloque de clase de Personaje; la AC (override: vacío vuelve a la calculada), en su casilla de Combate. Borrar está en el menú y abre la confirmación (`SheetEdit.DeleteCharacter`, el mismo diálogo con el botón "Borrar"). Borrar deja el estado en `Deleted` y la pantalla vuelve a la lista, que se recarga sola.
- **Subclase**: se elige en el diálogo de clase, con el catálogo de `GET /api/v1/classes` (lo carga la hoja después del personaje; si falla, el diálogo no muestra la subclase y se reintenta al abrirlo). Al guardar se manda primero la clase y después la subclase, solo si difiere de la que dejó el server. Por debajo del nivel de subclase el campo queda deshabilitado (y el server la borra). En solo lectura se ve en el encabezado: "Cleric 15 (Life Domain)".
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

Está en `../docs/api.md` (en la raíz de CharacterSheet). Manda el server: si no coincide con lo que devuelve, se corrige el doc.

La app tolera que falten campos (server viejo): los muestra con `—`. Las clases y los valores de competencias que no conoce (libros nuevos) se ignoran.

## Testing

- **ViewModels y repositorios**: unit tests en JVM (`src/test`), con fakes de los repositorios / del cliente HTTP en vez de mocks. Acá van la mayoría de los tests.
- **Mappers DTO <-> dominio**: unit tests, incluyendo deserializar JSON real de la API.
- **UI**: tests de Compose para las pantallas principales (que el estado se muestre bien y que los eventos lleguen
  al ViewModel). Corren en la JVM con Robolectric (`src/test`, `@RunWith(AndroidJUnit4::class)`), así que no
  necesitan dispositivo y corren en CI. Usar `androidx.compose.ui.test.junit4.v2.createComposeRule` y
  `performScrollTo()` antes de interactuar con lo que puede quedar fuera de la pantalla (la de Robolectric es chica).
  Las pantallas sin scroll (Skills) se testean con `@Config(qualifiers = PHONE_SCREEN)`, una pantalla de teléfono;
  los diálogos no, porque con esa pantalla nunca quedan idle.
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
