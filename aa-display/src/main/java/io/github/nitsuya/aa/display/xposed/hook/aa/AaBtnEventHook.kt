package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.util.ArraySet
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AABroadcastConst
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.hook.abortMethod
import io.github.nitsuya.aa.display.xposed.log
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.atomic.AtomicBoolean

object AaBtnEventHook: AaHook() {
    override val tagName: String = "AAD_AaBtnEventHook"

    override fun isSupportProcess(processName: String): Boolean {
        return processProjection == processName
    }

    override fun hook(config: SharedPreferences, lpparam: XC_LoadPackage.LoadPackageParam) {
        val enableDefVoiceAssist = AADisplayConfig.VoiceAssistShell.get(config).isNullOrBlank()
        val isLoadHookReceive = Collections.synchronizedSet(HashSet<String>())
        val isDisposeHookReceive = Collections.synchronizedMap(HashMap<String, Any>())
        val longPressStatusHookReceive = Collections.synchronizedMap(HashMap<Int, AtomicBoolean>())
        findAllMethods(ContextWrapper::class.java) {
            name == "registerReceiver"
        }.hookBefore { param ->
            if (param.args[0] == null) return@hookBefore
            val intentFilter = param.args[1] as IntentFilter
            val mActions = intentFilter.getObject("mActions") as Collection<String>
            log(tagName,"registerReceiver->${param.args[0].javaClass.name}:${mActions.joinToString()}")
            if (mActions.size != 1) {
                return@hookBefore
            }
            val needAction = mActions.first()
            if (!(needAction == "android.intent.action.MEDIA_BUTTON" || needAction == "android.intent.action.projected.KEY_EVENT")) {
                return@hookBefore
            }
            val clazz = param.args[0].javaClass
            val clazzName = clazz.name
            if (!isLoadHookReceive.add(clazzName)) {
                return@hookBefore
            }
            isDisposeHookReceive.putIfAbsent(needAction, param.args[0])
//                log(tagName,"registerReceiver->$clazzName:${mActions.joinToString()}")
            try {
                findMethod(clazz, findSuper = true) {
                    name == "onReceive"
                    && parameterTypes[0] == Context::class.java
                    && parameterTypes[1] == Intent::class.java
                }.hookBefore { param ->
                    val intent = param.args[1] as Intent
                    val eventAction = intent.action
                    val keyEvent = IntentCompat.getParcelableExtra(intent,"android.intent.extra.KEY_EVENT",  KeyEvent::class.java) ?: return@hookBefore
                    val keyCode = keyEvent.keyCode
                    if(enableDefVoiceAssist && keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return@hookBefore
                    }
                    param.abortMethod()
                    if (needAction != eventAction || isDisposeHookReceive[eventAction] != param.thisObject) {
                        return@hookBefore
                    }
                    log(tagName, "BroadcastReceiver onReceive $clazzName, action:$eventAction, keyCode:$keyCode, keyEvent:$keyEvent")
                    val longPress = longPressStatusHookReceive.computeIfAbsent(keyCode) { AtomicBoolean(false) }
                    if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                        if (longPress.get()) {
                            longPress.set(false)
                            return@hookBefore
                        }
                        log(tagName, "send click $keyCode")
                        (param.args[0] as Context).sendBroadcast(Intent().apply {
                            action = AABroadcastConst.ACTION_STEERING_WHEEL_CONTROL
                            putExtra(AABroadcastConst.EXTRA_ACTION, keyCode)
                        })
                    } else if (keyEvent.isLongPress) {
                        longPress.set(true)
                        log(tagName, "send long click $keyCode")
                        (param.args[0] as Context).sendBroadcast(Intent().apply {
                            action = AABroadcastConst.ACTION_STEERING_WHEEL_CONTROL
                            putExtra(AABroadcastConst.EXTRA_ACTION, keyCode)
                            putExtra(AABroadcastConst.EXTRA_TYPE, 1)
                        })
                    } else {
                        if (longPress.get()) {
                            return@hookBefore
                        }
                        keyEvent.startTracking();
                    }
                }
            } catch (e: Throwable) {
                log(tagName, "btnEvent registerReceiver onReceive $clazzName", e)
            }
        }
    }
}
