# Arkamo Rick & Morty Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a single-screen Android app that consumes Rick & Morty API and displays a character list with Loading/Success/Error+Retry state handling.

**Architecture:** Clean Architecture in 3 layers (data / domain / ui) with inverted dependencies. Hilt for DI, Compose for UI, `StateFlow` with `onStart + stateIn` for lazy loading on first observation.

**Tech Stack:** Kotlin 2.3.x, Jetpack Compose, Material 3, Hilt 2.57+, Retrofit + Kotlinx Serialization, Coil 3, JUnit 4 + MockK + kotlinx-coroutines-test.

**Spec:** [docs/specs/2026-05-15-arkamo-rickmorty-design.md](../specs/2026-05-15-arkamo-rickmorty-design.md)

---

## File Structure

Files to create or modify, grouped by layer:

**Root / config:**
- Modify: `CLAUDE.md` (overwrite with Arkamo-specific content)
- Create: `.gitignore` (already exists, possibly extend)
- Modify: `gradle/libs.versions.toml` (bump Kotlin, add deps)
- Modify: `build.gradle.kts` (root) (add plugins)
- Modify: `app/build.gradle.kts` (add plugins, deps, compiler flags)
- Modify: `app/src/main/AndroidManifest.xml` (INTERNET permission, ArkamoApp)

**Application:**
- Create: `app/src/main/java/com/devdaniel/arkamotest/ArkamoApp.kt`
- Modify: `app/src/main/java/com/devdaniel/arkamotest/MainActivity.kt`

**Domain (pure Kotlin, no Android imports):**
- Create: `app/src/main/java/com/devdaniel/arkamotest/domain/model/Character.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/domain/model/CharacterStatus.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/domain/repository/CharactersRepository.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/domain/usecase/GetCharactersUC.kt`

**Data:**
- Create: `app/src/main/java/com/devdaniel/arkamotest/data/remote/dto/CharacterDto.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/data/remote/dto/CharactersResponseDto.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/data/remote/RickAndMortyApi.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/data/mapper/CharacterMapper.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/data/repository/CharactersRepositoryImpl.kt`

**DI:**
- Create: `app/src/main/java/com/devdaniel/arkamotest/di/DispatcherModule.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/di/NetworkModule.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/di/RepositoryModule.kt`

**UI state + ViewModel:**
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/state/CharactersUiState.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/state/ErrorMessages.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/CharactersViewModel.kt`

**UI composables:**
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/CharactersScreen.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/CharacterAvatar.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/CharacterItem.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/StatusBadge.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/LoadingContent.kt`
- Create: `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/ErrorContent.kt`

**Drawables:**
- Create: `app/src/main/res/drawable/ic_avatar_placeholder.xml`
- Create: `app/src/main/res/drawable/ic_avatar_error.xml`

**Tests:**
- Create: `app/src/test/java/com/devdaniel/arkamotest/data/mapper/CharacterMapperTest.kt`
- Create: `app/src/test/java/com/devdaniel/arkamotest/domain/usecase/GetCharactersUCTest.kt`
- Create: `app/src/test/java/com/devdaniel/arkamotest/ui/characters/CharactersViewModelTest.kt`
- Delete: `app/src/test/java/com/devdaniel/arkamotest/ExampleUnitTest.kt`
- Delete: `app/src/androidTest/java/com/devdaniel/arkamotest/ExampleInstrumentedTest.kt`

**Docs:**
- Create (in Sprint 3): `README.md`

---

# SPRINT 1 — Setup (~45 min)

## Task 1: Initialize git and configure author

- [ ] **Step 1.1:** Initialize git repo

```bash
cd /Users/cerveruz/AndroidStudioProjects/ArkamoTest
git init
```

- [ ] **Step 1.2:** Configure author (local to this repo)

```bash
git config --local user.name "Daniel Leguizamón Rozo"
git config --local user.email "daniel.leguizamon.rozo@gmail.com"
```

- [ ] **Step 1.3:** Verify config

```bash
git config --local --get user.name && git config --local --get user.email
```

Expected: prints name and email.

## Task 2: Replace CLAUDE.md with Arkamo-specific content

