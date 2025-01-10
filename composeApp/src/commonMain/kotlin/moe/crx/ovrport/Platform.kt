package moe.crx.ovrport

interface Platform {
    val name: String
}

class JVMPlatform(override val name: String): Platform

class AndroidPlatform(override val name: String) : Platform

expect fun getPlatform(): Platform

// TODO This should be controlled by Gradle.
fun getCurrentVersion(): String = "1.0.0"