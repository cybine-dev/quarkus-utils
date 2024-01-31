pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "quarkus-utils"

include("common")
include("type-converter", "type-converter-deployment")
include("datasource-query", "datasource-query-deployment")
include("api-query", "api-query-deployment")
include("event-manager", "event-manager-deployment")
include("action-processor", "action-processor-deployment")
