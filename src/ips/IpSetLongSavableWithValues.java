package ips;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class IpSetLongSavableWithValues extends IpSetLongSavable {
    private final ArrayList<String> numbers = new ArrayList<>(7000);

    @Override
    public void add(Ipv4 ip) {
        numbers.add(String.format("%d.%d.%d.%d", ip.octets[0], ip.octets[1], ip.octets[2], ip.octets[3]));
        super.add(ip);
    }

    @Override
    public synchronized void merge(IpSet otherSet) {
        if (otherSet instanceof IpSetLongSavableWithValues other) {
            super.merge(otherSet);
            numbers.addAll(other.numbers);
        } else {
            throw new RuntimeException("Can't merge with unknown IP set " + otherSet.getClass().getName());
        }
    }

    public synchronized void printNumbers(PrintStream printStream) {
        for (var n : numbers) {
            printStream.println(n);
        }
    }

    @Override
    public void printAll(String suffix) {
        super.printAll(suffix);
        try (PrintStream out = new PrintStream(new FileOutputStream("ipnums_" + suffix + ".txt"))) {
            printNumbers(out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
