import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "com.valsagnapps.dndapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.valsagnapps.dndapp"
        minSdk = 36
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            // Desde el celular: el server de dev expuesto con Tailscale Serve, en local.properties.
            // Si no está definido, 10.0.2.2 (el localhost de la PC visto desde el emulador).
            val devBaseUrl = localProperties.getProperty("dndapp.devBaseUrl")
                ?: "http://10.0.2.2:8081/"
            buildConfigField("String", "BASE_URL", "\"$devBaseUrl\"")
        }
        release {
            // Host de Tailscale Serve, definido en local.properties (no se commitea).
            val prodBaseUrl = localProperties.getProperty("dndapp.prodBaseUrl")
                ?: "https://missing-prod-base-url.invalid/"
            buildConfigField("String", "BASE_URL", "\"$prodBaseUrl\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    testOptions {
        // Robolectric: los tests de Compose leen strings.xml y demás recursos.
        unitTests.isIncludeAndroidResources = true
        // Robolectric accede a internals del JDK que los JDK nuevos (21+) bloquean por defecto.
        unitTests.all {
            it.jvmArgs(
                "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                "--enable-native-access=ALL-UNNAMED",
            )
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}

// detekt necesita correr con la versión de Kotlin con la que fue compilado, no la del proyecto.
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(dev.detekt.gradle.plugin.getSupportedKotlinVersion())
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    detektPlugins(libs.compose.rules.detekt)
    ktlintRuleset(libs.compose.rules.ktlint)
}
