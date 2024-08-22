package ips;

import java.util.Arrays;

/**
 * An implementation of a set of IP v4 addresses that uses long as bit fields
 */
public class IpSetLong implements IpSet {
    // the innermost array represents bits for 256 * 256 values (the first two octets)
    // 256 is number of values in one octet
    protected final long[][][] ipSet = new long[256][256][];
    protected static final int UNIT_SIZE = Long.SIZE; //< currently using "long" as bit storage unit
    private final static int IDX_SHIFT = 2; //< Equals to log2(256 / UNIT_SIZE) = log2(4)

    // Keeping unique count at run time may degrade performance
    private static final boolean RUNTIME_UNIQUE_COUNT = true;
    private long uniqueCount = 0;

    @Override
    public void add(Ipv4 ip) {
        int[] octets = ip.octets;
        long[][] net8 = ipSet[octets[0]];

        int octet1 = octets[1];
        long[] net16 = net8[octet1];
        if (net16 == null) {
            net16 = net8[octet1] = new long[256 * 256 / UNIT_SIZE];
        }

        int octet3 = octets[3];
        int set4No = octet3 / UNIT_SIZE;
        long bit = 1L << (octet3 % UNIT_SIZE); //< bit for the IP inside the long value
        int idxOfLong = (octets[2] << IDX_SHIFT) + set4No; //< index of the long value with the IP
        if (RUNTIME_UNIQUE_COUNT) {
            if ((net16[idxOfLong] & bit) == 0) {
                uniqueCount++;
            }
        }
        net16[idxOfLong] |= bit;
    }

    @Override
    public synchronized void add(int[] ips, int ipsLen) {
        IpSet.super.add(ips, ipsLen);
    }

    @Override
    public synchronized void merge(IpSet otherSet) {
        if (otherSet instanceof IpSetLong other) {
            for (int octet0 = 0; octet0 < 256; octet0++) {
                for (int octet1 = 0; octet1 < 256; octet1++) {
                    long[] ourSubset = ipSet[octet0][octet1];
                    long[] theirSubset = other.ipSet[octet0][octet1];
                    if (theirSubset != null) {
                        if (ourSubset == null) {
                            ipSet[octet0][octet1] = Arrays.copyOf(theirSubset, theirSubset.length);
                        } else {
                            for (int idxLong = 0; idxLong < 256 * 256 / UNIT_SIZE; idxLong++) {
                                ourSubset[idxLong] |= theirSubset[idxLong];
                            }
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Can't merge with unknown IP set " + otherSet.getClass().getName());
        }
    }

    @Override
    public synchronized long getCachedUniqueCount() {
        return uniqueCount;
    }

    @Override
    public synchronized long calcUnique() {
        long uniqueRecalc = 0;
        for (long[][] set8 : ipSet) {
            for (long[] set16 : set8) {
                if (set16 == null) {
                    continue;
                }
                for (long unit : set16) {
                    uniqueRecalc += Long.bitCount(unit);
                }
            }
        }
        return uniqueRecalc;
    }

}
