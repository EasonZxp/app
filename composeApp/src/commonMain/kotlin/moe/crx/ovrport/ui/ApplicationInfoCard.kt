package moe.crx.ovrport.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.crx.ovrport.patch.Constants.LOADING_COMPATIBILITY
import moe.crx.ovrport.patch.Patch
import moe.crx.ovrport.patch.PatchStore
import moe.crx.ovrport.patch.getCompatibilityStatus
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun ApplicationInfoCard(
    innerPadding: PaddingValues = PaddingValues(16.dp),
    applicationName: String? = "Application",
    applicationPackage: String? = "",
    applicationVersion: String? = "1.0.0",
    applicationIcon: ImageBitmap? = null,
    onCancel: () -> Unit = {},
    onConfirm: (List<Patch>) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ApplicationInfo(applicationName, applicationPackage, applicationVersion, applicationIcon)
            ApplicationStatus(applicationName, applicationPackage)
        }

        val patches = remember { mutableStateListOf(*PatchStore.PATCHES.toTypedArray()) }

        fun togglePatch(patch: Patch) {
            val checked = patches.contains(patch)

            if (checked) {
                patches.remove(patch)
            } else {
                patches.add(patch)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(PatchStore.PATCHES) {
                ListItem(
                    headlineContent = {
                        Text(it.name)
                    },
                    supportingContent = {
                        it.desc?.let {
                            Text(it)
                        }
                    },
                    trailingContent = {
                        Checkbox(patches.contains(it), { _ ->
                            togglePatch(it)
                        })
                    },
                    modifier = Modifier.clickable {
                        togglePatch(it)
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onCancel
            ) {
                Text("Cancel")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onConfirm(patches)
                }
            ) {
                Text("Confirm")
            }
        }
    }
}