- [ ] **Step 2.1:** Overwrite `CLAUDE.md` with project-accurate content (Arkamo challenge, Clean Architecture, Hilt, Compose, Retrofit, Coil — describes the actual project)

Content guidance: short, points to spec for details, commands for build/test/lint, key architectural rules, package locations.

## Task 3: Verify/extend .gitignore for Android

- [ ] **Step 3.1:** Read existing `.gitignore` and ensure it covers: `.idea/`, `.gradle/`, `build/`, `local.properties`, `*.iml`, `.kotlin/`, `app/build/`

If missing entries, append them.

## Task 4: Create initial commit and GitHub repo

- [ ] **Step 4.1:** Stage everything and create initial commit

```bash
git add .
git commit -m "chore: initial template + spec + plan from Android Studio bootstrap"
```

- [ ] **Step 4.2:** Create remote repo and push

```bash
gh repo create Cerverussz/ArkamoTest --public --source=. --remote=origin \
  --description "Arkamo Android technical challenge — Rick & Morty character list with Clean Architecture, Hilt, Compose, Retrofit, Coil" \
  --push
```

Expected: outputs repo URL.

- [ ] **Step 4.3:** Verify remote

```bash
git remote -v
```

Expected: `origin  https://github.com/Cerverussz/ArkamoTest.git (fetch/push)`

## Task 5: Bump Kotlin and add dependencies to `libs.versions.toml`

**File:** `gradle/libs.versions.toml`

- [ ] **Step 5.1:** Replace the file entirely with the new version. Final content:

```toml
[versions]
agp = "9.2.1"
kotlin = "2.3.20"
ksp = "2.3.20-1.0.32"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
composeBom = "2026.02.01"
hilt = "2.57.1"
hiltNavigationCompose = "1.2.0"
retrofit = "2.11.0"
retrofitKotlinxSerialization = "1.0.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
coil = "3.1.0"
coroutines = "1.10.1"
mockk = "1.13.13"
javaxInject = "1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }

# DI
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
javax-inject = { group = "javax.inject", name = "javax.inject", version.ref = "javaxInject" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofitKotlinxSerialization" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

# Images
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network-okhttp = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Tests
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

**Risks acknowledged in spec section 14:** if any version mismatch surfaces during build, fallback path is documented (Kotlin 2.2.10 + Hilt 2.52).

## Task 6: Update Gradle files for new plugins and deps

**Files:** `build.gradle.kts` (root), `app/build.gradle.kts`

- [ ] **Step 6.1:** Replace `build.gradle.kts` (root):

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 6.2:** Replace `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.devdaniel.arkamotest"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.devdaniel.arkamotest"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

