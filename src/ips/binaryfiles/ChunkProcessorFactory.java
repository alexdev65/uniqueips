package ips.binaryfiles;

public interface ChunkProcessorFactory {
    ChunkProcessor createProcessor(ByteBufferProvider byteBufferProvider);
}
