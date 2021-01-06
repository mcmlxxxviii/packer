package org.lt.packer.cli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.lt.packer.operation.UnpackOperation;

@Command(name = "unpack", description = "extracts files/folder from the compressed archives generated by this tool")
class UnpackCommand implements Callable<Integer> {
    @Option(names = "--parallelism", paramLabel="<number of workers>", defaultValue = "5", description = "number of worker threads that start extracting, default is ${DEFAULT-VALUE}")
    private int numThreads;

    @Parameters(index = "0", paramLabel="<source>", description = "directory containing the archive files")
    private Path source;

    @Parameters(index = "1", paramLabel="<destination>", description = "output directory to place the extracted files/folders")
    private Path dest;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() throws Exception {
        try {
            new UnpackOperation(source, dest, numThreads).execute();
        } catch (Exception e) {
            System.err.println(e);
            return 1;
        }

        return 0; // exit code
    }
}