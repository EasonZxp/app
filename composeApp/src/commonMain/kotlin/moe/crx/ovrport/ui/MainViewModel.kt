package moe.crx.ovrport.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import moe.crx.ovrport.model.GithubRelease
import moe.crx.ovrport.patch.*
import moe.crx.ovrport.utils.HttpUtil.download
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private val json = Json { ignoreUnknownKeys = true }

class MainViewModel : ViewModel() {

    private var patcherState by mutableStateOf<PatcherContext?>(null)
    var working by mutableStateOf(false)
        private set
    private var fileName: String = "game"
    private var imageConverter: ((File) -> ImageBitmap)? = null

    private suspend fun usePatcher(block: PatcherContext.() -> Unit) {
        working = true
        withContext(Dispatchers.IO) {
            patcherState?.block()
        }
        working = false
    }

    suspend fun import(dataDirectory: File, name: String, inputStream: InputStream) {
        fileName = name
        patcherState = PatcherContext(dataDirectory)
        usePatcher { prepare(inputStream) }
    }

    fun patchedName(): String {
        return fileName.substringBeforeLast('.').let { "$it.output.apk" }
    }

    suspend fun process(patches: List<Patch>) {
        usePatcher { patch(patches) }
    }

    suspend fun export(outputStream: OutputStream) {
        usePatcher { export(outputStream) }
    }

    suspend fun cancel() {
        usePatcher { cleanup() }
        fileName = "game"
        patcherState = null
    }

    fun currentAppName(): String? {
        return patcherState?.appName()
    }

    fun currentAppPackage(): String? {
        return patcherState?.appPackage()
    }

    fun currentAppVersion(): String? {
        return patcherState?.appVersion()
    }

    fun setAppIconConverter(converter: (File) -> ImageBitmap) {
        imageConverter = converter
    }

    fun currentAppIcon(): ImageBitmap? {
        return patcherState?.appIcon()?.let { file ->
            imageConverter?.let {
                it(file)
            }
        }
    }

    fun isApkLoaded(): Boolean {
        return patcherState?.workingApk != null
    }

    fun versionToUpdate(): GithubRelease? {
        return runCatching {
            json
                .decodeFromString<List<GithubRelease>>(String(download(Constants.RELEASES_URL)))
                .reduceOrNull { left, right -> if (left.publishedAt > right.publishedAt) left else right }
        }.getOrNull()
    }
}