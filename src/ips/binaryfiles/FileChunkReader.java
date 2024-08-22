package ips.binaryfiles;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Reads large file by chunks using memory mapping for fastest access
 * Specifically cares that each chunk ends with EOL
 * For the sake of optimization this class assumes that EOL is '\n'
 */
public class FileChunkReader implements ByteBufferProvider {
    private final FileChannel fileChannel;
    private final long bufferSize;
    private final long totalFileSize;
    private long currentPosition = 0;
    private final long fileSize;
    private int nextBytesWithoutEOL;
    private static final int SLICE_SIZE = 128;
    private final PrintStream log;

    public FileChunkReader(FileChannel fileChannel, long bufferSize, long fileSizeLimit, PrintStream log) throws IOException {
        this.fileChannel = fileChannel;
        this.bufferSize = bufferSize;
        totalFileSize = fileChannel.size();
        this.fileSize = Math.min(totalFileSize, fileSizeLimit);
        this.log = log;
    }

    @Override
    public synchronized MappedByteBuffer getNextBuffer() throws IOException {
        if (currentPosition >= fileSize) {
            log.println("cur pos " + currentPosition + " >= file size " + fileSize);
            return null;
        } else { // when we read only a part of file we should skip the last incomplete line
            if (currentPosition + nextBytesWithoutEOL >= fileSize && fileSize < totalFileSize) {
                log.println("skipping last  " + nextBytesWithoutEOL + " bytes when cur pos "
                    + currentPosition + ", file size limit " + fileSize);
                return null;
            }
        }

        // Read the initial chunk
        long size = Math.min(bufferSize, fileSize - currentPosition);
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, currentPosition, size);

        // Adjust the end position to the next '\n' before the buffer's end
        int lastEOLIndex = findLastEOL(buffer);
        if (lastEOLIndex != -1) {
            nextBytesWithoutEOL = buffer.limit() - lastEOLIndex - 1;
            buffer.limit(lastEOLIndex + 1); // Set the limit to include the last '\n'
        } else { // file has incorrect format
            boolean notEOF = currentPosition + buffer.limit() < totalFileSize;
            if (notEOF) {
                throw new RuntimeException("No EOL found in buffer that starts from " + currentPosition
                    + " and has length " + buffer.limit());
            }
        }

        // Update currentPosition for the next chunk
        currentPosition += buffer.limit() - buffer.position();

        return buffer;
    }

    // Return -1 if no '\n' is found in the entire buffer
    private static int findLastEOL(MappedByteBuffer buffer) {
        int position = buffer.limit();

        while (position > 0) {
            int sliceStart = Math.max(0, position - SLICE_SIZE);
            int sliceLength = position - sliceStart;

            // Get the last SLICE_SIZE bytes as a byte array
            byte[] slice = new byte[sliceLength]; //< TODO: avoid allocation
            int originalPosition = buffer.position();
            buffer.position(sliceStart);
            buffer.get(slice);
            buffer.position(originalPosition); // Reset the buffer position

            // Now search backward in the slice for the last '\n'
            for (int i = slice.length - 1; i >= 0; i--) {
                if (slice[i] == '\n') {
                    return sliceStart + i;
                }
            }

            // Move the position back for the next iteration
            position = sliceStart;
        }

        return -1;
    }
}