dependencies {
    // Core / Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.javax.inject)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Tests (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Tests (instrumented)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

## Task 7: Create `ArkamoApp` application class

**File:** `app/src/main/java/com/devdaniel/arkamotest/ArkamoApp.kt`

- [ ] **Step 7.1:** Create file:

```kotlin
package com.devdaniel.arkamotest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArkamoApp : Application()
```

## Task 8: Add INTERNET permission, register ArkamoApp, annotate MainActivity

**Files:** `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/devdaniel/arkamotest/MainActivity.kt`

- [ ] **Step 8.1:** Read current `AndroidManifest.xml` to understand the existing structure.

- [ ] **Step 8.2:** Add `<uses-permission android:name="android.permission.INTERNET" />` before `<application>` and set `android:name=".ArkamoApp"` on the `<application>` tag.

- [ ] **Step 8.3:** Read current `MainActivity.kt`.

- [ ] **Step 8.4:** Add `@AndroidEntryPoint` annotation above the `class MainActivity : ComponentActivity()` declaration. Import `dagger.hilt.android.AndroidEntryPoint`. Leave rest of the template's setContent unchanged for now.

## Task 9: Verify Sprint 1 build

- [ ] **Step 9.1:** Run build

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. If there are version conflicts (Kotlin 2.3 / AGP 9 / Hilt), debug per spec section 14 (Riesgos).

- [ ] **Step 9.2:** Commit setup work

```bash
git add .
git commit -m "chore: bump Kotlin to 2.3, add Hilt/Retrofit/Coil/Serialization deps and DI scaffold"
git push -u origin main
```

---

# SPRINT 2 — Domain + Data + ViewModel + Tests (~90 min)

## Task 10: Domain — `Character` and `CharacterStatus` models

**File:** `app/src/main/java/com/devdaniel/arkamotest/domain/model/CharacterStatus.kt`

- [ ] **Step 10.1:** Create the enum with factory:

```kotlin
package com.devdaniel.arkamotest.domain.model

enum class CharacterStatus {
    Alive,
    Dead,
    Unknown,
    ;

    companion object {
        fun fromApi(raw: String): CharacterStatus = when (raw.lowercase()) {
            "alive" -> Alive
            "dead" -> Dead
            else -> Unknown
        }
    }
}
```

**File:** `app/src/main/java/com/devdaniel/arkamotest/domain/model/Character.kt`

- [ ] **Step 10.2:** Create the domain model:

```kotlin
package com.devdaniel.arkamotest.domain.model

data class Character(
    val id: Int,
    val name: String,
    val image: String,
    val status: CharacterStatus,
)
```

## Task 11: Domain — `CharactersRepository` interface

**File:** `app/src/main/java/com/devdaniel/arkamotest/domain/repository/CharactersRepository.kt`

- [ ] **Step 11.1:** Create the interface:

```kotlin
package com.devdaniel.arkamotest.domain.repository

import com.devdaniel.arkamotest.domain.model.Character

interface CharactersRepository {
    suspend fun getCharacters(): Result<List<Character>>
}
```

## Task 12: Domain — `GetCharactersUC` use case

**File:** `app/src/main/java/com/devdaniel/arkamotest/domain/usecase/GetCharactersUC.kt`

- [ ] **Step 12.1:** Create the use case:

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

- [ ] **Step 12.2:** Commit domain layer

```bash
git add app/src/main/java/com/devdaniel/arkamotest/domain
git commit -m "feat(domain): add Character model, repository interface, GetCharactersUC"
```

## Task 13: Data — DTOs

**File:** `app/src/main/java/com/devdaniel/arkamotest/data/remote/dto/CharacterDto.kt`

- [ ] **Step 13.1:** Create DTO:

```kotlin
package com.devdaniel.arkamotest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val image: String,
)
```

**File:** `app/src/main/java/com/devdaniel/arkamotest/data/remote/dto/CharactersResponseDto.kt`

- [ ] **Step 13.2:** Create response wrapper:

```kotlin
package com.devdaniel.arkamotest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    val results: List<CharacterDto>,
)
```

## Task 14: Data — Retrofit API interface

**File:** `app/src/main/java/com/devdaniel/arkamotest/data/remote/RickAndMortyApi.kt`

- [ ] **Step 14.1:** Create API:

```kotlin
package com.devdaniel.arkamotest.data.remote

import com.devdaniel.arkamotest.data.remote.dto.CharactersResponseDto
import retrofit2.http.GET

interface RickAndMortyApi {
    @GET("character")
    suspend fun getCharacters(): CharactersResponseDto
}
```

## Task 15: Data — `CharacterMapper`

**File:** `app/src/main/java/com/devdaniel/arkamotest/data/mapper/CharacterMapper.kt`

- [ ] **Step 15.1:** Create mapper:

```kotlin
package com.devdaniel.arkamotest.data.mapper

import com.devdaniel.arkamotest.data.remote.dto.CharacterDto
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus

internal fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    image = image,
    status = CharacterStatus.fromApi(status),
)
```

## Task 16: DI — `DispatcherModule`

**File:** `app/src/main/java/com/devdaniel/arkamotest/di/DispatcherModule.kt`

- [ ] **Step 16.1:** Create dispatcher module + qualifier:

```kotlin
package com.devdaniel.arkamotest.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

## Task 17: DI — `NetworkModule`

**File:** `app/src/main/java/com/devdaniel/arkamotest/di/NetworkModule.kt`

- [ ] **Step 17.1:** Create network module:

```kotlin
package com.devdaniel.arkamotest.di

import com.devdaniel.arkamotest.BuildConfig
import com.devdaniel.arkamotest.data.remote.RickAndMortyApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://rickandmortyapi.com/api/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideRickAndMortyApi(retrofit: Retrofit): RickAndMortyApi = retrofit.create()
}
```

## Task 18: Data — `CharactersRepositoryImpl`

**File:** `app/src/main/java/com/devdaniel/arkamotest/data/repository/CharactersRepositoryImpl.kt`

- [ ] **Step 18.1:** Create implementation:

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

## Task 19: DI — `RepositoryModule` (`@Binds`)

**File:** `app/src/main/java/com/devdaniel/arkamotest/di/RepositoryModule.kt`

- [ ] **Step 19.1:** Create binding module:

```kotlin
package com.devdaniel.arkamotest.di

