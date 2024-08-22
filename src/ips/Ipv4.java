package ips;

/**
 * IP v4 address.
 * Data class but not a record.
 * Since we don't create a lot of its instances in the current implementation it's optimized for speed, not space
 */
public class Ipv4 {
    public int[] octets = new int[4];
}
