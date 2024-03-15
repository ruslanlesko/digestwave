import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "com.leskor"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

val ollama4JVersion = "1.0.57"
val springKafkaVersion = "3.1.2"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.github.amithkoujalgi:ollama4j:$ollama4JVersion")
	implementation("org.springframework.kafka:spring-kafka:$springKafkaVersion")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.h2database:h2")
}

tasks.named<BootJar>("bootJar") {
	archiveFileName.set("app.jar")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
