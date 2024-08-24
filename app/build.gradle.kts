plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {

    namespace = libs.versions.applicationName.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {

        applicationId = libs.versions.applicationName.get()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.applicationVersion.get().toInt()
        versionName = libs.versions.applicationVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    // LIBRARY DEPENDENCIES
    implementation(libs.coil.compose)
    implementation(libs.bundles.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.gson)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.retrofit)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.bundles.compose.debug)

    // UNIT TEST DEPENDENCIES
    testImplementation(libs.bundles.unit.testing)
    testImplementation(libs.koin.test)

    // INSTRUMENTATION TEST DEPENDENCIES
    androidTestImplementation(libs.bundles.instrumentation.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.junit)

    implementation("com.squareup.okhttp3:logging-interceptor:4.7.2")
    debugImplementation(libs.ui.test.manifest)
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")


}