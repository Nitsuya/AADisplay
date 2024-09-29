package io.github.nitsuya.aa.display.xposed

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import android.view.*
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XSharedPreferences
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.model.RecentTask
import io.github.nitsuya.aa.display.ui.aa.AaVirtualDisplayAdapter
import io.github.nitsuya.aa.display.ui.window.DisplayWindow
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.util.Instances
import io.github.nitsuya.template.bases.runIO
import io.github.nitsuya.template.bases.runMain
import io.github.qauxv.ui.CommonContextWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class CoreManagerService private constructor(): ICoreManager.Stub() {
    companion object {
        const val TAG = "CoreManagerService"

        val instance: CoreManagerService by lazy {
            CoreManagerService().apply {
                log(TAG, "AADisplay service initialized")
            }
        }

        @SuppressLint("StaticFieldLeak")
        private lateinit var systemContextHost: Context
        var systemContext: Context
            get() = systemContextHost
            set(value) {
                log(TAG, "SystemContext.params is null: ${value.params}")
                systemContextHost = value.createContext(value.params ?: ContextParams.Builder().build())
            }

        val config: XSharedPreferences? by lazy {
            XSharedPreferences(BuildConfig.APPLICATION_ID, AADisplayConfig.ConfigName).let { config ->
                if(!config.file.canRead())
                    null
                else
                    config
            }
        }

        private var mDisplayWindow: DisplayWindow? = null
        private var mAaVirtualDisplayAdapter: AaVirtualDisplayAdapter? = null

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun systemReady() {
//            systemContext.registerReceiver(CoreBroadcastReceiver, IntentFilter().apply {
//                addAction("com.google.android.gearhead.ASSISTANT_STATE_CHANGED")
//                addAction("com.google.android.apps.auto.carservice.service.impl.NEARBY_ACTION_STOP_PROJECTION")
//                addAction("com.google.android.gms.car.STARTUP_LATENCY_MEASUREMENT_EVENT")
//                addAction("com.google.android.gms.car.FRX")
//                addAction("com.google.android.gms.car.DISCONNECTED")
//                addAction("com.google.android.gms.car.BIND_CAR_INPUT")
//                addAction("com.google.android.gms.car.PROJECTION_STARTED")
//                addAction("com.google.android.gms.car.PROJECTION_ENDED")
//                addAction("com.google.android.gms.car.FIRST_ACTIVITY")
//            })

            TipUtil.init(systemContext, "[AADisplay] ")
            Instances.init(systemContext)

//            runIO {
//                try{
//                    com.google.android.gms.car.a::class.java.invokeStaticMethod("b", args(systemContext), argTypes(Context::class.java))
//                    val classDynamicApiFactory = com.google.android.gms.car.a::class.java.field("a", true, Class::class.java).get(null)
//                    val instance = PathClassLoader(
//                        "/data/local/car_sdk_impl/sdk_impl.jar",
//                        CarSdkClassLoader(this.javaClass.classLoader)
//                    )
//                    instance.loadClass("com.google.android.gms.car.internal.GmsVersionChecker").invokeStaticMethod("disableGmsVersionChecks")
//                    instance.loadClass("com.google.android.gms.car.CarVersionUtils").invokeStaticMethod("versionCheck")
//                    classDynamicApiFactory.field("instance", true, ClassLoader::class.java).set(null, instance)
//                    val car = Car.createCar(systemContext, object: CarConnectionCallback(){
//                        override fun onConnected(car: Car) {
//                            log(TAG, "AA已连接")
//                            val carManager = car.getCarManager("car_1p")
//                            Companion.instance?.showToast("AA carManager: ${carManager!=null} -> ${carManager?.javaClass?.name}")
//                            log(TAG, "AA carManager: ${carManager!=null} -> ${carManager?.javaClass?.name}") //CarFirstPartyManager
//                            Companion.instance?.showToast("AA已连接")
//                        }
//                        override fun onDisconnected(car: Car) {
//                            log(TAG, "AA已断开")
//                            Companion.instance?.showToast("AA 已断开")
//                        }
//                    }).apply {
//                        connect()
//                        log(TAG, "AA初始化完成")
//                        Companion.instance?.showToast("AA初始化完成")
//                    }
//                    if(car == null){
//                        Companion.instance?.showToast("AA初始化失败")
//                        log(TAG, "AA初始化失败")
//                    }
//                } catch (e: Throwable){
//                    Companion.instance?.showToast("AA初始化异常:" + e.message)
//                    log(TAG, "AA初始化异常", e)
//                }
//            }
        }

        fun getDisplayId(): Int{
            return mAaVirtualDisplayAdapter?.mDisplayId ?: Display.INVALID_DISPLAY
        }

        fun getDensityDpi(): Int{
            return mAaVirtualDisplayAdapter?.mDensityDpi ?: 0
        }
    }

    override fun getVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    override fun getBuildTime(): Long {
        return BuildConfig.BUILD_TIME
    }

    override fun onCreateDisplay(width: Int, height: Int, densityDpi: Int, listener: IVirtualDisplayCreatedListener){
        runMain {
            mAaVirtualDisplayAdapter?.apply {
                onReconnected(width, height, densityDpi)
                mDisplayWindow?.onResume(width, height)
                listener.onAvailableDisplay(this.mDisplayId, false)
                return@runMain
            }
            config?.apply {
                reload()
                log(TAG, "config: ${this.all.map { "${it.key}=${it.value}[${it.value?.javaClass?.name}]" }.joinToString() }")
            }
            AaVirtualDisplayAdapter(systemContext, config){
                mAaVirtualDisplayAdapter = this
                onConnected(width, height, densityDpi){ displayId ->
                    listener.onAvailableDisplay(displayId, true)
                }
                mDisplayWindow?.onDestroyPromptly()
                mDisplayWindow = DisplayWindow(CommonContextWrapper.createAppCompatContext(systemContext), this, width, height, densityDpi)
            }
        }
    }

    override fun setDisplaySurface(surface: Surface?){
        runMain {
            mAaVirtualDisplayAdapter?.setSurface(surface)
        }
    }

    override fun onDestroyDisplay(){
        runMain {
            mDisplayWindow?.onDestroy {
                mAaVirtualDisplayAdapter?.onDestroy()
                mDisplayWindow = null
                mAaVirtualDisplayAdapter = null
            }
        }
    }

    override fun startLauncher() {
        runMain {
            mAaVirtualDisplayAdapter?.run {
                startLauncher()
            }
        }
    }

    override fun startActivity(packageName: String, userId: Int) {
        runMain {
            mAaVirtualDisplayAdapter?.run {
                startActivity(packageName, userId)
            }
        }
    }

    override fun startTaskId(taskId: Int, packageName: String, userId: Int) {
        runMain {
            mAaVirtualDisplayAdapter?.startTaskId(taskId, packageName, userId)
        }
    }

    override fun moveTaskId(taskId: Int, isVirtualDisplay: Boolean) {
        runMain {
            mAaVirtualDisplayAdapter?.moveTaskId(taskId, isVirtualDisplay)
        }
    }

    override fun moveTaskToFront(taskId: Int) {
        runMain {
            mAaVirtualDisplayAdapter?.moveTaskToFront(taskId)
        }
    }

    override fun moveSecondTaskToFront() {
        runMain {
            mAaVirtualDisplayAdapter?.moveSecondTaskToFront()
        }
    }

    @SuppressLint("MissingPermission")
    override fun removeTask(taskId: Int){
        runMain {
            mAaVirtualDisplayAdapter?.removeTask(taskId)
        }
    }

    override fun pressKey(action: Int) {
        runMain {
            mAaVirtualDisplayAdapter?.onPressKey(action)
        }
    }

    override fun touch(event: MotionEvent) {
        runBlocking(Dispatchers.IO) {
//        runMain {
            mAaVirtualDisplayAdapter?.onTouch(event)
        }
    }

//    private var repairDownTime: Long = Long.MIN_VALUE
//    fun touch2(event: MotionEvent, ratio: Float, repairDownTime: Long) {
//        mAaVirtualDisplayAdapter?.run {
//            val pointerCoords: Array<MotionEvent.PointerCoords?> = arrayOfNulls(event.pointerCount)
//            val pointerProperties: Array<MotionEvent.PointerProperties?> = arrayOfNulls(event.pointerCount)
//            val oldCoords = MotionEvent.PointerCoords()
//            for (i in 0 until event.pointerCount) {
//                val pointerProperty = MotionEvent.PointerProperties()
//                event.getPointerCoords(i, oldCoords)
//                event.getPointerProperties(i, pointerProperty)
//                pointerCoords[i] = MotionEvent.PointerCoords()
//                pointerCoords[i]!!.apply {
//                    x = if(ratio == 1f) oldCoords.x else oldCoords.x / ratio
//                    y = if(ratio == 1f) oldCoords.y else oldCoords.y / ratio
//                }
//                pointerProperties[i] = pointerProperty
//            }
//
//            val newEvent = MotionEvent.obtain(
//                if(repairDownTime == Long.MIN_VALUE) event.downTime else repairDownTime,
//                if(repairDownTime == Long.MIN_VALUE) event.eventTime else SystemClock.uptimeMillis(),
//                event.action,
//                event.pointerCount,
//                pointerProperties,
//                pointerCoords,
//                event.metaState,
//                event.buttonState,
//                event.xPrecision,
//                event.yPrecision,
//                event.deviceId,
//                event.edgeFlags,
//                event.source,
//                event.flags
//            )
//            onTouch(event)
//            newEvent.recycle()
//        }
//    }

    override fun toggleDisplayPower() {
        runIO {
            mDisplayWindow?.toggleDisplayPower()
        }
    }

    override fun displayPower(displayPower: Boolean) {
        runIO {
            mDisplayWindow?.toggleDisplayPower(displayPower)
        }
    }

    override fun addMirror(surfaceControl: SurfaceControl) {
        runMain {
            mAaVirtualDisplayAdapter?.addMirror(surfaceControl)
        }
    }

    override fun removeMirror(surfaceControl: SurfaceControl){
        runMain {
            mAaVirtualDisplayAdapter?.removeMirror(surfaceControl)
        }
    }

    override fun getRecentTask(): RecentTask {
        return runBlocking(Dispatchers.IO){
            mAaVirtualDisplayAdapter?.getRecentTask() ?: RecentTask(emptyList(), emptyList())
        }
    }

    @SuppressLint("RestrictedApi")
    override fun testCode(action: String){
//        runMain {
//            try {
//                when (action) {
//                    "displayPowerMode" -> {
//                        try{
//                            val internalDisplayToken = SurfaceControlHidden.getInternalDisplayToken()
//                            SurfaceControlHidden.setDisplayPowerMode(internalDisplayToken, 0) // POWER_MODE_OFF-0  POWER_MODE_NORMAL-2
//                        } catch (e: Throwable){
//                            log(TAG, "DisplayToken", e)
//                        }
//                    }
//                    "aaSdk" -> {
//                        try{
//                            com.google.android.gms.car.a::class.java.invokeStaticMethod("b", args(systemContext), argTypes(Context::class.java))
//                            val classDynamicApiFactory = com.google.android.gms.car.a::class.java.field("a", true, Class::class.java).get(null)
//                            val instance = PathClassLoader(
//                                "/data/local/car_sdk_impl/sdk_impl.jar",
//                                CarSdkClassLoader(this.javaClass.classLoader)
//                            )
//                            classDynamicApiFactory.field("instance", true, ClassLoader::class.java).set(null, instance)
//                            instance.loadClass("com.google.android.gms.car.internal.GmsVersionChecker").invokeStaticMethod("disableGmsVersionChecks")
//                            instance.loadClass("com.google.android.gms.car.CarVersionUtils").invokeStaticMethod("versionCheck")
//                            val car = Car.createCar(systemContext, object: CarConnectionCallback(){
//                                override fun onConnected(car: Car) {
//                                    log(TAG, "AA已连接")
//                                    TipUtil.showToast("AA已连接")
////                                    val carManager = car.getCarManager("car_1p")
////                                    showToast("AA carManager: ${carManager!=null} -> ${carManager?.javaClass?.name}")
////                                    log(TAG, "AA carManager: ${carManager!=null} -> ${carManager?.javaClass?.name}") //CarFirstPartyManager
//                                }
//                                override fun onDisconnected(car: Car) {
//                                    log(TAG, "AA已断开")
//                                    TipUtil.showToast("AA 已断开")
//                                }
//                            }).apply {
//                                connect()
//                                log(TAG, "AA初始化完成")
//                                TipUtil.showToast("AA初始化完成")
//                            }
//                            if(car == null){
//                                TipUtil.showToast("AA初始化失败")
//                                log(TAG, "AA初始化失败")
//                            }
//                        } catch (e: Throwable){
//                            TipUtil.showToast("AA初始化异常:" + e.message)
//                            log(TAG, "AA初始化异常", e)
//                        }
//                    }
//                }
//            }catch (e: Throwable){
//                TipUtil.showToast("TestCode[$action]: ${e.message}")
//                log(TAG, "TestCode[$action]:", e)
//            }
//        }
    }

    override fun toast(msg: String){
        runMain {
            TipUtil.showToast(msg)
        }
    }

    override fun printLog(tag: String, msg: String){
        runMain {
            log(tag, msg)
        }
    }
}