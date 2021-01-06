# packer

A command line tool to compress contents of a specified folder into a collection of files, each not exceeding a certain 
given size. The program also provides a subcommand to extract the original contents from the compressed files.

### Requirements
Tested with
- Java 15
- Maven 3.6
- Linux

Might work with Java 8+

### Build
As this is a Maven project, run this command from the project root to test & build the artifacts
```sh
mvn package
```

### Run
The program is packaged as a JAR file (with dependencies included)

See below for how to pass in arguments & options
```
Usage: <main class> pack [-h] [--chunk-size=<chunk size>]
                         [--parallelism=<number of workers>] <source>
                         <destination>
compresses files and folders of a given source directory into archive files not
exceeding a size
      <source>        source directory to compress
      <destination>   output directory to place the compressed files
      --chunk-size=<chunk size>
                      max size of file chunks, default is 2
  -h, --help          display this help message
      --parallelism=<number of workers>
                      number of worker threads that start compressing, default
                        is 5


Usage: <main class> unpack [-h] [--parallelism=<number of workers>] <source>
                           <destination>
extracts files/folder from the compressed archives generated by this tool
      <source>        directory containing the archive files
      <destination>   output directory to place the extracted files/folders
  -h, --help          display this help message
      --parallelism=<number of workers>
                      number of worker threads that start extracting, default
                        is 5
```

For example,

Compress
```sh
java -jar target/packer-1.0.0-jar-with-dependencies.jar pack source/ output/
```

Extract
```sh
java -jar target/packer-1.0.0-jar-with-dependencies.jar unpack output/ directory/
```

### How
The compress files created are simply ZIP archives which may (depending on their size) be split into multiple
files. This is achieved by passing a custom `OutputStream` implementation to `java.util.zip.ZipOutputStream` 
which essentially "rotates" the file (chunk) when it has reached the max size limit. The reverse happens while
extracting (reading). Names of these chunks are suffixed with a integer to identify the sequence in which they 
were written / have to be read back.

The tool employs a parallelism technique to speed up compression/extraction operations. It distributes the files 
to compress/inflate among a few worker threads (number of worker threads can be controlled via a command line 
switch). While compessing, each thread builds a single archive during it's entire lifetime.


### Limitations
- The tool cannot store symbolic links in the compressed files
- The file at present makes no provision to support multiple archive formats at the same time
- As files may get distributed unevenly (for example, few large files going to the same thread), there exists a
  trade-off between speed (parallelism) and compactness (number of files outputted). Compression run with the a
  single thread will most likely output the least number of files as all the content is put in the same archive.
  Running it with a high number of worker threads may generate a lot of small archives.
- Paralleism during extraction will be limited to the number of archives in the output. For example, if the 
  compressed files were built by a single threaded run (which means only one archive is present in the output), 
  then only one thread can be used to extract all of contents.