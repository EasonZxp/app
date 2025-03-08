package moe.crx.ovrport.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FrdaAsset(
    @SerialName("ID")
    val id: Long = 0,
    val path: String = "",
    val type: String = "store"
)

@Serializable
data class FrdaParameters(
    val assets: List<FrdaAsset> = listOf(),
    val skus: List<String> = listOf()
)

@Serializable
data class FrdaInteraction(
    val parameters: FrdaParameters = FrdaParameters(),
)

@Serializable
data class FrdaConfig(
    val interaction: FrdaInteraction = FrdaInteraction(),
)

private val json = Json { ignoreUnknownKeys = true }

fun generateConfig(jsonString: String): String {
    return json.decodeFromString<FrdaConfig>(jsonString).let { config ->
        json.encodeToString(
            OverportConfigV1(
                database = listOf(
                    OverportConfigApplication(
                        "__current__",
                        config.interaction.parameters.skus,
                        config.interaction.parameters.assets.map { OverportConfigAsset(it.id, it.path, it.type) })
                )
            )
        )
    }
}
