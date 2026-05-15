# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

**ArkamoTest** — single-screen Android app for the Arkamo technical challenge. Consumes the [Rick & Morty API](https://rickandmortyapi.com/api) and displays a character list with explicit Loading / Success / Error+Retry state handling.

- **Package:** `com.devdaniel.arkamotest`
- **Repo:** `github.com/Cerverussz/ArkamoTest` (public)
- **Author:** Daniel Leguizamón Rozo

See [docs/specs/2026-05-15-arkamo-rickmorty-design.md](docs/specs/2026-05-15-arkamo-rickmorty-design.md) for the full design and [docs/plans/2026-05-15-arkamo-rickmorty.md](docs/plans/2026-05-15-arkamo-rickmorty.md) for the implementation plan.

## Stack

- **Kotlin 2.3.x** (with `-Xexplicit-backing-fields` compiler flag enabled)
- **AGP 9.2.1**, **Gradle 9.4.1**, **compileSdk 36.1**, **minSdk 29**, **targetSdk 36**, **JVM 11**
- **UI:** Jetpack Compose + Material 3 (BOM 2026.02.01)
- **DI:** Hilt 2.57+ via KSP
- **Networking:** Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization JSON
- **Images:** Coil 3
- **Async / state:** Coroutines + `StateFlow` (`onStart + stateIn` pattern, no `init { load() }`)
- **Tests:** JUnit 4 + MockK + `kotlinx-coroutines-test`

## Commands

All commands from repo root.

- `./gradlew assembleDebug` — build debug APK.
- `./gradlew testDebugUnitTest` — run JVM unit tests (10 tests across mapper / use case / ViewModel).
- `./gradlew testDebugUnitTest --tests "com.devdaniel.arkamotest.ui.characters.CharactersViewModelTest"` — single test class.
- `./gradlew installDebug` — install on connected emulator / device.

No ktlint/detekt configured for this challenge.

## Architecture

Clean Architecture in 3 layers with inverted dependencies:

```
ui  ────►  domain  ◄────  data
           (pure)         (implements domain interfaces)
```

- **`domain/`** — pure Kotlin. Defines `CharactersRepository` interface + `GetCharactersUC` use case + `Character` / `CharacterStatus` models. No imports from `data/` or Android framework. Only `javax.inject.Inject`.
- **`data/`** — implements domain interfaces. `CharactersRepositoryImpl` + Retrofit `RickAndMortyApi` + DTOs (`@Serializable`) + mapper (`fun CharacterDto.toDomain()`).
- **`ui/`** — Compose screens + ViewModels (Hilt-injected). UI consumes use cases via `GetCharactersUC`, never the Repository directly.
- **`di/`** — Hilt modules. `RepositoryModule` uses `@Binds` to wire `CharactersRepository` ← `CharactersRepositoryImpl`. `NetworkModule` provides Retrofit/OkHttp/Json. `DispatcherModule` provides `@IoDispatcher`.

## Key patterns

- **Repository returns `Result<List<Character>>`.** Try/catch with explicit `CancellationException` re-throw — never `runCatching` (it swallows cancellation and breaks structured concurrency).
- **`@IoDispatcher` injected into the Repository, not the ViewModel.** Offload happens at the IO boundary; VM stays on `Dispatchers.Main.immediate` for fast UI updates.
- **ViewModel exposes `StateFlow<CharactersUiState>` via `onStart + stateIn`.** No `init { load() }` — load triggers only on first observer.
- **`LazyColumn(key = it.id)`** prevents item recomposition / image reload on scroll.
- **Errors translated to user messages in `ui/characters/state/ErrorMessages.kt`** (`Throwable.toUserMessage()` extension).

## Layer boundary rules (don't break)

- `domain/` MUST NOT import from `data/` or Android framework. Only `javax.inject.Inject` is allowed.
- DTOs MUST NOT cross into `domain/`. Mapping happens in `data/mapper/` (single direction: DTO → Domain).
- ViewModels MUST depend on UseCases, not Repositories.
- New use cases: create in `domain/usecase/` as `XxxUC` with `operator fun invoke(...)`.

## Style

- 4-space indent.
- Compose functions use PascalCase.
- Sealed `UiState` per screen, exhaustive `when` in the screen.

## Out of scope (do not add without asking)

- Paging 3 / pagination.
- Detail screen / navigation.
- Filters / search.
- Room / local cache.
- Instrumented tests (Compose UI tests).
- CI/CD config.
