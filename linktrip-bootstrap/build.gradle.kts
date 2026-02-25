plugins {
    id("linktrip-convention")
}

dependencies {
    implementation(project(":linktrip-input-http"))
    implementation(project(":linktrip-output-persistence:mysql"))
//    implementation(project(":linktrip-output-cache:redis"))
    implementation(project(":linktrip-output-storage:aws"))

    implementation(libs.bundles.bootstrap)

    testImplementation(project(":linktrip-application"))
}

tasks {
    bootJar {
        enabled = true
    }

    getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        mainClass.set("com.linktrip.bootstrap.LinktripBootstrapApplicationKt")
    }
}
