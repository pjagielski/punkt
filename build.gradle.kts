import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val kotlinVersion: String by project

plugins {
    `maven-publish`
    kotlin("jvm") version("2.3.0")
}

group = "pl.pjagielski"
version = "0.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2")
    implementation("org.http4k", "http4k-core", "3.285.0")
    implementation("org.http4k", "http4k-server-jetty", "3.285.0") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("org.http4k", "http4k-format-jackson", "3.285.0")
    api("org.jetbrains.kotlin", "kotlin-scripting-common", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm-host", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-script-runtime", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    api("io.github.oshai", "kotlin-logging", "7.0.13")
    api("com.uchuhimo", "konf", "1.1.2")
    api("com.illposed.osc", "javaosc-core", "0.8") {
        exclude("org.slf4j", "slf4j-log4j12")
    }
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.10.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.28.1")
    testImplementation("org.slf4j","slf4j-simple","1.7.29")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create("lib", MavenPublication::class) {
            groupId = "pl.pjagielski"
            artifactId = "punkt"
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}


