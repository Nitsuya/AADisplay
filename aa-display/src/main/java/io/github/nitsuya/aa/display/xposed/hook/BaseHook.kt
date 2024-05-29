package io.github.nitsuya.aa.display.xposed.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

abstract class BaseHook {
    var isInit: Boolean = false
    abstract val tagName: String
    abstract fun init(lpparam: XC_LoadPackage.LoadPackageParam)
}
inline fun XC_MethodHook.MethodHookParam.abortMethod() {
    this.result = null
}