import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Java plugin
    java
    // Apply the Spring Boot plugin (assuming 4.0.0 is the target version as you specified)
    id("org.springframework.boot") version "4.0.0"
    // Apply the Spring Dependency Management plugin
    id("io.spring.dependency-management") version "1.1.7"
    // Apply the Kotlin JVM plugin (required for using Kotlin DSL features easily)
    kotlin("jvm") version "1.9.24"
    // Apply the Spring Kotlin plugin
    kotlin("plugin.spring") version "1.9.24"
}

group = "com.aquarius"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// Define versions in one place for consistency
val testcontainersVersion = "1.19.8"
val lombokVersion = "1.18.30"
val postgresR2dbcVersion = "1.0.5.RELEASE"
val jjwtVersion = "0.11.5"

dependencies {
    // --- Application Dependencies ---
    // Core Reactive
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Reactive Data Access
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // Security and Actuator
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database Migration (Requires JDBC driver below)
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    // Core Spring Security
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-core")

    // R2DBC PostgreSQL Driver (Version required as it's not a starter)
    runtimeOnly("org.postgresql:r2dbc-postgresql:$postgresR2dbcVersion")

    // PostgreSQL JDBC Driver (Used by Flyway)
    implementation("org.postgresql:postgresql")
//    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JWT Implementation
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Lombok (Annotation Processing)
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Dev Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- Testing Dependencies ---
    // Core Spring Boot Test (Pulls in JUnit 5 API/Engine, Mockito, etc.)
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Reactive Testing Utility
    testImplementation("io.projectreactor:reactor-test")

    // R2DBC Test Slice (@DataR2dbcTest)
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc-test")

    // Testcontainers Integration (@ServiceConnection)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // Testcontainers Modules (Force consistent version)
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")

    // Mockito (Compatible version managed by spring-boot-starter-test)
    testImplementation("org.mockito:mockito-junit-jupiter")
}

// Configure Kotlin compilation (Standard block for Kotlin projects)
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21" // Assuming Java 21 or later for Spring Boot 4.x
    }
}

// Configure Test execution to use JUnit Platform
tasks.withType<Test> {
    useJUnitPlatform()
}