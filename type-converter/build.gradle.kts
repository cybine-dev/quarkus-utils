plugins {
    alias(libs.plugins.lombok)
    alias(libs.plugins.quarkus)
}

quarkusExtension {
    deploymentModule = "type-converter-deployment"
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-jackson")
}