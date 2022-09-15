val remoteRobotVersion = "0.11.16"

plugins {
    id("java")
    id("jacoco")
    id("org.jetbrains.intellij") version "1.9.0"
    id("org.sonarqube") version "3.4.0.2513"
}

group = "org.itsallcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    testImplementation("com.intellij.remoterobot:remote-robot:" + remoteRobotVersion)
    testImplementation("com.intellij.remoterobot:remote-fixtures:" + remoteRobotVersion)
    testImplementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    val junitVersion = "5.9.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:" + junitVersion)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:" + junitVersion)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    test {
        systemProperty("robot-server.port", "8082")
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    runIdeForUiTests {
        //    In case your Idea is launched on remote machine you can enable public port and enable encryption of JS calls
        //    systemProperty "robot-server.host.public", "true"
        //    systemProperty "robot.encryption.enabled", "true"
        //    systemProperty "robot.encryption.password", "my super secret"
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
        systemProperty("ide.mac.file.chooser.native", "false")
        systemProperty("jbScreenMenuBar.enabled", "false")
        systemProperty("apple.laf.useScreenMenuBar", "false")
        systemProperty("idea.trust.all.projects", "true")
        systemProperty("ide.show.tips.on.startup.default.value", "false")
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
        }
    }
}

tasks.sonarqube {
     dependsOn(tasks.jacocoTestReport)
}
