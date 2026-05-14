# indoornavigationtest

Production-oriented Android indoor navigation architecture using Kotlin, Jetpack Compose, Clean Architecture, MVVM, and a MultiSet SDK abstraction.

## 1) Target Architecture

```text
indoornavigationtest/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/berkbelhan/indoornavigation/
│       ├── MainActivity.kt
│       ├── IndoorNavigationApp.kt
│       ├── core/
│       │   ├── common/Result.kt
│       │   ├── common/AppError.kt
│       │   ├── flags/FeatureFlags.kt
│       │   ├── security/SecureTokenStore.kt
│       │   └── dispatcher/DispatcherProvider.kt
│       ├── domain/
│       │   ├── model/
│       │   ├── repository/
│       │   └── usecase/
│       ├── data/
│       │   ├── local/room/
│       │   ├── local/datastore/
│       │   ├── remote/api/
│       │   ├── multiset/
│       │   └── repository/
│       └── presentation/
│           ├── nav/AppNavGraph.kt
│           ├── auth/
│           ├── dashboard/
│           ├── maps/
│           ├── localization/
│           ├── navigation/
│           ├── ar/
│           ├── downloads/
│           └── settings/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

Feature modules represented by package boundaries: `auth`, `localization`, `navigation`, `maps`, `downloads`, `settings`, `ar`.

## 2) Dependency Setup (Gradle)

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "indoornavigationtest"
include(":app")
```

```kotlin
// app/build.gradle.kts (important dependencies)
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.01.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("com.google.dagger:hilt-android:2.56")
    kapt("com.google.dagger:hilt-android-compiler:2.56")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    implementation("androidx.room:room-runtime:2.8.0")
    implementation("androidx.room:room-ktx:2.8.0")
    kapt("androidx.room:room-compiler:2.8.0")

    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    implementation("com.google.ar:core:1.48.0")
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")

    // MultiSet SDK dependency should be pinned to official release/tag from:
    // https://github.com/MultiSet-AI/multiset-android-sdk
    // implementation("com.github.MultiSet-AI:multiset-android-sdk:<version>")
}
```

## 3) Core Contracts (Clean Architecture)

```kotlin
// core/common/Result.kt
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}

sealed interface AppError {
    data class Network(val message: String? = null) : AppError
    data class Auth(val message: String? = null) : AppError
    data class Localization(val message: String? = null) : AppError
    data class Storage(val message: String? = null) : AppError
    data class Unknown(val throwable: Throwable? = null) : AppError
}
```

```kotlin
// core/flags/FeatureFlags.kt
interface FeatureFlags {
    val arNavigationEnabled: kotlinx.coroutines.flow.Flow<Boolean>
    val offlineLocalizationEnabled: kotlinx.coroutines.flow.Flow<Boolean>
    val mapRenderingEngine: kotlinx.coroutines.flow.Flow<MapEngine>
}

enum class MapEngine { MAP_LIBRE, FILAMENT, DEFAULT_2D }
```

## 4) MultiSet SDK Abstraction (Provider Strategy)

```kotlin
// domain/repository/LocalizationRepository.kt
interface LocalizationRepository {
    suspend fun initialize(): Result<Unit>
    suspend fun authenticate(token: String): Result<Unit>
    suspend fun localizeSingleFrame(frameBytes: ByteArray): Result<LocalizedPose>
    suspend fun startTracking(): kotlinx.coroutines.flow.Flow<TrackingState>
    suspend fun stopTracking(): Result<Unit>
}

data class LocalizedPose(
    val mapId: String,
    val xMeters: Double,
    val yMeters: Double,
    val zMeters: Double,
    val headingDegrees: Float,
    val confidence: Float
)

enum class TrackingState { INITIALIZING, TRACKING, LOST, ERROR }
```

```kotlin
// data/multiset/LocalizationProvider.kt
interface LocalizationProvider {
    suspend fun initSdk(): Result<Unit>
    suspend fun login(accessToken: String): Result<Unit>
    suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose>
    fun trackingStates(): kotlinx.coroutines.flow.Flow<TrackingState>
    suspend fun shutdown(): Result<Unit>
}
```

```kotlin
// data/multiset/MultiSetLocalizationProvider.kt
class MultiSetLocalizationProvider @javax.inject.Inject constructor(
    private val io: kotlinx.coroutines.CoroutineDispatcher
) : LocalizationProvider {

    override suspend fun initSdk(): Result<Unit> = withContext(io) {
        runCatching {
            // MultiSet SDK init call here.
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(AppError.Localization(it.message)) }
        )
    }

    override suspend fun login(accessToken: String): Result<Unit> = withContext(io) {
        runCatching {
            // MultiSet SDK auth call here.
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(AppError.Auth(it.message)) }
        )
    }

    override suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose> = withContext(io) {
        runCatching {
            // TODO: Replace this placeholder with real MultiSet SDK localization output.
            LocalizedPose("default-map", 0.0, 0.0, 0.0, 0f, 0.0f) // Placeholder: unlocalized confidence
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Failure(AppError.Localization(it.message)) }
        )
    }

    override fun trackingStates(): kotlinx.coroutines.flow.Flow<TrackingState> =
        kotlinx.coroutines.flow.flowOf(TrackingState.INITIALIZING)

    override suspend fun shutdown(): Result<Unit> = Result.Success(Unit)
}
```

