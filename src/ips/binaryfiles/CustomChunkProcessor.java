package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Ipv4;
import ips.Stat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Each chunk (of file) is processed here and all IPs are merged to the global IP set
 * For the sake of optimization this class assumes that EOL is '\n'
 */
public class CustomChunkProcessor extends ChunkProcessor {
    private long lines = 0;
    private final AtomicLong totalLines;
    private IpSet ipSet;
    private final IpSet globalSet;
    private final Stat stat;
    private final IpParser ipParser;
    // This must be enough to contain each file line
    private final byte[] lineBytes = new byte[100];
    private int lineLength = 0;
    private final Ipv4 ipv4 = new Ipv4();
    private final Supplier<? extends IpSet> ipSetSupplier;

    public CustomChunkProcessor(ByteBufferProvider byteBufferProvider, AtomicLong totalLines,
                                IpSet globalSet, Stat stat, IpParser ipParser,
                                Supplier<? extends IpSet> ipSetSupplier) {
        super(byteBufferProvider);
        this.totalLines = totalLines;
        this.globalSet = globalSet;
        this.stat = stat;
        this.ipParser = ipParser;
        this.ipSetSupplier = ipSetSupplier;
        clearSet();
    }

    /**
     * Split into lines, each line is parsed to IP and the IP is added to an IP set
     * @param buffer input buffer of a part of the file
     */
    @Override
    protected void processBuffer(ByteBuffer buffer) {
        final byte EOL = (byte)'\n';
        long savedLines = lines;
        byte b = EOL;
        while (buffer.hasRemaining()) {
            b = buffer.get();
            if (b == EOL) {
                processLine();
                lineLength = 0;
            } else {
                lineBytes[lineLength++] = b;
            }
        }

        if (lineLength > 0) {
            processLine();
            lineLength = 0;
        }
        globalSet.merge(ipSet);
        clearSet();
        stat.update(lines - savedLines, globalSet.getCachedUniqueCount());
    }

    private void processLine() {
        ipParser.ipToOctetsFast(ipv4.octets, lineBytes, 0, lineLength);
        ipSet.add(ipv4);
        lines++;
    }

    private void clearSet() {
        ipSet = ipSetSupplier.get(); //< TODO: optimize memory allocations
    }

    @Override
    protected void finished() {
        totalLines.addAndGet(lines);
        globalSet.merge(ipSet);
        System.out.println("Thread " + Thread.currentThread().getName() + " finished. lines = " + lines +
                ", unique IPs in thread = " + ipSet.calcUnique());
    }
}
