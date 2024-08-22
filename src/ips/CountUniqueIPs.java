package ips;

import ips.binaryfiles.*;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * It's where all the processing is configured and started
 */
public class CountUniqueIPs {

    private static final DecimalFormat decimalFormatWithThousands;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('_');
        decimalFormatWithThousands = new DecimalFormat("####,###", symbols);
    }

    public static class Settings {

        // Custom settings
        int bufferSize = 10 * 1024 * 1024; //< file read buffer size in bytes
        int numThreads = Runtime.getRuntime().availableProcessors() * 2; //< number of additional threads, >= 0
        // only needed if we want to limit number of lines to read, at least this number of lines will be read:
        final long maxLines = 200_000_000_000L;
        long fileSizeLimit = maxLines * 120L / 7L; //< only needed if we want to limit number of bytes to read
        int timeoutSec = 3600; //< processing timeout

    }

    public static void main(String[] args) {
        String fileName = args[0];

        Settings settings = new Settings();
        PrintStream log = System.out;
        new CountUniqueIPs().countFromFile(fileName, settings, log);
    }

    public long countFromFile(String fileName, Settings settings, PrintStream log) {
        //Settings settings = new Settings();
        Supplier<IpSetLongSavable> ipSetSupplier = IpSetLongSavable::new;
        var ipv4Set = ipSetSupplier.get();
        var stat = new Stat(settings.maxLines, log);
        var ipParser = new IpParser();
        var totalLines = new AtomicLong();
        ChunkProcessorFactory processorFactory = (byteBufferProvider) -> new
                //CustomChunkProcessor(byteBufferProvider, totalLines, ipv4Set, stat, ipParser, ipSetSupplier);
                //CustomChunkProcessorLinesCountOnly(byteBufferProvider, totalLines, stat);
                CustomChunkProcessorArray(byteBufferProvider, totalLines, ipv4Set, stat, ipParser);

        FileProcessor fileProcessor = settings.numThreads == 0 ?
                new SingleThreadedFileProcessor(settings.bufferSize, processorFactory, settings.fileSizeLimit, log) :
                new MultiThreadedFileProcessor(settings.bufferSize, settings.numThreads, processorFactory, settings.timeoutSec, settings.fileSizeLimit, log);


        try {

            Runtime runtime = Runtime.getRuntime();
            long startTime = System.nanoTime();

            // All interesting happens here
            fileProcessor.processFile(fileName);

            double elapsed = ((double)(System.nanoTime() - startTime)) / 1e9;
            log.println("Total lines = " + decimalFormatWithThousands.format(totalLines.get()) +
                    ", elapsed = " + elapsed);

            long unique = printRecalcUniqueCount(ipv4Set, log);
            //ipv4Set.printAll("app3");
            gcAndPrint(runtime, log);

            long usedMem = runtime.totalMemory() - runtime.freeMemory();
            log.printf("Unique = %s, elapsed = %.6f s, used mem = %.3f Mb%n",
                    decimalFormatWithThousands.format(unique), elapsed, ((double)usedMem) / 1e9);

            return unique;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void gcAndPrint(Runtime runtime, PrintStream log) {
        runtime.gc();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        log.printf("Memory after gc: used = %dM, total = %dM, free = %dM%n",
                (totalMem - freeMem) / 1_000_000, totalMem / 1_000_000, freeMem / 1_000_000);
    }

    public static synchronized long printRecalcUniqueCount(IpSet ipSet, PrintStream log) {
        long recalcStartTime = System.nanoTime();
        long uniqueRecalc = ipSet.calcUnique();
        long recalcElapsed = System.nanoTime() - recalcStartTime;
        log.printf("Recalculated uniques = %s, time = %.3s%n",
                decimalFormatWithThousands.format(uniqueRecalc), recalcElapsed / 1e9);
        return uniqueRecalc;
    }

}
