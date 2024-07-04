package android.app;

import android.content.ComponentName;
import android.hardware.display.IDisplayManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;

public interface ITaskStackListener extends IInterface {
    void onTaskStackChanged() throws RemoteException;
    void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException;
    void onActivityUnpinned() throws RemoteException;
    void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible, boolean clearedTask, boolean wasVisible) throws RemoteException;
    void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException;
    void onActivityDismissingDockedTask() throws RemoteException;
    void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException;
    void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException;
    void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException;
    void onTaskRemoved(int taskId) throws RemoteException;
    void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException;
    void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) throws RemoteException;
    void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onTaskDisplayChanged(int taskId, int newDisplayId) throws RemoteException;
    void onRecentTaskListUpdated() throws RemoteException;
    void onRecentTaskListFrozenChanged(boolean frozen) throws RemoteException;
    void onTaskFocusChanged(int taskId, boolean focused) throws RemoteException;
    void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException;
    void onActivityRotation(int displayId) throws RemoteException;
    void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException;
    void onLockTaskModeChanged(int mode) throws RemoteException;

    //Samsung OneUi
    void onActivityDismissingSplitTask(String str) throws RemoteException;
    void onOccludeChangeNotice(ComponentName componentName, boolean z) throws RemoteException;
    void onTaskWindowingModeChanged(int i) throws RemoteException;

    abstract class Stub extends Binder implements ITaskStackListener {

        public static IDisplayManager asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
}
