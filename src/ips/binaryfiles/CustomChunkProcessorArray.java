package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Ipv4;
import ips.Stat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * See CustomChunkProcessorBase.
 * Uses array of integers as temporary storage
 */
public class CustomChunkProcessorArray extends CustomChunkProcessorBase {
    // Array for storing IP addresses as integers
    // We preallocate some storage though it's not required (can be empty). Value 1_250_000 is enough for 10M bytes buffer
    private int[] ips = new int[1_250_000];
    // Number of elements in the array
    private int ipsLen = 0;

    public CustomChunkProcessorArray(ByteBufferProvider byteBufferProvider, AtomicLong totalLines,
                                     IpSet globalSet, Stat stat, IpParser ipParser) {
        super(byteBufferProvider, totalLines, globalSet, stat, ipParser);
        clearSet();
    }

    private void ensureCapacity(ByteBuffer buffer) {
        int estimatedIps = buffer.limit() / 8;
        if (ips.length < estimatedIps) {
            ips = new int[estimatedIps + 1000];
        }
    }

    @Override
    protected void prepareForBufferProcessing(ByteBuffer buffer) {
        ensureCapacity(buffer);
    }

    @Override
    protected void syncToGlobalSet() {
        globalSet.add(ips, ipsLen);
    }

    @Override
    protected void processLine() {
        int[] octets = ipv4.octets;
        ipParser.ipToOctetsFast(octets, lineBytes, 0, lineLength);
        ips[ipsLen++] = (octets[0] << 24) + (octets[1] << 16) + (octets[2] << 8) + octets[3];
        lines++;
    }

    @Override
    protected void clearSet() {
        ipsLen = 0;
    }

}
