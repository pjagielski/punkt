val kotlinVersion: String by project

plugins {
    java
    maven
    publishing
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.0"
    kotlin("jvm") version("1.3.70")
}

group = "pl.pjagielski"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
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
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.0")
    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.21")

    runtimeOnly("org.slf4j","slf4j-simple","1.7.29")
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
