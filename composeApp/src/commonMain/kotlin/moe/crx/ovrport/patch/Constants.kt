package moe.crx.ovrport.patch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.json.Json
import moe.crx.ovrport.model.GithubIssues
import moe.crx.ovrport.patch.Constants.UNKNOWN_COMPATIBILITY
import moe.crx.ovrport.utils.HttpUtil.download
import org.jetbrains.compose.resources.StringResource
import overportapp.composeapp.generated.resources.*
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.status_has_problems
import overportapp.composeapp.generated.resources.status_not_working
import overportapp.composeapp.generated.resources.status_unknown
import java.net.URLEncoder

private val json = Json { ignoreUnknownKeys = true }

object Constants {
    const val APK_MIMETYPE = "application/vnd.android.package-archive"
    const val OVRPORT_VERSION = "3.2.2"
    const val LIBRARIES_URL = "https://files.crx.moe/ovrport/tracker/releases/download/$OVRPORT_VERSION/libraries.zip"
    const val LIBRARIES_DIR = "libraries/$OVRPORT_VERSION"
    const val RELEASES_URL = "https://api.github.com/repos/ovrport/app/releases"
    const val ISSUES_URL = "https://api.github.com/search/issues?q=%s+repo:ovrport/tracker"
    private const val TRACKER_URL = "https://github.com/ovrport/tracker/issues"
    val UNKNOWN_COMPATIBILITY = CompatibilityStatusInfo(CompatibilityStatus.UNKNOWN, TRACKER_URL)
    val LOADING_COMPATIBILITY = CompatibilityStatusInfo(CompatibilityStatus.LOADING, TRACKER_URL)
}

enum class CompatibilityStatus(
    val labelName: String,
    val icon: ImageVector,
    val nameResource: StringResource,
    val cardColor: Color,
    val contentColor: Color,
) {
    LOADING(
        "Loading",
        Icons.Default.Sync,
        Res.string.status_loading,
        Color(0xFF483f77),
        Color(0xFFe6deff),
    ),
    UNKNOWN(
        "Unknown",
        Icons.Default.QuestionMark,
        Res.string.status_unknown,
        Color(0xFF354479),
        Color(0xFFDCE1FF),
    ),
    NOT_WORKING(
        "Not working",
        Icons.Default.Close,
        Res.string.status_not_working,
        Color(0xFF73332f),
        Color(0xFFffdad7),
    ),
    HAS_PROBLEMS(
        "Has problems",
        Icons.Default.Warning,
        Res.string.status_has_problems,
        Color(0xFF5b4300),
        Color(0xFFffdf9f),
    ),
    PLAYABLE(
        "Playable",
        Icons.Default.Check,
        Res.string.status_playable,
        Color(0xFF2d4f1d),
        Color(0xFFc4efac),
    ),
}

data class CompatibilityStatusInfo(
    val status: CompatibilityStatus,
    val url: String,
)


fun getCompatibilityStatus(
    applicationName: String?,
    applicationPackage: String?
): CompatibilityStatusInfo {
    val pkg = applicationName ?: ""
    val name = applicationPackage ?: ""

    runCatching {
        val issue = json
            .decodeFromString<GithubIssues>(
                String(
                    download(
                        Constants.ISSUES_URL.format(
                            URLEncoder.encode(
                                pkg,
                                Charsets.UTF_8
                            )
                        )
                    )
                )
            )
            .items.firstOrNull { it.title.contains(pkg) || it.title.contains(name) }
            ?: json
                .decodeFromString<GithubIssues>(
                    String(
                        download(
                            Constants.ISSUES_URL.format(
                                URLEncoder.encode(
                                    name,
                                    Charsets.UTF_8
                                )
                            )
                        )
                    )
                )
                .items.firstOrNull { it.title.contains(pkg) || it.title.contains(name) }

        issue?.labels?.forEach { label ->
            CompatibilityStatus.entries.forEach { status ->
                if (status.labelName == label.name) {
                    return CompatibilityStatusInfo(status, issue.htmlUrl)
                }
            }
        }
    }

    return UNKNOWN_COMPATIBILITY
}