plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //kotlin("android")
    id("kotlin-android")
    id("dev.rikka.tools.refine") version "4.4.0"
}

android {
    val buildTime = System.currentTimeMillis()
    compileSdk = 33

    defaultConfig {
        applicationId = "io.github.nitsuya.aa.display"
        minSdk = 31
        targetSdk = 33
        versionCode = 1600
        versionName = "0.16#12.8+"
        buildConfigField("long", "BUILD_TIME", buildTime.toString())
    }

    packagingOptions {
        resources.excludes.addAll(
            arrayOf(
                "META-INF/**",
                "kotlin/**"
            )
        )
    }
    signingConfigs {
        create("release") {
            storeFile = file("../key.jks")
            storePassword = System.getenv("KEY_ANDROID")
            keyAlias = "key0"
            keyPassword = System.getenv("KEY_ANDROID")
            enableV1Signing = false
            enableV2Signing = false
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            sourceSets.getByName("main").java.srcDir(File("build/generated/ksp/release/kotlin"))
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "2.0"
    }
    buildFeatures {
        viewBinding = true
        aidl = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    androidResources.additionalParameters("--allow-reserved-package-id", "--package-id", "0x64")

    namespace = "io.github.nitsuya.aa.display"
}

configurations.all {
    exclude("androidx.appcompat", "appcompat")
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.activity:activity-ktx:1.7.1")
    implementation("androidx.fragment:fragment-ktx:1.5.7")
//    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("dev.rikka.rikkax.appcompat:appcompat:1.6.1")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")

    //kotlinx-coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    //ViewBindingUtil
    implementation("com.github.matsudamper:ViewBindingUtil:0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")

    compileOnly(project(":lib-stub"))
    implementation("dev.rikka.tools.refine:runtime:4.4.0")
    implementation("dev.rikka.hidden:compat:4.3.2")
    compileOnly("dev.rikka.hidden:stub:4.3.2")
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.github.kyuubiran:EzXHelper:1.0.3")
    implementation("com.github.topjohnwu.libsu:core:5.2.0")
    implementation("org.luckypray:dexkit:2.0.0-rc3")
//    implementation("com.github.martoreto:aauto-sdk:v4.7")
    implementation(files("./libs/aauto.aar"))

    //lifecycle
    val lifecycleVersion = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

}