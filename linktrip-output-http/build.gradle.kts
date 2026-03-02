plugins {
    id("linktrip-convention")
}

dependencies {
    implementation(project(":linktrip-application"))
    implementation(libs.bundles.adaptor.output.http)
}
