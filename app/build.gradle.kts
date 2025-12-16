plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.pillminder"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.pillminder"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // =====================================
    // DEPENDENCIAS DE FIREBASE Y ANDROIDX
    // =====================================

    // 1. Dependencia de la Plataforma (BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // 2. Dependencia para Firebase AUTHENTICATION (Soluciona los errores)
    implementation("com.google.firebase:firebase-auth")

    // 3. Dependencia para Firestore (Ya deberías tenerla de antes)
    implementation("com.google.firebase:firebase-firestore")

    // 4. Dependencias de AndroidX para LiveData y ViewModel (Importante para MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
}