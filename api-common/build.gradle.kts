plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-smallrye-openapi")
}