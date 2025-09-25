buildscript {

    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"

    alias(libs.plugins.compose.compiler) apply false

    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"


    //id("com.android.library") version "7.1.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false

    id("com.android.application") version "8.1.4" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

    id("com.google.dagger.hilt.android") version "2.51" apply false
    alias(libs.plugins.android.library) apply false

    id("com.google.firebase.crashlytics") version "3.0.2" apply false
//    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
}
