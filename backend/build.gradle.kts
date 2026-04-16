plugins {
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
	kotlin("plugin.jpa") version "1.9.22"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example.se405"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java 	{
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	runtimeOnly("org.postgresql:postgresql")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter:1.19.7")
	testImplementation("org.testcontainers:postgresql:1.19.7")
	
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
//		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}
