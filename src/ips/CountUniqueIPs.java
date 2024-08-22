package ips;

import ips.binaryfiles.ChunkProcessorFactory;
import ips.binaryfiles.CustomChunkProcessorArray;
import ips.binaryfiles.CustomChunkProcessorIpSet;
import ips.binaryfiles.CustomChunkProcessorLinesCountOnly;
import ips.binaryfiles.FileProcessor;
import ips.binaryfiles.MultiThreadedFileProcessor;
import ips.binaryfiles.SingleThreadedFileProcessor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * It's where all the processing is configured and started
 */
public class CountUniqueIPs {

    public enum ChunkProcessorChoice {
        ARRAY,
        IP_SET,
        LINE_COUNT_ONLY,
    }

    // Custom settings
    public static class Settings {

        final int MB = 1024 * 1024;
        int bufferSize = 10 * MB; //< file read buffer size in bytes

        // number of additional threads, >= 0. Zero means single threading
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;

        // only needed if we want to limit the number of lines to read, at least this number of lines will be read:
        final long maxLines = 200_000_000_000L;

        final int AVG_BYTES_PER_LINE = 14;
        // only needed if we want to limit the number of bytes to read
        // (by default it exceeds the expected maxLines bytes, which makes this limit essentially inactive)
        long fileSizeLimit = maxLines * (AVG_BYTES_PER_LINE + 3);

        int timeoutSec = 3600; //< processing timeout

        ChunkProcessorChoice chunkProcessorChoice = ChunkProcessorChoice.ARRAY;

    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Incorrect arguments. Expected arguments:  <fileName>");
            System.err.println("    where <fileName> is path and name of file that contains IP v4 addresses");
            System.exit(1);
        }
        String fileName = args[0];

        Settings settings = new Settings();
        PrintStream log = System.out;
        new CountUniqueIPs().countFromFile(fileName, settings, log);
    }

    @SuppressWarnings("UnusedReturnValue")
    public long countFromFile(String fileName, Settings settings, PrintStream log) {
        //Settings settings = new Settings();
        Supplier<IpSetLongSavable> ipSetSupplier = IpSetLongSavable::new;
        var ipv4Set = ipSetSupplier.get();
        var stat = new Stat(settings.maxLines, log);
        var ipParser = new IpParser();
        var totalLines = new AtomicLong();
        ChunkProcessorFactory processorFactory = (byteBufferProvider) -> switch (settings.chunkProcessorChoice) {
            case IP_SET -> new CustomChunkProcessorIpSet(byteBufferProvider, ipv4Set, stat, ipParser, ipSetSupplier);
            case LINE_COUNT_ONLY -> new CustomChunkProcessorLinesCountOnly(byteBufferProvider, totalLines, stat);
            case ARRAY -> new CustomChunkProcessorArray(byteBufferProvider, ipv4Set, stat, ipParser);
        };

        FileProcessor fileProcessor = settings.numThreads == 0 ?
                new SingleThreadedFileProcessor(settings.bufferSize, processorFactory, settings.fileSizeLimit, log) :
                new MultiThreadedFileProcessor(settings.bufferSize, settings.numThreads, processorFactory, settings.timeoutSec, settings.fileSizeLimit, log);


        try {

            Runtime runtime = Runtime.getRuntime();
            long startTime = System.nanoTime();

            // All interesting happens here
            fileProcessor.processFile(fileName);

            double elapsed = ((double)(System.nanoTime() - startTime)) / 1e9;
            log.println("Total lines = " + Utils.decimalFormatWithThousands.format(stat.getLineCount()) +
                    ", elapsed = " + elapsed);

            long unique = printRecalcUniqueCount(ipv4Set, log);
            //ipv4Set.printAll("app3");
            gcAndPrint(runtime, log);

            long usedMem = runtime.totalMemory() - runtime.freeMemory();
            log.printf("Unique = %s, elapsed = %.6f s, used mem = %.3f Mb%n",
                    Utils.decimalFormatWithThousands.format(unique), elapsed, ((double)usedMem) / 1e9);

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
                Utils.decimalFormatWithThousands.format(uniqueRecalc), recalcElapsed / 1e9);
        return uniqueRecalc;
    }

}
