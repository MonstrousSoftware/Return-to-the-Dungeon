package com.monstrous.dungeon.lwjgl3;


import com.monstrous.dungeon.Main;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplication;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    public static void createApplication () {

        WgDesktopApplicationConfiguration config = new WgDesktopApplicationConfiguration();
        config.setTitle("Dungeon");
        config.setWindowedMode(1200, 800);
        config.enableGPUtiming = false;
        config.useVsync(true);
        config.samples = 1;
        config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        new WgDesktopApplication(new Main(), config);
    }
}
