# Diseño — Arkamo Rick & Morty Challenge

**Autor:** Daniel Leguizamón Rozo
**Fecha:** 2026-05-15
**Estado:** Aprobado para implementación
**Repo destino:** `github.com/Cerverussz/ArkamoTest` (público)

---

## 1. Contexto

Reto técnico de Arkamo para posición Android Developer. El PDF (`Technical Challenge - Android Developer - Arkamo 1 1.pdf` en raíz del repo) pide una pantalla que consuma [Rick & Morty API](https://rickandmortyapi.com/api) y muestre lista de personajes. Tiempo sugerido por el PDF: 1-2h. Plazo real de entrega acordado: esta semana (2026-05-15 a 2026-05-22).

**Criterios de evaluación del PDF:**
- Arquitectura básica
- Manejo de estado (Loading / Success / Error con Retry — obligatorio)
- Carga eficiente de imágenes en listas (punto clave declarado)
- Claridad y criterio técnico ("buenas decisiones en poco tiempo, no app completa")

**Entregables:** repo público en GitHub, commits atómicos (no monolíticos), código compilable, README con decisiones técnicas, qué quedó fuera, qué mejoraría, y uso de IA.

---

## 2. Punto de partida

Directorio actual `/Users/cerveruz/AndroidStudioProjects/ArkamoTest/` ya contiene un template Android Studio fresh:

- AGP 9.2.1, Kotlin 2.2.10, Gradle 9.4.1, Compose BOM 2026.02.01
- `compileSdk 36.1`, `minSdk 29`, `targetSdk 36`, JVM 11
- Package `com.devdaniel.arkamotest`
- Solo `MainActivity` + `ui/theme/` + tests skeleton

**No se requiere regenerar nada del proyecto base.** El único artefacto contaminante es `CLAUDE.md` (describe un proyecto distinto, StoriBank-T) — se reemplaza por uno descriptivo de Arkamo.

---

## 3. Decisiones técnicas

| Área | Decisión | Justificación |
|------|----------|---------------|
| Lenguaje | Kotlin 2.3.x (bump desde 2.2.10) | Habilita `Explicit Backing Fields` (experimental). Si rompe con AGP 9 / Hilt, rollback a 2.2.10. |
| UI | Jetpack Compose + Material 3 | Recomendado por el PDF. Template ya lo trae. |
| DI | Hilt 2.57+ con KSP 2 | Solicitado por el usuario. KSP en vez de KAPT por requisito de Kotlin 2.3. |
| Networking | Retrofit 2.11 + OkHttp 4.12 + Kotlinx Serialization JSON | Retrofit es lo más idiomático y reconocible. Kotlinx Serialization > Gson en proyectos modernos (compile-time, más rápido, sin reflection). |
| Imágenes | Coil 3 | Compose-first, cache automático memory+disk, mantenido. |
| Estado | `StateFlow` + `sealed interface UiState` | Pattern moderno; sealed garantiza exhaustividad. |
| Async | Coroutines, `@IoDispatcher` inyectado al Repository | Offload de IO en el boundary correcto. VM se queda en Main para updates UI. |
| Tests | JUnit 4 + MockK + `kotlinx-coroutines-test` | Sin Turbine para reducir ceremonia (StateFlow tiene `.value` accesible en tests). |
| Logging | OkHttp `HttpLoggingInterceptor` en debug | Útil para depurar en entrevista, solo en debug. |

**Versiones a fijar en `libs.versions.toml`:**
```
kotlin = "2.3.20"           # bump desde 2.2.10
ksp = "2.3.20-1.0.32"
hilt = "2.57.1"             # primera versión con soporte Kotlin 2.3
hiltNavigationCompose = "1.2.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
retrofitKotlinxSerialization = "1.0.0"
coil = "3.1.0"
lifecycleViewModelCompose = "2.10.0"   # ya en toml
coroutines = "1.10.1"
mockk = "1.13.13"
coroutinesTest = "1.10.1"
```

**Flag de compilación a agregar:**
```kotlin
// app/build.gradle.kts
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}
```

---

## 4. Arquitectura

Clean Architecture con 3 capas y dependencias invertidas:

```
ui  ────►  domain  ◄──── data
           (puro)         (implementa interfaces de domain)
```

**Regla de dependencias:**
- `domain/` no importa nada de `data/` ni de framework Android. Define interfaces (Repository) y orquesta (UseCases).
- `data/` implementa interfaces de `domain/` — imports `domain` permitidos.
- `ui/` consume `domain/` (UseCases). No conoce `data/`.
- Hilt `@Binds` conecta `CharactersRepository` (interfaz en domain) ↔ `CharactersRepositoryImpl` (impl en data).

**Justificación:**
- **Interfaz de Repository:** la dirección de dependencias domain ← data es lo que define Clean Architecture. Si el evaluador busca "arquitectura básica" en sentido moderno, mostrar el invertido vale más que el ahorro de 1 archivo.
- **UseCase:** capa de orquestación entre ViewModel y Repository. Aunque hoy es un wrapper trivial (un solo `invoke`), establece el patrón para cuando crezca (combinar fuentes, validar, transformar).
- **Mappers en `data/`:** los DTOs nunca cruzan a domain.

### Estructura de paquetes

```
com.devdaniel.arkamotest/
├── ArkamoApp.kt                              // @HiltAndroidApp
├── MainActivity.kt                            // @AndroidEntryPoint
├── data/
│   ├── remote/
│   │   ├── RickAndMortyApi.kt                 // Retrofit interface
│   │   └── dto/
│   │       ├── CharacterDto.kt                // @Serializable
│   │       └── CharactersResponseDto.kt
│   ├── mapper/CharacterMapper.kt              // DTO → Domain (data → domain único)
│   └── repository/CharactersRepositoryImpl.kt // implementa domain.repository.CharactersRepository
├── domain/
│   ├── model/
│   │   ├── Character.kt                       // id, name, image, status
│   │   └── CharacterStatus.kt                 // enum Alive/Dead/Unknown
│   ├── repository/
│   │   └── CharactersRepository.kt            // INTERFAZ (contrato puro de domain)
│   └── usecase/
│       └── GetCharactersUC.kt                 // operator fun invoke(): Result<List<Character>>
├── ui/
│   ├── characters/
│   │   ├── CharactersScreen.kt
│   │   ├── CharactersViewModel.kt             // @HiltViewModel, inyecta GetCharactersUC
│   │   ├── components/
│   │   │   ├── CharacterAvatar.kt             // wrapper Coil AsyncImage
│   │   │   ├── CharacterItem.kt
│   │   │   ├── StatusBadge.kt
│   │   │   ├── ErrorContent.kt                // mensaje + botón Retry
│   │   │   └── LoadingContent.kt              // CircularProgressIndicator centrado
│   │   └── state/
│   │       ├── CharactersUiState.kt           // sealed interface
│   │       └── ErrorMessages.kt               // Throwable.toUserMessage()
│   └── theme/                                 // existente, sin cambios
└── di/
    ├── NetworkModule.kt                       // Retrofit/OkHttp/Api
    ├── DispatcherModule.kt                    // @IoDispatcher qualifier
    └── RepositoryModule.kt                    // @Binds: Repository ← RepositoryImpl
```

**Sobre `@Inject` en domain:** el constructor `@Inject` es solo un marcador de Hilt, no acopla la lógica de dominio al framework. Lo mantenemos en `GetCharactersUC` y `CharactersRepositoryImpl` por pragmatismo (idéntico criterio que StoriBank-T).

---

## 5. Modelo de estado

```kotlin
sealed interface CharactersUiState {
    data object Loading : CharactersUiState
    data class Success(val characters: List<Character>) : CharactersUiState
    data class Error(val message: String) : CharactersUiState
}
```

**Decisión:** un solo sealed cubre los 3 estados. La UI hace `when (state)` exhaustivo. Si más adelante se agregan más estados (refreshing, partial), se añaden al sealed sin tocar el shape del flow.

---

## 6. ViewModel

```kotlin
@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val getCharacters: GetCharactersUC,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharactersUiState>(CharactersUiState.Loading)
    val uiState: StateFlow<CharactersUiState> = _uiState
        .onStart { load() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharactersUiState.Loading,
        )

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CharactersUiState.Loading
            getCharacters()
                .onSuccess { _uiState.value = CharactersUiState.Success(it) }
                .onFailure { _uiState.value = CharactersUiState.Error(it.toUserMessage()) }
        }
    }
}
```

**Notas:**
- **Sin `init { load() }`.** `onStart` dispara la carga la primera vez que `uiState` se collecta (típicamente desde `collectAsStateWithLifecycle()` en la screen). Evita cargar si nadie observa.
- **`SharingStarted.WhileSubscribed(5_000)`** preserva estado 5s tras último collector (rotación / config change sin recarga).
- **Sin Explicit Backing Field para `uiState`.** Incompatible con la chain `.onStart.stateIn` — el field expone `MutableStateFlow` directo y la transformación downstream se pierde. Si más adelante necesitamos un `StateFlow` simple sin chain, podemos usar la sintaxis nueva.
- **Sin dispatcher en `viewModelScope.launch`.** El IO se offloada en el Repository impl (sección 7). El VM se queda en Main para actualizar `_uiState.value` eficiente.
- **Depende de `GetCharactersUC`, no de `CharactersRepository`.** Respeta el invertido de Clean Architecture: la UI llega a domain por UseCases, nunca por repos.

---

## 7. Domain, Repository y Networking

### Domain — interfaz de Repository

`domain/repository/CharactersRepository.kt`:

```kotlin
package com.devdaniel.arkamotest.domain.repository

import com.devdaniel.arkamotest.domain.model.Character

interface CharactersRepository {
    suspend fun getCharacters(): Result<List<Character>>
}
```

**Sin imports de `data/`, sin imports de framework.** El contrato vive en domain y define qué necesita el dominio del exterior.

### Domain — UseCase

`domain/usecase/GetCharactersUC.kt`:

```kotlin
package com.devdaniel.arkamotest.domain.usecase

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import javax.inject.Inject

class GetCharactersUC @Inject constructor(
    private val repository: CharactersRepository,
) {
    suspend operator fun invoke(): Result<List<Character>> = repository.getCharacters()
}
```

Hoy es wrapper trivial. Establece el patrón: si mañana el caso de uso requiere combinar fuentes (cache + red), filtrar, o validar reglas de negocio, el cambio queda contenido aquí — VM y Repository no se tocan.

### Data — DTOs

```kotlin
@Serializable
data class CharactersResponseDto(
    val results: List<CharacterDto>,
)

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,   // "Alive" | "Dead" | "unknown"
    val image: String,
)
```

### Data — API

```kotlin
interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(): CharactersResponseDto
}
```

### Data — Mapper

```kotlin
// data/mapper/CharacterMapper.kt
internal fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    image = image,
    status = CharacterStatus.fromApi(status),  // "Alive"→Alive, "Dead"→Dead, "unknown"/otros→Unknown
)
```

### Data — Repository implementation

`data/repository/CharactersRepositoryImpl.kt`:

```kotlin
package com.devdaniel.arkamotest.data.repository

import com.devdaniel.arkamotest.data.mapper.toDomain
import com.devdaniel.arkamotest.data.remote.RickAndMortyApi
import com.devdaniel.arkamotest.di.IoDispatcher
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CharactersRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CharactersRepository {

    override suspend fun getCharacters(): Result<List<Character>> =
        withContext(ioDispatcher) {
            try {
                Result.success(api.getCharacters().results.map { it.toDomain() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
}
```

**Por qué `try/catch` explícito y no `runCatching`:** `runCatching` atrapa `CancellationException` → rompe structured concurrency. El idiom seguro es `try/catch` con re-throw explícito.

### DI — RepositoryModule (`@Binds`)

`di/RepositoryModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharactersRepository(
        impl: CharactersRepositoryImpl,
    ): CharactersRepository
}
```

`@Binds` es más eficiente que `@Provides` para "tomar esta impl y exponerla como esta interfaz" (cero overhead en código generado).

### DI — NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://rickandmortyapi.com/api/"

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideApi(retrofit: Retrofit): RickAndMortyApi = retrofit.create()
}
```

### DI — DispatcherModule

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

### Resumen del grafo de dependencias

```
CharactersScreen
    └─► CharactersViewModel
            └─► GetCharactersUC  (domain)
                    └─► CharactersRepository  (interfaz domain)
                            ▲
                            │ @Binds
                            │
                    CharactersRepositoryImpl  (data)
                            └─► RickAndMortyApi  (Retrofit)
                                    └─► OkHttpClient + Json
