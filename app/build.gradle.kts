plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tam.scottishfootballpredictor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tam.scottishfootballpredictor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_11  // Updated to Java 11
        targetCompatibility = JavaVersion.VERSION_11  // Updated to Java 11
    }

    kotlinOptions {
        jvmTarget = "11"  // Updated to Java 11
    }
}

tasks.register<JavaExec>("runScraper") {
    dependsOn("compileDebugKotlin")
    group = "scraping"
    description = "Runs the table scraper"

    classpath = files(
        android.sourceSets.getByName("main").java.srcDirs,
        project.buildDir.resolve("intermediates/javac/debug/classes"),
        project.buildDir.resolve("tmp/kotlin-classes/debug")
    ) + configurations.getByName("debugRuntimeClasspath")

    mainClass.set("com.tam.scottishfootballpredictor.update.StatsScraper")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Web scraping
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Selenium
    implementation("org.seleniumhq.selenium:selenium-java:4.16.1")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.16.1")
    implementation("org.seleniumhq.selenium:selenium-api:4.16.1")

    // HTTP & JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}