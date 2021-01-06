package org.lt.packer.cio;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Assert;

public class ChunkedFileOutputStreamTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBasicWrites() throws IOException {
        String base = folder.newFile().getPath();

        try (OutputStream cfos = new ChunkedFileOutputStream(base, 1024)) {
            cfos.write(4);
            cfos.write(new byte[]{93, 21, 51, 29, 2});
            cfos.write(new byte[]{19, 72, 37, 46}, 1, 2);

            cfos.flush();
        }

        Assert.assertArrayEquals(
            new byte[]{4, 93, 21, 51, 29, 2, 72, 37},
            Files.readAllBytes(Paths.get(base + ".0"))
        );
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        try (OutputStream cfos = new ChunkedFileOutputStream(folder.newFile().getPath(), 1024)) {
            cfos.close();
            cfos.write(new byte[]{93, 21, 51, 29, 2});
        }
    }

    @Test
    public void testOneChunkedFile() throws IOException, NoSuchAlgorithmException {
        testChunkedWrites(102, 73, 10, new int[]{73});
    }

    @Test
    public void testOneChunkedFileExact() throws IOException, NoSuchAlgorithmException {
        testChunkedWrites(512, 512, 200, new int[]{512});
    }

    @Test
    public void testMultipleChunkedFiles() throws IOException, NoSuchAlgorithmException {
        testChunkedWrites(1024, 2874, 500, new int[]{1024, 1024, 826});
    }

    @Test
    public void testMultipleChunkedFilesHugeBatch() throws IOException, NoSuchAlgorithmException {
        testChunkedWrites(1024, 3746, 10000, new int[]{1024, 1024, 1024, 674});
    }

    private void testChunkedWrites(int chunkSize, int numBytesToWrite, int byteBatchSize, int[] fileSizes)
        throws IOException, NoSuchAlgorithmException {

        MessageDigest adigest = MessageDigest.getInstance("SHA-256");

        Random random = new Random();
        byte[] byteBuffer = new byte[byteBatchSize];

        String base = folder.newFile().getPath();

        try (OutputStream cfos = new ChunkedFileOutputStream(base, chunkSize)) {
            for (int bw = 0; bw < numBytesToWrite; bw += byteBatchSize) {
                int length = Math.min(byteBatchSize, numBytesToWrite - bw);

                random.nextBytes(byteBuffer);

                cfos.write(byteBuffer, 0, length);
                adigest.update(byteBuffer, 0, length);
            }
        }

        MessageDigest bdigest = MessageDigest.getInstance("SHA-256");

        for (int i = 0; i < fileSizes.length; i++) {
            try (FileInputStream fis = new FileInputStream(base + "." + i)) {
                int length, size = 0;

                while ((length = fis.read(byteBuffer)) >= 0) {
                    bdigest.update(byteBuffer, 0, length);
                    size += length;
                }

                Assert.assertEquals(fileSizes[i], size);
            }
        }

        Assert.assertTrue("digest should equal", MessageDigest.isEqual(adigest.digest(), bdigest.digest()));
    }
}