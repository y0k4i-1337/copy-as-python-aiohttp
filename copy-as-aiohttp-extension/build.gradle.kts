version = "0.1.0"

plugins {
    // Apply the java-library plugin for API and implementation separation.
    id("java-library")
    // Apply the distribution plugin to create a distribution of the project.
    id("java-library-distribution")
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
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Define the main distribution of the project.
distributions {
    main {
        distributionBaseName = "copy-as-python-aiohttp"
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.jar {
    // Include the dependencies in the JAR file
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory()) zipTree(it) else it })
    // Customize the manifest file
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                         "Implementation-Version" to project.version))
    }
}
