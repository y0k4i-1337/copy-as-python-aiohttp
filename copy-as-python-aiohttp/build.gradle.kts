version = "1.1.1"

plugins {
    // Apply the java-library plugin for API and implementation separation.
    id("java-library")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20240303")

    // Dependency needed to create a new extension
    // https://mvnrepository.com/artifact/net.portswigger.burp.extensions/montoya-api
    implementation("net.portswigger.burp.extensions:montoya-api:2023.12.1")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents.core5/httpcore5
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.4")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

// Define a task to create a fat jar with all dependencies included
tasks.register<Jar>("fatJar") {
    archiveClassifier = "fat"
    manifest {
        attributes(mapOf("Main-Class" to "burp.BurpExtender", "Implementation-Title" to project.name,
                         "Implementation-Version" to project.version))
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

// Ensure the fatJar task runs when the build task is executed
tasks.named("build") {
    dependsOn(tasks.named("fatJar"))
}
