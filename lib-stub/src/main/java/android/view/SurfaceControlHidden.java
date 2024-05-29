package android.view;

import android.os.IBinder;

import androidx.annotation.NonNull;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(SurfaceControl.class)
public class SurfaceControlHidden {

    /**
     * Display power mode off: used while blanking the screen.
     * Use only with {@link SurfaceControl#setDisplayPowerMode}.
     * @hide
     */
    public static final int POWER_MODE_OFF = 0;

    /**
     * Display power mode doze: used while putting the screen into low power mode.
     * Use only with {@link SurfaceControl#setDisplayPowerMode}.
     * @hide
     */
    public static final int POWER_MODE_DOZE = 1;

    /**
     * Display power mode normal: used while unblanking the screen.
     * Use only with {@link SurfaceControl#setDisplayPowerMode}.
     * @hide
     */
    public static final int POWER_MODE_NORMAL = 2;

    /**
     * Display power mode doze: used while putting the screen into a suspended
     * low power mode.  Use only with {@link SurfaceControl#setDisplayPowerMode}.
     * @hide
     */
    public static final int POWER_MODE_DOZE_SUSPEND = 3;

    /**
     * Display power mode on: used while putting the screen into a suspended
     * full power mode.  Use only with {@link SurfaceControl#setDisplayPowerMode}.
     * @hide
     */
    public static final int POWER_MODE_ON_SUSPEND = 4;

    public static void setDisplayPowerMode(@NonNull IBinder displayToken, int mode) {
        throw new RuntimeException("Stub!");
    }

    @NonNull
    public static IBinder getInternalDisplayToken() {
        throw new RuntimeException("Stub!");
    }

}