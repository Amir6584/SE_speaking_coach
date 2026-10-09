plugins { id("com.android.application") }

android {
    namespace = "se.example.swedishcoach"
    compileSdk = 35

    defaultConfig {
        applicationId = "se.example.swedishcoach"
        minSdk = 24
        targetSdk = 35
        versionCode = 31
        versionName = "3.1.0-qwen3-workflow-fix"
        ndk { abiFilters += listOf("arm64-v8a") }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    androidResources {
        noCompress += "bin"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:language-id:17.0.6")
    implementation("dev.ffmpegkit-maintained:llama-android:0.1.1")
}

val prepareGrammarModel = tasks.register<Exec>("prepareGrammarModel") {
    workingDir(rootProject.projectDir)
    commandLine("bash", "scripts/prepare_qwen_model.sh")
}

tasks.named("preBuild").configure {
    dependsOn(prepareGrammarModel)
}
