package io.github.nitsuya.aa.display.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.car.Car
import android.support.car.CarAppFocusManager
import android.support.car.CarConnectionCallback
import android.support.car.navigation.CarNavigationInstrumentCluster
import android.support.car.navigation.CarNavigationStatusManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.IntentCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.github.kyuubiran.ezxhelper.utils.field
import com.google.android.apps.auto.sdk.nav.state.TurnEvent
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.util.getGmsCarFirstPartyManager
import io.github.nitsuya.aa.display.util.startCarAaDisplay
import io.github.nitsuya.template.bases.runIO
import io.github.nitsuya.template.bases.runMain
import kotlinx.coroutines.delay


class AaControlService: MediaBrowserServiceCompat() {
    companion object {
        const val TAG = "AaControlService"
    }
    private val CLASS_NAME_AA_CONTROL_SERVICE = AaControlService.javaClass.declaringClass.name
    private val CLASS_NAME_AA_ACTIVITY_SERVICE = AaActivityService::class.java.name

    private lateinit var session: MediaSessionCompat
    private lateinit var config: SharedPreferences

    private var car:Car? = null
    private var carNavigationStatusManager: CarNavigationStatusManager? = null
    private val carConnectionCallback = object: CarConnectionCallback(){
        override fun onConnected(car: Car) {
            if(carManager == null) {
                carManager = car.getGmsCarFirstPartyManager()?.apply {
                    registerCarActivityStartListener(carManagerCarActivityStartListener)
                }
            }
            if(AADisplayConfig.AutoOpen.get(config)){
                runIO {
                    delay(2000)
                    runMain {
                        startCarActivity()
                    }
                }
            }

            if(carNavigationStatusManager == null){
                carNavigationStatusManager = car.getCarManager(CarNavigationStatusManager::class.java)?.apply {
                    addListener(object: CarNavigationStatusManager.CarNavigationCallback{
                        override fun onInstrumentClusterStarted(p0: CarNavigationStatusManager, p1: CarNavigationInstrumentCluster) {
//                            CoreApi.printLog("CarNavigationStatusManager", "######### load onInstrumentClusterStarted -> ${p0 != null}, ${p1}")
                            p0.sendNavigationStatus(CarNavigationStatusManager.STATUS_ACTIVE)
                            p0.sendNavigationTurnDistanceEvent(50/* distanceMeters */, 120/* timeSeconds */, 100/* displayDistanceMillis */, TurnEvent.DistanceUnit.METERS/* displayDistanceUnit */)
                            if (p1.supportsCustomImages()){
                                val customImages = BitmapFactory.decodeResource(resources, R.drawable.ic_aa_home_44)
                                p0.sendNavigationTurnEvent(CarNavigationStatusManager.TURN_DEPART/* event */, "测试路名,支持自定义"/* road */, 0/* turnAngle */, 0/* turnNumber */, customImages/* image */, TurnEvent.TurnSide.RIGHT/* turnSide */)
                            } else {
                                p0.sendNavigationTurnEvent(CarNavigationStatusManager.TURN_DEPART/* event */, "测试路名"/* road */, 0/* turnAngle */, 0/* turnNumber */, TurnEvent.TurnSide.RIGHT/* turnSide */)
                            }
                            //https://github.com/Iscle/OrangePi_4G-IOT_Android_8.1_BSP/blob/58548740b6e9afe99a55b77582588c37609d2bca/packages/services/Car/tests/android_car_api_test/src/android/car/apitest/CarNavigationManagerTest.java#L96
                            //p0.sendEvent(CarNavigationStatusManager.EVENT_TYPE_NEXT_MANEUVER_INFO, Bundle().apply {
                            //    putInt("BUNDLE_INTEGER_VALUE", 1234)
                            //    putFloat("BUNDLE_FLOAT_VALUE", 12.3456f)
                            //    putStringArrayList(
                            //        "BUNDLE_ARRAY_OF_STRINGS",
                            //        arrayListOf("Value A", "Value B", "Value Z")
                            //    )
                            //})
                        }
                        override fun onInstrumentClusterStopped(p0: CarNavigationStatusManager) {}
                    })
                }
            }
        }
        override fun onDisconnected(car: Car) {
            carManager?.unregisterCarActivityStartListener(carManagerCarActivityStartListener)
            carManager = null
            carNavigationStatusManager?.removeListener()
            carNavigationStatusManager = null
        }
    }

