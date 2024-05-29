package io.github.nitsuya.aa.display.xposed

import android.os.IBinder.DeathRecipient
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceControl
import io.github.nitsuya.aa.display.model.RecentTask
import io.github.nitsuya.template.bases.runIO
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object CoreManager : ICoreManager, DeathRecipient {
    private const val TAG = "CoreManager"

    private class ServiceProxy(private val obj: ICoreManager) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            val result = method.invoke(obj, *args.orEmpty())
            if (result == null) Log.i(TAG, "Call service method ${method.name}")
            else Log.i(TAG, "Call service method ${method.name} with result " + result.toString().take(20))
            return result
        }
    }

    @Volatile
    private var service: ICoreManager? = null

    override fun binderDied() {
        service = null
        Log.e(TAG, "Binder died")
    }

    override fun asBinder() = service?.asBinder()

    override fun getVersionName(): String? {
        return getService()?.versionName
    }

    override fun getVersionCode() = getService()?.versionCode ?: 0

    override fun getUid() = getService()?.uid ?: -1

    override fun getBuildTime(): Long {
        return getService()?.buildTime ?: 0
    }

    override fun onCreateDisplay(with: Int, height: Int, densityDpi: Int, listener: IVirtualDisplayCreatedListener) {
        getService()?.onCreateDisplay(with, height, densityDpi, listener)
    }

    override fun setDisplaySurface(surface: Surface?) {
        getService()?.setDisplaySurface(surface)
    }

    override fun onDestroyDisplay() {
        getService()?.onDestroyDisplay()
    }

    override fun startLauncher() {
        getService()?.startLauncher()
    }

    override fun startActivity(packageName: String, userId: Int) {
        getService()?.startActivity(packageName, userId)
    }

    override fun startTaskId(taskId: Int, packageName: String, userId: Int) {
        getService()?.startTaskId(taskId, packageName, userId)
    }

    override fun moveTaskId(taskId: Int, isVirtualDisplay: Boolean) {
        getService()?.moveTaskId(taskId, isVirtualDisplay)
    }

    override fun moveTaskToFront(taskId: Int) {
        getService()?.moveTaskToFront(taskId)
    }

    override fun moveSecondTaskToFront() {
        getService()?.moveSecondTaskToFront()
    }

    override fun removeTask(taskId: Int){
        getService()?.removeTask(taskId)
    }

    override fun pressKey(action: Int) {
        getService()?.pressKey(action)
    }

    override fun touch(motionEvent: MotionEvent) {
        getService()?.touch(motionEvent)
    }

    override fun toggleDisplayPower() {
        getService()?.toggleDisplayPower()
    }

    override fun displayPower(displayPower: Boolean) {
        getService()?.displayPower(displayPower)
    }

    override fun addMirror(surfaceControl: SurfaceControl) {
        getService()?.addMirror(surfaceControl)
    }

    override fun removeMirror(surfaceControl: SurfaceControl){
        getService()?.removeMirror(surfaceControl)
    }

    override fun getRecentTask(): RecentTask? {
        return getService()?.recentTask
    }

    override fun testCode(action: String){
        getService()?.testCode(action)
    }

    override fun toast(msg: String){
        getService()?.toast(msg)
    }

    override fun printLog(tag: String, msg: String){
        getService()?.printLog(tag, msg)
    }

    private fun getService(): ICoreManager? {
        if (service != null) return service
        val pm = ServiceManager.getService("package")
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        val remote = try {
            data.writeInterfaceToken(BridgeService.DESCRIPTOR)
            data.writeInt(BridgeService.ACTION_GET_BINDER)
            pm.transact(BridgeService.TRANSACTION, data, reply, 0)
            reply.readException()
            val binder = reply.readStrongBinder()
            ICoreManager.Stub.asInterface(binder)
        } catch (e: RemoteException) {
            Log.d(TAG, "Failed to get binder")
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
        if (remote != null) {
            Log.i(TAG, "Binder acquired")
            remote.asBinder().linkToDeath(this, 0)
            service = Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(ICoreManager::class.java),
                ServiceProxy(remote)
            ) as ICoreManager
        }
        return service
    }
}