package io.github.nitsuya.aa.display.xposed.hook

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.CoreManagerService
import io.github.nitsuya.aa.display.xposed.log
import io.github.nitsuya.template.bases.runMain
import io.github.qauxv.util.Initiator


object LauncherHook : BaseHook() {
    override val tagName: String = "AAD_LauncherHook"
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        Initiator.init(lpparam.classLoader)
        log(tagName, "xposed init")

        val launcherModeProps = AADisplayConfig.LauncherModeProps.get(CoreManagerService.config)

        findMethod(Instrumentation::class.java) {
            name == "execStartActivity"
            && parameterCount == 7
            && parameterTypes[0] == Context::class.java           //who
            && parameterTypes[1] == IBinder::class.java           //contextThread
            && parameterTypes[2] == IBinder::class.java           //token
            && parameterTypes[3] == Activity::class.java          //target
            && parameterTypes[4] == Intent::class.java            //intent
            && parameterTypes[5] == Int::class.javaPrimitiveType  //requestCode
            && parameterTypes[6] == Bundle::class.java            //options
        }.hookBefore { param ->
            val context = param.args[0] as Context
            val intent = param.args[4] as Intent
            val component = intent.component ?: return@hookBefore
            val modeName = launcherModeProps?.get(component.packageName) ?: return@hookBefore
            val mode = Mode.getMode(modeName as String)
            if(mode == null){
                runMain {
                    Toast.makeText(context, "${modeName}:未知模式", Toast.LENGTH_LONG).show()
                }
                return@hookBefore
            }
            val newIntent = mode.buildIntent(context, intent)
            if(newIntent == null){
                runMain {
                    Toast.makeText(context, "${modeName}:${component.packageName}未匹配到模式", Toast.LENGTH_LONG).show()
                }
                return@hookBefore
            }
            log(tagName, "${modeName}:${component.packageName}")
            param.args[4] = newIntent
        }
    }
    abstract class Mode {
        abstract fun buildIntent(context: Context, intent: Intent) : Intent?
        companion object {
            fun getMode(modeName: String) : Mode? = when(modeName){
                "ucar" -> UcarMode
                "samsung" -> SamsungMode
                "vivo" -> VivoMode
                "huawei" -> HuaweiMode
                else -> null
            }
        }
    }
    object UcarMode : Mode(){
        override fun buildIntent(context: Context, intent: Intent): Intent? {
            val component = intent.component ?: return null
            val queryIntent = Intent("com.ucar.intent.action.UCAR", null).apply { setPackage(component.packageName) }
            val resolveInfo = context.packageManager.queryIntentActivities(queryIntent, 0).firstOrNull() ?: return null
            val launchIntent = Intent("com.ucar.intent.action.UCAR")
            launchIntent.putExtra("isUcarMode", true)
            launchIntent.component = ComponentName(resolveInfo.activityInfo.packageName,resolveInfo.activityInfo.name)
            return launchIntent;
        }
    }
    object SamsungMode : Mode(){
        override fun buildIntent(context: Context, intent: Intent): Intent? {
            val component = intent.component ?: return null
            val queryIntent = Intent("samsung.intent.action.carlink.kit", null).apply { setPackage(component.packageName) }
            val resolveInfo = context.packageManager.queryIntentActivities(queryIntent, 0).firstOrNull() ?: return null
            val launchIntent = Intent("samsung.intent.action.carlink.kit")
            launchIntent.component = ComponentName(resolveInfo.activityInfo.packageName,resolveInfo.activityInfo.name)
            return launchIntent
        }
    }
    object VivoMode : Mode(){
        override fun buildIntent(context: Context, intent: Intent): Intent? {
            val component = intent.component ?: return null
            val queryIntent = Intent("vivo.intent.action.carlink.kit", null).apply { setPackage(component.packageName) }
            val resolveInfo = context.packageManager.queryIntentActivities(queryIntent, 0).firstOrNull() ?: return null
            val launchIntent = Intent("vivo.intent.action.carlink.kit")
            launchIntent.putExtra("isVivoCarLinkMode", true)
            launchIntent.component = ComponentName(resolveInfo.activityInfo.packageName,resolveInfo.activityInfo.name)
            return launchIntent
        }
    }
    object HuaweiMode : Mode(){
        override fun buildIntent(context: Context, intent: Intent): Intent? {
            val component = intent.component ?: return null
            if (component.packageName == "com.kugou.android") {
                val launchIntent = Intent()
                launchIntent.component = ComponentName("com.kugou.android","com.kugou.android.app.hicar.HiCarSplashActivity")
                launchIntent.putExtra("isHiCarMode", true)
                return launchIntent
            }
            return null
        }
    }

}