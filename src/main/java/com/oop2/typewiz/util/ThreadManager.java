package com.oop2.typewiz.util;

import javafx.application.Platform;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {

    // A background thread pool for tasks
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // Run a task in the background
    public static void runAsync(Runnable task) {
        executor.submit(task);
    }

    // Run a task later on the JavaFX Application Thread
    public static void runOnUI(Runnable task) {
        Platform.runLater(task);
    }

    // Run a background task and continue on UI thread
    public static void runAsyncThenUI(Runnable backgroundTask, Runnable uiTask) {
        executor.submit(() -> {
            backgroundTask.run();
            runOnUI(uiTask);
        });
    }

    // Shutdown (optional, e.g., on exit)
    public static void shutdown() {
        executor.shutdown();
    }
}
