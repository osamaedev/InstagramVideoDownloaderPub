plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    alias(libs.plugins.compose.compiler)
//    id("com.google.devtools.ksp")
}

android {
    namespace = "com.instagram.video.downloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.video.instagram.story.reels.downloader.ig"
        minSdk = 21
        targetSdk = 35
        versionCode = 15
        versionName = "1.0.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        maybeCreate("debug").apply {
            storeFile = file("C:\\Path\\To\\.android\\debug.keystore")
        }

        maybeCreate("release").apply {
            storeFile =
                file("C:\\Path\\To\\insta_saver_keystore.jks")
            keyAlias = "key0"
            storePassword = "-----"
            keyPassword = "-----"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
        compose = true
    }

    externalNativeBuild {
        cmake {
            version = "3.31.0"
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
    ndkVersion = "27.0.12077973"

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.14"
//    }

    kotlin {
        jvmToolchain(17)
    }

    kapt {
        correctErrorTypes = true

        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

//    sourceSets {
//        named("main") {
//            jniLibs.srcDirs(listOf("src/main/jni"))
//        }
//    }
//
//    splits {
//        abi {
//            isEnable = true
//            reset()
//            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
//            isUniversalApk = true
//        }
//    }
//
//    packaging {
//        jniLibs {
//            useLegacyPackaging = true
//        }
//    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.ads.lite)
//    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlin.stdlib)

    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.collection.ktx)


    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.reactivestreams.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.security.crypto)

    implementation(libs.play.services.auth)
    implementation(libs.review)
    implementation(libs.review.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.work)
    kapt(libs.hilt.android.compiler)
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)


    implementation(libs.glide)
    kapt(libs.compiler)


    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging.ktx)


    implementation(libs.retrofit)
//    implementation(libs.gson)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.conscrypt.android)
    implementation(libs.logging.interceptor)


    implementation(libs.androidx.preference.ktx)


    implementation(libs.coil)

    implementation(libs.guava)


    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.billing)

    implementation(libs.play.services.ads)

    implementation(libs.xercesimpl)


    implementation(libs.timber)

    implementation(files("libs/anim.jar"))

    implementation(libs.lottie)

    implementation(libs.core)
    implementation(libs.ext.strikethrough)
    implementation(libs.image)
    implementation(libs.recycler)
    implementation(libs.linkify)


    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.accompanist.themeadapter.material3)

    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.android.database.sqlcipher)
    implementation(libs.androidx.sqlite)

    implementation(libs.androidx.viewpager2)

    implementation(libs.androidx.paging.runtime.ktx)


    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.audience.network.sdk)

//    implementation(libs.applovin.sdk)

    implementation(libs.androidx.lifecycle.process)


    implementation(libs.compose)

//    implementation(libs.flow.preferences)

//    implementation(libs.ui.tiles)
//    implementation(libs.ui.tiles.extended)

    implementation(libs.circularprogressbar)

//    implementation(libs.play.services.ads.identifier)

//    implementation("androidx.ads:ads-identifier:1.0.0-alpha05")


    implementation(libs.androidx.recyclerview.selection)
    implementation(libs.androidx.recyclerview)

//    implementation(project(":media-player"))
    implementation(project(":nativetemplates"))
//    implementation(project(":ijkplayer-java"))

    implementation(libs.photoview)

    implementation(libs.readmore.textview)

    implementation(libs.libvlc.all)

    implementation(libs.konfetti.xml)

}