package org.lt.packer.packfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import org.lt.packer.Constants;
import org.lt.packer.cio.ChunkedFileInputStream;

class ZIPPackFileReader implements PackFileReader {
    private ChunkedFileInputStream cfis;
    private ZipInputStream zis;

    ZIPPackFileReader(String basePath) {
        cfis = new ChunkedFileInputStream(basePath);
        zis = new ZipInputStream(cfis);
    }

    @Override
    public void extractFilesTo(File directory) throws IOException {
        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {
            extractFile(directory, entry);

            entry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    @Override
    public void close() throws IOException {
        zis.close();
        cfis.close();
    }


    private byte[] sharedByteBuffer = new byte[Constants.IO_BYTE_BUFFER_SIZE];

    private void extractFile(File directory, ZipEntry entry) throws IOException {
        File file = new File(directory, entry.getName());

        ensureFileWithinDirectory(directory, file, entry);

        if (entry.isDirectory()) {
            createDirectoryIfRequired(file);

            restoreFileMetadata(file, entry);
            return;
        }

        // in case entry for the containing directory wasn't found in this packfile
        createDirectoryIfRequired(file.getParentFile());
        
        // write file content
        FileOutputStream fos = new FileOutputStream(file);
        int length;

        while ((length = zis.read(sharedByteBuffer)) > 0) {
            fos.write(sharedByteBuffer, 0, length);
        }

        fos.close();

        restoreFileMetadata(file, entry);
    }

    private void ensureFileWithinDirectory(File directory, File file, ZipEntry entry) throws IOException {
        String directoryPath = directory.getCanonicalPath();
        String filePath = file.getCanonicalPath();

        if (!filePath.startsWith(directoryPath + File.separator)) {
            throw new IOException(String.format("File entry '%s' is outside of the target directory", entry.getName()));
        }
    }

    private void restoreFileMetadata(File file, ZipEntry entry) {
        // TODO: restore more file metadata
        file.setLastModified(entry.getTime());
    }

    private void createDirectoryIfRequired(File file) throws IOException {
        if (!file.isDirectory() && !file.mkdirs()) {
            // directory may have been concurrently created by another thread
            if (!file.isDirectory()) {
                throw new IOException(String.format("Failed to create directory '%s'", file));
            }
        }
    }
}