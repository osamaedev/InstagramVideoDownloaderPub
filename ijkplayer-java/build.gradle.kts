plugins {
    id("com.android.library")
}

android {

    namespace = "tv.danmaku.ijk.media"

    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }

//    lintOptions {
//        abortOnError = false
//    }
}

//android {
//
//    lintOptions {
//        abortOnError false
//    }
//    defaultConfig {
//        minSdkVersion 21
//        targetSdkVersion 34
//    }
//    buildTypes {
//        release {
//            minifyEnabled false
//            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
//        }
//    }
//}

//dependencies {
//    implementation fileTree(dir: 'libs', include: ['*.jar'])
//}
