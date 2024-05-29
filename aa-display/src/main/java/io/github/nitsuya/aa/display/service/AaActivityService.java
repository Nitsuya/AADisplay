package io.github.nitsuya.aa.display.service;

import android.content.pm.ActivityInfo;
import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarActivityService;
import io.github.nitsuya.aa.display.ui.aa.AaDisplayActivity;

public class AaActivityService extends CarActivityService {

    @Override
    public Class<? extends CarActivity> getCarActivity() {
        return AaDisplayActivity.class;
    }

    @Override
    public int getHandledConfigChanges() {
        return ActivityInfo.CONFIG_MCC
                | ActivityInfo.CONFIG_MNC
                | ActivityInfo.CONFIG_LOCALE
                | ActivityInfo.CONFIG_TOUCHSCREEN
                | ActivityInfo.CONFIG_KEYBOARD
                | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
                | ActivityInfo.CONFIG_NAVIGATION
                | ActivityInfo.CONFIG_ORIENTATION
                | ActivityInfo.CONFIG_SCREEN_LAYOUT
                | ActivityInfo.CONFIG_UI_MODE
                | ActivityInfo.CONFIG_SCREEN_SIZE
                | ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
                | ActivityInfo.CONFIG_DENSITY
                | ActivityInfo.CONFIG_LAYOUT_DIRECTION
                | ActivityInfo.CONFIG_COLOR_MODE
                | ActivityInfo.CONFIG_FONT_SCALE
                | ActivityInfo.CONFIG_FONT_WEIGHT_ADJUSTMENT
        ;
    }

}