```

Domain (`Character`, `CharactersRepository`, `GetCharactersUC`) **no importa nada de `data/` ni de Retrofit / OkHttp / Hilt-framework**. Solo `javax.inject.Inject`.

---

## 8. Manejo de errores

### Taxonomía esperada

| Excepción | Origen | Mensaje al usuario |
|-----------|--------|---------------------|
| `UnknownHostException` | Sin DNS / sin internet | "Sin conexión. Verifica tu internet." |
| `SocketTimeoutException` | Red lenta | "La conexión tardó demasiado. Intenta de nuevo." |
| `IOException` (otros) | I/O genérico | "Problema de red. Intenta de nuevo." |
| `HttpException` (Retrofit) 5xx | Servidor | "El servidor está caído. Intenta en un momento." |
| `HttpException` (Retrofit) 4xx | Cliente | "No se pudieron cargar los personajes." |
| `SerializationException` | JSON corrupto | "Respuesta del servidor inválida." |
| `Throwable` | Catch-all | "Algo salió mal. Intenta de nuevo." |

### Translation layer

`ui/characters/state/ErrorMessages.kt`:

```kotlin
fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "Sin conexión. Verifica tu internet."
    is SocketTimeoutException -> "La conexión tardó demasiado. Intenta de nuevo."
    is HttpException -> if (code() in 500..599)
        "El servidor está caído. Intenta en un momento."
    else
        "No se pudieron cargar los personajes."
    is IOException -> "Problema de red. Intenta de nuevo."
    is SerializationException -> "Respuesta del servidor inválida."
    else -> "Algo salió mal. Intenta de nuevo."
}
```

### Retry

`CharactersUiState.Error` lleva el mensaje. La UI muestra botón "Reintentar" → llama `viewModel.onRetry()` → re-dispara `load()` → emite `Loading` y vuelve a intentar.

---

## 9. Carga de imágenes

### Wrapper Composable

`ui/characters/components/CharacterAvatar.kt`:

```kotlin
@Composable
fun CharacterAvatar(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        placeholder = painterResource(R.drawable.ic_avatar_placeholder),
        error = painterResource(R.drawable.ic_avatar_error),
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape),
    )
}
```

Coil 3 trae cache memory + disk activos por default. No requiere configuración extra para este caso.

### Lista eficiente

`CharactersScreen` usa `LazyColumn` con `key = { it.id }`:

```kotlin
LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(state.characters, key = { it.id }) { character ->
        CharacterItem(character)
    }
}
```

El `key` estable asegura que composiciones se reusen al hacer scroll. Coil reusa la imagen cacheada en memoria — cero recargas visibles.

### Drawables placeholder y error

Vector drawables simples a crear en `app/src/main/res/drawable/`:
- `ic_avatar_placeholder.xml`: círculo gris (#E0E0E0)
- `ic_avatar_error.xml`: círculo gris con ícono de alerta (Material `ic_error_outline`)

---

## 10. UI — pantalla principal

`CharactersScreen.kt`:

```kotlin
@Composable
fun CharactersScreen(
    viewModel: CharactersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
        when (val s = state) {
            CharactersUiState.Loading -> LoadingContent()
            is CharactersUiState.Success -> CharactersList(s.characters)
            is CharactersUiState.Error -> ErrorContent(
                message = s.message,
                onRetry = viewModel::onRetry,
            )
        }
    }
}
```

Sin TopAppBar para minimalismo (PDF: una sola pantalla). Si sobra tiempo, agregar `TopAppBar` con título "Personajes".

---

## 11. Tests

Ubicación: `app/src/test/java/com/devdaniel/arkamotest/`

Total: **3 clases de test, 10 tests**.

### `CharactersViewModelTest.kt` (5 tests)

Mocks: `GetCharactersUC` (la UI no conoce el Repository — testeamos el contrato de UC).

1. **happy path:** `getCharacters()` retorna `Result.success(lista)` → emite `Loading` → `Success(lista)`.
2. **error de red:** retorna `Result.failure(UnknownHostException())` → emite `Loading` → `Error("Sin conexión...")`.
3. **error HTTP 500:** retorna `Result.failure(HttpException(5xx))` → emite `Loading` → `Error("El servidor está caído...")`.
4. **retry tras error:** primer call falla, `onRetry()` llamado, segundo call ok → emite `Loading` → `Error` → `Loading` → `Success`.
5. **`onStart` solo carga la primera vez:** dos collectors → `getCharacters()` se llama 1 sola vez (verifica el comportamiento de `SharingStarted.WhileSubscribed`).

### `GetCharactersUCTest.kt` (2 tests)

Mocks: `CharactersRepository` (interfaz de domain).

1. **delega y propaga Success:** repo retorna `Result.success(lista)` → UC retorna lo mismo. `coVerify { repository.getCharacters() }`.
2. **delega y propaga Failure:** repo retorna `Result.failure(Throwable)` → UC retorna lo mismo.

UC hoy es trivial, pero el test fija el contrato y previene regresiones cuando se le agregue lógica (filtrado, validación).

### `CharacterMapperTest.kt` (3 tests)

Mocks: ninguno.

1. `CharacterDto("Alive").toDomain().status == CharacterStatus.Alive`
2. `CharacterDto("Dead").toDomain().status == CharacterStatus.Dead`
3. `CharacterDto("unknown").toDomain().status == CharacterStatus.Unknown` y `CharacterDto("garbage").toDomain().status == CharacterStatus.Unknown` (fallback).

### Setup compartido (VM test)

```kotlin
class CharactersViewModelTest {
    private val getCharacters: GetCharactersUC = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    // tests usan runTest, state.value y advanceUntilIdle()
}
```

### Sin test del `CharactersRepositoryImpl`

Es wrapper trivial sobre Retrofit + mapper. Mockear `RickAndMortyApi` para verificar que se llama y se mapea sería testear MockK. Si crece (cache, retries, validaciones), agregar test. Lo menciono en "qué mejoraría" del README.

### Sin tests instrumentados

`app/src/androidTest/` queda con el skeleton del template. Justificación en el README: el reto pide claridad/criterio sobre cobertura, y los unit tests cubren la lógica relevante (VM + UC + Mapper).

---

## 12. Sprint plan

### Sprint 1 — Setup (~45 min)

Objetivo: proyecto compila con todas las deps y configuración nueva. Repo creado en GitHub.

1. `git init` en `/Users/cerveruz/AndroidStudioProjects/ArkamoTest/`
2. Configurar `git config user.name "Daniel Leguizamón Rozo"` y `user.email "daniel.leguizamon.rozo@gmail.com"` (local al repo)
3. Reemplazar `CLAUDE.md` por uno descriptivo del proyecto Arkamo
4. Crear `.gitignore` adecuado para Android
5. Commit inicial con el template tal como está + spec en `docs/specs/`
6. `gh repo create Cerverussz/ArkamoTest --public --source=. --remote=origin --description "..."`
7. Push inicial
8. Bumpear Kotlin a 2.3.x en `libs.versions.toml`; agregar KSP, Hilt, Retrofit, OkHttp, Kotlinx Serialization, Coil, MockK, coroutines-test
9. Agregar plugin `kotlin.serialization` y `ksp` y `hilt` en root y `app/build.gradle.kts`
10. Agregar `freeCompilerArgs.add("-Xexplicit-backing-fields")`
11. Permiso `INTERNET` en `AndroidManifest.xml`
12. Crear `ArkamoApp.kt` con `@HiltAndroidApp`; declararlo en manifest
13. `@AndroidEntryPoint` en `MainActivity`
14. `./gradlew assembleDebug` → verde
15. Commit: "chore: bump Kotlin to 2.3, add Hilt/Retrofit/Coil deps"

**Commits objetivo S1:** 4-5 (init, spec, deps, hilt scaffold, lint pass).

### Sprint 2 — Domain + Data + ViewModel + Tests (~90 min)

Objetivo: lógica completa testeada, sin UI. Orden de adentro hacia afuera (domain primero, luego data, luego UI state/VM).

**Domain (puro, sin dependencias externas):**
1. `domain/model/Character.kt` + `domain/model/CharacterStatus.kt` (con `fromApi(String)`)
2. `domain/repository/CharactersRepository.kt` (interfaz)
3. `domain/usecase/GetCharactersUC.kt`

**Data:**
4. `data/remote/dto/CharacterDto.kt`, `CharactersResponseDto.kt`
5. `data/mapper/CharacterMapper.kt` (`fun CharacterDto.toDomain()`)
6. `data/remote/RickAndMortyApi.kt`
7. `data/repository/CharactersRepositoryImpl.kt` (implementa interfaz de domain)

**DI:**
8. `di/DispatcherModule.kt` con `@IoDispatcher`
9. `di/NetworkModule.kt` con Json/OkHttp/Retrofit/Api
10. `di/RepositoryModule.kt` con `@Binds`

**UI state + VM:**
11. `ui/characters/state/CharactersUiState.kt`
12. `ui/characters/state/ErrorMessages.kt`
13. `ui/characters/CharactersViewModel.kt`

**Tests:**
14. `CharacterMapperTest.kt` (3 tests)
15. `GetCharactersUCTest.kt` (2 tests)
16. `CharactersViewModelTest.kt` (5 tests)
17. `./gradlew testDebugUnitTest` → verde

**Commits objetivo S2:** 6-7 (uno por capa lógica: domain, data-network, data-repository-impl, di, vm+state, tests).

### Sprint 3 — UI + README + push final (~60 min)

Objetivo: app corre, README pulido, código en `main`.

1. `ui/characters/components/CharacterAvatar.kt`
2. `ui/characters/components/StatusBadge.kt`
3. `ui/characters/components/CharacterItem.kt`
4. `ui/characters/components/LoadingContent.kt`
5. `ui/characters/components/ErrorContent.kt`
6. `ui/characters/CharactersScreen.kt`
7. Drawables `ic_avatar_placeholder.xml`, `ic_avatar_error.xml`
8. Wire `MainActivity` → `setContent { ArkamoTestTheme { CharactersScreen() } }`
9. Verificación manual en emulador: lista carga, scroll fluido, avión mode → Error con Retry, recuperar → Success
10. Escribir `README.md` final con las 4 secciones obligatorias del PDF
11. Push final a `main`

**Commits objetivo S3:** 4-5.

### Total

| | |
|---|---|
| Tiempo estimado | ~3.25h enfocadas, distribuidas |
| Commits totales | 14-17 (atómicos) |
| Tests unitarios | 10 (5 VM + 2 UC + 3 Mapper) |
| Pantallas | 1 |
| Capas | data + domain + ui + di |

---

## 13. README final (esqueleto)

Estructura para cumplir las 4 secciones obligatorias del PDF:

```markdown
# Arkamo — Rick & Morty Character List

