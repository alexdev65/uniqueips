package ips;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.stream.Collectors;

public class IpSetBitSetSavable extends IpSetBitSet {

    public synchronized void print(PrintStream printStream) {
        for (int i = 0, ipSetLength = ipSet.length; i < ipSetLength; i++) {
            BitSet[] set8 = ipSet[i];
            for (int j = 0, set2Length = set8.length; j < set2Length; j++) {
                BitSet set16 = set8[j];
                if (set16 == null) {
                    continue;
                }
                printStream.printf("%3d.%3d:", i, j);
                String chars = set16.stream().mapToObj(n -> String.format("%4h", n)).collect(Collectors.joining());
                printStream.print(chars);
                printStream.println();
            }
        }
    }

    public void printAll(String suffix) {
        try (PrintStream out = new PrintStream(new FileOutputStream("ipset_" + suffix + ".txt"), false, StandardCharsets.US_ASCII)) {
            print(out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
