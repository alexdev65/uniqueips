package ips.binaryfiles;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Obtains the next buffer (chunk of data), processes it and cleans it
 */
public abstract class ChunkProcessor implements Runnable {
    private final ByteBufferProvider byteBufferProvider;

    public ChunkProcessor(ByteBufferProvider byteBufferProvider) {
        this.byteBufferProvider = byteBufferProvider;
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer;
            while ((buffer = byteBufferProvider.getNextBuffer()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                processBuffer(buffer);
                cleanBuffer(buffer);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            finished();
        }
    }

    private static void cleanBuffer(ByteBuffer buffer) {
        try {
            // Dirty hack to clean up memory. The buffer must not be used after this.
            // Java 9+ only. (There's probably a better approach in JDK 19: https://bugs.openjdk.org/browse/JDK-4724038)
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            Method invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            invokeCleaner.invoke(unsafe, buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void processBuffer(ByteBuffer buffer);
    protected abstract void finished();
}
