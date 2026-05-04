/*
 * core-ml — inference abstraction. Phase 1 ships llama.rn JNI per ADR-0003.
 *
 * NDK is set up here even though the native sources are not in this commit; the .so
 * libraries land in a follow-on commit that wires up the extracted llama.rn JNI.
 * The externalNativeBuild block is intentionally not yet present — we will add it
 * with a CMakeLists.txt when the native port lands. NDK ABI filters scope us to
 * arm64 only (modern Android only; ADR-0007 minSdk 26).
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.digitalgnosis.atalaya.ml"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = libs.versions.ndk.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        ndk {
            // Modern phones only — arm64-v8a covers Pixel/Samsung/OnePlus from ~2017+.
            abiFilters += listOf("arm64-v8a")
        }

        // TODO(phase-1-step-3): wire externalNativeBuild { cmake { ... } } when the
        // extracted llama.rn JNI sources land in src/main/cpp/.
    }

    // jniLibs/ (prebuilt .so) will be picked up automatically once dropped in.
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(libs.versions.jvmTarget.get().toInt())
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
