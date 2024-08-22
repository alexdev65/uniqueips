package ips;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class IpSetLongSavable extends IpSetLong {


    public synchronized void print(PrintStream printStream) {
        for (int i = 0, ipSetLength = ipSet.length; i < ipSetLength; i++) {
            long[][] set8 = ipSet[i];
            for (int j = 0, set2Length = set8.length; j < set2Length; j++) {
                long[] set16 = set8[j];
                if (set16 == null) {
                    continue;
                }
                printStream.printf("%3d.%3d:", i, j);
                char[] chars = longArrayToHex(set16);
                printStream.print(chars);
                printStream.println();
            }
        }
    }

    private static char[] longArrayToHex(long[] array) {
        final int bitsInByte = 8;
        final char[] hexDigits = "0123456789abcdef".toCharArray();
        final int bitsInHex = 4;
        final int hexDigitMask = (1 << bitsInHex) - 1;

        char[] chars = new char[UNIT_SIZE / bitsInByte * array.length];
        int pos = 0;
        for (long unit : array) {
            for (int h = UNIT_SIZE - bitsInHex; h >= 0; h -= bitsInHex) {
                int digit = (int)((unit >> h) & hexDigitMask);
                chars[pos++] = hexDigits[digit];
            }
        }
        return chars;
    }

    public void printAll(String suffix) {
        try (PrintStream out = new PrintStream(new FileOutputStream("ipset_" + suffix + ".txt"), false, StandardCharsets.US_ASCII)) {
            print(out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
