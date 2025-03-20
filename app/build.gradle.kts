plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sky.note_a_lot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sky.note_a_lot"
        minSdk = 23
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.1.2")
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

    implementation ("com.squareup.okhttp3:okhttp:4.5.0")

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

}
