package org.lt.packer.cio;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * This class implements an input stream for reading sequential
 * file/data chunks written by `ChunkedFileOutputStream`
 *
 * @author Lithin Thomas
 */
public class ChunkedFileInputStream extends InputStream {
    private InputStream currentFileInStream;
    private long currentFileIndex = -1;
    private long currentFileBytesRead;

    private String basePath;
    private long currentFileSize;

    private boolean closed;
    private boolean reachedEOF;

    public ChunkedFileInputStream(String basePath) {
        this.basePath = basePath;
    }

    private void nextFile() throws IOException {
        currentFileIndex += 1;
        currentFileBytesRead = 0;

        File currentFile = new File(basePath + "." + currentFileIndex);

        if (!currentFile.exists() || !currentFile.isFile()) {
            reachedEOF = true;
            currentFileInStream = null;

            return;
        }

        currentFileSize = currentFile.length();
        currentFileInStream = new FileInputStream(currentFile);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        ensureOpen();

        if (reachedEOF) {
            return -1;
        }

        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (currentFileInStream == null) {
            nextFile();

            if (reachedEOF) {
                return -1;
            }
        }

        if (currentFileBytesRead + len > currentFileSize) {
            len = (int)(currentFileSize - currentFileBytesRead);
        }

        len = currentFileInStream.read(b, off, len);
        if (len == -1) {
            throw new EOFException("Unexpected end of file");
        }

        currentFileBytesRead += len;

        if (currentFileBytesRead >= currentFileSize) {
            currentFileInStream.close();

            currentFileInStream = null;
        }

        return len;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            if (currentFileInStream != null) {
                currentFileInStream.close();
            }

            closed = true;
        }
    }

    @Override
    public int available() throws IOException {
        ensureOpen();

        return reachedEOF ? 0 : (currentFileInStream != null ? currentFileInStream.available() : 0);
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    private byte[] singleByteBuf = new byte[1];

    @Override
    public int read() throws IOException {
        return read(singleByteBuf, 0, 1) == -1 ? -1 : Byte.toUnsignedInt(singleByteBuf[0]);
    }
}