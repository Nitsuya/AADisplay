package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IWindowManager extends IInterface {

    void setForcedDisplayDensityForUser(int displayId, int density, int userId);

    int getWindowingMode(int displayId);

    void setWindowingMode(int displayId, int mode);

    boolean shouldShowWithInsecureKeyguard(int displayId);

    void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow);

    boolean shouldShowSystemDecors(int displayId);

    void setShouldShowSystemDecors(int displayId, boolean shouldShow);

    int getDisplayImePolicy(int displayId);

    void setDisplayImePolicy(int displayId, int imePolicy);

    int watchRotation(IRotationWatcher watcher, int displayId) throws RemoteException;

    void removeRotationWatcher(IRotationWatcher watcher) throws RemoteException;

    SurfaceControl mirrorWallpaperSurface(int displayId) throws RemoteException;

    android.graphics.Bitmap snapshotTaskForRecents(int taskId);

    boolean mirrorDisplay(int displayId, SurfaceControl outSurfaceControl) throws RemoteException;

    abstract class Stub extends Binder implements IWindowManager {
        public static IWindowManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
