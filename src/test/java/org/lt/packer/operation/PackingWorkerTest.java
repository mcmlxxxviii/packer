package org.lt.packer.operation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.Rule;
import org.junit.Before;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.junit.runner.RunWith;  
import org.powermock.api.mockito.PowerMockito;  
import org.powermock.core.classloader.annotations.PrepareForTest;  
import org.powermock.modules.junit4.PowerMockRunner;  

import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileWriter;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(PackFile.class)
public class PackingWorkerTest {
    @Before
    public void setup() {
        Logger.getLogger("tool").setLevel(Level.OFF);
    }

    @Test
    public void testRunHappy() throws IOException, InterruptedException {

        PackFileWriter mockPFW = mock(PackFileWriter.class);
        ExecutorService mockES = mock(ExecutorService.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(PackFile.getPackFileWriter(anyString(), anyLong())).thenReturn(mockPFW);

        Path p1 = Paths.get("from/a/file");
        Path p2 = Paths.get("from/the/heart");

        LinkedBlockingQueue<Path> queue = new LinkedBlockingQueue<Path>();
        queue.put(p1);
        queue.put(p2);

        new PackingWorker(mockES, queue, Paths.get("from"), Paths.get("to"), 4096).run();

        PowerMockito.verifyStatic(PackFile.class);
        PackFile.getPackFileWriter(anyString(), eq(4096L));

        verify(mockPFW, times(1)).writeFile(eq(p2.toFile()), eq("the/heart"));
        verify(mockPFW, times(1)).writeFile(eq(p1.toFile()), eq("a/file"));

        verifyNoInteractions(mockES);

    }

    @Test
    public void testRunErr() throws IOException, InterruptedException {

        PackFileWriter mockPFW = mock(PackFileWriter.class);
        ExecutorService mockES = mock(ExecutorService.class);

        PowerMockito.mockStatic(PackFile.class);
        PowerMockito.when(PackFile.getPackFileWriter(anyString(), anyLong())).thenReturn(mockPFW);

        LinkedBlockingQueue<Path> queue = new LinkedBlockingQueue<Path>();
        queue.put(Paths.get("from/a/file"));

        doThrow(IOException.class).when(mockPFW).writeFile(any(), any());

        new PackingWorker(mockES, queue, Paths.get("from"), Paths.get("to"), 4096).run();

        verify(mockES, times(1)).shutdownNow();
    }
}