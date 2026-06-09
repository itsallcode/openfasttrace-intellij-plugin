import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun isNonStableVersion(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val stableVersion = "^[0-9,.v-]+(-r)?$".toRegex().matches(version)
    return !stableKeyword && !stableVersion
}

// [bld->dsn~plugin-build-uses-intellij-platform-gradle-plugin~1]
plugins {
    id("java")
    id("jacoco")
    id("com.github.ben-manes.versions") version "0.54.0"
    id("com.diffplug.spotless") version "8.4.0"
    id("org.itsallcode.openfasttrace") version "3.1.1"
    id("org.jetbrains.intellij.platform") version "2.15.0"
    id("org.sonarqube") version "7.2.3.7755"
}

val pluginVersion = providers.gradleProperty("version")

group = "org.itsallcode.openfasttrace"
version = pluginVersion.get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.13"
}

// [bld->dsn~gradle-dependency-maintenance-uses-locks-and-versions-plugin~1]
dependencyLocking {
    lockAllConfigurations()
}

sonar {
    properties {
        property("sonar.organization", "itsallcode")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath
        )
    }
}

requirementTracing {
    failBuild = true
    inputDirectories = files("doc", "src/main/java", "src/test/java")
    tags {
        tag {
            paths = fileTree("./").include("build.gradle.kts") as FileCollection?
            tagArtifactType = "bld"
            coveredItemArtifactType = "dsn"
        }
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.itsallcode.openfasttrace:openfasttrace:4.4.0")

    intellijPlatform {
        intellijIdea("2026.1")
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }

    testImplementation(platform("org.junit:junit-bom:5.14.1"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        id = "org.itsallcode.openfasttrace-intellij-plugin"
        name = "OpenFastTrace"
        version = pluginVersion

        vendor {
            name = "Itsallcode.org"
            email = "opensource@itsallcode.org"
            url = "https://itsallcode.org/"
        }

        ideaVersion {
            sinceBuild = "261"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

intellijPlatformTesting {
    runIde {
        create("manualTestIde") {
            sandboxDirectory.set(layout.buildDirectory.dir("manual-test-ide-sandbox"))
            task {
                description = "Launches a throwaway IntelliJ instance with the plugin installed for manual testing."
            }
        }
    }
}

val instrumentedMainClasses = layout.buildDirectory.dir("instrumented/instrumentCode")

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
    }

    named<Zip>("buildPlugin") {
        archiveBaseName.set("OpenFastTrace")
    }

    withType<DependencyUpdatesTask>().configureEach {
        revision = "release"
        gradleReleaseChannel = "current"
        rejectVersionIf {
            isNonStableVersion(candidate.version) && !isNonStableVersion(currentVersion)
        }
    }

    test {
        useJUnitPlatform()
        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    jacocoTestReport {
        dependsOn(test)
        classDirectories.setFrom(instrumentedMainClasses)
        reports {
            xml.required = true
            html.required = true
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(test)
        classDirectories.setFrom(instrumentedMainClasses)
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }

    named("sonar") {
        dependsOn(jacocoTestReport)
    }

    named("verifyPluginProjectConfiguration") {
        dependsOn("spotlessCheck")
    }

    named("verifyPluginStructure") {
        dependsOn("spotlessCheck")
    }

    check {
        dependsOn(
            traceRequirements,
            jacocoTestCoverageVerification,
        )
    }
}
