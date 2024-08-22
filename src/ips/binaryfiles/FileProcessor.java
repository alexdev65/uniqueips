package ips.binaryfiles;

import java.io.IOException;

public interface FileProcessor {
    void processFile(String filePath) throws IOException;
}
