package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.ContentResolver
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.nfc.Tag
import androidx.core.content.edit
import com.github.kyuubiran.ezxhelper.utils.field
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.putObject
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.log
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.FindMethod
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.MethodMatcher
import rikka.core.content.put
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.HashMap

object AaPropsHook: AaHook() {
    override val tagName: String = "AAD_AaPropsHook"

    private lateinit var method: Method
    private lateinit var groupField: Field
    private lateinit var keyField: Field
    private lateinit var defValueField: Field

    override fun isSupportProcess(processName: String): Boolean {
        //return processMain == processName || processProjection == processName || processCar == processName
        return true
    }

    override fun loadDexClass(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        val methodMatcher = MethodMatcher().usingStrings {
            add(
                "Must call PhenotypeContext.setContext() first",
                StringMatchType.Equals,
                false
            )
        }
        val fieldName = arrayOf("c", "d", "e")
        val fieldsInfo = linkedMapOf(
            fieldName[0] to "java.lang.String", //groupField
            fieldName[1] to "java.lang.String", //keyField
            fieldName[2] to "java.lang.Object"  //defValueField
        )
        val classes = bridge.findClass {
            searchPackages = listOf("")
            matcher {
                fields {
                    fieldsInfo.forEach { (name, typeName) ->
                        add {
                            modifiers(Modifier.PRIVATE)
                            type(typeName)
                            name(name)
                        }
                    }
                }
                methods {
                    add(methodMatcher)
                }
            }
        }
        if (classes.isEmpty() || classes.size > 1) {
            throw NoSuchMethodException("AaPropsHook: not found props class：${classes.size}")
        }
        val methodDatas = classes[0].getMethods().findMethod(FindMethod().matcher(methodMatcher))
        if (methodDatas.isEmpty() || methodDatas.size > 1) {
            throw NoSuchMethodException("AaPropsHook: not found props method：${classes.size}")
        }
        val methodData = methodDatas[0]
        val clazz = loadClass(methodData.className)
        groupField = clazz.field(fieldName[0]) //com.google.android.projection.gearhead
        keyField = clazz.field(fieldName[1]) //Coolwalk__enabled
        defValueField = clazz.field(fieldName[2]) //true
        log(tagName, "$clazz#${methodData.methodName}#${fieldName.joinToString()}")
        method = findMethod(clazz) {
            name == methodData.methodName
            && parameterCount == 0
        }
    }

    override fun hook(config: SharedPreferences, lpparam: XC_LoadPackage.LoadPackageParam) {
        hookComGoogleAndroidProjectionGearheadProps(config, lpparam)
        hookComGoogleAndroidGmsCarProps(config, lpparam)
    }

    private fun hookComGoogleAndroidProjectionGearheadProps(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        val props = AADisplayConfig.ComGoogleAndroidProjectionGearheadProps.get(config) ?: return
        if (props.isEmpty) {
            return
        }
        val keyValue = HashMap<String, Any?>(props.size, 1f)
        method.hookAfter { param ->
            val thisObject = param.thisObject
            val group = groupField.get(thisObject)
            if (group != "com.google.android.projection.gearhead") {
                return@hookAfter
            }
            val key = keyField.get(thisObject) as String
            if (!props.containsKey(key)){
                return@hookAfter
            }
            val value = keyValue.computeIfAbsent(key) {
                val defValue = defValueField.get(thisObject) ?: return@computeIfAbsent null
                val value = props[key] as String
                log(tagName, "$key,$value,${defValue}")
                try {
                    when (defValue.javaClass) {
                        String::class.java -> value
                        java.lang.Boolean::class.java, Boolean::class.java -> value.toBoolean()
                        java.lang.Long::class.java, Long::class.java -> value.toLong()
                        Integer::class.java, Int::class.java -> value.toInt()
                        else -> {
                            log(tagName, "Android Auto[com.google.android.projection.gearhead] config, $key=$value: unsupported type [${defValue.javaClass}]")
                            null
                        }
                    }
                } catch (e: Exception) {
                    log(tagName,"Android Auto[com.google.android.projection.gearhead] config, $key=$value convert exception", e)
                    null
                }
            }
            if (value != null) {
                param.result = value
            }
        }
    }

    private fun hookComGoogleAndroidGmsCarProps(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        val props = AADisplayConfig.ComGoogleAndroidGmsCarProps.get(config) ?: return
        if (props.isEmpty) {
            return
        }
        try {
            val matrixCursor = MatrixCursor(arrayOf("key", "value"), props.size).apply {
                props.forEach { prop ->
                    addRow(arrayOf(prop.key, prop.value))
                }
            }
            findMethod(ContentResolver::class.java) {
                name == "query"
                && parameterCount == 5
                && parameterTypes[0] == Uri::class.java             // uri
                && parameterTypes[1] == Array<String>::class.java   // projection
                && parameterTypes[2] == String::class.java          // selection
                && parameterTypes[3] == Array<String>::class.java   // selectionArgs
                && parameterTypes[4] == String::class.java          // sortOrder
            }.hookAfter { param ->
                val uri = param.args[0] as Uri
                if (uri.authority != "com.google.android.gms.phenotype") return@hookAfter
                //log(tagName, "ContentProvider.query: uri: $uri")
                if (uri.path != "/com.google.android.gms.car") return@hookAfter
                //log(AaUiHook.tagName, "GmsCarProps-----${lpparam.processName}------")
                param.result = if (param.result == null) matrixCursor else MergeCursor(
                    arrayOf(
                        param.result as Cursor,
                        matrixCursor
                    )
                )
            }
        } catch (e: Throwable) {
            log(tagName, "[com.google.android.gms.car] config", e)
        }
    }

}
