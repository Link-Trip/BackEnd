plugins {
    id("linktrip-convention")
    jacoco
}

dependencies {
    implementation(libs.bundles.domain.application)
}

val applicationTest =
    tasks.register<Test>("applicationTest") {
        description = "Runs tests under com/linktrip/application in the linktrip-application module."
        group = "verification"
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        include("com/linktrip/application/**")
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

val applicationTestCoverage =
    tasks.register<JacocoReport>("applicationTestCoverage") {
        description = "Generates JaCoCo coverage for applicationTest."
        group = "verification"
        dependsOn(applicationTest)
        executionData(layout.buildDirectory.file("jacoco/applicationTest.exec"))
        sourceDirectories.setFrom(files(sourceSets["main"].allSource.srcDirs))
        classDirectories.setFrom(files(sourceSets["main"].output))
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}
