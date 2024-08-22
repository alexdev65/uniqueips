package ips.binaryfiles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class to propagate exceptions from threads
 */
public class ExceptionHandlingRunnable implements Runnable {
    private final Runnable task;
    private final ExecutorService executorService;
    private final AtomicBoolean hasError;

    public ExceptionHandlingRunnable(Runnable task, ExecutorService executorService, AtomicBoolean hasError) {
        this.task = task;
        this.executorService = executorService;
        this.hasError = hasError;
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception e) {
            // Signal that an exception occurred
            hasError.set(true);

            // Initiate shutdown of the ExecutorService
            executorService.shutdownNow();

            // Rethrow the exception to be caught by Future.get()
            throw e;
        }
    }
}
