plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.eenact.dailymetrics"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // Must stay unchanged so existing users upgrade in place and keep their data.
        applicationId = "com.eenact.dailymetrics"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
        base.archivesName.set("DailyMetrics-$versionCode")
    }

    buildTypes {
        debug {
            // Override with -Pdm.debugAppIdSuffix=.upgradetest to test upgrades without touching an installed debug app.
            applicationIdSuffix = providers.gradleProperty("dm.debugAppIdSuffix").getOrElse(".debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
}
