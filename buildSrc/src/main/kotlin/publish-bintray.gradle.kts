import java.util.Properties

plugins {
    maven
    `maven-publish`
}

apply(from = "${rootProject.projectDir}/gradle/maven-central.gradle".replace("buildSrc", ""))
// todo: figure out how we can check it in a generic way
val isMultiplatform = project.name == "atomicfu"
val settings = fetchBintraySettings()

if (!isMultiplatform) {
    // Regular java modules need 'java-library' plugin for proper publication.
    apply(plugin = "java-library")

    // MPP projects pack their sources automatically. Java libraries need to explicitly pack them.
    tasks.create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from("src/main/kotlin")
    }
}

// empty xxx-javadoc.jar
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            val publish = false.intValue
            val projectName = "kotlinx.atomicfu"
            val baseUrl = "https://api.bintray.com/maven"
            url = uri("$baseUrl/${settings.org}/${settings.repo}/$projectName/;publish=$publish")

            credentials {
                username = if (settings.user.isNotEmpty()) settings.user else System.getenv("BINTRAY_USER")
                password = if (settings.apiKey.isNotEmpty()) settings.apiKey else System.getenv("BINTRAY_API_KEY")
            }
        }
    }

    if (!isMultiplatform) {
        // Configure java publications for non-MPP projects.
        publications {
            // Plugin configures its own publication pluginMaven.
            if (project.name == "atomicfu-gradle-plugin") {
                create<MavenPublication>("maven") {
                    artifact("sourcesJar")
                }
            } else {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    artifact("sourcesJar")
                    if (project.name.endsWith("-maven-plugin")) {
                        pom.packaging = "maven-plugin"
                    }
                }
            }
        }
    }

    publications.forEach { pub ->
        // TODO: Figure out how to configure Maven Central metadata.
//        pub.pom.withXml(configureMavenCentralMetadata)

        // Add empty javadocs.
        if (pub.name != "kotlinMultiplatform") {
            // TODO: Find out how to set javadocJar task as the artifact for a publication.
            // The root module gets the JVM's javadoc JAR.
//            pub.artifact(javadocJar)
        }
    }
}

data class BintraySettings(
    val user: String,
    val apiKey: String,
    val org: String,
    val repo: String,
    val publishingEnabled: Boolean,
    val override: Boolean
)

fun fetchBintraySettings(): BintraySettings {
    val properties = Properties()
    val file = file("${rootProject.rootDir}/bintray.properties")
    var user = ""
    var apiKey = ""
    var org = ""
    var repo = ""
    var publishingEnabled = false
    var override = false

    if (file.exists()) {
        file.bufferedReader().use { br ->
            properties.load(br)
            user = properties.getProperty("user") ?: ""
            apiKey = properties.getProperty("apiKey") ?: ""
            org = properties.getProperty("org") ?: ""
            repo = properties.getProperty("repo") ?: ""
            publishingEnabled = properties.getProperty("publishingEnabled")?.toBoolean() ?: false
            override = properties.getProperty("override")?.toBoolean() ?: false
        }
    }
    return BintraySettings(user = user, apiKey = apiKey, org = org, repo = repo, publishingEnabled = publishingEnabled,
        override = override)
}

val Boolean.intValue: Int
    get() = if (this) 1 else 0
