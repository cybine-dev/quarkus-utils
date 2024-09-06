plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation(project(":type-converter"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-jackson-deployment")

    testImplementation(project(":test-utils"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-internal")
}