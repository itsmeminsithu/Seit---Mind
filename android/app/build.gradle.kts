plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
val mindApiUrl = providers.gradleProperty("mindApiBaseUrl").orElse("http://10.0.2.2:8000").get()
android { namespace = "org.minsithu.stillmind"; compileSdk = 35
    defaultConfig { applicationId = "org.minsithu.stillmind"; minSdk = 26; targetSdk = 35; versionCode = 8; versionName = "0.8.0"; buildConfigField("String", "API_BASE_URL", "\"$mindApiUrl\"") }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.core:core-ktx:1.15.0")
}
