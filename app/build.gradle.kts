plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.codedroid.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codedroid.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // Редактор кода с подсветкой
    implementation("io.github.rosemoe.sora-editor:editor:0.23.4")
    // VSIX-архивы — это zip
    implementation("org.apache.commons:commons-compress:1.26.2")
    // JS-движок для запуска кода
    implementation("app.cash.quickjs:quickjs-android:0.9.2")
    // HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
