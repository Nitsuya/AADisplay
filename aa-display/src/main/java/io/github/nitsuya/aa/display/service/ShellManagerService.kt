package io.github.nitsuya.aa.display.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import com.topjohnwu.superuser.Shell
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.IShellManager

class ShellManagerService: Service() {
    private lateinit var config: SharedPreferences
    private val stub: IShellManager.Stub = object: IShellManager.Stub(){
        override fun createVirtualDisplayBefore(): Boolean = execConfigShell(AADisplayConfig.CreateVirtualDisplayBefore.get(config))
        override fun destroyVirtualDisplayAfter(): Boolean = execConfigShell(AADisplayConfig.DestroyVirtualDisplayAfter.get(config))

        private fun execConfigShell(commands: Array<String>): Boolean {
            if(commands.isEmpty()) return true;
            return Shell.getShell().newJob().add(*commands).exec().isSuccess
        }
    }
    override fun onCreate() {
        super.onCreate()
        config = this.getSharedPreferences(AADisplayConfig.ConfigName, MODE_WORLD_READABLE)
    }
    override fun onBind(intent: Intent?): IBinder = stub
}