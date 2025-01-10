package moe.crx.ovrport.patch

import com.reandroid.json.JSONObject
import java.io.File

data class Patch(
    val name: String = "",
    val desc: String? = null,
    val executor: PatchExecutor.() -> Unit
)

class FileTransformer(val file: File) {
    fun replace(input: String, output: String) {
        file.writeText(Regex(input, RegexOption.DOT_MATCHES_ALL).replace(file.readText(), output))
    }

    fun find(input: String): Boolean {
        return Regex(input, RegexOption.DOT_MATCHES_ALL).find(file.readText()) != null
    }

    fun append(line: String) {
        file.appendText(line)
    }

    fun readJson(): JSONObject? {
        return runCatching { JSONObject(file.readText()) }.getOrNull()
    }

    fun writeJson(json: JSONObject) {
        file.writeText(json.toString())
    }

    fun replaceHex(input: String, output: String) {
        val bytes = file.readBytes()
        val inputBytes = input.split(' ')

        for (i in 0..<bytes.size-inputBytes.size+1) {
            var matches = true

            for (j in i..<bytes.size) {
                if (inputBytes[j] != "??" && bytes[j] != inputBytes[j].toByte(16)) {
                    matches = false
                    break
                }
            }

            if (matches) {
                val replaced = bytes.slice(0..<i) +
                        output.split(' ').map { it.toByte(16) } +
                        bytes.slice(i+inputBytes.size..<bytes.size)
                file.writeBytes(replaced.toByteArray())
                return
            }
        }
    }
}

class PatchExecutor(private val workingDir: File) {
    fun selectWorkspace(block: FileTransformer.() -> Unit) {
        workingDir.run {
            if (exists()) {
                FileTransformer(this).block()
            }
        }
    }

    fun selectFile(fileName: String, block: FileTransformer.() -> Unit) {
        workingDir.resolve(fileName).run {
            if (exists()) {
                FileTransformer(this).block()
            }
        }
    }

    fun selectResources(block: FileTransformer.() -> Unit) {
        selectFile("resources/resources.arsc.json", block)
    }

    fun selectManifest(block: FileTransformer.() -> Unit) {
        selectFile("AndroidManifest.xml.json", block)
    }

    fun selectLibrary(fileName: String, block: FileTransformer.() -> Unit) {
        listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64").forEach { arch ->
            val archDirectory = workingDir.resolve("root/lib/$arch")
            if (archDirectory.exists()) {
                archDirectory.resolve(fileName).run {
                    if (exists()) {
                        FileTransformer(this).block()
                    }
                }
            }
        }
    }

    fun createLibrary(fileName: String, block: FileTransformer.() -> Unit) {
        listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64").forEach { arch ->
            val archDirectory = workingDir.resolve("root/lib/$arch")
            if (archDirectory.exists()) {
                archDirectory.resolve(fileName).run {
                    createNewFile()
                    FileTransformer(this).block()
                }
            }
        }
    }

    fun selectSmali(vararg fileNames: String, block: FileTransformer.() -> Unit) {
        workingDir.resolve("smali").listFiles()?.forEach { dir ->
            if (fileNames.isEmpty()) {
                dir.walk().forEach { file ->
                    if (file.isFile && file.name.endsWith(".smali")) {
                        FileTransformer(file).block()
                    }
                }
            } else {
                fileNames.forEach { fileName ->
                    dir.resolve("${fileName}.1.smali").run {
                        if (exists()) {
                            FileTransformer(this).block()
                        }
                    }
                    dir.resolve("${fileName}.smali").run {
                        if (exists()) {
                            FileTransformer(this).block()
                        }
                    }
                }
            }
        }
    }

    fun createSmali(fileName: String, block: FileTransformer.() -> Unit) {
        workingDir.resolve("smali").listFiles()?.forEach { dir ->
            dir.resolve("${fileName}.1.smali").run {
                if (exists()) {
                    FileTransformer(this).block()
                    return
                }
            }
            dir.resolve("${fileName}.smali").run {
                if (exists()) {
                    FileTransformer(this).block()
                    return
                }
            }
        }

        workingDir
            .resolve("smali")
            .listFiles()
            ?.reduceOrNull { left, right ->
                val leftValue = left.name.substringAfter("classes").toIntOrNull() ?: 0
                val rightValue = right.name.substringAfter("classes").toIntOrNull() ?: 0
                if (rightValue > leftValue) right else left
            }
            .let {
                it ?: workingDir.resolve("smali/classes").apply { mkdirs() }
            }
            .resolve("${fileName}.smali")
            .apply { createNewFile() }
            .run {
                if (exists()) {
                    FileTransformer(this).block()
                }
            }
    }
}
