package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.window.TaskSnapshot;

import java.util.List;

public interface IActivityManager extends IInterface {

    void killBackgroundProcesses(final String packageName, int userId) throws RemoteException;

    abstract class Stub extends Binder implements IActivityManager {
        public static IActivityManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
}
