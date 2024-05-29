package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IActivityTaskManager extends IInterface {
    void moveRootTaskToDisplay(int taskId, int displayId) throws RemoteException;

    void registerTaskStackListener(ITaskStackListener listener) throws RemoteException;

    void unregisterTaskStackListener(ITaskStackListener listener) throws RemoteException;

    int getFrontActivityScreenCompatMode();

    void setFrontActivityScreenCompatMode(int mode);

    void setFocusedTask(int taskId);

    boolean removeTask(int taskId);

    ActivityManager.TaskDescription getTaskDescription(int taskId) throws RemoteException;

    List<ActivityTaskManager.RootTaskInfo> getAllRootTaskInfosOnDisplay(int displayId) throws RemoteException;

    TaskSnapshot getTaskSnapshot(int taskId, boolean isLowResolution);

    TaskSnapshot getTaskSnapshot(int taskId, boolean isLowResolution, boolean takeSnapshotIfNeeded);

    abstract class Stub extends Binder implements IActivityTaskManager {
        public static IActivityTaskManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
}
