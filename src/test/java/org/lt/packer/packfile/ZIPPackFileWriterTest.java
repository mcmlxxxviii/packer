package org.lt.packer.packfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Assert;

import org.lt.packer.Constants;
import org.lt.packer.testutils.TestUtils;

public class ZIPPackFileWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testHappyWrite() throws IOException {
        Path source = TestUtils.getFileFromResources("lorem/unpacked").toPath();

        List<Path> paths;

        try (Stream<Path> stream = Files.walk(source)) {
            paths = stream
                .filter(path -> !source.equals(path))
                .sorted()
                .collect(Collectors.toList());
        }

        String packfilePath = new File(folder.getRoot(), "archive.zip").getPath();
        FileTime lastModifiedTime = FileTime.fromMillis(1607467460000L);

        try (ZIPPackFileWriter pfw = new ZIPPackFileWriter(packfilePath, 2 * Constants.MB_IN_BYTES)) {
            for (Path p : paths) {
                Files.setLastModifiedTime(p, lastModifiedTime);

                pfw.writeFile(p.toFile(), source.relativize(p).toString());
            }
        }

        TestUtils.assertDirectoriesEqual(TestUtils.getFileFromResources("lorem/packed"), folder.getRoot());
    }
}