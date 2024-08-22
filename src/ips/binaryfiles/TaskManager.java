package ips.binaryfiles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages multithreading executor service. Ensures exceptions are propagated from threads to the main thread.
 */
public class TaskManager {
    private final ExecutorService executorService;
    private final AtomicBoolean hasError = new AtomicBoolean(false);
    private final List<Future<?>> futures = new ArrayList<>();
    private final int timeoutSec    ;

    public TaskManager(int numThreads, int timeoutSec) {
        this.executorService = Executors.newFixedThreadPool(numThreads, r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) ->
                    System.err.println("Uncaught exception: " + e.getMessage() + ", in thread " + t.getName()));
            return thread;
        });
        this.timeoutSec = timeoutSec;
    }

    public void submitTask(Runnable task) {
        futures.add(executorService.submit(new ExceptionHandlingRunnable(task, executorService, hasError)));
    }

    /**  Wait for all tasks to complete or detect an exception */
    public void awaitCompletion() throws InterruptedException, ExecutionException {
        try {
            for (Future<?> future : futures) {
                future.get(); // This will rethrow any exception from the thread
            }
        } catch (ExecutionException e) {
            if (hasError.get()) {
                executorService.shutdownNow();
                throw e;
            }
        }
    }

    public void shutdownGracefully() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeoutSec, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }
}
