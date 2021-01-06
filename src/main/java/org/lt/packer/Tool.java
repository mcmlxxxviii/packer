package org.lt.packer;

import org.lt.packer.cli.CLI;

/**
 * packer!
 *
 */
public class Tool {
    public static void main(String[] args) {
        int exitCode = CLI.exec(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
