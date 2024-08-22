package ips.binaryfiles;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides next chunk of data (e.g. from a file) returning null at the end
 */
public interface ByteBufferProvider {
    ByteBuffer getNextBuffer() throws IOException;
}
