plugins {
    kotlin("jvm") version "1.9.20"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.github.cashubtc"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    
    // For coroutines support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
    explicitApi()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

// Copy the native library into the resources
tasks.register<Copy>("copyNativeLibrary") {
    from("../bindings/kotlin/libcdk_ffi.so") {
        rename { "libcdk_ffi.so" }
    }
    into("src/main/resources")
    
    doFirst {
        logger.info("Copying native library from ../bindings/kotlin/libcdk_ffi.so")
    }
}

// Copy and process the generated Kotlin bindings
tasks.register<Copy>("copyKotlinBindings") {
    from("../bindings/kotlin/uniffi/cdk_ffi/cdk_ffi.kt")
    into("src/main/kotlin/com/github/cashubtc/")
    
    filter { line ->
        // Update package declaration
        if (line.startsWith("package uniffi.cdk_ffi")) {
            "package com.github.cashubtc.cdk"
        } else {
            line
        }
    }
    
    doFirst {
        logger.info("Copying and processing Kotlin bindings")
    }
}

// Ensure native library and bindings are copied before compiling
tasks.named("compileKotlin") {
    dependsOn("copyNativeLibrary", "copyKotlinBindings")
}

tasks.named("processResources") {
    dependsOn("copyNativeLibrary")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("CDK FFI Kotlin")
                description.set("Kotlin bindings for the Cashu Development Kit (CDK) via FFI")
                url.set("https://github.com/cashubtc/cdk-ffi")
                
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("cashubtc")
                        name.set("Cashu BTC")
                        email.set("info@cashu.space")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/cashubtc/cdk-ffi.git")
                    developerConnection.set("scm:git:ssh://github.com/cashubtc/cdk-ffi.git")
                    url.set("https://github.com/cashubtc/cdk-ffi")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: ""
                password = project.findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}

// Task to build the Rust library before packaging
tasks.register<Exec>("buildRustLibrary") {
    workingDir = file("..")
    commandLine = listOf("just", "build-kotlin")
    
    doFirst {
        logger.info("Building Rust library and generating Kotlin bindings...")
    }
}

// Ensure Rust library is built before copying resources
tasks.named("copyNativeLibrary") {
    dependsOn("buildRustLibrary")
}

tasks.named("copyKotlinBindings") {
    dependsOn("buildRustLibrary")
}

// Create a fat JAR with the native library included
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// JVM arguments to help with library loading for tests
tasks.withType<Test> {
    systemProperty("java.library.path", "src/main/resources")
    systemProperty("jna.library.path", "src/main/resources")
}

// Task to verify the package can be used
tasks.register<JavaExec>("verify") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.github.cashubtc.cdk.VerifyPackageKt")
    
    systemProperty("java.library.path", "src/main/resources")
    systemProperty("jna.library.path", "src/main/resources")
    
    dependsOn("build")
}
