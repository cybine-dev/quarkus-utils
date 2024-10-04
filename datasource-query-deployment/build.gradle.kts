plugins {
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(platform(libs.quarkus.bom))

    implementation(project(":datasource-query"))
    implementation(project(":type-converter-deployment"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-hibernate-orm-deployment")
    implementation("io.quarkus:quarkus-hibernate-validator-deployment")
    implementation("io.quarkus:quarkus-jackson-deployment")
    implementation("io.quarkus:quarkus-smallrye-openapi-deployment")

    testImplementation(project(":common"))
    testImplementation(project(":type-converter"))

    testImplementation(project(":test-utils"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-internal")
    testImplementation("io.quarkus:quarkus-hibernate-orm")
    testImplementation("io.quarkus:quarkus-jdbc-h2")
    testImplementation("io.quarkus:quarkus-jdbc-mariadb")
    testImplementation("io.quarkus:quarkus-jdbc-postgresql")
    testImplementation("io.quarkus:quarkus-liquibase")
}
