package io.github.nitsuya.aa.display.util

import android.content.SharedPreferences
import com.github.kyuubiran.ezxhelper.utils.tryOrNull
import java.io.StringReader
import java.util.*
import kotlin.streams.toList

sealed class AADisplayConfig<T>(val key: String) {
    companion object {
        const val ConfigName = "aadisplay_config"
    }

    abstract fun get(config: SharedPreferences?): T

    object LauncherPackage: StringConfig("LauncherPackage", "com.wow.carlauncher.mini")
    object AutoOpen: BooleanConfig("AutoOpen", true)
    object VirtualDisplayDpi: IntConfig("VirtualDisplayDpi", 0)
    object AndroidAutoDpi: IntConfig("AndroidAutoDpi", 0)
    object DelayDestroyTime: IntConfig("DelayDestroyTime", 180)
    object ScreenOffReplaceLockScreen: BooleanConfig("ScreenOffReplaceLockScreen", false)
    object CloseLauncherDashboard: BooleanConfig("CloseLauncherDashboard", true)
    object ForceRightAngle: BooleanConfig("ForceRightAngle", true)
    object DisplayImePolicy: IntConfig("DisplayImePolicy", 1) //WindowManager.DISPLAY_IME_POLICY_LOCAL:0, WindowManager.DISPLAY_IME_POLICY_FALLBACK_DISPLAY:1
    object VoiceAssistShell: StringConfig("VoiceAssistShell", null)
    object CreateVirtualDisplayBefore: ArrayStringConfig("CreateVirtualDisplayBefore")
    object DestroyVirtualDisplayAfter: ArrayStringConfig("DestroyVirtualDisplayAfter")
    object ComGoogleAndroidGmsCarProps: PropertiesConfig("ComGoogleAndroidGmsCarProps")
    object ComGoogleAndroidProjectionGearheadProps: PropertiesConfig("ComGoogleAndroidProjectionGearheadProps")
    object LauncherModeProps: PropertiesConfig("LauncherModeProps")


    abstract class StringConfig(key: String, private val defValue: String? = null): AADisplayConfig<String?>(key){
        override fun get(config: SharedPreferences?): String? = config?.getString(key, defValue)?.trim()?.let {
            it.ifBlank { defValue }
        } ?: null
    }
    abstract class BooleanConfig(key: String, private val defValue: Boolean = false): AADisplayConfig<Boolean>(key){
        override fun get(config: SharedPreferences?): Boolean = config?.getBoolean(key, defValue) ?: defValue
    }
    abstract class IntConfig(key: String, private val defValue: Int = 0): AADisplayConfig<Int>(key){
        private val defValueStr = defValue.toString()
        override fun get(config: SharedPreferences?): Int = tryOrNull { config?.getString(key, defValueStr)?.toInt() } ?: defValue
    }
    abstract class ArrayStringConfig(key: String): AADisplayConfig<Array<String>>(key){
        override fun get(config: SharedPreferences?): Array<String>{
            return config?.getString(key, null)?.let {
                it.split("\n").stream().map(String::trim).filter(String::isNotBlank).toList().toTypedArray()
            } ?: emptyArray()
        }
    }
    abstract class PropertiesConfig(key: String): AADisplayConfig<Properties?>(key){
        override fun get(config: SharedPreferences?): Properties?{
            return config?.getString(key, null)?.let {
                Properties().apply {
                    load(StringReader(it))
                }
            } ?: null
        }
    }

}
