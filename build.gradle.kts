val kotlinVersion: String by project

plugins {
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    kotlin("jvm") version("1.3.70")
}

group = "pl.pjagielski"
version = "0.1.0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.3")
    api("org.jetbrains.kotlin", "kotlin-scripting-common", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-scripting-jvm-host-embeddable", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-script-runtime", kotlinVersion)
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", kotlinVersion)
    api("io.github.microutils", "kotlin-logging", "1.7.9")
    api("com.uchuhimo", "konf", "0.22.1")
    api("com.illposed.osc", "javaosc-core", "0.6") {
        exclude("org.slf4j", "slf4j-log4j12")
    }
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.6.0")
    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.21")

    runtimeOnly("org.slf4j","slf4j-simple","1.7.29")
}

val sourcesJar by tasks.creating(Jar::class) {
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

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

bintray {
    user = project.findProperty("bintray.user") as String? ?: System.getenv("USERNAME")
    key = project.findProperty("bintray.key") as String? ?: System.getenv("TOKEN")
    publish = true
    setPublications("lib")
    pkg.apply {
        repo = "punkt"
        name = "punkt"
        userOrg = "punkt"
        githubRepo = "pjagielski/punkt"
        vcsUrl = "https://github.com/pjagielski/punkt"
        description = "Live music coding library/environment for Kotlin"
        setLabels("kotlin")
        setLicenses("Apache-2.0")
        desc = description
    }
}
