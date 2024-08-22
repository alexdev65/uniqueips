package ips.binaryfiles;

import ips.IpParser;
import ips.IpSet;
import ips.Stat;
import ips.Utils;

import java.nio.ByteBuffer;

/**
 * See CustomChunkProcessorBase.
 * Uses array of integers as temporary storage. It uses less memory and is also faster compared to IpSet storage.
 */
public class CustomChunkProcessorArray extends CustomChunkProcessorBase {
    // Array for storing IP addresses as integers
    // We preallocate some storage though it's not required (can be empty). Value 1_250_000 is enough for 10M bytes buffer
    private int[] ips = new int[1_250_000];
    // Number of elements in the array
    private int ipsLen = 0;

    public CustomChunkProcessorArray(ByteBufferProvider byteBufferProvider,
                                     IpSet globalSet, Stat stat, IpParser ipParser) {
        super(byteBufferProvider, globalSet, stat, ipParser);
        clearSet();
    }

    private void ensureCapacity(ByteBuffer buffer) {
        final int MIN_IP_STRING_SIZE = 8; //< "1.2.3.4\n"
        int maxExpectedIps = buffer.limit() / MIN_IP_STRING_SIZE;
        if (ips.length < maxExpectedIps) {
            ips = new int[maxExpectedIps + 1000]; //< plus some extra for not reallocating too often
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
        ips[ipsLen++] = Utils.octetsToInt(octets);
        lines++;
    }

    @Override
    protected void clearSet() {
        ipsLen = 0;
    }

}
