package io.github.nitsuya.aa.display.ui.aa.fragment

import android.annotation.SuppressLint
import android.content.*
import android.graphics.SurfaceTexture
import android.os.SystemClock
import android.support.car.Car
import android.support.car.CarConnectionCallback
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.InputDeviceCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.kyuubiran.ezxhelper.utils.tryOrNull
import com.google.android.gms.car.CarFirstPartyManager
import com.topjohnwu.superuser.Shell
import io.github.duzhaokun123.template.bases.BaseFragment
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.databinding.FragmentAaMainBinding
import io.github.nitsuya.aa.display.ui.aa.AaDisplayActivityKt
import io.github.nitsuya.aa.display.util.AABroadcastConst
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.util.getGmsCarFirstPartyManager
import io.github.nitsuya.aa.display.util.startCarAaDisplay
import io.github.nitsuya.aa.display.util.startCarTelecom
import io.github.nitsuya.aa.display.xposed.IVirtualDisplayCreatedListener
import io.github.nitsuya.template.bases.runMain


class AaMainFragment : BaseFragment<FragmentAaMainBinding>(FragmentAaMainBinding::class.java), TextureView.SurfaceTextureListener {

    private var displayId: Int = Display.INVALID_DISPLAY
    private var repairDownTime = Long.MIN_VALUE
    private var isForeground = false
    private lateinit var config: SharedPreferences

    private var car:Car? = null
    private var carManager: CarFirstPartyManager? = null
    private val carConnectionCallback = object: CarConnectionCallback(){
        override fun onConnected(car: Car) {
            if(carManager == null) {
                carManager = car.getGmsCarFirstPartyManager()
            }
        }
        override fun onDisconnected(car: Car) {
            carManager = null
        }
    }

    fun startActivity(){
        carManager.startCarAaDisplay()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        val voiceAssistShell by lazy { AADisplayConfig.VoiceAssistShell.get(config) }
        fun startVoiceAssist() {
            voiceAssistShell?.let {
                CoreApi.displayPower(true)
                tryOrNull {
                    Shell.cmd(
                        it.let {
                            it.replace("\${DisplayId}", (if(displayId == Display.INVALID_DISPLAY) Display.DEFAULT_DISPLAY else displayId).toString())
                        }
                    ).exec()
                }
            }
        }
        override fun onReceive(context: Context?, intent: Intent) {
            when(intent.action){
                AABroadcastConst.ACTION_SCREEN_CONTROL -> {
                    when(val action = intent.getIntExtra(AABroadcastConst.EXTRA_ACTION, 0)){
                        KeyEvent.KEYCODE_FEATURED_APP_1 -> carManager.startCarTelecom()
                        KeyEvent.KEYCODE_SEARCH -> startVoiceAssist()
                        KeyEvent.KEYCODE_POWER -> CoreApi.toggleDisplayPower()
                        else -> {
                            if(!isForeground){
                                carManager.startCarAaDisplay()
                                return
                            }
                            when(action){
                                KeyEvent.KEYCODE_DEMO_APP_1 -> CoreApi.moveSecondTaskToFront()
                                KeyEvent.KEYCODE_BACK -> CoreApi.pressKey(action)
                                KeyEvent.KEYCODE_HOME -> CoreApi.startLauncher()
                                KeyEvent.KEYCODE_APP_SWITCH -> runMain {
                                    AaDisplayActivityKt.showRecentTask(this@AaMainFragment.parentFragmentManager)
                                }
                            }
                        }
                    }
                }
                AABroadcastConst.ACTION_STEERING_WHEEL_CONTROL -> {
                    val action = intent.getIntExtra(AABroadcastConst.EXTRA_ACTION, 0)
                    when (intent.getIntExtra(AABroadcastConst.EXTRA_TYPE, 0)) {
                        0 -> {
                            when(action){
                                KeyEvent.KEYCODE_SEARCH                /* 84*/ -> startVoiceAssist()
                                KeyEvent.KEYCODE_MEDIA_NEXT            /* 87*/,
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS        /* 88*/,
                                KeyEvent.KEYCODE_HEADSETHOOK           /* 79*/,
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE      /* 85*/,
                                KeyEvent.KEYCODE_MEDIA_STOP            /* 86*/,
                                KeyEvent.KEYCODE_MEDIA_REWIND          /* 89*/,
                                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD    /* 90*/,
                                KeyEvent.KEYCODE_MUTE                  /* 91*/,
                                KeyEvent.KEYCODE_MEDIA_PLAY            /*126*/,
                                KeyEvent.KEYCODE_MEDIA_PAUSE           /*127*/,
                                KeyEvent.KEYCODE_MEDIA_RECORD          /*130*/-> CoreApi.pressKey(action)
                                else -> CoreApi.toast("方控[$action]未设置")
                            }
                        }
                        1 -> {
                            when(action){
                                KeyEvent.KEYCODE_SEARCH              /* 84*/ -> startVoiceAssist()
                                KeyEvent.KEYCODE_MEDIA_REWIND         /* 89*/ -> CoreApi.toggleDisplayPower()
                                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD   /* 90*/ -> CoreApi.moveSecondTaskToFront()
                                else -> CoreApi.toast("方控长按[$action]未设置")
                            }
                        }
                        2 -> {
                            CoreApi.toast("方控双击[$action]未设置")
                        }
                    }
                }
            }
        }
    }

