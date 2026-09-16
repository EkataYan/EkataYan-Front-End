import java.util.Properties
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localConfiguration = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val productionBackendUrl = "https://ekatayan-back-end-production.up.railway.app/"
val backendUrl = providers.gradleProperty("BACKEND_BASE_URL")
    .orElse(provider { localConfiguration.getProperty("BACKEND_BASE_URL", productionBackendUrl) })
    .map { it.ifBlank { productionBackendUrl } }
val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
    .orElse(provider { localConfiguration.getProperty("SUPABASE_URL", "") })
val supabasePublishableKey = providers.gradleProperty("SUPABASE_PUBLISHABLE_KEY")
    .orElse(provider { localConfiguration.getProperty("SUPABASE_PUBLISHABLE_KEY", "") })
val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID")
    .orElse(provider { localConfiguration.getProperty("GOOGLE_WEB_CLIENT_ID", "") })
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val clientConfigurationErrors = buildList {
    val url = supabaseUrl.get().trim()
    if (url.isBlank()) add("SUPABASE_URL is missing")
    else if (runCatching { URI(url) }.getOrNull()?.let { it.scheme != "https" || it.host.isNullOrBlank() } != false) {
        add("SUPABASE_URL must be a valid HTTPS URL")
    }
    val key = supabasePublishableKey.get().trim()
    if (key.isBlank()) add("SUPABASE_PUBLISHABLE_KEY is missing")
    else if (!key.startsWith("sb_publishable_") && !key.startsWith("eyJ")) {
        add("SUPABASE_PUBLISHABLE_KEY is not a publishable/legacy anon key")
    }
    val clientId = googleWebClientId.get().trim()
    if (clientId.isBlank()) add("GOOGLE_WEB_CLIENT_ID is missing")
    else if (!clientId.endsWith(".apps.googleusercontent.com")) add("GOOGLE_WEB_CLIENT_ID is invalid")
}
check(clientConfigurationErrors.isEmpty()) {
    clientConfigurationErrors.joinToString(
        prefix = "Client authentication configuration is incomplete:\n- ",
        separator = "\n- ",
        postfix = "\nCopy local.properties.example to local.properties and provide the missing values.",
    )
}

android {
    namespace = "com.ekatayan.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ekatayan.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BACKEND_BASE_URL", quoted(backendUrl.get()))
        buildConfigField("String", "SUPABASE_URL", quoted(supabaseUrl.get()))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", quoted(supabasePublishableKey.get()))
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quoted(googleWebClientId.get()))
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    sourceSets["main"].assets.srcDir("../docs")
}

ksp {
    arg("room.schemaLocation", file("schemas").path)
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.okhttp.mockwebserver)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
//    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.play.services.location)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
