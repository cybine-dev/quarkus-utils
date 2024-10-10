plugins {
    alias(libs.plugins.lombok)
    alias(libs.plugins.quarkus)
}

quarkusExtension {
    deploymentModule = "api-client-deployment"
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":api-common"))
    implementation(project(":common"))
    implementation("io.quarkus:quarkus-core")
}