package io.github.nitsuya.aa.display.xposed.hook

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.IPackageManager
import android.content.res.Configuration
import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.IsSystemEnv
import io.github.nitsuya.aa.display.xposed.BridgeService
import io.github.nitsuya.aa.display.xposed.CoreManagerService
import io.github.nitsuya.aa.display.xposed.log
import io.github.qauxv.util.Initiator

object AndroidHook : BaseHook() {
    override val tagName: String = "AAD_AndroidHook"
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        Initiator.init(lpparam.classLoader)
        log(tagName, "xposed init")
        var serviceManagerHook: XC_MethodHook.Unhook? = null
        serviceManagerHook = findMethod("android.os.ServiceManager") {
            name == "addService"
        }.hookBefore { param ->
            if (param.args[0] == "package") {
                serviceManagerHook?.unhook()
                val pms = param.args[1] as IPackageManager
                log(tagName, "Got pms: $pms")
                runCatching {
                    BridgeService.register(pms)
                    log(tagName, "Bridge service injected")
                }.onFailure {
                    log(tagName, "System service crashed", it)
                }
            }
        }

        var activityManagerServiceConstructorHook: List<XC_MethodHook.Unhook> = emptyList()
        activityManagerServiceConstructorHook = findAllConstructors("com.android.server.am.ActivityManagerService") {
            parameterTypes[0] == Context::class.java
        }.hookAfter {
            activityManagerServiceConstructorHook.forEach { hook -> hook.unhook() }
            CoreManagerService.systemContext = it.thisObject.getObjectAs("mUiContext")
            log(tagName, "get systemUiContext")
        }.also {
            if (it.isEmpty())
                log(tagName, "no constructor with parameterTypes[0] == Context found")
        }

        var activityManagerServiceSystemReadyHook: XC_MethodHook.Unhook? = null
        activityManagerServiceSystemReadyHook = findMethod("com.android.server.am.ActivityManagerService") {
            name == "systemReady"
        }.hookAfter {
            activityManagerServiceSystemReadyHook?.unhook()
            CoreManagerService.systemReady()
            log(tagName, "system ready")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//10+
            var className = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)  //12+
                "com.android.server.wm.ActivityTaskSupervisor"
            else  //10+
                "com.android.server.wm.ActivityStackSupervisor"
            findMethod(className){
                name == "isCallerAllowedToLaunchOnDisplay"
                && parameterCount == 4
                && parameterTypes[0] == Int::class.javaPrimitiveType //callingPid
                && parameterTypes[1] == Int::class.javaPrimitiveType //callingUid
                && parameterTypes[2] == Int::class.javaPrimitiveType //launchDisplayId
                && parameterTypes[3] == ActivityInfo::class.java
            }.hookAfter { param ->
                if((param.result as Boolean).not() && param.args[2] == CoreManagerService.getDisplayId()){
                    param.result = true
                    log(tagName,"hook isCallerAllowedToLaunchOnDisplay success")
                }
            }
        }

    }

    object Power {
        private val powerPress by lazy {
            if(!IsSystemEnv) return@lazy null
            try{
                findMethod("com.android.server.policy.PhoneWindowManager") {
                    name == "powerPress"
                            && parameterCount == 3
                            && parameterTypes[0] == Long::class.javaPrimitiveType //eventTime
                            && parameterTypes[1] == Int::class.javaPrimitiveType //count
                            && parameterTypes[2] == Boolean::class.javaPrimitiveType //beganFromNonInteractive
                }
            } catch (e: Throwable){
                log(tagName,  "Power PhoneWindowManager.powerPress", e)
                null
            }
        }
        private var hookPower : XC_MethodHook.Unhook? = null
        fun hook(){
            unHook()
            hookPower = powerPress?.hookBefore {
                if (!(it.args[2] as Boolean)) {
                    CoreApi.toggleDisplayPower()
                    it.abortMethod()
                } else {
                    CoreApi.displayPower(true)
                }
            }
        }
        fun unHook(){
            hookPower?.unhook()
            hookPower = null
        }
    }
    object FuckAppUseApplicationContext {
        private val appInitUseDisplay: HashMap<String, Int> = hashMapOf()
        private val activityTaskManagerService_startProcessAsync by lazy {
            if(!IsSystemEnv) return@lazy null
            try{
                findMethod("com.android.server.wm.ActivityTaskManagerService"){
                    name == "startProcessAsync"
                }
            } catch (e: Throwable){
                log(tagName,  "FuckAppUseAppContext ActivityTaskManagerService.startProcessAsync method", e)
                null
            }
        }
        private val applicationThread_bindApplication by lazy {
            if(!IsSystemEnv) return@lazy null
            try{
                findMethod("android.app.IApplicationThread\$Stub\$Proxy"){
                    name == "bindApplication"
                }
            } catch (e: Throwable){
                log(tagName,  "FuckAppUseAppContext IApplicationThread.bindApplication method", e)
                null
            }
        }

        private var activityTaskManagerService_startProcessAsync_hook : XC_MethodHook.Unhook? = null
        private var applicationThread_bindApplication_hook : XC_MethodHook.Unhook? = null
        fun hook(){
            unHook()
            activityTaskManagerService_startProcessAsync_hook  = activityTaskManagerService_startProcessAsync?.hookBefore { param ->
                try {
                    val activityRecord = param.args[0]
                    val displayId = activityRecord.invokeMethod("getDisplayId") as Int
                    val packageName = activityRecord.getObject("packageName") as String
                    if(displayId == 0){
                        if(appInitUseDisplay.containsKey(packageName)){
                            appInitUseDisplay.remove(packageName)
                        }
                        return@hookBefore
                    }
                    appInitUseDisplay[packageName] = displayId
                } catch (e: Exception) {
                    log(tagName, "activityTaskManagerService_startProcessAsync Hook Exception", e)
                }
            }
            applicationThread_bindApplication_hook = applicationThread_bindApplication?.hookBefore { param ->
                try {
                    val configuration = param.args[15]
                    if(configuration !is Configuration){
                        return@hookBefore
                    }
                    val packageName = (param.args[0] as String).run {
                        this.substringBeforeLast(":")
                    }
                    if(appInitUseDisplay.containsKey(packageName)){
                        val densityDpi = CoreManagerService.getDensityDpi()
                        if(densityDpi != 0){
                            configuration.densityDpi = densityDpi
                        }
                    }
                } catch (e: Exception) {
                    log(tagName, "activityTaskManagerService_startProcessAsync Hook Exception", e)
                }
            }
        }

        fun unHook(){
            appInitUseDisplay.clear()
            activityTaskManagerService_startProcessAsync_hook?.apply { unhook() }
            activityTaskManagerService_startProcessAsync_hook = null

            applicationThread_bindApplication_hook?.apply { unhook() }
            applicationThread_bindApplication_hook = null
        }

    }
}