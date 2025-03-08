package moe.crx.ovrport.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import moe.crx.ovrport.patch.Patch
import moe.crx.ovrport.patch.PatchStore
import org.jetbrains.compose.ui.tooling.preview.Preview

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