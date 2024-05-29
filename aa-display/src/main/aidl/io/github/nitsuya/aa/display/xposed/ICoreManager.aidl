package io.github.nitsuya.aa.display.xposed;

import android.view.Surface;
import android.view.SurfaceControl;
import io.github.nitsuya.aa.display.xposed.IVirtualDisplayCreatedListener;
import io.github.nitsuya.aa.display.model.RecentTask;

interface ICoreManager {

    String getVersionName();
    int getVersionCode();
    int getUid();
    long getBuildTime();

    void onCreateDisplay(int with, int height, int densityDpi, IVirtualDisplayCreatedListener listener);
    void setDisplaySurface(in Surface surface);
    void onDestroyDisplay();

    void startLauncher();
    void startActivity(String packageName, int userId);
    void startTaskId(int taskId, String packageName, int userId);
    void moveTaskId(int taskId, boolean isVirtualDisplay);
    void moveTaskToFront(int taskId);
    void moveSecondTaskToFront();
    void removeTask(int taskId);
    void pressKey(int action);
    void touch(in MotionEvent motionEvent);
    void toggleDisplayPower();
    void displayPower(boolean displayPower);

    void addMirror(in SurfaceControl surfaceControl);
    void removeMirror(in SurfaceControl surfaceControl);

    RecentTask getRecentTask();

    void testCode(String action);
    void toast(String msg);
    void printLog(String tag, String msg);
}