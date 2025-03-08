package moe.crx.ovrport.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import moe.crx.ovrport.AndroidPlatform
import moe.crx.ovrport.getCurrentVersion
import moe.crx.ovrport.patch.Patch
import moe.crx.ovrport.getPlatform
import moe.crx.ovrport.model.GithubRelease
import moe.crx.ovrport.patch.Constants
import moe.crx.ovrport.ui.theme.OverportTheme
import moe.crx.ovrport.utils.HttpUtil
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.overport
import overportapp.composeapp.generated.resources.update_available

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    onOpen: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: (List<Patch>) -> Unit
) {
    OverportTheme(darkTheme = true) {
        var versionToUpdate by remember { mutableStateOf<GithubRelease?>(null) }
        val urlHandler = LocalUriHandler.current

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                versionToUpdate = viewModel.versionToUpdate()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (getPlatform() is AndroidPlatform) {
                        Modifier.clip(RoundedCornerShape(32.dp))
                    } else Modifier
                ),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(Res.drawable.overport),
                                contentDescription = null,
                                modifier = Modifier.size(112.dp, 24.dp)
                            )
                            Text(
                                Constants.OVRPORT_VERSION,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 18.sp,
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            versionToUpdate?.name != null && versionToUpdate?.name != getCurrentVersion(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(
                                    RoundedCornerShape(16.dp)
                                ).clickable {
                                    versionToUpdate?.htmlUrl?.let { urlHandler.openUri(it) }
                                }) {
                                Icon(
                                    Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
                                )
                                Text(
                                    stringResource(Res.string.update_available),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(0.dp, 8.dp, 8.dp, 8.dp),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                )
            }
        ) { innerPadding ->
            val inProgress = viewModel.working
            val isPatcherVisible = viewModel.isApkLoaded() && !inProgress

            AnimatedVisibility(
                !isPatcherVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StartingCard(innerPadding, inProgress) {
                    onOpen()
                }
            }

            AnimatedVisibility(
                isPatcherVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ApplicationInfoCard(
                    innerPadding,
                    viewModel.currentAppName(),
                    viewModel.currentAppPackage(),
                    viewModel.currentAppVersion(),
                    viewModel.currentAppIcon(),
                    onCancel = {
                        onCancel()
                    },
                    onConfirm = {
                        onConfirm(it)
                    }
                )
            }
        }
    }
}