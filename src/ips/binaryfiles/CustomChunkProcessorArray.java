package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Ipv4;
import ips.Stat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Each chunk (of file) is processed here and all IPs are merged to the global IP set
 * For the sake of optimization this class assumes that EOL is '\n'
 */
public class CustomChunkProcessorArray extends ChunkProcessor {
    private long lines = 0;
    private final AtomicLong totalLines;
    private int[] ips = new int[1_250_000];
    private int ipsLen = 0;
    private final IpSet globalSet;
    private final Stat stat;
    private final IpParser ipParser;
    // This must be enough to contain each file line
    private final byte[] lineBytes = new byte[100];
    private int lineLength = 0;
    private final Ipv4 ipv4 = new Ipv4();

    public CustomChunkProcessorArray(ByteBufferProvider byteBufferProvider, AtomicLong totalLines,
                                     IpSet globalSet, Stat stat, IpParser ipParser) {
        super(byteBufferProvider);
        this.totalLines = totalLines;
        this.globalSet = globalSet;
        this.stat = stat;
        this.ipParser = ipParser;
        clearSet();
    }

    private void ensureCapacity(ByteBuffer buffer) {
        int estimatedIps = buffer.limit() / 8;
        if (ips.length < estimatedIps) {
            ips = new int[estimatedIps + 1000];
        }
    }

    /**
     * Split into lines, each line is parsed to IP and the IP is added to an IP set
     * @param buffer input buffer of a part of the file
     */
    @Override
    protected void processBuffer(ByteBuffer buffer) {
        ensureCapacity(buffer);
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
        globalSet.add(ips, ipsLen);
        clearSet();
        stat.update(lines - savedLines, 0);
    }

    private void processLine() {
        int[] octets = ipv4.octets;
        ipParser.ipToOctetsFast(octets, lineBytes, 0, lineLength);
        ips[ipsLen++] = (octets[0] << 24) + (octets[1] << 16) + (octets[2] << 8) + octets[3];
        lines++;
    }

    private void clearSet() {
        ipsLen = 0;
    }

    @Override
    protected void finished() {
        totalLines.addAndGet(lines);
        globalSet.add(ips, ipsLen);
        //System.out.println("Thread " + Thread.currentThread().getName() + " finished. lines = " + lines);
    }
}
