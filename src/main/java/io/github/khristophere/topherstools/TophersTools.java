package io.github.khristophere.topherstools;

import io.github.khristophere.topherstools.fastasyncworldedit.brushtool.utils.FAWEBTUtils;
import io.github.khristophere.topherstools.scheduler.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class TophersTools extends JavaPlugin {

    private static TophersTools tophersToolsInstance;

    /**
     *  This is where the plugin starts, when the server calls to enable the plugin it calls to enable all the below functionalities.
     */
    @Override
    public void onEnable() {
        tophersToolsInstance = this; // Sets up accessible plugin instance.
        new FAWEBTUtils();
    }

    /**
     * Provides the current instance of TophersTools
     * @return TophersTools Plugin Instance.
     */
    public static TophersTools getInstance(){
        return tophersToolsInstance;
    }

    /**
     * Attempts to shut down the sequential async queue.
     */
    @Override
    public void onDisable() {
        Scheduler.shutdownAsyncThread();
    }

}


