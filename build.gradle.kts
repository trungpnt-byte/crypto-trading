plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.aquarius"
version = "0.0.1-SNAPSHOT"
description = "A crypto trading application"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-database-postgresql")
    compileOnly("org.projectlombok:lombok:1.18.30") // Use the latest version
    annotationProcessor("org.projectlombok:lombok:1.18.30") // Use the latest version
    runtimeOnly("org.postgresql:postgresql")
//    testImplementation("org.testcontainers:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // JJWT dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core:5.+")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.security:spring-security-config")
//    implementation("org.springframework.security:spring-security-webflux")
    implementation("org.springframework.security:spring-security-core")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
// Use the latest stable version
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
// Use the same version as jjwt-api
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
// Use the same version as jjwt-api parsing
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.aquarius.crypto.CryptoTradingApplication.java"
    }
}