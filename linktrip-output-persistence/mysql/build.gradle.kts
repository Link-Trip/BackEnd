plugins {
    id("linktrip-convention")
}

dependencies {
    implementation(project(":linktrip-application"))
    implementation(libs.bundles.adaptor.persistence.mysql)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
