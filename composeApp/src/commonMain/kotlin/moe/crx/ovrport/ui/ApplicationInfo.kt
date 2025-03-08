package moe.crx.ovrport.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.unknown_package

@Composable
fun ApplicationInfo(
    applicationName: String? = "Application",
    applicationPackage: String? = "com.company.application",
    applicationVersion: String? = "1.0.0",
    applicationIcon: ImageBitmap? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (applicationIcon == null) {
            Icon(
                Icons.Default.Widgets,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Image(
                applicationIcon,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
            )
        }
        Column(
            modifier = Modifier.height(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("$applicationName ($applicationVersion)")
            Text(
                applicationPackage ?: stringResource(Res.string.unknown_package),
            )
        }
    }
}