import com.devdaniel.arkamotest.data.repository.CharactersRepositoryImpl
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

- [ ] **Step 19.2:** Build to verify Hilt graph resolves

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. If KSP/Hilt errors, check `kapt.correctErrorTypes` style messages and fix.

- [ ] **Step 19.3:** Commit data layer + DI

```bash
git add app/src/main/java/com/devdaniel/arkamotest/data app/src/main/java/com/devdaniel/arkamotest/di
git commit -m "feat(data,di): add API, DTOs, mapper, repository impl, Hilt modules"
```

## Task 20: UI state — `CharactersUiState`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/state/CharactersUiState.kt`

- [ ] **Step 20.1:** Create sealed interface:

```kotlin
package com.devdaniel.arkamotest.ui.characters.state

import com.devdaniel.arkamotest.domain.model.Character

sealed interface CharactersUiState {
    data object Loading : CharactersUiState
    data class Success(val characters: List<Character>) : CharactersUiState
    data class Error(val message: String) : CharactersUiState
}
```

## Task 21: UI state — `ErrorMessages` extension

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/state/ErrorMessages.kt`

- [ ] **Step 21.1:** Create translation layer:

```kotlin
package com.devdaniel.arkamotest.ui.characters.state

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "Sin conexión. Verifica tu internet."
    is SocketTimeoutException -> "La conexión tardó demasiado. Intenta de nuevo."
    is HttpException -> if (code() in 500..599) {
        "El servidor está caído. Intenta en un momento."
    } else {
        "No se pudieron cargar los personajes."
    }
    is IOException -> "Problema de red. Intenta de nuevo."
    is SerializationException -> "Respuesta del servidor inválida."
    else -> "Algo salió mal. Intenta de nuevo."
}
```

## Task 22: ViewModel — `CharactersViewModel`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/CharactersViewModel.kt`

- [ ] **Step 22.1:** Create ViewModel:

```kotlin
package com.devdaniel.arkamotest.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devdaniel.arkamotest.domain.usecase.GetCharactersUC
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState
import com.devdaniel.arkamotest.ui.characters.state.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

- [ ] **Step 22.2:** Build to confirm

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 22.3:** Commit UI state + VM

```bash
git add app/src/main/java/com/devdaniel/arkamotest/ui/characters/state app/src/main/java/com/devdaniel/arkamotest/ui/characters/CharactersViewModel.kt
git commit -m "feat(ui): add CharactersUiState, ErrorMessages, CharactersViewModel"
```

## Task 23: Test — `CharacterMapperTest`

**File:** `app/src/test/java/com/devdaniel/arkamotest/data/mapper/CharacterMapperTest.kt`

- [ ] **Step 23.1:** Delete the template example test:

```bash
rm app/src/test/java/com/devdaniel/arkamotest/ExampleUnitTest.kt
```

- [ ] **Step 23.2:** Create mapper test:

```kotlin
package com.devdaniel.arkamotest.data.mapper

