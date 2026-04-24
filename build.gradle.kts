import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

// [[plugin-build-uses-intellij-platform-gradle-plugin:1]]
plugins {
    id("java")
    id("jacoco")
    id("com.diffplug.spotless") version "8.4.0"
    id("org.itsallcode.openfasttrace") version "3.1.1"
    id("org.jetbrains.intellij.platform") version "2.14.0"
    id("org.sonarqube") version "7.2.3.7755"
    id("org.sonatype.gradle.plugins.scan") version "3.1.5"
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.13"
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

val ossIndexUsername = providers.gradleProperty("ossIndexUsername")
    .orElse(providers.environmentVariable("OSSINDEX_USERNAME"))
    .orNull
val ossIndexToken = providers.gradleProperty("ossIndexToken")
    .orElse(providers.environmentVariable("OSSINDEX_TOKEN"))
    .orNull

ossIndexAudit {
    ossIndexUsername?.let { username = it }
    ossIndexToken?.let { password = it }
    isUseCache = true
    isPrintBanner = false
    isColorEnabled = false
    isFailOnDetection = true
}

requirementTracing {
    failBuild = true
    inputDirectories = files("doc", "src/main/java", "src/test/java")
    tags {
        tag {
            paths = files("build.gradle.kts")
            coveredItemArtifactType = "dsn"
            tagArtifactType = "bld"
            coveredItemNamePrefix = ""
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
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }

    testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junitBomVersion").get()}"))
    testImplementation("junit:junit:${providers.gradleProperty("junit4Version").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest:${providers.gradleProperty("hamcrestVersion").get()}")
    testImplementation("org.opentest4j:opentest4j:${providers.gradleProperty("opentest4jVersion").get()}")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        id = providers.gradleProperty("pluginId")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("version")

        vendor {
            name = providers.gradleProperty("pluginVendor")
            email = providers.gradleProperty("pluginVendorEmail")
            url = providers.gradleProperty("pluginVendorUrl")
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("platformSinceBuild")
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

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
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
        reports {
            xml.required = true
            html.required = true
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(test)
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
