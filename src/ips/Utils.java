package ips;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Utils {
    static final DecimalFormat decimalFormatWithThousands;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('_');
        decimalFormatWithThousands = new DecimalFormat("####,###", symbols);
    }

}