import com.devdaniel.arkamotest.data.remote.dto.CharacterDto
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterMapperTest {

    @Test
    fun `toDomain maps Alive status correctly`() {
        val dto = CharacterDto(id = 1, name = "Rick", status = "Alive", image = "url")

        val result = dto.toDomain()

        assertEquals(CharacterStatus.Alive, result.status)
        assertEquals(1, result.id)
        assertEquals("Rick", result.name)
        assertEquals("url", result.image)
    }

    @Test
    fun `toDomain maps Dead status correctly`() {
        val dto = CharacterDto(id = 2, name = "Birdperson", status = "Dead", image = "url")

        val result = dto.toDomain()

        assertEquals(CharacterStatus.Dead, result.status)
    }

    @Test
    fun `toDomain maps unknown and unrecognized status to Unknown`() {
        val unknownDto = CharacterDto(id = 3, name = "??", status = "unknown", image = "url")
        val garbageDto = CharacterDto(id = 4, name = "??", status = "garbage", image = "url")

        assertEquals(CharacterStatus.Unknown, unknownDto.toDomain().status)
        assertEquals(CharacterStatus.Unknown, garbageDto.toDomain().status)
    }
}
```

- [ ] **Step 23.3:** Run

```bash
./gradlew testDebugUnitTest --tests "com.devdaniel.arkamotest.data.mapper.CharacterMapperTest"
```

Expected: 3 tests pass.

## Task 24: Test — `GetCharactersUCTest`

**File:** `app/src/test/java/com/devdaniel/arkamotest/domain/usecase/GetCharactersUCTest.kt`

- [ ] **Step 24.1:** Create UC test:

```kotlin
package com.devdaniel.arkamotest.domain.usecase

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import com.devdaniel.arkamotest.domain.repository.CharactersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCharactersUCTest {

    private val repository: CharactersRepository = mockk()
    private val useCase = GetCharactersUC(repository)

    @Test
    fun `invoke delegates to repository and propagates Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { repository.getCharacters() } returns Result.success(expected)

        val result = useCase()

        assertEquals(Result.success(expected), result)
        coVerify(exactly = 1) { repository.getCharacters() }
    }

    @Test
    fun `invoke propagates Failure from repository`() = runTest {
        val error = RuntimeException("boom")
        coEvery { repository.getCharacters() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
```

- [ ] **Step 24.2:** Run

```bash
./gradlew testDebugUnitTest --tests "com.devdaniel.arkamotest.domain.usecase.GetCharactersUCTest"
```

Expected: 2 tests pass.

## Task 25: Test — `CharactersViewModelTest`

**File:** `app/src/test/java/com/devdaniel/arkamotest/ui/characters/CharactersViewModelTest.kt`

- [ ] **Step 25.1:** Create VM test:

```kotlin
package com.devdaniel.arkamotest.ui.characters

import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.domain.model.CharacterStatus
import com.devdaniel.arkamotest.domain.usecase.GetCharactersUC
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    private val getCharacters: GetCharactersUC = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `happy path emits Loading then Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returns Result.success(expected)
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(CharactersUiState.Success(expected), viewModel.uiState.value)
        collector.cancel()
    }

    @Test
    fun `network failure emits Loading then Error with no-internet message`() = runTest {
        coEvery { getCharacters() } returns Result.failure(UnknownHostException("no net"))
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CharactersUiState.Error)
        assertEquals(
            "Sin conexión. Verifica tu internet.",
            (state as CharactersUiState.Error).message,
        )
        collector.cancel()
    }

    @Test
    fun `http 500 emits Loading then Error with server-down message`() = runTest {
        val httpError = HttpException(
            retrofit2.Response.error<Any>(
                500,
                "".toResponseBody("text/plain".toMediaType()),
            ),
        )
        coEvery { getCharacters() } returns Result.failure(httpError)
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CharactersUiState.Error)
        assertEquals(
            "El servidor está caído. Intenta en un momento.",
            (state as CharactersUiState.Error).message,
        )
        collector.cancel()
    }

    @Test
    fun `onRetry after error re-emits Loading then Success`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returnsMany listOf(
            Result.failure(UnknownHostException("no net")),
            Result.success(expected),
        )
        val viewModel = CharactersViewModel(getCharacters)

        val collector = launch { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is CharactersUiState.Error)

        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(CharactersUiState.Success(expected), viewModel.uiState.value)
        collector.cancel()
    }

    @Test
    fun `onStart triggers load only once per WhileSubscribed window`() = runTest {
        val expected = listOf(
            Character(id = 1, name = "Rick", image = "url", status = CharacterStatus.Alive),
        )
        coEvery { getCharacters() } returns Result.success(expected)
        val viewModel = CharactersViewModel(getCharacters)

        val c1 = launch { viewModel.uiState.collect() }
        val c2 = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        coVerify(exactly = 1) { getCharacters() }
        c1.cancel()
        c2.cancel()
    }
}
```

- [ ] **Step 25.2:** Run the full test suite

```bash
./gradlew testDebugUnitTest
```

Expected: 10 tests pass total.

- [ ] **Step 25.3:** Commit tests

```bash
git add app/src/test/java/com/devdaniel/arkamotest
git commit -m "test: add unit tests for mapper, use case, and ViewModel (10 tests)"
git push
```

---

# SPRINT 3 — UI + README + push final (~60 min)

## Task 26: Drawables — placeholder and error avatars

**File:** `app/src/main/res/drawable/ic_avatar_placeholder.xml`

- [ ] **Step 26.1:** Create placeholder vector (filled circle):

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#E0E0E0"
        android:pathData="M12,12m-12,0a12,12 0 1,0 24,0a12,12 0 1,0 -24,0" />
</vector>
```

