package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.SharedPreferences
import android.graphics.Point
import android.graphics.Rect
import android.util.Size
import com.github.kyuubiran.ezxhelper.utils.findConstructor
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.log
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Constructor

object AaDpiHook: AaHook() {
    override val tagName: String = "AAD_AaDpiHook"

    private lateinit var displayParamsConstructor: Constructor<*>
    private lateinit var carDisplayConstructor: Constructor<*>

    override fun isSupportProcess(processName: String): Boolean {
        return processCar == processName
    }

    override fun loadDexClass(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        val classes = bridge.findClass {
            searchPackages = listOf("")
            matcher {
                usingStrings {
                    add(
                        "DisplayParams(selectedIndex=",
                        StringMatchType.StartsWith,
                        false
                    )
                }
            }
        }
        if (classes.isEmpty() || classes.size > 1) {
            throw NoSuchMethodException("AaDpiHook: not found DisplayParams class：${classes.size}")
        }
        displayParamsConstructor = findConstructor(classes[0].className) {
            //int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, float f, int i10, float f2, Size size, Rect rect, Rect rect2, CarDisplayUiFeatures carDisplayUiFeatures, int i11
            parameterCount == 17
            && parameterTypes[0]        == Int::class.javaPrimitiveType      //selectedIndex
            && parameterTypes[1]        == Int::class.javaPrimitiveType      //codecWidth
            && parameterTypes[2]        == Int::class.javaPrimitiveType      //codecHeight
            && parameterTypes[3]        == Int::class.javaPrimitiveType      //fps
            && parameterTypes[4]        == Int::class.javaPrimitiveType      //dispWidth
            && parameterTypes[5]        == Int::class.javaPrimitiveType      //dispHeight
            && parameterTypes[6]        == Int::class.javaPrimitiveType      //dispLeft
            && parameterTypes[7]        == Int::class.javaPrimitiveType      //dispTop
            && parameterTypes[8]        == Int::class.javaPrimitiveType      //densityDpi
            && parameterTypes[9]        == Float::class.javaPrimitiveType    //pixelAspectRatio
            && parameterTypes[10]       == Int::class.javaPrimitiveType     //depth
            && parameterTypes[11]       == Float::class.javaPrimitiveType   //scaledPixelAspectRatio
            && parameterTypes[12]       == Size::class.java                 //scaledDimensions
            && parameterTypes[13]       == Rect::class.java                 //stableInsets
            && parameterTypes[14]       == Rect::class.java                 //initialInsets
            && parameterTypes[15].name  == "com.google.android.gms.car.display.CarDisplayUiFeatures"
            && parameterTypes[16]       == Int::class.javaPrimitiveType      //unknown 65535
        }
        carDisplayConstructor = findConstructor("com.google.android.gms.car.display.CarDisplay") {
            parameterCount == 8
            && parameterTypes[0].name == "com.google.android.gms.car.display.CarDisplayId"
            && parameterTypes[1] == Int::class.javaPrimitiveType    //carDisplayType MAIN-0,CLUSTER-1,AUXILIARY-2,UNKNOWN-3,
            && parameterTypes[2] == Int::class.javaPrimitiveType    //displayDpi
            && parameterTypes[3] == Point::class.java               //displayDimensions
            && parameterTypes[4] == Rect::class.java                //stableInsets
            && parameterTypes[5] == Rect::class.java                //contentInsets
            && parameterTypes[6] == Int::class.javaPrimitiveType    //initialContentType UNKNOWN-0,NAVIGATION-1,TURN_CARD-2
            && parameterTypes[7] == String::class.java              //configurationId
        }
    }

    override fun hook(config: SharedPreferences, lpparam: XC_LoadPackage.LoadPackageParam) {
        AADisplayConfig.AndroidAutoDpi.get(config).also { androidAutoDpi ->
            if (androidAutoDpi < 50) return@also
            displayParamsConstructor.hookAfter { param -> log(tagName, param.thisObject.toString()) }
            carDisplayConstructor.hookAfter { param -> log(tagName, param.thisObject.toString()) }
            displayParamsConstructor.hookBefore { param ->
                param.args[8] = androidAutoDpi
            }
            carDisplayConstructor.hookBefore { param ->
                if (param.args[1] == 0 && param.args[2] != androidAutoDpi) {
                    param.args[2] = androidAutoDpi
                }
            }
        }
    }


}
