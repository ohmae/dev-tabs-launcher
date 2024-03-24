import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.gradleVersions)
}

val applicationName = "dev-tabs-launcher"
val versionMajor = 0
val versionMinor = 0
val versionPatch = 2

android {
    compileSdk = 34

    namespace = "net.mm2d.dev.tabs.launcher"
    defaultConfig {
        applicationId = "net.mm2d.dev.tabs.launcher"
        minSdk = 26
        targetSdk = 34
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
    }
    buildTypes {
        debug {
            isDebuggable = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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
        viewBinding = true
    }
    lint {
        abortOnError = true
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxActivity)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxConstraintLayout)
    implementation(libs.androidxRecyclerView)
    implementation(libs.material)
    implementation(libs.colorChooser)

    debugImplementation(libs.leakcanary)
    releaseImplementation(libs.bundles.flipper)
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.uppercase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
