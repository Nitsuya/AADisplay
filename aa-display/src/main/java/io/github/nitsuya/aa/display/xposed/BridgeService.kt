package io.github.nitsuya.aa.display.xposed

import android.content.pm.IPackageManager
import android.os.Binder
import android.os.Build
import android.os.Parcel
import android.os.Process
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import io.github.nitsuya.aa.display.BuildConfig

object BridgeService {

    private const val TAG = "AADisplay_Bridge"

    private var appUid = 0

    fun register(pms: IPackageManager) {
        log(TAG, "Initialize AADisplayService - Version ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
        appUid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pms.getPackageUid(BuildConfig.APPLICATION_ID, 0L, 0)
        } else {
            pms.getPackageUid(BuildConfig.APPLICATION_ID, 0, 0)
        }
//        val appPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            pms.getPackageInfo(BuildConfig.APPLICATION_ID, 0L, 0)
//        } else {
//            pms.getPackageInfo(BuildConfig.APPLICATION_ID, 0, 0)
//        }
//        if (!Utils.verifyAppSignature(appPackage.applicationInfo.sourceDir)) {
//            logE(TAG, "Fatal: App signature mismatch")
//            return
//        }
        log(TAG, "Client uid: $appUid")
        log(TAG, "Service uid: ${Process.myUid()}")
        log(TAG, "Initialize service proxy")
        pms.javaClass.findMethod(true) {
            name == "onTransact"
        }.hookBefore { param ->
            val code = param.args[0] as Int
            val data = param.args[1] as Parcel
            val reply = param.args[2] as Parcel?
            if (myTransact(code, data, reply)) param.result = true
        }
    }

    const val TRANSACTION = ('A'.code shl 24) or ('A'.code shl 16) or ('D'.code shl 8) or 'D'.code
    const val DESCRIPTOR = "android.content.pm.IPackageManager"
    const val ACTION_GET_BINDER = 1

    private fun myTransact(code: Int, data: Parcel, reply: Parcel?): Boolean {
        if (code == TRANSACTION) {
            if (Binder.getCallingUid() == appUid) {
                log(TAG, "Transaction from client")
                runCatching {
                    data.enforceInterface(DESCRIPTOR)
                    when (data.readInt()) {
                        ACTION_GET_BINDER -> {
                            reply?.writeNoException()
                            reply?.writeStrongBinder(CoreManagerService.instance)
                            return true
                        }
                        else -> log(TAG, "Unknown action")
                    }
                }.onFailure {
                    log(TAG, "Transaction error", it)
                }
            } else {
                log(TAG, "Someone else trying to get my binder?")
            }
            data.setDataPosition(0)
            reply?.setDataPosition(0)
        }
        return false
    }
}