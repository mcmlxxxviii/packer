package org.lt.packer.packfile;

import java.io.File;
import java.io.IOException;

public interface PackFileReader extends AutoCloseable {
    void extractFilesTo(File directory) throws IOException;

    void close() throws IOException;
}