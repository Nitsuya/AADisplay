package com.android.internal.statusbar;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IStatusBarService extends IInterface {

    void collapsePanels();

    abstract class Stub extends Binder implements IStatusBarService {
        public static IStatusBarService asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IBinder asBinder() {
            throw new UnsupportedOperationException();
        }
    }
}
