package org.lt.packer.cli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.lt.packer.operation.PackOperation;

@Command(name = "pack", description = "compresses files and folders of a given source directory into archive files not exceeding a certain size")
class PackCommand implements Callable<Integer> {
    @Option(names = "--parallelism", paramLabel="<number of workers>", defaultValue = "5", description = "number of worker threads that start compressing, default is ${DEFAULT-VALUE}")
    private int numThreads;

    @Option(names = "--chunk-size", paramLabel="<chunk size>", defaultValue = "2", description = "max size of file chunks, default is ${DEFAULT-VALUE}")
    private int chunkSize;

    @Parameters(index = "0", paramLabel="<source>", description = "source directory to compress")
    private Path source;

    @Parameters(index = "1", paramLabel="<destination>",  description = "output directory to place the compressed files")
    private Path dest;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() throws Exception {
        try {
            new PackOperation(source, dest, chunkSize, numThreads).execute();
        } catch (Exception e) {
            System.err.println(e);
            return 1;
        }

        return 0; // exit code
    }
}