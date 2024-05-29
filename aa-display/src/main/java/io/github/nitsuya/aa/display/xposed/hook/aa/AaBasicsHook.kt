package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findConstructor
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AABroadcastConst
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.hook.abortMethod
import io.github.nitsuya.aa.display.xposed.log
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.atomic.AtomicBoolean

object AaBasicsHook: AaHook() {
    override val tagName: String = "AAD_AaBasicsHook"

    override fun isSupportProcess(processName: String): Boolean {
//        return processMain == processName || processProjection == processName || processCar == processName
        return true
    }

    override fun hook(config: SharedPreferences, lpparam: XC_LoadPackage.LoadPackageParam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {//11+
            try {
                findMethod(InstallSourceInfo::class.java) {
                    name == "getInitiatingPackageName"
                }.hookAfter { param ->
                    param.result = "com.android.vending"
                }
            } catch (e: Throwable) {
                log(tagName, "InstallSourceInfo.getInitiatingPackageName", e)
            }
        } else {
            try{
                findMethod(PackageManager::class.java) {
                    name == "getInstallerPackageName"
                }.hookAfter { param ->
                    param.result = "com.android.vending"
                }
            } catch (e: Throwable) {
                log(tagName, "PackageManager.getInstallerPackageName", e)
            }
        }


    }
}
