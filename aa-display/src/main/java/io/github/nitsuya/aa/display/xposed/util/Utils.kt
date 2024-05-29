package io.github.nitsuya.aa.display.xposed

import de.robv.android.xposed.XposedBridge

fun log(tag: String, message: String) {
    XposedBridge.log("[$tag] $message")
}

fun log(tag: String, message: String, t: Throwable?) {
    XposedBridge.log("[$tag] $message")
    if(t != null){
        XposedBridge.log(t)
    }
}