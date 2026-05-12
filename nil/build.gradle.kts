plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.sandesh.nil"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

mavenPublishing {
    coordinates(
        groupId = "io.github.sandeshyele2000",
        artifactId = "nil",
        version = "1.0.2"
    )

    pom {
        name.set("Nil")
        description.set("A composable Android library available via Maven Central.")
        inceptionYear.set("2025")
        url.set("https://github.com/sandeshyele2000/nil-android")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("sandeshyele2000")
                name.set("Sandesh Yele")
                url.set("https://github.com/sandeshyele2000")
            }
        }

        scm {
            url.set("https://github.com/sandeshyele2000/nil-android")
            connection.set("scm:git:git://github.com/sandeshyele2000/nil-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/sandeshyele2000/nil-android.git")
        }
    }
}
