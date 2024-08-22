package ips.binaryfiles;

import ips.Stat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Only calculates number of lines. Useful to compare performance.
 */
public class CustomChunkProcessorLinesCountOnly extends ChunkProcessor {
    private long lines = 0;
    private final Stat stat;

    public CustomChunkProcessorLinesCountOnly(ByteBufferProvider byteBufferProvider,
                                              Stat stat) {
        super(byteBufferProvider);
        this.stat = stat;
    }

    @Override
    protected void processBuffer(ByteBuffer buffer) {
        long savedLines = lines;
        final byte EOL = '\n';
        byte b = EOL;
        while (buffer.hasRemaining()) {
            b = buffer.get();
            if (b == EOL) {
                lines++;
            }
        }
        if (b != EOL) {
            lines++;
        }
        stat.update(lines - savedLines, 0);
    }

    @Override
    protected void finished() {
//        System.out.println("Thread " + Thread.currentThread().getName() + " finished. Sum = " + sum
//                + ", cnt = " + cnt + ", lines = " + lines);
    }
}
