package io.github.nitsuya.aa.display.xposed

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AndroidAuoHook
import io.github.nitsuya.aa.display.xposed.hook.AndroidHook
import io.github.nitsuya.aa.display.xposed.hook.BaseHook
import io.github.nitsuya.aa.display.xposed.hook.LauncherHook
import io.github.nitsuya.aa.display.xposed.hook.OtherHook

class XposedInit : IXposedHookZygoteInit, IXposedHookLoadPackage{
    companion object {
        const val TAG = "AADisplay_XposedInit"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        when{
            packageName == "android" -> arrayOf(AndroidHook)
            packageName == "com.google.android.projection.gearhead" -> arrayOf(AndroidAuoHook)
            packageName == BuildConfig.APPLICATION_ID || lpparam.appInfo == null || lpparam.appInfo.uid == 1000 -> null
            //packageName == AADisplayConfig.LauncherPackage.get(CoreManagerService.config) -> arrayOf(LauncherHook)
            else -> arrayOf(OtherHook)
        }?.also {
            initHooks(lpparam, *it)
        }
    }

    private fun initHooks(lpparam: XC_LoadPackage.LoadPackageParam, vararg hook: BaseHook) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init(lpparam)
                it.isInit = true
                log(TAG, "Inited hook: ${it.tagName}")
            }.logexIfThrow("Failed init hook: ${it.tagName}")
        }
    }
}


