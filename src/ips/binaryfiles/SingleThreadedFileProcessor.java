package ips.binaryfiles;

import ips.StopException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SingleThreadedFileProcessor implements FileProcessor {

    private final int bufferSize;
    private final ChunkProcessorFactory processorFactory;
    private final long fileSizeLimit;

    public SingleThreadedFileProcessor(int bufferSize, ChunkProcessorFactory processorFactory, long fileSizeLimit) {
        this.bufferSize = bufferSize;
        this.processorFactory = processorFactory;
        this.fileSizeLimit = fileSizeLimit;
    }

    @Override
    public void processFile(String filePath) throws IOException {

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            try {
                var chunkReader =
                        new FileChunkReader(fileChannel, bufferSize, fileSizeLimit);

                processorFactory.createProcessor(chunkReader).run();

            } catch (StopException stopException) {
                System.out.println("Stop exception caught in processor");
            }
        }
    }
}
