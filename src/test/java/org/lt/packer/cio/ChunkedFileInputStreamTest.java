package org.lt.packer.cio;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
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

public class ChunkedFileInputStreamTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBasicReads() throws IOException {
        String base = folder.newFile().getPath();

        Files.write(Paths.get(base + ".0"), new byte[]{4, 93, 21, 51, 29, 2, 72, 37});

        try (InputStream cfis = new ChunkedFileInputStream(base)) {
            Assert.assertEquals(4, cfis.read());

            byte[] b = new byte[]{0, 0, 0, 0, 0};
            
            Assert.assertFalse(cfis.markSupported());

            Assert.assertEquals(3, cfis.read(b, 2, 3));
            Assert.assertArrayEquals(new byte[]{0, 0, 93, 21, 51}, b);

            Assert.assertNotEquals(-1, cfis.available());

            Assert.assertEquals(4, cfis.read(b));
            Assert.assertArrayEquals(new byte[]{29, 2, 72, 37, 51}, b);

            Assert.assertEquals(-1, cfis.read());
            Assert.assertEquals(-1, cfis.read(b));

            Assert.assertEquals(0, cfis.available());
        }
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        String base = folder.newFile().getPath();

        Files.write(Paths.get(base + ".0"), new byte[]{4, 93, 21, 51, 29, 2, 72, 37});

        try (InputStream cfis = new ChunkedFileInputStream(base)) {
            cfis.close();
            cfis.read(new byte[]{0});
        }
    }

    @Test
    public void testOneChunkedFile() throws IOException, NoSuchAlgorithmException {
        testChunkedFileReads(10, new int[]{73});
    }

    @Test
    public void testAbsentChunkedFiles() throws IOException, NoSuchAlgorithmException {
        testChunkedFileReads(500, new int[]{});
    }

    @Test
    public void testMultipleChunkedFiles() throws IOException, NoSuchAlgorithmException {
        testChunkedFileReads(500, new int[]{1024, 512, 826});
    }

    @Test
    public void testMultipleChunkedFilesHugeBatch() throws IOException, NoSuchAlgorithmException {
        testChunkedFileReads(10000, new int[]{1024, 1024, 1024, 674});
    }

    private void testChunkedFileReads(int byteBatchSize, int[] fileSizes)
        throws IOException, NoSuchAlgorithmException {

        Random random = new Random();
        byte[] byteBuffer = new byte[byteBatchSize];

        String base = folder.newFile().getPath();

        MessageDigest adigest = MessageDigest.getInstance("SHA-256");
        int totalSize = 0;

        for (int i = 0; i < fileSizes.length; i++) {
            try (FileOutputStream fos = new FileOutputStream(base + "." + i)) {
                for (int bw = 0; bw < fileSizes[i]; bw += byteBatchSize) {
                    int length = Math.min(byteBatchSize, fileSizes[i] - bw);

                    random.nextBytes(byteBuffer);

                    fos.write(byteBuffer, 0, length);
                    adigest.update(byteBuffer, 0, length);
                }
            }

            totalSize += fileSizes[i];
        }
    
        MessageDigest bdigest = MessageDigest.getInstance("SHA-256");

        try (InputStream cfis = new ChunkedFileInputStream(base)) {
            int length;

            while ((length = cfis.read(byteBuffer)) >= 0) {
                bdigest.update(byteBuffer, 0, length);
                totalSize -= length;
            }
        }

        Assert.assertEquals("file size should equal", 0, totalSize);
        Assert.assertTrue("digest should equal", MessageDigest.isEqual(adigest.digest(), bdigest.digest()));
    }
}