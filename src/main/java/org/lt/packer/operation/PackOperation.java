package org.lt.packer.operation;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.lt.packer.Constants;
import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileWriter;


public class PackOperation {
    private ExecutorService service;
    private BlockingQueue<Path> workQueue;

    private final Path source;
    private final Path dest;

    private final int numWorkers;
    private final int chunkSize;

    public PackOperation(Path source, Path dest, int chunkSize, int numWorkers) {
        this.source = source;
        this.dest = dest;
        this.numWorkers = numWorkers;
        this.chunkSize = chunkSize;
    }

    public void execute() throws IOException {

        if (source == null || !Files.exists(source) || !Files.isDirectory(source)) {
            throw new IllegalArgumentException(String.format("Given source '%s' either does not exist or is not a directory", source));
        }

        if (dest == null || (Files.exists(dest) && !Files.isDirectory(dest))) {
            throw new IllegalArgumentException(String.format("Given destination '%s' is not a directory", dest));
        }

        if (!Files.exists(dest)) {
            Files.createDirectories(dest);
        } else {
            if (isDirectoryNotEmpty(dest)) {
                throw new IllegalArgumentException(String.format("Given destination directory '%s' is not empty", dest));
            }
        }

        service = Executors.newFixedThreadPool(numWorkers);

        workQueue = new LinkedBlockingQueue<Path>();

        for (int i = 0; i < numWorkers; i++) {
            service.submit(new PackingWorker(service, workQueue, source, dest, (long)chunkSize * Constants.MB_IN_BYTES));
        }

        queueAllContainedPaths();

        service.shutdown();
    }


    private boolean isDirectoryNotEmpty(Path path) throws IOException {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.findFirst().isPresent();
        }
    }

    private void queueAllContainedPaths() throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream
                .filter(path -> !source.equals(path))
                .forEach(this::queuePath);
        }
    }

    private void queuePath(Path item) {
        try {
            workQueue.put(item);
        } catch (InterruptedException ex) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}


class PackingWorker implements Runnable {
    private final ExecutorService service;
    private final BlockingQueue<Path> workQueue;

    private final Path source;
    private final Path dest;

    private long chunkSize;

    private final Logger logger = Logger.getLogger("tool.operation.pack");

    public PackingWorker(ExecutorService service, BlockingQueue<Path> workQueue, Path source, Path dest, long chunkSize) {
        this.service = service;
        this.workQueue = workQueue;

        this.source = source;
        this.dest = dest;

        this.chunkSize = chunkSize;
    }

    @Override
    public void run() {
        String packfilePath = dest.resolve(Thread.currentThread().getId() + PackFile.getFileExtension()).toString();

        try (PackFileWriter pfw = PackFile.getPackFileWriter(packfilePath, chunkSize)) {
        
            while (!Thread.currentThread().isInterrupted()) {
                Path path = workQueue.poll(Constants.MAX_QUEUE_RECEIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (path == null) {
                    return;
                }

                logger.log(Level.FINE, String.format("packing %s", source.relativize(path).toString()));

                pfw.writeFile(path.toFile(), source.relativize(path).toString());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Something went wrong!", e);
            service.shutdownNow();
        }
    }
}