plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":action-processor"))
    implementation(project(":common"))
    implementation(project(":type-converter-deployment"))

    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation(project(":api-common"))
    testImplementation(project(":api-query"))
    testImplementation(project(":datasource-query"))
    testImplementation(project(":type-converter"))

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-hibernate-orm")
    testImplementation("io.quarkus:quarkus-jdbc-mariadb")
    testImplementation("io.quarkus:quarkus-liquibase")
}