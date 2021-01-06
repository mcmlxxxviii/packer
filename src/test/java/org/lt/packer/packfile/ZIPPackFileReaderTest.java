package org.lt.packer.packfile;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Assert;

import org.lt.packer.testutils.TestUtils;

public class ZIPPackFileReaderTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testHappyExtract() throws IOException {
        File source = TestUtils.getFileFromResources("lorem/packed");
        String packfilePath = new File(source, "archive.zip").getPath();

        try (ZIPPackFileReader pfr = new ZIPPackFileReader(packfilePath)) {
            pfr.extractFilesTo(folder.getRoot());
        }

        TestUtils.assertDirectoriesEqual(TestUtils.getFileFromResources("lorem/unpacked"), folder.getRoot());
    }

    @Test(expected = IOException.class)
    public void testEntryOutsideDest() throws IOException {
        File source = TestUtils.getFileFromResources("malum/packed");
        String packfilePath = new File(source, "archive.zip").getPath();

        try (ZIPPackFileReader pfr = new ZIPPackFileReader(packfilePath)) {
            pfr.extractFilesTo(folder.getRoot());
        }
    }
}