plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":action-processor"))
    implementation(project(":common"))
    implementation(project(":type-converter-deployment"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation("io.quarkus:quarkus-junit5-internal")
}