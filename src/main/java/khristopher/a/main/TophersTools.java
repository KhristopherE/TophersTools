package khristopher.a.main;

import khristopher.FAWE_BT_Utils.FAWEBTUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TophersTools extends JavaPlugin {

    private static TophersTools tophersToolsInstance;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "TophersTools-Sequential-Async-Requests"));
    public void runSequentialAsync(Runnable task) {
        task.run();
    }
    public void runOnPrimaryThread(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    /**
     *  This is where the plugin starts, when the server calls to enable the plugin it calls to enable all the below functionalities.
     */
    @Override
    public void onEnable() {
        tophersToolsInstance = this; // Sets up accessible plugin instance.
        new FAWEBTUtils();
    }

    /**
     * Attempts to shut down the sequential async queue.
     */
    @Override
    public void onDisable() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(8, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                TophersTools.getInstance().getSLF4JLogger().error("TophersTools Async Requests did NOT shut down in time.");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            TophersTools.getInstance().getSLF4JLogger().error("TophersTools Async Requests did NOT shut down in time.", e);
        }
    }

    /**
     * Provides the current instance of TophersTools
     * @return TophersTools Plugin Instance.
     */
    public static TophersTools getInstance(){
        return tophersToolsInstance;
    }

}


