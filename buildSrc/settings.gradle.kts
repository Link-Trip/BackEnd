rootProject.name = "linktrip-convention"

plugins {
    id("com.gradle.develocity") version ("3.17.6")
}

develocity {
    buildScan {
        projectId.set("linktrip-application")

        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")

        link("VCS", "https://github.com/Link-Trip/BackEnd/tree/develop")
        value("hash", System.getenv("CURRENT_GIT_SHA"))
        value("status", System.getenv("STATUS"))

        buildFinished {
            if (this.failures.isNotEmpty()) {
                val failureMessages = this.failures.mapNotNull { it.message }.joinToString(",") { it }
                value("Failed with", failureMessages)
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}


