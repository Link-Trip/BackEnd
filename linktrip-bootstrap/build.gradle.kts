plugins {
    id("linktrip-convention")
}

dependencies {
    implementation(project(":linktrip-application"))
    implementation(project(":linktrip-input-http"))
    implementation(project(":linktrip-input-batch"))
    implementation(project(":linktrip-output-persistence:mysql"))
//    implementation(project(":linktrip-output-cache:redis"))
    implementation(project(":linktrip-output-cache:caffeine"))
    implementation(project(":linktrip-output-storage:aws"))
    implementation(project(":linktrip-output-http"))

    implementation(libs.bundles.bootstrap)
}

tasks {
    bootJar {
        enabled = true
    }

    named("bootJar") {
        dependsOn("ktlintFormat")
    }

    getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        mainClass.set("com.linktrip.bootstrap.LinktripBootstrapApplicationKt")
    }

    getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
        imageName.set("${System.getenv("ECR_REGISTRY") ?: "linktrip"}/${System.getenv("ECR_REPOSITORY") ?: "linktrip"}")
        environment.set(
            mapOf(
                "BP_JVM_VERSION" to "21",
            ),
        )
    }
}

springBoot {
    buildInfo()
}
