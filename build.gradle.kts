import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("com.adarshr.test-logger") version "4.0.0"
    `java-library`
    `maven-publish`
}

group = "be.vamaralds"
version = "0.0.1-SNAPSHOT"

fun envConfig() = object : ReadOnlyProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
        if (ext.has(property.name)) {
            ext[property.name] as? String
        } else {
            System.getenv(property.name)
        }
}

val repositoryUser by envConfig()
val repositoryPassword by envConfig()
val signingKeyId by envConfig()
val signingSecretKey by envConfig()
val signingPassword by envConfig()
val signingSecretKeyRingFile by envConfig()

repositories {
    mavenCentral()
}

dependencies {
    // Functional Programming
    api("io.arrow-kt:arrow-core:1.2.4")
    api("io.arrow-kt:arrow-fx-coroutines:1.2.4")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()

    testlogger {
        setTheme("standard")
        showExceptions = true
        showStackTraces = true
        showFullStackTraces = false
        showCauses = true
        slowThreshold = 2000
        showSummary = true
        showSimpleNames = false
        showPassed = true
        showSkipped = true
        showFailed = true
        showOnlySlow = false
        showStandardStreams = false
        showPassedStandardStreams = true
        showSkippedStandardStreams = true
        showFailedStandardStreams = true
        setLogLevel("lifecycle")
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.dokkaHtml {
    outputDirectory.set(rootDir.resolve("docs/"))
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

koverReport {
    filters {
        excludes {
            //classes("")
        }
    }

    verify {
        rule {
            isEnabled = true
        }
    }
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("ffsmkt") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJar"])

            pom {
                name.set("ffsmkt")
                description.set("Functional Finite State Machine Library for the JVM")
                url.set("https://github.com/AmaVic/ffsmkt")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("fanceCode")
                        name.set("John Doe")
                        email.set("info@example.com")
                    }
                }
                scm {
                    connection.set("https://github.com/fancycode/fancy-library.git")
                    developerConnection.set("https://github.com/fancycode/fancy-library.git")
                    url.set("https://github.com/fancycode/fancy-library")
                }
            }
        }
    }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("localrepo"))
        }

        maven {
            name = "MavenCentral"
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = repositoryUser
                password = repositoryPassword
            }
        }
    }
}