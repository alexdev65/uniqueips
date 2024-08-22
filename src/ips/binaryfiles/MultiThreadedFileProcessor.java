package ips.binaryfiles;

import ips.StopException;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MultiThreadedFileProcessor implements FileProcessor {

    private final int bufferSize;
    private final int numThreads;
    private final ChunkProcessorFactory processorFactory;
    private final int timeoutSec;
    private final long fileSizeLimit;

    public MultiThreadedFileProcessor(int bufferSize, int numThreads, ChunkProcessorFactory processorFactory, int timeoutSec, long fileSizeLimit) {
        this.bufferSize = bufferSize;
        this.numThreads = numThreads;
        this.processorFactory = processorFactory;
        this.timeoutSec = timeoutSec;
        this.fileSizeLimit = fileSizeLimit;
    }

    @Override
    public void processFile(String filePath) throws IOException {
        TaskManager taskManager = new TaskManager(numThreads, timeoutSec);

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            try {
                var chunkReader =
                        new FileChunkReader(fileChannel, bufferSize, fileSizeLimit);

                for (int threadNo = 0; threadNo < numThreads; threadNo++) {
                    taskManager.submitTask(processorFactory.createProcessor(chunkReader));
                }

                taskManager.awaitCompletion();

            } catch (StopException stopException) {
                System.out.println("Stop exception caught in processor");
            } catch (Exception e) {
                System.out.println("Exception " + e);
                if (e.getCause() instanceof StopException || e.getCause() instanceof ClosedByInterruptException) {
                    System.out.println("Stop exception caught in processor");
                } else {
                    System.out.println("Unexpected exception " + e);
                }
            } finally {
                taskManager.shutdownGracefully();
            }
        }
    }
}
