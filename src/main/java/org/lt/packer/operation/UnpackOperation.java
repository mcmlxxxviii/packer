package org.lt.packer.operation;

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.lt.packer.packfile.PackFile;
import org.lt.packer.packfile.PackFileReader;

public class UnpackOperation {
    private Path source;
    private Path dest;
    private int numWorkers;

    public UnpackOperation(Path source, Path dest, int numWorkers) {
        this.source = source;
        this.dest = dest;
        this.numWorkers = numWorkers;
    }

    public void execute() throws IOException {

        if (source == null || !Files.exists(source) || !Files.isDirectory(source)) {
            throw new IllegalArgumentException(String.format("Given source '%s' either does not exist or is not a directory", source));
        }

        if (dest == null || (Files.exists(dest) && !Files.isDirectory(dest))) {
            throw new IllegalArgumentException(String.format("Given destination '%s' is not a directory", dest));
        }

        ExecutorService service = Executors.newFixedThreadPool(numWorkers);

        try (Stream<Path> stream = Files.list(source)) {
            stream
                .filter( path -> Files.isRegularFile(path) && path.toString().endsWith(".0") )       // a hack to quickly locate the starting chunks of the packfiles
                .map( path -> path.toAbsolutePath().toString() )
                .map( p -> p.substring(0, p.length() - 2) )                                          // removing the '.0' portion from the end
                .forEach( packfilePath -> service.submit(new UnpackingWorker(service, packfilePath, dest)) );
        }

        service.shutdown();
    }
}


class UnpackingWorker implements Runnable {
    private final ExecutorService service;

    private final String packfilePath;
    private final Path dest;

    private final Logger logger = Logger.getLogger("tool.operation.unpack");

    public UnpackingWorker(ExecutorService service, String packfilePath, Path dest) {
        this.service = service;

        this.packfilePath = packfilePath;
        this.dest = dest;
    }

    @Override
    public void run() {
        logger.log(Level.FINE, String.format("unpacking %s", Paths.get(packfilePath).getFileName().toString()));

        try (PackFileReader pfr = PackFile.getPackFileReader(packfilePath)) {

            pfr.extractFilesTo(dest.toFile());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Something went wrong!", e);
            service.shutdownNow();
        }
    }

}