package org.lt.packer;

public interface Constants {

    /*
     * Controls speed of reads/writes
     */
    static int IO_BYTE_BUFFER_SIZE = 20 * 1024;

    static int MB_IN_BYTES = 1048576;

    static String PACKFILE_EXTENSION = ".zip";

    static long MAX_QUEUE_RECEIVE_TIMEOUT_MS = 200L;
}