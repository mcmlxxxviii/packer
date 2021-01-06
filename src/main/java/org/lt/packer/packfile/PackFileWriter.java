package org.lt.packer.packfile;

import java.io.File;
import java.io.IOException;

public interface PackFileWriter extends AutoCloseable {
    void writeFile(File file, String withPath) throws IOException;

    void close() throws IOException;
}