**File:** `app/src/main/res/drawable/ic_avatar_error.xml`

- [ ] **Step 26.2:** Create error vector (circle with broken-image icon):

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#E0E0E0"
        android:pathData="M12,12m-12,0a12,12 0 1,0 24,0a12,12 0 1,0 -24,0" />
    <path
        android:fillColor="#9E9E9E"
        android:pathData="M11,15h2v2h-2zM11,7h2v6h-2z" />
</vector>
```

## Task 27: Composable — `CharacterAvatar`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/CharacterAvatar.kt`

- [ ] **Step 27.1:** Create wrapper:

```kotlin
package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.devdaniel.arkamotest.R

@Composable
fun CharacterAvatar(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier.size(64.dp),
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

## Task 28: Composable — `StatusBadge`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/StatusBadge.kt`

- [ ] **Step 28.1:** Create badge:

```kotlin
package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devdaniel.arkamotest.domain.model.CharacterStatus

@Composable
fun StatusBadge(
    status: CharacterStatus,
    modifier: Modifier = Modifier,
) {
    val (color, label) = when (status) {
        CharacterStatus.Alive -> Color(0xFF2E7D32) to "Alive"
        CharacterStatus.Dead -> Color(0xFFC62828) to "Dead"
        CharacterStatus.Unknown -> Color(0xFF757575) to "Unknown"
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

## Task 29: Composable — `CharacterItem`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/CharacterItem.kt`

- [ ] **Step 29.1:** Create row item:

```kotlin
package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devdaniel.arkamotest.domain.model.Character

@Composable
fun CharacterItem(
    character: Character,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CharacterAvatar(
            imageUrl = character.image,
            contentDescription = character.name,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
            )
            StatusBadge(status = character.status)
        }
    }
}
```

## Task 30: Composable — `LoadingContent`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/LoadingContent.kt`

- [ ] **Step 30.1:** Create centered spinner:

```kotlin
package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
```

## Task 31: Composable — `ErrorContent`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/components/ErrorContent.kt`

- [ ] **Step 31.1:** Create error UI with retry:

```kotlin
package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
```

## Task 32: Composable — `CharactersScreen`

**File:** `app/src/main/java/com/devdaniel/arkamotest/ui/characters/CharactersScreen.kt`

- [ ] **Step 32.1:** Create screen:

```kotlin
package com.devdaniel.arkamotest.ui.characters

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devdaniel.arkamotest.domain.model.Character
import com.devdaniel.arkamotest.ui.characters.components.CharacterItem
import com.devdaniel.arkamotest.ui.characters.components.ErrorContent
import com.devdaniel.arkamotest.ui.characters.components.LoadingContent
import com.devdaniel.arkamotest.ui.characters.state.CharactersUiState

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

@Composable
private fun CharactersList(characters: List<Character>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(characters, key = { it.id }) { character ->
            CharacterItem(character)
            HorizontalDivider()
        }
    }
}
```

## Task 33: Wire `MainActivity` to `CharactersScreen`

**File:** `app/src/main/java/com/devdaniel/arkamotest/MainActivity.kt`

- [ ] **Step 33.1:** Replace the entire file with this final version:

```kotlin
package com.devdaniel.arkamotest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.devdaniel.arkamotest.ui.characters.CharactersScreen
import com.devdaniel.arkamotest.ui.theme.ArkamoTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArkamoTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    CharactersScreen()
                }
            }
        }
    }
}
```

