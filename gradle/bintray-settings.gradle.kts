import java.util.Properties

private val bintraySettings = fetchBintraySettings()

fun getBintraySettings(): BintraySettings = bintraySettings

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