## 5) Offline Bundle Management (Download + Cache)

```kotlin
// domain/repository/MapBundleRepository.kt
interface MapBundleRepository {
    fun observeDownloads(): kotlinx.coroutines.flow.Flow<List<MapBundleDownload>>
    suspend fun queueDownload(mapId: String, version: String): Result<Unit>
    suspend fun pauseDownload(mapId: String): Result<Unit>
    suspend fun resumeDownload(mapId: String): Result<Unit>
    suspend fun removeBundle(mapId: String): Result<Unit>
    suspend fun validateBundle(mapId: String, version: String): Result<Boolean>
}

data class MapBundleDownload(
    val mapId: String,
    val version: String,
    val progressPercent: Int,
    val state: DownloadState,
    val bytesDownloaded: Long,
    val bytesTotal: Long
)

enum class DownloadState { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }
```

```kotlin
// data/local/room/MapBundleEntity.kt
@androidx.room.Entity(tableName = "map_bundles")
data class MapBundleEntity(
    @androidx.room.PrimaryKey val mapId: String,
    val version: String,
    val localPath: String,
    val checksum: String,
    val sizeBytes: Long,
    val updatedAtEpochMs: Long
)
```

```kotlin
// data/download/BundleDownloadWorker.kt
class BundleDownloadWorker @javax.inject.Inject constructor(
    private val repository: MapBundleRepository
) {
    suspend fun start(mapId: String, version: String): Result<Unit> {
        return repository.queueDownload(mapId, version)
    }
}
```

## 6) Navigation + AR Flow

```kotlin
// domain/usecase/StartArGuidanceUseCase.kt
class StartArGuidanceUseCase @javax.inject.Inject constructor(
    private val localizationRepository: LocalizationRepository,
    private val featureFlags: FeatureFlags
) {
    suspend operator fun invoke(): Result<Unit> {
        val enabled = featureFlags.arNavigationEnabled.first()
        if (!enabled) return Result.Failure(AppError.Localization("AR disabled by feature flag"))
        return localizationRepository.initialize()
    }
}
```

```kotlin
// presentation/ar/ArNavigationViewModel.kt
@dagger.hilt.android.lifecycle.HiltViewModel
class ArNavigationViewModel @javax.inject.Inject constructor(
    private val startArGuidanceUseCase: StartArGuidanceUseCase
) : androidx.lifecycle.ViewModel() {

    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(ArUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<ArUiState> = _uiState

    fun start() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = when (val result = startArGuidanceUseCase()) {
                is Result.Success -> _uiState.value.copy(isLoading = false, isTracking = true)
                is Result.Failure -> _uiState.value.copy(isLoading = false, error = result.error.toString())
            }
        }
    }
}

data class ArUiState(
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val error: String? = null
)
```

## 7) Compose Shell (Material 3)

```kotlin
// presentation/nav/AppNavGraph.kt
@Composable
fun AppNavGraph() {
    val navController = androidx.navigation.compose.rememberNavController()
    androidx.navigation.compose.NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(onOpenMap = { navController.navigate("map") }) }
        composable("map") { IndoorMapScreen(onOpenAr = { navController.navigate("ar") }) }
        composable("ar") { ArNavigationScreen() }
        composable("settings") { SettingsScreen() }
    }
}
```

```kotlin
// presentation/maps/IndoorMapScreen.kt
@Composable
fun IndoorMapScreen(onOpenAr: () -> Unit) {
    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(onClick = onOpenAr) {
                androidx.compose.material3.Text("AR")
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("Indoor map viewport")
        }
    }
}
```

## 8) Security + Enterprise Constraints

- Store tokens in encrypted preferences (`EncryptedSharedPreferences`) behind `SecureTokenStore`.
- Use SSL pinning through `OkHttp CertificatePinner`.
- Keep SDK/API credentials out of source code and inject at runtime.
- Persist offline license + bundle metadata locally with integrity checks (checksum/version).
- Add R8/Proguard rules for SDK + reflection-heavy AR components.

## 9) Implementation Order

1. Bootstrap app shell + Hilt + Compose navigation.
2. Add domain contracts (`LocalizationRepository`, `MapBundleRepository`, use cases).
3. Implement MultiSet adapter layer and repository wrappers.
4. Add Room + DataStore for map bundle metadata, settings, and feature flags.
5. Add download manager + WorkManager background bundle orchestration.
6. Add map rendering integration (MapLibre first, optional Filament 3D extension).
7. Add ARCore guidance and stabilization loop.
8. Add optimization/security hardening (throttling, lifecycle camera handling, SSL pinning, root hooks).

## 10) Notes for MultiSet Roadmap Alignment

To support current cloud VPS and evolving on-device localization support, keep a swappable provider strategy:

- `CloudLocalizationProvider`
- `HybridLocalizationProvider`
- `OnDeviceLocalizationProvider`

Select provider at runtime using feature flags and capability checks, without changing UI or domain layers.
