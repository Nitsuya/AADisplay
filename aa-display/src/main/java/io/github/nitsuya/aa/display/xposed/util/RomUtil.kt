package io.github.nitsuya.aa.display.xposed.util

import android.os.Build
import android.os.SystemProperties

object RomUtil {
    private const val KEY_VERSION_MIUI = "ro.miui.ui.version.name"
    private const val KEY_VERSION_EMUI = "ro.build.version.emui"
    private const val KEY_VERSION_OPPO = "ro.build.version.opporom"
    private const val KEY_VERSION_VIVO = "ro.vivo.os.version"
    private val KEY_VERSION = arrayListOf<String>(
         KEY_VERSION_MIUI
        ,KEY_VERSION_EMUI
        ,KEY_VERSION_OPPO
        ,KEY_VERSION_VIVO
    )
    private var mPropKey: String? = null
    fun isMiui() = check(KEY_VERSION_MIUI)
    fun isEmui() = check(KEY_VERSION_EMUI)
    fun isVivo() = check(KEY_VERSION_VIVO)
    fun isOppo() = check(KEY_VERSION_OPPO)
    fun check(propKey: String): Boolean {
        if (mPropKey == null) {
            mPropKey = KEY_VERSION.find { SystemProperties.get(it).isNotBlank() } ?: Build.UNKNOWN
        }
        return mPropKey == propKey
    }
}