    private fun aaConnected() = carManager != null && car != null

    private var focusAaActivity = false
    private var carManager: com.google.android.gms.car.CarFirstPartyManager? = null
    private val carManagerCarActivityStartListener = object: com.google.android.gms.car.CarFirstPartyManager.CarActivityStartListener{
        override fun onActivityStarted(intent: Intent) {
            CoreApi.printLog(TAG, "onActivityStarted: ${intent.component?.toShortString()} -> \r\n ${ if(intent.extras != null) printBundle(intent.extras!!, 0) else "" }")
            intent.component?.apply {
                //com.google.android.projection.gearhead
                //  .oem.0EMExitService
                //  .system.ProjectionTrampolineFallbackService
                //  .system.applauncher.GhAppLauncherService
                //  .media.MediaService
                //  .telecom.TelecomService
                if(className == "com.google.android.projection.gearhead.media.MediaService"){
                    if(IntentCompat.getParcelableExtra(intent, "GH.TargetComponent", ComponentName::class.java)?.className == CLASS_NAME_AA_CONTROL_SERVICE){
                        startCarActivity()
                    }
                }
                focusAaActivity = className == CLASS_NAME_AA_ACTIVITY_SERVICE
            }
        }
        override fun onNewActivityRequest(intent: Intent) {
            CoreApi.toast("[AaControlService]-onNewActivityRequest: $intent")
            CoreApi.printLog(TAG, "onNewActivityRequest : $intent")
        }
    }



    private fun startCarActivity(){
        try {
            carManager.startCarAaDisplay()
        } catch (e: Throwable) {}
    }

    private fun printBundle(extras: Bundle, index: Int): String{
        val keys = extras.keySet()
        return keys.joinToString { it } + " \r\n " + keys.mapNotNull { key ->
            val value = extras.get(key)
            if (value == null) "$key -> null"
            if (value is Bundle) {
                "$key -> Type:Bundle, ${printBundle(value, index + 1)}"
            } else {
                "$key -> ${value.toString()}, Type:${value!!::class.java.name}"
            }
        }.joinToString(separator = "\r\n", prefix = "    ".repeat(index)) { it }
    }

    override fun onCreate() {
        super.onCreate()
        config = this.getSharedPreferences(AADisplayConfig.ConfigName, MODE_WORLD_READABLE)

        session = MediaSessionCompat(this, "AaControlService").apply {
            setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this@AaControlService,
                    0,
                    Intent(Intent.ACTION_MEDIA_BUTTON, null, this@AaControlService, MediaButtonReceiver::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        sessionToken = session.sessionToken
        session.isActive = true
        val playbackStateCompat = PlaybackStateCompat.Builder().apply {
            setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
            setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            )
        }.build()
        session.setPlaybackState(playbackStateCompat)

        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "AAControl")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, null)
                .build()
        )
        session.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
        session.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)

        car?.disconnect()
        car = Car.createCar(this, carConnectionCallback).apply {
            connect()
        }
    }

    override fun onDestroy() {
        session.release()
        car?.disconnect()
        car = null
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("ROOT", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        if(parentId == "ROOT"){
            result.sendResult(ArrayList())
            return
        }
        result.sendResult(ArrayList<MediaBrowserCompat.MediaItem>().apply {
            add(
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder().apply {
                        setMediaId("1")
                        setTitle("AAControl")
                    }.build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                )
            )
        })
    }
}