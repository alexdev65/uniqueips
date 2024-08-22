package ips.binaryfiles;

import ips.IpParser;
import ips.IpSetLong;
import ips.Stat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Only calculates number of lines. Useful to compare performance.
 */
public class CustomChunkProcessorLinesCountOnly extends ChunkProcessor {
    private byte sum = 0;
    private long cnt = 0;
    private long lines = 0;
    private final AtomicLong totalLines;
    private final Stat stat;

    public CustomChunkProcessorLinesCountOnly(ByteBufferProvider byteBufferProvider, AtomicLong totalLines,
                                              Stat stat) {
        super(byteBufferProvider);
        this.totalLines = totalLines;
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
        totalLines.addAndGet(lines);
//        System.out.println("Thread " + Thread.currentThread().getName() + " finished. Sum = " + sum
//                + ", cnt = " + cnt + ", lines = " + lines);
    }
}
