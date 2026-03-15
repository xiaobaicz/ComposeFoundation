plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.xiaobaicz.compose.foundation.tv"
    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)

    api(project(":foundation"))
}