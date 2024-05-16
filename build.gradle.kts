import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("com.adarshr.test-logger") version "4.0.0"
    id("net.researchgate.release") version "2.8.1"
    `java-library`
    `maven-publish`
    signing
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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
                description.set("Functional finite state machine library for the JVM")
                url.set("https://github.com/AmaVic/ffsmkt")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("AmaVic")
                        name.set("Victor Amaral de Sousa")
                    }
                }
                scm {
                    connection.set("https://github.com/AmaVic/ffsmkt/ffsmkt.git")
                    developerConnection.set("https://github.com/AmaVic/ffsmkt/ffsmkt.git")
                    url.set("https://github.com/AmaVic/ffsmkt")
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

signing {
    if (!signingKeyId.isNullOrEmpty()) {
        project.ext["signing.keyId"] = signingKeyId
        project.ext["signing.password"] = signingPassword
        project.ext["signing.secretKeyRingFile"] = signingSecretKeyRingFile

        logger.info("Signing key id provided. Sign artifacts for $project.")

        isRequired = true
    } else if (!signingSecretKey.isNullOrEmpty()) {
        useInMemoryPgpKeys(signingSecretKey, signingPassword)
    } else {
        logger.warn("${project.name}: Signing key not provided. Disable signing for $project.")
        isRequired = false
    }

    sign(publishing.publications)
}

release {
    buildTasks = listOf("build", "publish")
}

tasks {
    named<Javadoc>("javadoc") {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}
