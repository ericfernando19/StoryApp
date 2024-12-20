plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-parcelize")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.dicoding.storyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dicoding.storyapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    viewBinding {
        enable = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    implementation (libs.androidx.paging.common)


    testImplementation (libs.androidx.core.testing)
    testImplementation (libs.mockito.core)
    testImplementation (libs.mockito.inline.v520)

    testImplementation (libs.kotlinx.coroutines.test)

    implementation (libs.androidx.paging.runtime.ktx)
    implementation (libs.androidx.paging.runtime)

    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation (libs.play.services.basement)
    implementation (libs.androidx.core)

    implementation(libs.androidx.room.runtime)
    implementation (libs.okhttp)

    implementation(libs.logging.interceptor)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    implementation (libs.androidx.cardview)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    implementation (libs.androidx.datastore.preferences)

    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

    implementation (libs.androidx.core.ktx.v1150)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}