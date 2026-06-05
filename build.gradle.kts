plugins {
    kotlin("jvm") version "2.4.0-RC"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:10.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")
    implementation("com.charleskorn.kaml:kaml:0.104.0")
}

kotlin {
    jvmToolchain(21)
}
