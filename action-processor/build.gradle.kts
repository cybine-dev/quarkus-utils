plugins {
    alias(libs.plugins.lombok)
    alias(libs.plugins.quarkus)
    alias(libs.plugins.jandex)
}

quarkusExtension {
    deploymentModule = "action-processor-deployment"
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation(project(":type-converter"))
    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-smallrye-openapi")
}