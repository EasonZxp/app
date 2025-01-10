package moe.crx.ovrport

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import moe.crx.ovrport.ui.AppContent
import org.jetbrains.skia.Image
import java.io.FileInputStream
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import moe.crx.ovrport.patch.CantDecodeApkException
import moe.crx.ovrport.patch.CantUpdateOverportException
import moe.crx.ovrport.ui.MainViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import overportapp.composeapp.generated.resources.*
import overportapp.composeapp.generated.resources.Res
import overportapp.composeapp.generated.resources.apk_file_exported
import overportapp.composeapp.generated.resources.cant_read_apk
import overportapp.composeapp.generated.resources.window_icon
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "overport",
        icon = painterResource(Res.drawable.window_icon),
    ) {
        val viewModel: MainViewModel = viewModel { MainViewModel() }
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        // TODO Deal with this copy-paste
        val cantReadApkMessage = stringResource(Res.string.cant_read_apk)
        val apkFileExportedMessage = stringResource(Res.string.apk_file_exported)
        val cantUpdateOverportMessage = stringResource(Res.string.cant_update_overport)
        val unknownErrorMessage = stringResource(Res.string.unknown_error)

        suspend fun openFile(file: File) {
            if (!file.exists() || file.extension != "apk") {
                return
            }

            val dataDir = File(System.getProperty("user.home")).resolve("overport/cache")
            dataDir.mkdirs()

            FileInputStream(file).use { s ->
                try {
                    viewModel.import(dataDir, file.name, s)
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

        val dragAndDropTarget = remember {
            object: DragAndDropTarget {
                override fun onDrop(event: DragAndDropEvent): Boolean {
                    val value: List<File>? = event.awtTransferable
                        .takeIf { it.isDataFlavorSupported(DataFlavor.javaFileListFlavor) }
                        ?.getTransferData(DataFlavor.javaFileListFlavor)
                        ?.let { it as? List<*> }
                        ?.filterIsInstance<File>()

                    if (value?.size == 1) {
                        coroutineScope.launch {
                            openFile(value.first())
                        }

                        return true
                    }

                    return false
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().dragAndDropTarget(
            shouldStartDragAndDrop = { !viewModel.working && !viewModel.isApkLoaded() },
            target = dragAndDropTarget
        )) {
            AppContent(
                viewModel = viewModel.apply {
                    setAppIconConverter {
                        FileInputStream(it).use { stream ->
                            Image.makeFromEncoded(stream.readAllBytes()).toComposeImageBitmap()
                        }
                    }
                },
                snackbarHostState = snackbarHostState,
                onOpen = {
                    val fileDialog = FileDialog(Frame(), "Select a file", FileDialog.LOAD)
                    fileDialog.isVisible = true
                    val file = fileDialog.file?.let { File(fileDialog.directory).resolve(it) }

                    coroutineScope.launch {
                        if (file != null) {
                            openFile(file)
                        }
                    }
                },
                onConfirm = {
                    val fileDialog = FileDialog(Frame(), "Select a file", FileDialog.SAVE)

                    coroutineScope.run {
                        fileDialog.file = viewModel.patchedName()
                    }

                    fileDialog.isVisible = true
                    val file = fileDialog.file?.let { File(fileDialog.directory).resolve(it) }

                    coroutineScope.launch {
                        if (file != null) {
                            viewModel.process(it)
                            FileOutputStream(file).use { s ->
                                viewModel.export(s)
                                launch {
                                    snackbarHostState.showSnackbar(apkFileExportedMessage)
                                }
                            }
                            viewModel.cancel()
                        }
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        viewModel.cancel()
                    }
                },
            )
        }
    }
}