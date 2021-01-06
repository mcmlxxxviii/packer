package org.lt.packer.operation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;  
import org.powermock.core.classloader.annotations.PrepareForTest;  
import org.powermock.modules.junit4.PowerMockRunner;  

import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileReader;
import org.lt.packer.testutils.TestUtils;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(PackFile.class)
public class UnpackOperationTest {
    @Test
    public void testExecute() throws IOException, InterruptedException {
        Path source = TestUtils.getFileFromResources("lorem/packed").toPath();

        PackFileReader mockPFR = mock(PackFileReader.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(
            PackFile.getPackFileReader(eq(source.resolve("archive.zip").toAbsolutePath().toString()))
        ).thenReturn(mockPFR);

        Path dest = Paths.get("to");

        new UnpackOperation(source, dest, 1).execute();

        PowerMockito.verifyStatic(PackFile.class, times(1));
        PackFile.getPackFileReader(anyString());

        verify(mockPFR, times(1)).extractFilesTo(eq(dest.toFile()));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void testSourceIsNull() throws IOException {
        new UnpackOperation(null, Paths.get("to"), 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestIsNull() throws IOException {
        new UnpackOperation(folder.getRoot().toPath(), null, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceDoesNotExist() throws IOException {
        Path source = folder.getRoot().toPath().resolve("404");
        Path dest = Paths.get("to");

        new UnpackOperation(source, dest, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceIsRegularFile() throws IOException {
        Path source = folder.newFile().toPath();
        Path dest = Paths.get("to");

        new UnpackOperation(source, dest, 3).execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestIsRegularFile() throws IOException {
        Path source = folder.newFolder().toPath();
        Path dest = folder.newFile().toPath();

        new UnpackOperation(source, dest, 3).execute();
    }
}