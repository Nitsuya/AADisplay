package io.github.nitsuya.aa.display.xposed;

interface IShellManager {
    boolean createVirtualDisplayBefore();
    boolean destroyVirtualDisplayAfter();
}