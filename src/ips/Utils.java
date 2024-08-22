package ips;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Utils {
    public static final DecimalFormat decimalFormatWithThousands;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('_');
        decimalFormatWithThousands = new DecimalFormat("####,###", symbols);
    }

    public static void intToOctets(int intIp, int[] octets) {
        octets[0] = intIp >>> 24;
        octets[1] = (intIp >> 16) & 0xff;
        octets[2] = (intIp >> 8) & 0xff;
        octets[3] = intIp & 0xff;
    }

    public static int octetsToInt(int[] octets) {
        return (octets[0] << 24) + (octets[1] << 16) + (octets[2] << 8) + octets[3];
    }
}