App Android que consume Rick & Morty API y muestra lista de personajes con manejo
explícito de estados (Loading / Success / Error+Retry).

## Stack
- Kotlin 2.3.x, Compose, Material 3
- Hilt, Retrofit + Kotlinx Serialization, Coil 3
- Coroutines + StateFlow
- JUnit + MockK

## Cómo correr
1. `git clone ...`
2. Abrir en Android Studio (Iguana o superior)
3. Run on emulator API 29+ con internet

## Decisiones técnicas
- Clean Architecture en 3 capas (data / domain / ui) con inversión de dependencias:
  `domain` define el contrato (`CharactersRepository` interfaz + `GetCharactersUC`)
  y `data` lo implementa (`CharactersRepositoryImpl`). Hilt `@Binds` conecta ambos.
  Domain no importa nada de data ni de framework.
- `Result<T>` para errores en lugar de excepciones cruzando capas. `try/catch`
  explícito en Repository con re-throw de `CancellationException` (evita romper
  structured concurrency).
- `StateFlow` con pattern `onStart + stateIn` para carga lazy en primera observación
  (sin `init { load() }`, que dispararía aunque nadie observe).
- Coil 3 con cache memory+disk automático; `LazyColumn(key = it.id)` para evitar
  recomposiciones / recargas innecesarias en scroll.
