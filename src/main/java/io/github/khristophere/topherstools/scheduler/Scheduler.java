/*
 * Copyright (C) 2026 KhristopherE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.khristophere.topherstools.scheduler;

import io.github.khristophere.topherstools.TophersTools;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class for managing asynchronous sequential tasks and primary main thread scheduling.
 *
 * <p>This scheduler provides a single-threaded background executor to ensure tasks
 * submitted to be processed are executed in the order received, as well
 * as helper methods to dispatch tasks back to the primary thread.</p>
 */
public class Scheduler {

    private static final String ASYNC_THREAD_NAME = "TophersTools-Sequential-Async-Worker";
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newSingleThreadExecutor(r -> new Thread(r, "TophersTools-Sequential-Async-Requests"));

    /**
     * Submits a task to be executed asynchronously.
     * <p>Tasks submitted via this method are guaranteed to run sequentially
     * in a FIFO First-In, First-Out order on a dedicated background thread.</p>
     *
     * @param task the runnable task to execute.
     */
    public static void runSequentialAsync(Runnable task) {
        ASYNC_EXECUTOR.execute(task);
    }

    /**
     * Schedules a task to run on the Minecraft primary (main) thread on the next tick.
     *
     * @param task the runnable task to execute.
     */
    public static void runOnPrimaryThread(Runnable task) {
        Bukkit.getServer().getScheduler().runTask(TophersTools.getInstance(), task);
    }

    /**
     * Attempts to shut down the sequential asynchronous executor service.
     *
     * <p>Attempts to terminate running tasks within an 8-second window. If tasks fail
     * to complete within this timeframe, a forced shutdown is initiated and an error
     * is logged.</p>
     */
    public static void shutdownAsyncThread() {
        ASYNC_EXECUTOR.shutdown();
        try {
            if (!ASYNC_EXECUTOR.awaitTermination(8, java.util.concurrent.TimeUnit.SECONDS)) {
                ASYNC_EXECUTOR.shutdownNow();
                TophersTools.getInstance().getSLF4JLogger().error(ASYNC_THREAD_NAME + " did NOT shut down in time. Forced Termination.");
            }
        } catch (InterruptedException e) {
            ASYNC_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
            TophersTools.getInstance().getSLF4JLogger().error(ASYNC_THREAD_NAME + "TophersTools-Sequential-Async-Requests did NOT shut down in time. Interrupted During Shutdown & Forced Termination.", e);
        }
    }

}
