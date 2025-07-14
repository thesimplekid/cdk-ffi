plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    
    // For coroutines if you need async operations
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
}

application {
    mainClass.set("com.example.MainKt")
}

// Task to copy native library and Kotlin bindings
tasks.register<Copy>("copyBindings") {
    from("../bindings/kotlin/libcdk_ffi.so") {
        into(".")
    }
    from("../bindings/kotlin/uniffi/cdk_ffi/cdk_ffi.kt") {
        into("uniffi/cdk_ffi")
    }
    into("src/main/resources")
}

// Make sure bindings are copied before compiling
tasks.named("compileKotlin") {
    dependsOn("copyBindings")
}

// JVM arguments to help with library loading
tasks.withType<JavaExec> {
    systemProperty("java.library.path", "src/main/resources")
    systemProperty("jna.library.path", "src/main/resources")
}

// Set library path for tests too
tasks.withType<Test> {
    systemProperty("java.library.path", "src/main/resources")
    systemProperty("jna.library.path", "src/main/resources")
}
