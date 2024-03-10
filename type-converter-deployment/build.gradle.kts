plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":common"))
    implementation(project(":type-converter"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-jackson-deployment")

    testImplementation("io.quarkus:quarkus-junit5-internal")
}