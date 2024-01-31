plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-jackson")

    implementation(libs.uuid.generator)
}