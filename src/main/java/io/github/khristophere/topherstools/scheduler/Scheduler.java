package io.github.khristophere.topherstools.scheduler;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {

    private static final ExecutorService asyncExecutorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "TophersTools-Sequential-Async-Requests"));

    public static void runSequentialAsync(Runnable task) {
        asyncExecutorService.execute(task);
    }

    public static void runOnPrimaryThread(Runnable task) {
        Bukkit.getServer().getScheduler().runTask(TophersTools.getInstance(), task);
    }

    public static void shutdownAsyncThread(){
        asyncExecutorService.shutdown();
        try {
            if (!asyncExecutorService.awaitTermination(8, java.util.concurrent.TimeUnit.SECONDS)) {
                asyncExecutorService.shutdownNow();
                TophersTools.getInstance().getSLF4JLogger().error("TophersTools-Sequential-Async-Requests did NOT shut down in time.");
            }
        } catch (InterruptedException e) {
            asyncExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
            TophersTools.getInstance().getSLF4JLogger().error("TophersTools-Sequential-Async-Requests did NOT shut down in time.", e);
        }
    }

}
