import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    application
}

var ktxVersion = "1.10.0-b2"
var gdxVersion = "1.10.0"
var gdxControllersVersion = "2.2.1"
var korioVersion = "2.4.1"

group = "sh.nes"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven { setUrl("https://oss.sonatype.org/content/repositories/releases/") }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("com.charleskorn.kaml:kaml:0.35.3")
    implementation("org.reflections:reflections:0.9.12")
    implementation("io.github.libktx:ktx-app:$ktxVersion")
    implementation("io.github.libktx:ktx-json:$ktxVersion")
    implementation("io.github.libktx:ktx-assets-async:$ktxVersion")
    implementation("io.github.libktx:ktx-freetype-async:$ktxVersion")
    implementation("io.github.libktx:ktx-actors:$ktxVersion")
    implementation("io.github.libktx:ktx-async:$ktxVersion")
    implementation("io.github.libktx:ktx-scene2d:$ktxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-tools:$gdxVersion")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-core:$gdxControllersVersion")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-desktop:$gdxControllersVersion")
    implementation("com.soywiz.korlibs.korio:korio-jvm:$korioVersion")
    implementation("com.soywiz.korlibs.korio:korio:$korioVersion")
    implementation("com.google.jimfs:jimfs:1.2")
    implementation("org.reflections:reflections:0.9.11")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
}

application {
    mainClass.set("sh.nes.combattle.MainKt")
}