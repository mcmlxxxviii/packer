package org.lt.packer.packfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import org.lt.packer.Constants;
import org.lt.packer.cio.ChunkedFileOutputStream;

class ZIPPackFileWriter implements PackFileWriter {
    private ChunkedFileOutputStream cfos;
    private ZipOutputStream zos;

    ZIPPackFileWriter(String basePath, long chunkSize) {
      cfos = new ChunkedFileOutputStream(basePath, chunkSize);
      zos = new ZipOutputStream(cfos);
    }

    private byte[] sharedByteBuffer = new byte[Constants.IO_BYTE_BUFFER_SIZE];

    @Override
    public void writeFile(File file, String withPath) throws IOException {
        if (file.isDirectory()) {
            if (!withPath.endsWith("/")) {
                withPath += "/";
            }

            zos.putNextEntry(getEntry(file, withPath));
            zos.closeEntry();

            return;
        }

        zos.putNextEntry(getEntry(file, withPath));

        FileInputStream fis = new FileInputStream(file);
        int length;

        while ((length = fis.read(sharedByteBuffer)) >= 0) {
            zos.write(sharedByteBuffer, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    private ZipEntry getEntry(File file, String withPath) {
        ZipEntry entry = new ZipEntry(withPath);

        // TODO: add more file metadata
        entry.setTime(file.lastModified());

        return entry;
    }

    @Override
    public void close() throws IOException {
        zos.close();
        cfos.close();
    }
}