package moe.crx.ovrport

import android.os.Build

actual fun getPlatform(): Platform = AndroidPlatform("Android ${Build.VERSION.SDK_INT}")