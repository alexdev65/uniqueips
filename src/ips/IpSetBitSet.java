package ips;

import java.util.BitSet;

/**
 * An implementation of a set of IP v4 addresses that uses BitSet as bit fields
 */
public class IpSetBitSet implements IpSet {
    // the innermost array represents bits for 256 * 256 values
    protected final BitSet[][] ipSet = new BitSet[256][256];
    // Keeping unique count at run time may degrade performance
    private static final boolean RUNTIME_UNIQUE_COUNT = false;
    private long uniqueCount = 0;

    @Override
    public void add(Ipv4 ip) {
        int[] octets = ip.octets;
        BitSet[] net8 = ipSet[octets[0]];

        int num1 = octets[1];
        BitSet net16 = net8[num1];
        if (net16 == null) {
            net16 = net8[num1] = new BitSet(256 * 256);
        }
        int bitIndex = (octets[2] << 8) + octets[3];
        if (RUNTIME_UNIQUE_COUNT) {
            if (!net16.get(bitIndex)) {
                uniqueCount++;
            }
        }
        net16.set(bitIndex);
    }

    @Override
    public synchronized void merge(IpSet otherSet) {
        if (otherSet instanceof IpSetBitSet other) {
            for (int octet0 = 0; octet0 < 256; octet0++) {
                for (int octet1 = 0; octet1 < 256; octet1++) {
                    BitSet ourSubset = ipSet[octet0][octet1];
                    BitSet theirSubset = other.ipSet[octet0][octet1];
                    if (theirSubset != null) {
                        if (ourSubset == null) {
                            ipSet[octet0][octet1] = (BitSet) theirSubset.clone();
                        } else {
                            ourSubset.or(theirSubset);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Can't merge with unknown IP set " + otherSet.getClass().getName());
        }
    }

    @Override
    public long getCachedUniqueCount() {
        return uniqueCount;
    }

    @Override
    public synchronized long calcUnique() {
        long uniqueRecalc = 0;
        for (BitSet[] set8 : ipSet) {
            for (BitSet set16 : set8) {
                if (set16 == null) {
                    continue;
                }
                    uniqueRecalc += set16.cardinality();
            }
        }
        return uniqueRecalc;
    }
}
