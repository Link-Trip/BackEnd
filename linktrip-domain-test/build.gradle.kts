import java.io.ByteArrayOutputStream

fun Project.isWsl(): Boolean {
    val osRelease = file("/proc/sys/kernel/osrelease")
    return osRelease.exists() && osRelease.readText().contains("microsoft", ignoreCase = true)
}

fun Project.openCoverageReport(reportFile: File) {
    val reportPath = reportFile.absolutePath
    val osName = System.getProperty("os.name").lowercase()

    when {
        isWsl() -> {
            val output = ByteArrayOutputStream()
            exec {
                commandLine("wslpath", "-w", reportPath)
                standardOutput = output
            }
            val windowsPath = output.toString().trim()
            exec {
                commandLine(
                    "powershell.exe",
                    "-NoProfile",
                    "-Command",
                    "Start-Process chrome '$windowsPath'",
                )
            }
        }
        osName.contains("win") -> {
            exec {
                commandLine(
                    "powershell",
                    "-NoProfile",
                    "-Command",
                    "Start-Process chrome '${reportFile.toURI()}'",
                )
            }
        }
        osName.contains("mac") -> {
            exec {
                commandLine("open", "-a", "Google Chrome", reportPath)
            }
        }
        else -> {
            exec {
                commandLine("xdg-open", reportPath)
            }
        }
    }
}

tasks.register("TDD-Application") {
    group = "test run"
    description = "Runs all tests in the linktrip-application module with coverage and opens the HTML report."

    doLast {
        exec {
            workingDir = file("..")
            commandLine(
                "./gradlew",
                "--rerun-tasks",
                ":linktrip-application:applicationTest",
                ":linktrip-application:applicationTestCoverage",
            )
        }

        openCoverageReport(
            file("../linktrip-application/build/reports/jacoco/applicationTestCoverage/html/index.html"),
        )
    }
}
