plugins {
    alias(libs.plugins.lombok)
    alias(libs.plugins.quarkus)
}

quarkusExtension {
    deploymentModule = "api-query-deployment"
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation(project(":datasource-query"))
    implementation(project(":type-converter"))
    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-smallrye-openapi")
}