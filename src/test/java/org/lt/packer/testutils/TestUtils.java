package org.lt.packer.testutils;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.Assert;

public class TestUtils {
    public static File getFileFromResources(String path) throws FileNotFoundException {
        URL url = TestUtils.class.getClassLoader().getResource(path);
        if (url == null) {
            throw new FileNotFoundException();
        }

        return new File(url.getPath());
    }

    public static void assertDirectoriesEqual(File fileA, File fileB) throws IOException {
        Path aPath = fileA.toPath();
        Path bPath = fileB.toPath();

        HashMap<String, Path> map = new HashMap<String, Path>();

        try (Stream<Path> paths = Files.walk(aPath)) {
            paths
                .filter(path -> !aPath.equals(path))
                .forEach(path -> map.put(aPath.relativize(path).toString(), path) );
        }

        List<Path> paths;

        try (Stream<Path> stream = Files.walk(bPath)) {
            paths = stream
                .filter(path -> !bPath.equals(path))
                .collect(Collectors.toList());
        }

        for (Path path : paths) {
            String ps = bPath.relativize(path).toString();

            Path other = map.get(ps);

            Assert.assertNotNull("second directory has more files/folders -- " + ps, other);

            Assert.assertEquals("file content mismatch", Files.isRegularFile(path), Files.isRegularFile(path));
            Assert.assertEquals("files not of same type", Files.isDirectory(path), Files.isDirectory(path));

            if (Files.isRegularFile(path)) {
                Assert.assertArrayEquals("size diff", Files.readAllBytes(path), Files.readAllBytes(other));
            }

            map.remove(ps);
        }

        Assert.assertArrayEquals("first directory has more files/folders", new String[]{}, map.keySet().toArray());
    }
}