The Scaffold inner padding is intentionally ignored — for a single full-screen list with edge-to-edge enabled, the LazyColumn inside `CharactersScreen` will draw under the system bars; this is acceptable for the challenge scope.

- [ ] **Step 33.2:** Build

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 33.3:** Commit UI composables

```bash
git add app/src/main/java/com/devdaniel/arkamotest/ui app/src/main/res/drawable/ic_avatar_placeholder.xml app/src/main/res/drawable/ic_avatar_error.xml app/src/main/java/com/devdaniel/arkamotest/MainActivity.kt
git commit -m "feat(ui): add CharactersScreen, components (avatar/item/badge/loading/error), wire to MainActivity"
```

## Task 34: Manual verification in emulator

- [ ] **Step 34.1:** Install to a running emulator

```bash
./gradlew installDebug
```

Expected: `Installed on N device.`

- [ ] **Step 34.2:** Launch the app on the emulator. Verify:
  - Lista de personajes carga (debería mostrar ~20 personajes de la primera página)
  - Scroll fluido sin parpadeos en imágenes
  - Cada item muestra avatar circular, nombre y badge de estado con color
  - Activar modo avión en el emulador (Quick Settings → Airplane) y presionar Retry → muestra Error con mensaje "Sin conexión..."
  - Desactivar modo avión y presionar Retry → vuelve a Success con la lista

- [ ] **Step 34.3:** If anything breaks, fix and re-run.

## Task 35: Write `README.md`

**File:** `README.md`

- [ ] **Step 35.1:** Create README per spec section 13:

```markdown
# Arkamo — Rick & Morty Character List

App Android que consume [Rick & Morty API](https://rickandmortyapi.com/api) y muestra una lista de personajes con manejo explícito de estados (Loading / Success / Error + Retry).

Prueba técnica para Arkamo (Android Developer). Ver [docs/specs/](docs/specs/2026-05-15-arkamo-rickmorty-design.md) para el diseño detallado y [docs/plans/](docs/plans/2026-05-15-arkamo-rickmorty.md) para el plan de implementación.

## Stack
- **Lenguaje:** Kotlin 2.3.x
- **UI:** Jetpack Compose, Material 3
- **DI:** Hilt 2.57+ (KSP)
- **Networking:** Retrofit + Kotlinx Serialization + OkHttp
- **Imágenes:** Coil 3
- **Async / estado:** Coroutines, `StateFlow`
- **Tests:** JUnit 4 + MockK + `kotlinx-coroutines-test`

## Cómo correr
1. `git clone https://github.com/Cerverussz/ArkamoTest.git`
2. Abrir en Android Studio (Iguana o superior).
3. Ejecutar en un emulador con API 29+ y conexión a internet.

Comandos útiles:

| Comando | Acción |
|---|---|
| `./gradlew assembleDebug` | Compila la APK debug |
| `./gradlew testDebugUnitTest` | Ejecuta los 10 tests JVM |
| `./gradlew installDebug` | Instala en el emulador conectado |

## Arquitectura

Clean Architecture en 3 capas con inversión de dependencias:

```
ui  ────►  domain  ◄────  data
           (puro)         (implementa interfaces de domain)
```

- `domain/` define el contrato (`CharactersRepository` interfaz + `GetCharactersUC`) y modelos puros.
- `data/` implementa la interfaz (`CharactersRepositoryImpl`), maneja Retrofit y mapeo DTO → Domain.
- `ui/` consume use cases vía ViewModel. No conoce `data/`.
- Hilt `@Binds` conecta `CharactersRepository` ↔ `CharactersRepositoryImpl`.

## Decisiones técnicas

