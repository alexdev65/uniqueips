package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Ipv4;
import ips.Stat;

import java.nio.ByteBuffer;

/**
 * Each chunk (of file) is processed here and all IPs are merged to the global IP set
 * For the sake of optimization this class assumes that EOL is '\n'
 */
public abstract class CustomChunkProcessorBase extends ChunkProcessor {
    protected long lines = 0;
    protected final IpSet globalSet;
    private final Stat stat;
    protected final IpParser ipParser;
    // This must be enough to contain each file line
    protected final byte[] lineBytes = new byte[100];
    protected int lineLength = 0;
    protected final Ipv4 ipv4 = new Ipv4();

    public CustomChunkProcessorBase(ByteBufferProvider byteBufferProvider,
                                    IpSet globalSet, Stat stat, IpParser ipParser) {
        super(byteBufferProvider);
        this.globalSet = globalSet;
        this.stat = stat;
        this.ipParser = ipParser;
    }

    /**
     * Split into lines, each line is parsed to IP and the IP is added to an IP set
     * @param buffer input buffer of a part of the file
     */
    @Override
    protected void processBuffer(ByteBuffer buffer) {
        prepareForBufferProcessing(buffer);
        final byte EOL = (byte)'\n';
        final long previousLines = lines;
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
        // there may be the last line without EOL character
        if (lineLength > 0) {
            processLine();
            lineLength = 0;
        }
        syncToGlobalSet();
        clearSet();
        stat.update(lines - previousLines, globalSet.getCachedUniqueCount());
    }

    // executed at the beginning of the buffer processing
    protected abstract void prepareForBufferProcessing(ByteBuffer buffer);

    // called each time array lineBytes is filled with a complete line
    protected abstract void processLine();

    // synchronize locally parsed IPs to the global set of IPs
    protected abstract void syncToGlobalSet();

    // clears locally parsed IPs
    protected abstract void clearSet();

    @Override
    protected void finished() {
        //System.out.println("Thread " + Thread.currentThread().getName() + " finished. lines = " + lines);
    }
}
