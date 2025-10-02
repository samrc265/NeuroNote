import org.gradle.kotlin.dsl.implementation
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Add the Kotlin Serialization plugin here
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    // 🚀 PROTOBUF PLUGIN (REQUIRED FOR DATASTORE)
    id("com.google.protobuf") version "0.9.4"
}

// 🚀 PROTOBUF CONFIGURATION
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { // SWITCHED TO JAVA GENERATION
                    option("lite")
                }
                // REMOVED Kotlin generation task to fix "Unresolved reference 'kotlin'" errors
            }
        }
    }
}

android {
    namespace = "com.example.neuronote"
    compileSdk = 36
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"${property("GEMINI_API_KEY")}\"")
        applicationId = "com.example.neuronote"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // FIX: Source set path changed to point to Java generated code
    sourceSets.getByName("main").java.srcDirs("build/generated/source/proto/main/java")
}

dependencies {
    implementation ("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation (libs.androidx.core.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.activity.compose)
    implementation (platform(libs.androidx.compose.bom))
    implementation (libs.androidx.ui)
    implementation (libs.androidx.ui.graphics)
    implementation (libs.androidx.ui.tooling.preview)
    implementation (libs.androidx.material3)
    implementation (libs.androidx.navigation.runtime.android)
    implementation (libs.androidx.navigation.compose.android)
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.compose.material3:material3:1.2.1")
    // ✅ MPAndroidChart from JitPack
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // ✅ Added for more icons like dark/light mode toggles
    implementation ("androidx.compose.material:material-icons-extended-android:1.6.7")
    implementation (libs.generativeai)
    testImplementation (libs.junit)
    androidTestImplementation (libs.androidx.junit)
    androidTestImplementation (libs.androidx.espresso.core)
    androidTestImplementation (platform(libs.androidx.compose.bom))
    androidTestImplementation (libs.androidx.ui.test.junit4)
    debugImplementation (libs.androidx.ui.tooling)
    debugImplementation (libs.androidx.ui.test.manifest)
    // ✅ Java 8+ API desugaring
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")
    // Add the Kotlinx Serialization runtime library here
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 🚀 PROTO DATASTORE
    val datastore_version = "1.0.0"
    implementation("androidx.datastore:datastore:$datastore_version")
    // FIX: Switched to the full Protobuf Java library
    implementation("com.google.protobuf:protobuf-java:3.25.1")
    // FIX: Added Kotlin extensions for easier interoperability
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1")
    implementation("androidx.datastore:datastore-core:$datastore_version")

    // 🚀 Coroutines for data ops
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}