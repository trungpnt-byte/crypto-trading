import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

group = "com.aquarius"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val testcontainersVersion = "1.19.8"
val lombokVersion = "1.18.30"
val postgresR2dbcVersion = "1.0.5.RELEASE"
val jjwtVersion = "0.11.5"

dependencies {
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
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

    // R2DBC PostgreSQL Driver (Version required as it's not a starter)
    runtimeOnly("org.postgresql:r2dbc-postgresql:$postgresR2dbcVersion")

    // PostgreSQL JDBC Driver (Used by Flyway)
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JWT Implementation
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")
    // Dev Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}