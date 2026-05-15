# Arkano — Rick & Morty Character List

App Android que consume la [Rick & Morty API](https://rickandmortyapi.com/api) y muestra una lista de personajes con manejo explícito de estados (Loading / Success / Error + Retry).

Prueba técnica para Arkano (Android Developer). Ver [docs/specs/2026-05-15-arkamo-rickmorty-design.md](docs/specs/2026-05-15-arkamo-rickmorty-design.md) para el diseño detallado y [docs/plans/2026-05-15-arkamo-rickmorty.md](docs/plans/2026-05-15-arkamo-rickmorty.md) para el plan de implementación paso a paso.

## Stack

| Área | Versión |
|---|---|
| Lenguaje | Kotlin 2.3.21 (con flag `-Xexplicit-backing-fields`) |
| UI | Jetpack Compose + Material 3 (BOM 2026.02.01) |
| DI | Hilt 2.59.2 (KSP 2.3.7) |
| Networking | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization 1.7.3 |
| Imágenes | Coil 3.1.0 |
| Estado / async | StateFlow + Coroutines 1.10.1 |
| Build | AGP 9.2.1 + Gradle 9.4.1 + JVM 11 |
| SDK | compileSdk 36.1, minSdk 29, targetSdk 36 |
| Tests | JUnit 4 + MockK 1.13.13 + kotlinx-coroutines-test |

## Cómo correr

1. `git clone https://github.com/Cerverussz/ArkamoTest.git`
2. Abrir en Android Studio (Iguana o superior).
3. Ejecutar en un emulador o dispositivo físico con API 29+ y conexión a internet.

Comandos útiles desde la raíz:

| Comando | Acción |
|---|---|
| `./gradlew assembleDebug` | Compila la APK debug. |
| `./gradlew testDebugUnitTest` | Ejecuta los 10 tests JVM. |
| `./gradlew installDebug` | Instala en el dispositivo/emulador conectado. |

## Arquitectura

Clean Architecture en 3 capas con inversión de dependencias:

```
ui  ────►  domain  ◄────  data
           (puro)         (implementa interfaces de domain)
```

- `domain/` define el contrato (`CharactersRepository` interfaz + `GetCharactersUC`) y modelos puros (`Character`, `CharacterStatus`). **No importa nada de `data/` ni de framework**; solo `javax.inject.Inject`.
- `data/` implementa la interfaz (`CharactersRepositoryImpl`), conoce Retrofit y mapea DTO → Domain.
- `ui/` consume use cases vía ViewModel. **No conoce `data/`**.
- Hilt `@Binds` conecta `CharactersRepository` ↔ `CharactersRepositoryImpl` desde `di/RepositoryModule.kt`.

### Grafo de dependencias

```
CharactersScreen
    └─► CharactersViewModel
            └─► GetCharactersUC          (domain)
                    └─► CharactersRepository    (interfaz domain)
                            ▲
                            │ @Binds
                            │
                    CharactersRepositoryImpl    (data)
                            └─► RickAndMortyApi (Retrofit)
                                    └─► OkHttpClient + Json
```

## Decisiones técnicas

- **Clean Architecture estricta** con interfaz de repositorio en `domain/` e implementación en `data/`. La UI llega a domain por UseCases, nunca por repositorios. El UseCase actual es trivial, pero deja el patrón establecido si crece (cache, validación, combinación de fuentes).
- **`Result<T>` para errores** en lugar de excepciones cruzando capas. `try/catch` explícito en el Repository con re-throw de `CancellationException` (evita romper structured concurrency, problema típico de `runCatching`).
- **`StateFlow` con `onStart + stateIn`** para carga lazy en primera observación. Sin `init { load() }` porque dispararía aunque nadie observe; `SharingStarted.WhileSubscribed(5_000)` preserva estado en rotaciones de pantalla.
- **Coil 3** con cache memory + disk automático; `LazyColumn(key = it.id)` para evitar recomposiciones y recargas innecesarias en scroll.
- **Hilt** aun siendo un proyecto pequeño: demuestra DI idiomática y deja base para escalar.
- **`@IoDispatcher` inyectado al Repository** (no al VM) — offload de IO en el boundary correcto, VM se queda en `Dispatchers.Main.immediate` para updates UI eficientes.
- **Kotlin 2.3.21** habilita el flag `-Xexplicit-backing-fields`. No se usa en `uiState` por incompatibilidad con la chain `onStart + stateIn`, pero queda disponible para casos futuros.
- **`@param:IoDispatcher`** en el constructor para silenciar el warning de Kotlin 2.3 sobre `annotation default target`.
- **Mensajes de error traducidos** en `ui/characters/state/ErrorMessages.kt` (`Throwable.toUserMessage()`): mapea `UnknownHostException`, `SocketTimeoutException`, `IOException`, `HttpException` 4xx/5xx, `SerializationException` y catch-all a strings legibles en español.

## Estructura de paquetes

```
com.devdaniel.arkamotest/
├── ArkamoApp.kt                              // @HiltAndroidApp
├── MainActivity.kt                            // @AndroidEntryPoint
├── data/
│   ├── remote/
│   │   ├── RickAndMortyApi.kt
│   │   └── dto/{CharacterDto, CharactersResponseDto}.kt
│   ├── mapper/CharacterMapper.kt              // DTO → Domain
│   └── repository/CharactersRepositoryImpl.kt
├── domain/
│   ├── model/{Character, CharacterStatus}.kt
│   ├── repository/CharactersRepository.kt     // interfaz
│   └── usecase/GetCharactersUC.kt
├── ui/
│   ├── characters/
│   │   ├── CharactersScreen.kt
│   │   ├── CharactersViewModel.kt
│   │   ├── components/{CharacterAvatar, CharacterItem, StatusBadge, LoadingContent, ErrorContent}.kt
│   │   └── state/{CharactersUiState, ErrorMessages}.kt
│   └── theme/
└── di/{NetworkModule, DispatcherModule, RepositoryModule}.kt
```

## Tests

10 tests unitarios JVM:

- `CharacterMapperTest` (3): mapeo de status Alive / Dead / Unknown + fallback.
- `GetCharactersUCTest` (2): delegación al Repository, propagación Success / Failure.
- `CharactersViewModelTest` (5):
  - Happy path emite Loading → Success.
  - `UnknownHostException` emite Loading → Error con "Sin conexión...".
  - HTTP 500 emite Loading → Error con "El servidor está caído...".
  - `onRetry` tras error vuelve a emitir Loading → Success.
  - `onStart` solo dispara `load()` una vez aunque haya múltiples collectors (`SharingStarted.WhileSubscribed`).

Stack de testing: MockK + `kotlinx-coroutines-test` (`StandardTestDispatcher` + `runTest` + `advanceUntilIdle`).

## Qué quedó fuera por tiempo

- Paginación. La API soporta paginación; se carga solo la primera página (~20 personajes).
- Pantalla de detalle de personaje.
- Filtros por estado (Alive / Dead / Unknown).
- Tests instrumentados (Compose UI tests con `ComposeRule`).
- Tests del `CharactersRepositoryImpl` (wrapper trivial sobre Retrofit + mapper).
- Dark mode personalizado (se usa el default de Material 3).
- Internacionalización (strings en español hardcodeadas).

## Qué mejoraría con más tiempo

- Paging 3 para scroll infinito con manejo de carga incremental y placeholders por item.
- Pantalla de detalle con navegación Compose y `SharedTransition` para la imagen.
- Cache local con Room para offline-first.
- Tests instrumentados de `CharactersScreen` con `ComposeRule`.
- Test del `CharactersRepositoryImpl` con un `MockWebServer` (verifica el flujo Retrofit + Serialization + mapper completo).
- CI con GitHub Actions: build + tests + ktlint en cada PR.
- Animaciones de entrada para items de la lista.

## Uso de IA

Usé [Claude Code](https://claude.com/claude-code) (Anthropic) en este proyecto:

- **Brainstorming inicial:** discusión iterativa de stack, alcance y arquitectura. Cubrimos Hilt vs DI manual, Clean Architecture estricta vs simplificada, el pattern `onStart + stateIn` y la elección entre `runCatching` y `try/catch` con re-throw de `CancellationException`.
- **Generación del spec de diseño** ([docs/specs/2026-05-15-arkamo-rickmorty-design.md](docs/specs/2026-05-15-arkamo-rickmorty-design.md)) y del **plan de implementación** ([docs/plans/2026-05-15-arkamo-rickmorty.md](docs/plans/2026-05-15-arkamo-rickmorty.md)) — ambos incluidos en el repo para transparencia.
- **Asistencia en codificación:** scaffolding de módulos Hilt, configuración Retrofit + Kotlinx Serialization, estructura de los tests (MockK + StandardTestDispatcher).
- **Revisión de patrones:** detección del bug del `get() = _uiState.onStart{}.stateIn(...)` (re-crea el flow en cada acceso); recordatorio de la trampa de `runCatching` con `CancellationException`; señalado del warning de Kotlin 2.3 sobre annotation default target en parámetros de constructor.
- **Verificación de versiones contra repositorios oficiales** (GitHub releases de Dagger, KSP, Kotlin) antes de fijar el `libs.versions.toml`. Esto evitó perder tiempo con la combinación errónea Kotlin 2.3 + Hilt 2.57 (issue [dagger#5001](https://github.com/google/dagger/issues/5001), corregido en Hilt 2.58+).

Las decisiones técnicas, la revisión del código y los commits son propios.
