import java.util.Properties

/*
 * Copyright 2017-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

// Configures publishing of Maven artifacts to Bintray
apply(plugin = "maven")
apply(plugin = "maven-publish")
apply("gradle/maven-central.gradle")

// todo: figure out how we can check it in a generic way
val isMultiplatform = project.name == "atomicfu"

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
tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

// TODO: Convert rest of publishing block from Groovy DSL to Kotlin DSL.
//publishing {
//    repositories {
//        maven {
//            val settings = fetchBintraySettings()
//            val publish = if (settings.publishingEnabled) 1 else 0
//            val override = if (settings.override) 1 else 0
//            val name = "kotlinx.atomicfu"
//            val baseUrl = "https://api.bintray.com/maven"
//            url = uri("$baseUrl/${settings.user}/${settings.repo}/$name/;publish=$publish;override=$override")
//
//            credentials {
//                if (settings.user.isNotEmpty()) {
//                    username = settings.user
//                } else {
//                    username = System.getenv("BINTRAY_USER")
//                }
//                if (settings.apiKey.isNotEmpty()) {
//                    password = settings.apiKey
//                } else {
//                    password = System.getenv("BINTRAY_API_KEY")
//                }
//            }
//        }
//    }
//
//    if (!isMultiplatform) {
//        // Configure java publications for non-MPP projects
//        publications {
//            // plugin configures its own publication pluginMaven
//            if (project.name == "atomicfu-gradle-plugin") {
//                pluginMaven(MavenPublication) {
//                    artifact(sourcesJar)
//                }
//            } else {
//                maven(MavenPublication) {
//                    from(components.java)
//                    artifact(sourcesJar)
//
//                    if (project.name.endsWith("-maven-plugin")) pom.packaging = "maven-plugin"
//                }
//            }
//        }
//    }
//
//    publications.all {
//        pom.withXml(configureMavenCentralMetadata)
//        // Add empty javadocs.
//        if (it.name != "kotlinMultiplatform") {
//            // The root module gets the JVM's javadoc JAR.
//            it.artifact(javadocJar)
//        }
//    }
//}

data class BintraySettings(
    val user: String,
    val apiKey: String,
    val org: String,
    val repo: String,
    val publishingEnabled: Boolean,
    val override: Boolean
)

fun fetchBintraySettings(): BintraySettings {
    val filePath = "bintray.properties"
    val properties = Properties()
    var user = ""
    var apiKey = ""
    var org = ""
    var repo = ""
    var publishingEnabled = false
    var override = false

    if (file(filePath).exists()) {
        file(filePath).bufferedReader().use { br ->
            properties.load(br)
            user = properties.getProperty("user") ?: ""
            apiKey = properties.getProperty("apiKey") ?: ""
            org = properties.getProperty("org") ?: ""
            repo = properties.getProperty("repo") ?: ""
            publishingEnabled = properties.getProperty("publishingEnabled")?.toBoolean() ?: true
            override = properties.getProperty("override")?.toBoolean() ?: true
        }
    }
    return BintraySettings(
        user = user,
        apiKey = apiKey,
        org = org,
        repo = repo,
        publishingEnabled = publishingEnabled,
        override = override
    )
}
