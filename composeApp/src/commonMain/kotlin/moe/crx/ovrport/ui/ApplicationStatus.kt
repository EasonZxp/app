package moe.crx.ovrport.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.crx.ovrport.patch.Constants.LOADING_COMPATIBILITY
import moe.crx.ovrport.patch.getCompatibilityStatus
import org.jetbrains.compose.resources.stringResource

@Composable
fun ApplicationStatus(
    applicationName: String? = "Application",
    applicationPackage: String? = "",
) {
    var statusInfo by remember(applicationPackage) { mutableStateOf(LOADING_COMPATIBILITY) }
    val urlHandler = LocalUriHandler.current

    LaunchedEffect(applicationPackage) {
        withContext(Dispatchers.IO) {
            statusInfo = getCompatibilityStatus(applicationName, applicationPackage)
        }
    }

    Card(colors = CardDefaults.cardColors(statusInfo.status.cardColor, statusInfo.status.contentColor)) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(
                RoundedCornerShape(16.dp)
            ).clickable {
                urlHandler.openUri(statusInfo.url)
            }) {
            Icon(
                statusInfo.status.icon,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
            )
            Text(
                stringResource(statusInfo.status.nameResource),
                fontSize = 18.sp,
                modifier = Modifier.padding(0.dp, 16.dp, 16.dp, 16.dp),
            )
        }
    }
}
