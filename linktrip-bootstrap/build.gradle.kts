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
        imageName.set("${System.getenv("ECR_REGISTRY")}/${System.getenv("ECR_REPOSITORY")}")
        environment.set(
            mapOf(
                "BP_JVM_VERSION" to "21",
                "BPE_SPRING_PROFILES_ACTIVE" to "prod",
                "BPE_JAVA_TOOL_OPTIONS" to
                    buildString {
                        append("-XX:+UseG1GC ")
                        append("-XX:+UseContainerSupport ")
                        append("-Xms512m -Xmx512m ")
                        append("-XX:+HeapDumpOnOutOfMemoryError ")
                        append("-XX:HeapDumpPath=/root/heapDump/%Y%m%d_%H%M%S.hprof ")
                        append("-XX:+UseStringDeduplication ")
                        append("-XX:+ExitOnOutOfMemoryError ")
                        append("-Dfile.encoding=UTF-8")
                    },
            ),
        )
        docker {
            publishRegistry {
                url.set(System.getenv("ECR_REGISTRY"))
                username.set("AWS")
                password.set(System.getenv("ECR_PASSWORD") ?: "")
            }
        }
        publish.set(true)
    }
}

springBoot {
    buildInfo()
}