    override fun initViews() {
        config = this.requireContext().getSharedPreferences(AADisplayConfig.ConfigName, MediaBrowserServiceCompat.MODE_WORLD_READABLE)
        baseBinding.tvDisplay.post  {
            CoreApi.onCreateDisplay(
                baseBinding.tvDisplay.width,
                baseBinding.tvDisplay.height,
                AADisplayConfig.VirtualDisplayDpi.get(config).let {
                    if(it <= 50) resources.displayMetrics.densityDpi
                    else it
                },
                object : IVirtualDisplayCreatedListener.Stub() {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onAvailableDisplay(displayId: Int, create: Boolean) {
                        this@AaMainFragment.displayId = displayId
                        runMain {
                            if (baseBinding.tvDisplay.isAvailable) {
                                CoreApi.setDisplaySurface(Surface(baseBinding.tvDisplay.surfaceTexture))
                            }
                            baseBinding.tvDisplay.surfaceTextureListener = this@AaMainFragment
                            baseBinding.tvDisplay.setOnTouchListener { _, e ->
                                val uptimeMillis = SystemClock.uptimeMillis()
                                if (e.action === MotionEvent.ACTION_DOWN) {
                                    repairDownTime = uptimeMillis
                                }
                                val pointerCoords: Array<MotionEvent.PointerCoords?> = arrayOfNulls(e.pointerCount)
                                val pointerProperties: Array<MotionEvent.PointerProperties?> = arrayOfNulls(e.pointerCount)
                                for (i in 0 until e.pointerCount) {
                                    pointerCoords[i] = MotionEvent.PointerCoords().apply {
                                        e.getPointerCoords(i, this)
                                    }
                                    pointerProperties[i] = MotionEvent.PointerProperties().apply {
                                        e.getPointerProperties(i, this)
                                    }
                                }
                                //val newEvent = MotionEvent.obtain(repairDownTime, uptimeMillis, e.action, e.pointerCount, pointerProperties, pointerCoords, e.metaState, e.buttonState, e.xPrecision, e.yPrecision, e.deviceId, e.edgeFlags, e.source, e.flags)
                                val newEvent = MotionEvent.obtain(repairDownTime, uptimeMillis, e.action, e.pointerCount, pointerProperties, pointerCoords,0,0,1.0f,1.0f,0,0,0,0)
                                newEvent.source = InputDeviceCompat.SOURCE_TOUCHSCREEN
                                CoreApi.touch(newEvent)
                                newEvent.recycle()
                                true
                            }

                            ContextCompat.registerReceiver(this@AaMainFragment.requireContext(), broadcastReceiver, IntentFilter().apply {
                                addAction(AABroadcastConst.ACTION_SCREEN_CONTROL)
                                addAction(AABroadcastConst.ACTION_STEERING_WHEEL_CONTROL)
                            }, ContextCompat.RECEIVER_EXPORTED)

                        }
                    }
                }
            )
        }

        car?.disconnect()
        car = Car.createCar(this.requireContext(), carConnectionCallback).apply {
            connect()
        }
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        CoreApi.onDestroyDisplay()
        tryOrNull {
            this@AaMainFragment.requireContext().unregisterReceiver(broadcastReceiver)
        }
        car?.disconnect()
        car = null
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        CoreApi.setDisplaySurface(Surface(surface))
    }
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean  = false
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
}