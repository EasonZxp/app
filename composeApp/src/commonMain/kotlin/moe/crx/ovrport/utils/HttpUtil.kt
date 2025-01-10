package moe.crx.ovrport.utils

import java.net.HttpURLConnection
import java.net.URI

object HttpUtil {
    fun download(url: String): ByteArray {
        val uri = URI(url).toURL().openConnection() as HttpURLConnection

        val bytes = uri.errorStream?.use {
            it.readBytes()
        } ?: uri.inputStream.use {
            it.readBytes()
        }

        uri.disconnect()
        return bytes
    }
}