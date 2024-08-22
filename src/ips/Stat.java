package ips;

import java.io.PrintStream;

/**
 * Keeps and logs statistics for the data being processed. Stops processing when maximum number of lines is reached.
 */
public class Stat {
    private final Runtime runtime = Runtime.getRuntime();
    private long lineCount = 0;
    private long lastReportLineCount = 0;
    private final long startTime = System.nanoTime();
    // defines how often current stats will be logged
    private final long linesPerStatsPrint;
    private final long maxLines;
    private final PrintStream log;
    private long prevSpeedTime = startTime;
    private long prevSpeedLines = 0;
    private static final long speedMeasureTimeNs = 1_000_000_000;
    private long speed = 0;

    public Stat(long maxLines, PrintStream log, long linesPerStatsPrint) {
        this.maxLines = maxLines;
        this.log = log;
        this.linesPerStatsPrint = linesPerStatsPrint;
    }

    public synchronized void update(long lines, long uniqueCount) throws StopException {
        lineCount += lines;
        long linesSinceLastReport = lineCount - lastReportLineCount;
        if (linesSinceLastReport >= linesPerStatsPrint) {
            lastReportLineCount = lineCount;
            //runtime.gc();
            long curTime = System.nanoTime();
            double elapsedSec = ((double) (curTime - startTime)) / 1e9;
            if (curTime - prevSpeedTime >= speedMeasureTimeNs) {
                speed = (long) ((lineCount - prevSpeedLines) * 1e9 / (curTime - prevSpeedTime));
                prevSpeedTime = curTime;
                prevSpeedLines = lineCount;
            }
            long avgSpeed = (long) ((lineCount) * 1e9 / (curTime - startTime));
            long totMem = runtime.totalMemory();
            long usedMem = totMem - runtime.freeMemory();
            log.printf("Lines=%13s, unique=%13s, elapsed=%.1fs, mem=%4dM, spd=%6d kl/s, avg.spd=%6d kl/s" +
                            ", tot mem=%4dM, max mem=%5dM%n",
                    Utils.decimalFormatWithThousands.format(lineCount),
                    Utils.decimalFormatWithThousands.format(uniqueCount),
                    elapsedSec, usedMem / 1_000_000, speed / 1000, avgSpeed / 1000,
                    totMem / 1_000_000, runtime.maxMemory() / 1_000_000);
            if (lineCount >= maxLines) {
                throw new StopException();
            }
        }
    }

    public synchronized long getLineCount() {
        return lineCount;
    }
}
