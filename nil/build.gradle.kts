plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.sandesh.nil"

    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

}

dependencies {
    val roomVersion = "2.7.1"

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1"
    )

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )

    implementation(
        "androidx.room:room-runtime:$roomVersion"
    )

    implementation(
        "androidx.room:room-ktx:$roomVersion"
    )

    ksp(
        "androidx.room:room-compiler:$roomVersion"
    )

    implementation(
        "androidx.compose.ui:ui:1.7.0"
    )

    implementation(
        "androidx.compose.material3:material3:1.3.0"
    )

    implementation(
        "androidx.compose.material:material-icons-extended:1.7.0"
    )

    implementation(
        "androidx.activity:activity-compose:1.9.2"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.8.6"
    )

    testImplementation(
        "junit:junit:4.13.2"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.3.0"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.7.0"
    )
}
