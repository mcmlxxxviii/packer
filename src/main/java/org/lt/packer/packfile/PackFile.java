package org.lt.packer.packfile;

import java.io.File;
import java.io.IOException;

import org.lt.packer.Constants;

/*
 * A packfile is an archive split into multiple smaller files 
 * each more than `chunkSize` bytes
 */
public class PackFile {
    public static PackFileReader getPackFileReader(String base) {
        return new ZIPPackFileReader(base);
    }

    public static PackFileWriter getPackFileWriter(String base, long chunkSize) {
        return new ZIPPackFileWriter(base, chunkSize);
    }

    public static String getFileExtension() {
        return Constants.PACKFILE_EXTENSION;
    }
}