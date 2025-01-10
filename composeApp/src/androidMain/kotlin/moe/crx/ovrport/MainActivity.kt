package moe.crx.ovrport

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import moe.crx.ovrport.patch.CantDecodeApkException
import moe.crx.ovrport.patch.CantUpdateOverportException
import moe.crx.ovrport.patch.Constants
import moe.crx.ovrport.ui.AppContent
import moe.crx.ovrport.ui.MainViewModel
import org.jetbrains.compose.resources.stringResource
import overportapp.composeapp.generated.resources.*
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.apk_file_exported
import overportapp.composeapp.generated.resources.cant_read_apk
import overportapp.composeapp.generated.resources.cant_update_overport
import java.io.FileInputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActivityContent(this)
        }
    }
}

@Composable
private fun ActivityContent(activity: MainActivity) {
    val viewModel: MainViewModel = viewModel { MainViewModel() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val cantReadApkMessage = stringResource(Res.string.cant_read_apk)
    val apkFileExportedMessage = stringResource(Res.string.apk_file_exported)
    val cantUpdateOverportMessage = stringResource(Res.string.cant_update_overport)
    val unknownErrorMessage = stringResource(Res.string.unknown_error)

    val openApkFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        coroutineScope.launch {
            if (it != null) {
                val stream = activity.contentResolver.openInputStream(it) ?: return@launch
                val fileName = it.lastPathSegment?.substringAfter(':') ?: "game"

                stream.use { s ->
                    try {
                        viewModel.import(activity.cacheDir, fileName, s)
                    } catch (_: CantDecodeApkException) {
                        viewModel.cancel()
                        snackbarHostState.showSnackbar(cantReadApkMessage)
                    } catch (_: CantUpdateOverportException) {
                        viewModel.cancel()
                        snackbarHostState.showSnackbar(cantUpdateOverportMessage)
                    } catch (_: Throwable) {
                        viewModel.cancel()
                        snackbarHostState.showSnackbar(unknownErrorMessage)
                    }
                }
            }
        }
    }

    val saveApkFile =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Constants.APK_MIMETYPE)) {
            coroutineScope.launch {
                if (it != null) {
                    val stream = activity.contentResolver.openOutputStream(it) ?: return@launch

                    stream.use { s ->
                        viewModel.export(s)
                        launch {
                            snackbarHostState.showSnackbar(apkFileExportedMessage)
                        }
                    }
                }
                viewModel.cancel()
            }
        }

    AppContent(
        viewModel = viewModel.apply {
            setAppIconConverter {
                FileInputStream(it).use { stream ->
                    BitmapFactory.decodeStream(stream).asImageBitmap()
                }
            }
        },
        snackbarHostState = snackbarHostState,
        onOpen = {
            coroutineScope.launch {
                openApkFile.launch(arrayOf(Constants.APK_MIMETYPE))
            }
        },
        onConfirm = {
            coroutineScope.launch {
                viewModel.process(it)
                saveApkFile.launch(viewModel.patchedName())
            }
        },
        onCancel = {
            coroutineScope.launch {
                viewModel.cancel()
            }
        },
    )
}

