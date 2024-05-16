import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
//	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.jpa") version "1.9.23"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
//	sourceCompatibility = JavaVersion.VERSION_1_8
//	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
//	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("org.postgresql:postgresql")
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
//	testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
//		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

//springBoot { mainClass.set("com.TMDAD_2024.ChatApplicationKt") }

application {
	mainClass.set("com.TMDAD_2024.ChatApplicationKt")
}

//tasks.register<Copy>("kotlinClasspath") {
//	// Copy the Kotlin standard library to a separate directory
//	from(configurations["implementation"].filter { it.name.contains("kotlin-stdlib") })
//	into("${layout.buildDirectory}/kotlinClasspath")
//}
//
//tasks.getByName("build") {
//	// Make the build task depend on the kotlinClasspath task
//	dependsOn("kotlinClasspath")
//}

tasks.withType<Jar> {
//	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	manifest {
		attributes["Main-Class"] = "com.TMDAD_2024.ChatApplicationKt"
	}

	// To avoid the duplicate handling strategy error
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	// To add all of the dependencies
	from(sourceSets.main.get().output)
//
	dependsOn(configurations.runtimeClasspath)
	from({
//		configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
		configurations.runtimeClasspath.get().filter { it.name.endsWith("stdlib-1.9.23.jar") || it.name.endsWith("boot-3.2.4.jar")
				|| it.name.endsWith("context-6.1.5.jar")}.map { zipTree(it) }
//		configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
//		configurations.runtimeClasspath.get().map { if (it.isDirectory) println("Folder " + it.name) else println("File " + it.name) }
//		configurations.runtimeClasspath.get().map { if (it.name == "kotlin-stdlib-1.9.23.jar") zipTree(it) }
	})
//	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//	with(tasks.jar.get())
}


