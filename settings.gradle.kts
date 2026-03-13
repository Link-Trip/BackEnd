pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "linktrip"

include("linktrip-application")
include("linktrip-bootstrap")
include("linktrip-common")
include("linktrip-input-http")
include("linktrip-input-batch")
//include("linktrip-output-cache:redis")
include("linktrip-output-cache:caffeine")
include("linktrip-output-persistence:mysql")
include("linktrip-output-storage:aws")
include("linktrip-output-http")
