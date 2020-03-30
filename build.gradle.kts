val kotlinVersion: String by project

plugins {
    java
<<<<<<< HEAD
<<<<<<< HEAD
    maven
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.0"
=======
>>>>>>> Initial working with Synth and Sample
=======
    maven
<<<<<<< HEAD
    publishing
>>>>>>> Split to packages; Added Config and Application
=======
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.0"
>>>>>>> Add LFO; Test application
    kotlin("jvm") version("1.3.70")
}

group = "pl.pjagielski"
version = "1.0.0-SNAPSHOT"

repositories {
<<<<<<< HEAD
<<<<<<< HEAD
    mavenLocal()
=======
>>>>>>> Initial working with Synth and Sample
=======
    mavenLocal()
>>>>>>> Split to packages; Added Config and Application
    mavenCentral()
}

dependencies {
<<<<<<< HEAD
<<<<<<< HEAD
    compile("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    api("org.jetbrains.kotlin", "kotlin-scripting-common", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm-host-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-script-runtime", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    implementation("io.github.microutils", "kotlin-logging", "1.7.9")
    compile("com.uchuhimo", "konf", "0.22.1")
    implementation("com.illposed.osc", "javaosc-core", "0.6") {
        exclude("org.slf4j", "slf4j-log4j12")
    }
=======
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    implementation("org.jetbrains.kotlin", "kotlin-scripting-common", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-scripting-jvm", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-scripting-jvm-host-embeddable", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-script-runtime", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    implementation("io.github.microutils", "kotlin-logging", "1.7.8")
    implementation("com.illposed.osc", "javaosc-core", "0.6")
>>>>>>> Initial working with Synth and Sample
=======
    compile("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    api("org.jetbrains.kotlin", "kotlin-scripting-common", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm-host-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-script-runtime", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    implementation("io.github.microutils", "kotlin-logging", "1.7.9")
    compile("com.uchuhimo", "konf", "0.22.1")
    implementation("com.illposed.osc", "javaosc-core", "0.6") {
        exclude("org.slf4j", "slf4j-log4j12")
    }
>>>>>>> Split to packages; Added Config and Application

    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.0")
    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.21")

    runtimeOnly("org.slf4j","slf4j-simple","1.7.29")
<<<<<<< HEAD
<<<<<<< HEAD
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pjagielski/punkt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    publications {
        register("gpr", MavenPublication::class) {
            from(components["java"])
        }
    }
=======
    runtimeOnly("org.apache.logging.log4j", "log4j-slf4j-impl", "2.13.0")
>>>>>>> Initial working with Synth and Sample
=======
>>>>>>> Split to packages; Added Config and Application
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
