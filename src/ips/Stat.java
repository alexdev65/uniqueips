package ips;

import java.io.PrintStream;

/**
 * Keeps statistics for the data being processed. Stops processing when maximum number of lines is reached.
 */
public class Stat {
    private final Runtime runtime = Runtime.getRuntime();
    private long lineCount = 0;
    private long lastReportLineCount = 0;
    private long startTime = System.nanoTime();
    private long prevTime = startTime;
    // defines how often current stats will be logged
    private final long linesPeriod = 1_000_000;
    private final long maxLines;
    private final PrintStream log;

    public Stat(long maxLines, PrintStream log) {
        this.maxLines = maxLines;
        this.log = log;
    }

    public synchronized void update(long lines, long uniqueCount) throws StopException {
        lineCount += lines;
        if (lineCount - lastReportLineCount >= linesPeriod) {
            lastReportLineCount = lineCount;
            //runtime.gc();
            long curTime = System.nanoTime();
            double elapsedSec = ((double) (curTime - startTime)) / 1e9;
            long speed = (long) (linesPeriod * 1e9 / (curTime - prevTime));
            prevTime = curTime;
            long totMem = runtime.totalMemory();
            long usedMem = totMem - runtime.freeMemory();
            log.printf("Lines=%10d, unique=%10d, elapsed=%7.3fs, mem=%4dM, spd=%5d kl/s" +
                            ", tot mem=%4dM, max mem=%5dM%n",
                    lineCount, uniqueCount, elapsedSec, usedMem / 1_000_000, speed / 1000,
                    totMem / 1_000_000, runtime.maxMemory() / 1_000_000);
            if (lineCount >= maxLines) {
                throw new StopException();
            }
        }
    }

    public long getLineCount() {
        return lineCount;
    }
}
