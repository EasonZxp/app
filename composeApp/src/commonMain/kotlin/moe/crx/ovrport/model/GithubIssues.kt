package moe.crx.ovrport.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubIssueLabel(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("color")
    val color: String,
)

@Serializable
data class GithubIssue(
    @SerialName("html_url")
    var htmlUrl: String,
    @SerialName("title")
    val title: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("labels")
    val labels: List<GithubIssueLabel>
)

@Serializable
data class GithubIssues(
    @SerialName("items")
    val items: List<GithubIssue>
)