package moe.crx.ovrport.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import moe.crx.ovrport.patch.Patch
import moe.crx.ovrport.patch.PatchStore
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import org.jetbrains.compose.resources.stringResource
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.unknown_package

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun ApplicationInfoCard(
    innerPadding: PaddingValues = PaddingValues(16.dp),
    applicationName: String? = "Application",
    applicationPackage: String? = "com.company.application",
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
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    color = MaterialTheme.colorScheme.secondary
                )
            }
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