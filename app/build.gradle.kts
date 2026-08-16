plugins {
    id("com.android.application") version "8.6.1"
    id("org.jetbrains.kotlin.android") version "2.0.21"
}

android {
    namespace = "com.mohsen.voicecallalert"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mohsen.voicecallalert"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }
}

kotlinOptions { jvmTarget = "17" }

buildTypes {
    release { isMinifyEnabled = false }
}
