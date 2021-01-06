package org.lt.packer.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(
    description = "Compresses folders into split archives",
    subcommands = {
        HelpCommand.class,
        PackCommand.class,
        UnpackCommand.class
    }
)
public class CLI {
    public static int exec(String[] args) {
        return new CommandLine(new CLI()).execute(args);
    }
}
