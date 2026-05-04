/*
 * core-observability — facade over cipherware-sdk-android.
 *
 * The cipherware-sdk-android dependency is intentionally NOT declared here yet.
 * That artifact lives in a separate DG-owned repo and will be wired up in a
 * follow-on commit (Phase 1 Step 3 vertical slice). For now this module is an
 * empty Android library so the Gradle graph compiles.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.digitalgnosis.atalaya.observability"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    // TODO(phase-1-step-3): add cipherware-sdk-android once published.

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
