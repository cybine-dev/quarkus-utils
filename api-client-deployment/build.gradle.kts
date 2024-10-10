plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":api-client"))
    implementation(project(":type-converter-deployment"))
    implementation("io.quarkus:quarkus-arc-deployment")
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-jackson-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation(project(":test-utils"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-internal")
}