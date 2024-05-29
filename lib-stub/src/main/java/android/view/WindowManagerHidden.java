package android.view;

import androidx.annotation.IntDef;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(WindowManager.class)
public interface WindowManagerHidden {
    /**
     * Display IME Policy: The IME should appear on the local display.
     * @hide
     */
    int DISPLAY_IME_POLICY_LOCAL = 0;

    /**
     * Display IME Policy: The IME should appear on the fallback display.
     * @hide
     */
    int DISPLAY_IME_POLICY_FALLBACK_DISPLAY = 1;

    /**
     * Display IME Policy: The IME should be hidden.
     *
     * Setting this policy will prevent the IME from making a connection. This
     * will prevent any IME from receiving metadata about input.
     * @hide
     */
    int DISPLAY_IME_POLICY_HIDE = 2;

    /**
     * @hide
     */
    @IntDef({
            DISPLAY_IME_POLICY_LOCAL,
            DISPLAY_IME_POLICY_FALLBACK_DISPLAY,
            DISPLAY_IME_POLICY_HIDE,
    })
    @interface DisplayImePolicy {}

    /**
     * Sets the policy for how the display should show IME.
     *
     * @param displayId Display ID.
     * @param imePolicy Indicates the policy for how the display should show IME.
     * @hide
     */
    default void setDisplayImePolicy(int displayId, @DisplayImePolicy int imePolicy) {
    }

    /**
     * Indicates the policy for how the display should show IME.
     *
     * @param displayId The id of the display.
     * @return The policy for how the display should show IME.
     * @hide
     */
    default @DisplayImePolicy int getDisplayImePolicy(int displayId) {
        return DISPLAY_IME_POLICY_FALLBACK_DISPLAY;
    }
}
