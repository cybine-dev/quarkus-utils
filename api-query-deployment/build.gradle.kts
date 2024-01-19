plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":api-query"))
    implementation(project(":datasource-query-deployment"))
    implementation(project(":type-converter-deployment"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-resteasy-reactive-deployment")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson-deployment")
    implementation("io.quarkus:quarkus-security-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation("io.quarkus:quarkus-junit5-internal")
}