package org.lt.packer.operation;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileReader;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PackFile.class)
public class UnpackingWorkerTest {
    @Before
    public void setup() {
        Logger.getLogger("tool").setLevel(Level.OFF);
    }

    @Test
    public void testRunHappy() throws IOException {

        PackFileReader mockPFR = mock(PackFileReader.class);
        ExecutorService mockES = mock(ExecutorService.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(PackFile.getPackFileReader(anyString())).thenReturn(mockPFR);

        Path dest = Paths.get("to");

        new UnpackingWorker(mockES, "path/to/pack/file", dest).run();

        PowerMockito.verifyStatic(PackFile.class);
        PackFile.getPackFileReader("path/to/pack/file");

        verify(mockPFR, times(1)).extractFilesTo(eq(dest.toFile()));
        verifyNoInteractions(mockES);

    }

    @Test
    public void testRunErr() throws IOException {

        PackFileReader mockPFR = mock(PackFileReader.class);
        ExecutorService mockES = mock(ExecutorService.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(PackFile.getPackFileReader(anyString())).thenReturn(mockPFR);

        doThrow(IOException.class).when(mockPFR).extractFilesTo(any());

        new UnpackingWorker(mockES, "path/to/pack/file", Paths.get("to")).run();

        verify(mockES, times(1)).shutdownNow();
    }
}