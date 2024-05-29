package io.github.nitsuya.aa.display.xposed;

interface IVirtualDisplayCreatedListener {
    void onAvailableDisplay(int displayId, boolean create);
}