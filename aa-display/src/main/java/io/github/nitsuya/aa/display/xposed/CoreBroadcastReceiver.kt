package io.github.nitsuya.aa.display.xposed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.nitsuya.aa.display.BuildConfig

object CoreBroadcastReceiver : BroadcastReceiver() {
    const val TAG = "AADisplay_CoreBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        TipUtil.showToast("事件: ${intent.action}")
    }
}