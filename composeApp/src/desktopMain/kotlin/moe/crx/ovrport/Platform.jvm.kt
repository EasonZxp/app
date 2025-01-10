package moe.crx.ovrport

actual fun getPlatform(): Platform = JVMPlatform("Java ${System.getProperty("java.version")}")