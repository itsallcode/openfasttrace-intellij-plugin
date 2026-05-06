plugins {
    java
}

repositories {
    mavenCentral()
}

val junitBomVersion = providers.gradleProperty("junitBomVersion")
val hamcrestVersion = providers.gradleProperty("hamcrestVersion")

dependencies {
    testImplementation(platform("org.junit:junit-bom:${junitBomVersion.get()}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest:${hamcrestVersion.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
