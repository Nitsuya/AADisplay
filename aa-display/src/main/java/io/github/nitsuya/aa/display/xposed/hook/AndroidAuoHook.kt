package io.github.nitsuya.aa.display.xposed.hook

import android.app.Application
import android.app.Instrumentation
import android.content.SharedPreferences
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.aa.AaBasicsHook
import io.github.nitsuya.aa.display.xposed.hook.aa.AaBtnEventHook
import io.github.nitsuya.aa.display.xposed.hook.aa.AaDpiHook
import io.github.nitsuya.aa.display.xposed.hook.aa.AaPropsHook
import io.github.nitsuya.aa.display.xposed.hook.aa.AaSignatureHook
import io.github.nitsuya.aa.display.xposed.hook.aa.AaUiHook
import io.github.nitsuya.aa.display.xposed.log
import org.luckypray.dexkit.DexKitBridge
import kotlin.system.measureTimeMillis


abstract class AaHook {
    companion object {
        const val processMain =       "com.google.android.projection.gearhead"
        const val processProjection = "com.google.android.projection.gearhead:projection"
        const val processCar =        "com.google.android.projection.gearhead:car"
    }
    abstract val tagName: String
    abstract fun isSupportProcess(processName: String) : Boolean
    open fun loadDexClass(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {}
    abstract fun hook(config: SharedPreferences, lpparam: XC_LoadPackage.LoadPackageParam)
}

object AndroidAuoHook : BaseHook() {
    override val tagName: String = "AAD_AndroidAuoHook"
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        val processName = lpparam.processName
        val hooks = listOf(AaBasicsHook, AaSignatureHook, AaDpiHook, AaBtnEventHook, AaUiHook, AaPropsHook).filter { i -> i.isSupportProcess(processName) }
        if(hooks.isEmpty()) return

        val configPreferences = XSharedPreferences(BuildConfig.APPLICATION_ID, AADisplayConfig.ConfigName)
        if(!configPreferences.file.canRead()){
            log(tagName,"load configPreferences fail")
            return
        }

        var onCreateApplication: XC_MethodHook.Unhook? = null
        onCreateApplication = findMethod(Instrumentation::class.java) {
            name == "callApplicationOnCreate"
            && parameterCount == 1
            && parameterTypes[0] == Application::class.java
        }.hookBefore {
            onCreateApplication?.unhook()
            EzXHelperInit.initAppContext()
            System.loadLibrary("dexkit")
            DexKitBridge.create(lpparam.appInfo.sourceDir).use { bridge ->
                if(bridge == null){
                    log(tagName,"DexKitBridge.create() failed")
                    return@hookBefore
                }
                val measureTimeMillis = measureTimeMillis {
                    hooks.forEach { h ->
                        h.loadDexClass(bridge, lpparam)
                    }
                }
                log(tagName,"${lpparam.processName} load class measure ${measureTimeMillis}ms")
            }
            hooks.forEach { h ->
                h.hook(configPreferences, lpparam)
            }
        }
    }
}


