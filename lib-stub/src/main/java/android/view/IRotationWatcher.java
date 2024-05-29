package android.view;

import android.os.Binder;

public interface IRotationWatcher {
    void onRotationChanged(int rotation);

    abstract class Stub extends Binder implements IRotationWatcher {
    }

}