buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.20-1.0.32")
    }
}
