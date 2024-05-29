package android.os;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PowerManager.class)
public class PowerManagerHidden {

    public PowerManager.WakeLock newWakeLock(int levelAndFlags, String tag, int displayId) {
        throw new RuntimeException("Stub!");
    }

}