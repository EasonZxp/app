package moe.crx.ovrport.patch

import com.android.apksig.ApkSigner
import com.reandroid.apk.*
import com.reandroid.archive.FileInputSource
import moe.crx.ovrport.utils.HttpUtil
import org.jf.baksmali.Baksmali
import org.jf.baksmali.BaksmaliOptions
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.VersionMap
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.dexbacked.raw.HeaderItem
import org.jf.smali.Smali
import org.jf.smali.SmaliOptions
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.zip.ZipInputStream

data class PatcherContext(
    val patcherDirectory: File,
    val dataDir: File = patcherDirectory.resolve("dec"),

    val fileName: String = "working",
    val inputApk: File = dataDir.resolve("$fileName.apk"),
    val outputApk: File = dataDir.resolve("$fileName.output.apk"),

    val workingDir: File = dataDir.resolve(fileName),
    var workingApk: ApkModule? = null,
)

fun PatcherContext.prepare(stream: InputStream) {
    val librariesDir = patcherDirectory.resolve(Constants.LIBRARIES_DIR)

    if (!librariesDir.exists()) {
        val downloadedArchive = runCatching { HttpUtil.download(Constants.LIBRARIES_URL) }.getOrElse {
            librariesDir.deleteRecursively()
            throw CantUpdateOverportException()
        }

        librariesDir.mkdirs()

        ZipInputStream(downloadedArchive.inputStream()).use { zip ->
            generateSequence { zip.nextEntry }
                .forEach {
                    if (it.isDirectory) {
                        librariesDir.resolve(it.name).mkdirs()
                    } else {
                        zip.copyTo(FileOutputStream(librariesDir.resolve(it.name)))
                    }
                }
        }
    }

    dataDir.mkdirs()

    workingDir.deleteRecursively()
    inputApk.delete()
    outputApk.delete()

    Files.copy(stream, inputApk.toPath(), REPLACE_EXISTING)

    val apk = runCatching { ApkModule.loadApkFile(inputApk) }.getOrElse {
        throw CantDecodeApkException()
    }

    val decoder = ApkModuleJsonDecoder(apk).apply {
        dexDecoder = DexDecoder { dexFile, dir ->
            val bytes = ByteArrayOutputStream().apply {
                dexFile.write(this)
            }.toByteArray()

            val version = HeaderItem.getVersion(bytes, 0)
            val api = VersionMap.mapDexVersionToApi(version)
            val opcodes = Opcodes.forApi(api)

            val classesName = dexFile.dexNumber.let { if (it != 0) "classes$it" else "classes" }
            val smaliDir = dir.resolve("smali").resolve(classesName)

            val options = BaksmaliOptions().apply {
                localsDirective = true
                sequentialLabels = true
                skipDuplicateLineNumbers = true
                apiLevel = api
            }
            Baksmali.disassembleDexFile(DexBackedDexFile(opcodes, bytes), smaliDir, 4, options)
        }
    }

    decoder.sanitizeFilePaths()
    decoder.decode(workingDir)

    workingApk = apk
}

fun PatcherContext.patch(patches: List<Patch>) {
    patches.forEach {
        it.executor(PatchExecutor(workingDir))
    }

    val encoder = ApkModuleJsonEncoder().apply {
        dexEncoder = DexEncoder { _, dir ->
            dir
                .resolve("smali")
                .listFiles()
                ?.filter { file -> file.isDirectory }
                ?.map {
                    dir.resolve(".cache").mkdirs()
                    val file = dir.resolve(".cache").resolve("${it.name}.dex")

                    val smaliOptions = SmaliOptions().apply {
                        outputDexFile = file.absolutePath
                        jobs = 4
                        apiLevel = 29
                    }

                    Smali.assemble(smaliOptions, it.absolutePath)
                    FileInputSource(file, file.name)
                }
                ?.toList()
        }
    }

    encoder.scanDirectory(workingDir)
    encoder.apkModule.run {
        zipEntryMap.autoSortApkFiles()
        writeApk(outputApk)
        close()
    }

    val signatureDir = patcherDirectory.resolve("signatures")
    signatureDir.mkdirs()
    val signature = (appPackage() ?: "default")
        .plus(".keystore")
        .let { signatureDir.resolve(it) }
        .let { SignatureStore.getSignature(it) }

    val tempFile = outputApk
        .parentFile
        ?.resolve(outputApk.name + ".temp")
        ?.let {
            outputApk.copyTo(it)
        }

    try {
        ApkSigner
            .Builder(listOf(signature))
            .setAlignFileSize(true)
            .setMinSdkVersion(29)
            .setInputApk(tempFile)
            .setOutputApk(outputApk)
            .build()
            .sign()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    tempFile?.delete()
}

fun PatcherContext.export(stream: OutputStream) {
    Files.copy(outputApk.toPath(), stream)
}

fun PatcherContext.cleanup() {
    workingDir.deleteRecursively()
    workingApk?.close()
    workingApk = null
    inputApk.delete()
    outputApk.delete()
}

fun PatcherContext.appPackage(): String? {
    return workingApk?.androidManifest?.packageName
}

fun PatcherContext.appVersion(): String? {
    return workingApk?.androidManifest?.versionName
        ?: workingApk?.androidManifest?.versionCode?.toString()
}

fun PatcherContext.appName(): String? {
    return workingApk?.androidManifest?.applicationLabelString
        ?: workingApk
            ?.tableBlock
            ?.getEntries(workingApk?.androidManifest?.applicationLabelReference ?: 0)
            ?.asSequence()
            ?.firstOrNull()
            ?.valueAsString
}

fun PatcherContext.appIcon(): File? {
    val iconId = workingApk?.androidManifest?.iconResourceId?.takeIf { it != 0 }
        ?: workingApk?.androidManifest?.roundIconResourceId?.takeIf { it != 0 }
        ?: return null

    val result = workingApk?.listResFiles(iconId, null)
        ?: return null

    // TODO layered adaptive icons support

    arrayOf(
        "-xxxhdpi",
        "-xxhdpi",
        "-xhdpi",
        "-hdpi",
        "-mdpi",
        "-ldpi",
        "-anydpi",
        ""
    ).forEach { dpi ->
        arrayOf(".webp", ".png").forEach { ext ->
            result.firstOrNull { it.filePath.contains(dpi) && it.filePath.endsWith(ext) }?.let {
                workingDir.resolve("root").resolve(it.filePath).run {
                    if (exists()) {
                        return this
                    }
                }
            }
        }
    }

    return null
}