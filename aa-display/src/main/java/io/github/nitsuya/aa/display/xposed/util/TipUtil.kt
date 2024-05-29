package io.github.nitsuya.aa.display.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object TipUtil {
    private lateinit var context: Context
    private lateinit var prefix: String

    fun init(context: Context, prefix: String = "") {
        TipUtil.context = context
        TipUtil.prefix = prefix
    }

    fun showToast(msg: String) {
        Toast.makeText(context, "$prefix$msg", Toast.LENGTH_LONG).show()
    }
}