package android.app;


import android.os.RemoteException;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(ActivityManager.class)
public class ActivityManagerHidden {

    public void forceStopPackageAsUser(String packageName, int userId) {
        throw new RuntimeException("Stub!");
    }

}