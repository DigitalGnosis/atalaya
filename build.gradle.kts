/*
 * Atalaya — root build script.
 *
 * All plugins declared here with `apply false` so each subproject's classpath
 * resolves to the same plugin versions. Subprojects opt in via `alias(libs.plugins.X)`.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
