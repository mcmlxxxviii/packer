package org.lt.packer.operation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Assert;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileWriter;
import org.lt.packer.Constants;
import org.lt.packer.testutils.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PackFile.class)
public class PackOperationTest {
    @Test
    public void testExecute() throws IOException {
        Path source = TestUtils.getFileFromResources("lorem/unpacked").toPath();
        Path dest = folder.getRoot().toPath().resolve("dir");

        PackFileWriter mockPFW = mock(PackFileWriter.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(PackFile.getPackFileWriter(anyString(), anyLong())).thenReturn(mockPFW);

        new PackOperation(source, dest, 4, 3).execute();

        PowerMockito.verifyStatic(PackFile.class, times(3));
        PackFile.getPackFileWriter(anyString(), eq(4L * Constants.MB_IN_BYTES));

        List<Path> paths;

        try (Stream<Path> stream = Files.walk(source)) {
            paths = stream
                .filter(path -> !source.equals(path))
                .collect(Collectors.toList());
        }

        for (Path path : paths) {
            verify(mockPFW, times(1)).writeFile(eq(path.toFile()), eq(source.relativize(path).toString()));
        }

        Assert.assertTrue("destination directory was created", Files.isDirectory(dest));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testSourceIsNull() throws IOException {
        new PackOperation(null, Paths.get("to"), 4, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestIsNull() throws IOException {
        new PackOperation(folder.getRoot().toPath(), null, 4, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceDoesNotExist() throws IOException {
        Path source = folder.getRoot().toPath().resolve("404");
        Path dest = Paths.get("to");

        new PackOperation(source, dest, 4, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceIsRegularFile() throws IOException {
        Path source = folder.newFile().toPath();
        Path dest = Paths.get("to");

        new PackOperation(source, dest, 4, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestIsRegularFile() throws IOException {
        Path source = folder.newFolder().toPath();
        Path dest = folder.newFile().toPath();

        new PackOperation(source, dest, 4, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestIsNotEmpty() throws IOException {
        folder.newFile();

        Path source = TestUtils.getFileFromResources("lorem/unpacked").toPath();
        Path dest = folder.getRoot().toPath();

        new PackOperation(source, dest, 4, 3).execute();
    }
}