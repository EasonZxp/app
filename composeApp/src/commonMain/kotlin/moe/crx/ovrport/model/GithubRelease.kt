package moe.crx.ovrport.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    @SerialName("html_url")
    var htmlUrl: String,
    @SerialName("name")
    val name: String,
    @SerialName("published_at")
    val publishedAt: String,
)