package ips;

/**
 * Keeps set of IP v4 addresses and calculates number of uniques
 */
public interface IpSet {
    // add IP to set
    void add(Ipv4 ip);

    // add multiple IPs from array
    default void add(int[] ips, int ipsLen) {
        Ipv4 ip = new Ipv4();
        int[] octets = ip.octets;
        for (int i = 0; i < ipsLen; i++) {
            int intIp = ips[i];
            Utils.intToOctets(intIp, octets);
            add(ip);
        }
    }

    // merge another set to this set
    void merge(IpSet otherSet);

    // optional: its calculation may be skipped to improve performance
    long getCachedUniqueCount();

    // calculate number of unique IPs
    long calcUnique();

}
