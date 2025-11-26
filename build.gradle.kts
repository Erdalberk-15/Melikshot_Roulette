plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.25.0"
    kotlin("jvm") version "1.9.24"
}

group = "com.melikshotroulette"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.melikshotroulette.melikshotroulette.MainKt")
    applicationName = "MelikshotRoulette"
}

javafx {
    version = "23" // ⚠️ JavaFX 23 kullan, Java 21 ile tam uyumlu
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.media")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("net.synedra:validatorfx:0.6.1") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.json:json:20231013")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Create fat JAR with all dependencies
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.melikshotroulette.melikshotroulette.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    archiveBaseName.set("MelikshotRoulette")
}

tasks.named<JavaExec>("run") {
    // JavaFX modules are automatically handled by the javafx plugin
    // No need to manually specify module-path
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/MelikshotRoulette-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "MelikshotRoulette"
    }
    jpackage {
        imageName = "MelikshotRoulette"
        installerName = "MelikshotRoulette"
        appVersion = "1.0.0"
        installerOptions.addAll(listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut"
        ))
    }
}
