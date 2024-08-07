plugins {
    id("maven-publish")
    id("java-library")
}

subprojects {
    group = "de.cybine.quarkus"
    version = "0.4.1-SNAPSHOT"

    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name = project.name

                    developers {
                        developer {
                            id = "boelter.fabio"
                            name = "Fabio Boelter"
                            email = "boelter.fabio@cybine.de"
                        }
                    }

                    scm {
                        connection = "scm:git:git://github.com/cybine-dev/quarkus-utils.git"
                        developerConnection = "scm:git:ssh://github.com/cybine-dev/quarkus-utils.git"
                        url = "https://github.com/cybine-dev/quarkus-utils"
                    }
                }
            }
        }

        repositories {
            maven {
                val releasesRepoUrl = "https://repository.cybine.de/repository/maven-releases/"
                val snapshotsRepoUrl = "https://repository.cybine.de/repository/maven-snapshots/"

                val isSnapshot = (project.version as String).endsWith("-SNAPSHOT")

                name = "cybine"
                url = uri(if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl)

                credentials(PasswordCredentials::class)
            }
        }
    }
}