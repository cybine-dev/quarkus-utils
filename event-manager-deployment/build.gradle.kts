plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":event-manager"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-mutiny-deployment")

    testImplementation("io.quarkus:quarkus-junit5-internal")
}