- Hilt aun siendo un proyecto pequeño: demuestra DI idiomática y deja base para
  escalar.
- Inyección de `@IoDispatcher` al Repository (no al VM) — offload de IO en el
  boundary correcto, VM se queda en `Dispatchers.Main.immediate` para updates UI.

## Qué quedó fuera por tiempo
- Paginación (la API soporta paginación; se cargó solo la primera página).
- Pantalla de detalle de personaje.
- Filtros por estado.
- Tests instrumentados.
- Dark mode personalizado (se usa el default de Material 3).

## Qué mejoraría con más tiempo
- Paging 3 para scroll infinito con manejo de carga incremental.
- Pantalla de detalle con navegación Compose y `SharedTransition`.
- Cache local con Room para offline-first.
- Tests instrumentados de la `CharactersScreen` con `ComposeRule`.
- Animaciones de entrada para items de la lista.

## Uso de IA
Usé Claude Code (Anthropic) para:
- Brainstorming inicial: discusión de stack, alcance y arquitectura.
- Generación del spec de diseño (`docs/specs/2026-05-15-...md`).
- Asistencia en codificación: scaffolding de boilerplate Hilt/Retrofit,
  estructura de tests.
- Revisión de patrones (p. ej. detección del bug de `get() =` con `stateIn`).

