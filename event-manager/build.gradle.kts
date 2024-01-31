plugins {
    alias(libs.plugins.lombok)
    alias(libs.plugins.quarkus)
}

quarkusExtension {
    deploymentModule = "event-manager-deployment"
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation("io.quarkus:quarkus-core")
    implementation("io.quarkus:quarkus-mutiny")
}