plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.nitsuya.aa.lib_stub"
    compileSdk = 33

    defaultConfig {
        minSdk = 31
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    annotationProcessor("dev.rikka.tools.refine:annotation-processor:4.4.0")
    compileOnly("dev.rikka.tools.refine:annotation:4.4.0")
    compileOnly("dev.rikka.hidden:stub:4.3.2")
    compileOnly("androidx.annotation:annotation:1.6.0")
}