Las decisiones técnicas y la revisión final son propias.
```

---

## 14. Riesgos conocidos

| Riesgo | Mitigación |
|--------|------------|
| Kotlin 2.3.x incompatible con AGP 9.2.1 | Rollback a 2.2.10, sintaxis tradicional `_uiState` + `asStateFlow()`, 10-20 min |
| Hilt sin versión que soporte Kotlin 2.3 | Bajar a Kotlin 2.2.10 + Hilt 2.52 (último estable amplio) |
| KSP plugin no resuelve para Kotlin 2.3.20 | Buscar latest `ksp` version matching Kotlin |
| Compose Compiler plugin no listo para Kotlin 2.3 | Lo trae el plugin `kotlin.plugin.compose` (built-in), versión sigue Kotlin auto |
| Drawables placeholder/error no renderizan | Empezar con assets simples vector → si falla, usar `ColorPainter` inline |
| API de Rick & Morty caída en momento de demo | Cachear screenshot, mencionar en README, comportamiento Error+Retry es justamente lo que cubre esto |

---

## 15. Out of scope explícito

- Paginación (la API la soporta, pero el PDF pide "una lista", no scroll infinito).
- Pantalla de detalle.
- Filtros / búsqueda.
- Dark mode personalizado.
- Animaciones.
- Tests instrumentados (Compose UI tests).
- CI/CD (GitHub Actions). El PDF no lo pide; lo menciono en "qué mejoraría" del README.
- Internacionalización. Strings hardcodeadas en español por simplicidad — si saliera del scope, mover a `strings.xml`.
- Persistencia / cache local con Room.
- Modo offline.

---

## 16. Definición de "Done"

- [ ] `./gradlew assembleDebug` compila sin warnings.
- [ ] `./gradlew testDebugUnitTest` pasa todos los tests.
- [ ] App instalada en emulador carga lista de personajes.
- [ ] Activar modo avión + Retry → muestra Error → desactivar avión + Retry → muestra Success.
- [ ] `README.md` cubre las 4 secciones obligatorias del PDF.
- [ ] Repo `Cerverussz/ArkamoTest` público en GitHub con todos los commits pushed a `main`.
- [ ] PDF original `Technical Challenge - Android Developer - Arkamo 1 1.pdf` conservado en raíz como referencia (committed).
- [ ] Spec (`docs/specs/2026-05-15-arkamo-rickmorty-design.md`) committed.
- [ ] CLAUDE.md actualizado para reflejar el proyecto real.
