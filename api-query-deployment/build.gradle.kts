plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":api-common"))
    implementation(project(":api-query"))
    implementation(project(":common"))
    implementation(project(":datasource-query-deployment"))
    implementation(project(":type-converter-deployment"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-resteasy-reactive-deployment")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson-deployment")
    implementation("io.quarkus:quarkus-security-deployment")
    implementation("io.quarkus:quarkus-smallrye-jwt-deployment")
    implementation("io.quarkus:quarkus-smallrye-jwt-build-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation(project(":datasource-query"))
    testImplementation(project(":type-converter"))

    testImplementation(project(":test-utils"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-internal")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-hibernate-orm")
    testImplementation("io.quarkus:quarkus-jdbc-h2")
    testImplementation("io.quarkus:quarkus-jdbc-mariadb")
    testImplementation("io.quarkus:quarkus-jdbc-postgresql")
    testImplementation("io.quarkus:quarkus-liquibase")
}