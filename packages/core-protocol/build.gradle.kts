/*
 * core-protocol — wire types shared across Atalaya apps.
 *
 * Trade-off: README envisions Kotlin Multiplatform eventually. For Phase 1 we ship
 * as an Android library because every consumer is Android (apps/node, future
 * apps/control) and KMP setup is more friction than payoff right now. When apps/hub
 * (Phase 2, JVM) needs these types we either (a) flip this to a kotlin-jvm module or
 * (b) introduce KMP and split commonMain/jvmMain/androidMain. Decision deferred to
 * Phase 2.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.digitalgnosis.atalaya.protocol"
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
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