- **Clean Architecture estricta** con interfaz de repositorio en `domain/` e impl en `data/`. La UI llega a domain por UseCases, nunca por repositorios. El UseCase actual es trivial, pero deja el patrón establecido si crece (cache, validación, combinación de fuentes).
- **`Result<T>` para errores** en lugar de excepciones cruzando capas. `try/catch` explícito en el Repository con re-throw de `CancellationException` (evita romper structured concurrency, problema típico de `runCatching`).
- **`StateFlow` con `onStart + stateIn`** para carga lazy en primera observación. Sin `init { load() }` porque dispararía aunque nadie observe; `SharingStarted.WhileSubscribed(5_000)` preserva estado en rotaciones.
- **Coil 3** con cache memory + disk automático; `LazyColumn(key = it.id)` para evitar recomposiciones y recargas innecesarias en scroll.
- **Hilt** aun siendo un proyecto pequeño: demuestra DI idiomática y deja base para escalar.
- **`@IoDispatcher` inyectado al Repository** (no al VM) — offload de IO en el boundary correcto, VM se queda en `Dispatchers.Main.immediate` para updates UI eficientes.
- **Kotlin 2.3.x** habilita el flag `-Xexplicit-backing-fields`. No se usa en `uiState` por incompatibilidad con `onStart + stateIn`, pero queda disponible para casos futuros.

## Qué quedó fuera por tiempo
- Paginación (la API la soporta; se carga solo la primera página, ~20 personajes).
- Pantalla de detalle de personaje.
- Filtros por estado (Alive/Dead/Unknown).
- Tests instrumentados (Compose UI tests con `ComposeRule`).
- Tests del `CharactersRepositoryImpl` (wrapper trivial sobre Retrofit + mapper).
- Dark mode personalizado (se usa el default de Material 3).
- Internacionalización (strings en español hardcodeadas).

## Qué mejoraría con más tiempo
- Paging 3 para scroll infinito con manejo de carga incremental y placeholders.
- Pantalla de detalle con navegación Compose y `SharedTransition`.
- Cache local con Room para offline-first.
- Tests instrumentados de `CharactersScreen` con `ComposeRule`.
- Test de `CharactersRepositoryImpl` con un `MockWebServer`.
- CI con GitHub Actions: build + tests + ktlint en cada PR.
- Animaciones de entrada para items de la lista.

## Uso de IA
Usé [Claude Code](https://claude.com/claude-code) (Anthropic) para:
- **Brainstorming inicial:** discusión de stack, alcance y arquitectura. Iteramos sobre Hilt vs DI manual, Clean Architecture estricta vs simplificada, y el pattern `onStart + stateIn`.
- **Generación del spec de diseño** (`docs/specs/2026-05-15-arkamo-rickmorty-design.md`).
- **Generación del plan de implementación** (`docs/plans/2026-05-15-arkamo-rickmorty.md`).
- **Asistencia en codificación:** scaffolding de Hilt modules, configuración Retrofit + Kotlinx Serialization, estructura de tests.
- **Revisión de patrones:** detección del bug del `get() =` con `stateIn` (re-crea el flow en cada acceso); recordatorio de la trampa de `runCatching` con `CancellationException`.

Las decisiones técnicas y la revisión final son propias.
```

## Task 36: Final commit and push

- [ ] **Step 36.1:** Run final verification

```bash
./gradlew assembleDebug && ./gradlew testDebugUnitTest
```

Expected: both green, 10 tests pass.

- [ ] **Step 36.2:** Clean up template instrumented test

```bash
rm app/src/androidTest/java/com/devdaniel/arkamotest/ExampleInstrumentedTest.kt
```

- [ ] **Step 36.3:** Commit and push

```bash
git add README.md app/src/androidTest
git commit -m "docs: add README with stack, architecture, decisions, and AI usage notes"
git push
```

- [ ] **Step 36.4:** Verify on GitHub

```bash
gh repo view Cerverussz/ArkamoTest --web
```

Confirm in browser: latest commits visible, README rendered, no missing files.

---

## Definition of Done (final checklist)

- [ ] `./gradlew assembleDebug` compila sin warnings.
- [ ] `./gradlew testDebugUnitTest` pasa 10 tests.
- [ ] App instalada en emulador carga lista de personajes con scroll fluido.
- [ ] Modo avión + Retry → muestra Error → desactivar avión + Retry → muestra Success.
- [ ] `README.md` cubre las 4 secciones obligatorias del PDF.
- [ ] Repo `Cerverussz/ArkamoTest` público en GitHub con todos los commits pushed a `main`.
- [ ] PDF original `Technical Challenge - Android Developer - Arkamo 1 1.pdf` conservado en raíz como referencia.
- [ ] Spec y plan committed bajo `docs/`.
- [ ] CLAUDE.md actualizado para reflejar el proyecto real.
