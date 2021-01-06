package org.lt.packer.cio;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class implements an output stream for writing sequential
 * file/data chunks each not more than `chunkSize` bytes. Each
 * chunk's name is suffixed with '.0', '.1', and so on..
 *
 * @author Lithin Thomas
 */
public class ChunkedFileOutputStream extends OutputStream {
    private OutputStream currentFileOutStream;
    private long currentFileIndex = -1;
    private long currentFileBytes;

    private String basePath;
    private long chunkSize;

    private boolean closed;

    public ChunkedFileOutputStream(String basePath, long chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize is invalid");
        }

        this.basePath = basePath;
        this.chunkSize = chunkSize;
    }

    private void newFile() throws IOException {
        currentFileIndex += 1;
        currentFileBytes = 0;

        currentFileOutStream = new FileOutputStream(basePath + "." + currentFileIndex);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        ensureOpen();

        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        while (len > chunkSize) {
            write(b, off, (int)chunkSize);

            off += (int)chunkSize;
            len -= (int)chunkSize;
        }

        if (len == 0) {
            return;
        }

        if (currentFileOutStream == null) {
            newFile();
        }

        int l = len;

        if (currentFileBytes + len > chunkSize) {
            l = (int)(chunkSize - currentFileBytes);
        }

        currentFileOutStream.write(b, off, l);

        currentFileBytes += l;

        if (currentFileBytes >= chunkSize) {
            currentFileOutStream.flush();
            currentFileOutStream.close();

            currentFileOutStream = null;
        }

        if (l < len) {
            write(b, off + l, len - l);
        }
    }
  
    @Override
    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)(b & 0xff);
        write(buf, 0, 1);
    }

    @Override
    public void flush() throws IOException {
        if (currentFileOutStream != null) {
            currentFileOutStream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            if (currentFileOutStream != null) {
                currentFileOutStream.close();
            }

            closed = true;
        }
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }
}