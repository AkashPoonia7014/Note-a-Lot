plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.sky.note_a_lot"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sky.note_a_lot"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.firebase:firebase-firestore:25.1.4")
    implementation("com.google.firebase:firebase-storage:22.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Room (for database)
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")
    implementation ("commons-codec:commons-codec:1.15")

    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // scalable Size Unit (support for different screen sizes)
    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    implementation ("com.intuit.ssp:ssp-android:1.1.0")

    // Rounded Imageview
    implementation ("com.makeramen:roundedimageview:2.3.0")

    // Progress Bar
    implementation ("com.github.yatindeokar:MyLoadingButton:v1.0.1")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //KeyboardVisibilityEvent Library
    implementation ("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:3.0.0-RC3")
    
    //IntentAnimation Library
    implementation ("com.github.hajiyevelnur92:intentanimation:1.0")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    //  UIUImagePicker
    implementation ("com.github.dhaval2404:imagepicker:2.1")

    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Supabase

    implementation("io.github.jan-tennert.supabase:supabase-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.4")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Ktor client for Android(Supabase)
    implementation("io.ktor:ktor-client-android:2.3.12")

}
