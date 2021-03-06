THIS USAGE IS NO LONGER TESTED OR MAINTAINED. HBASE-GENOMES IS THE MAIN APPLICATION NOW.
2014

-----------------------------------------------------------------------------------------------
Disclaimer:

This software is under development and provided as-is. We cannot guarantee that it is free of bugs,
nor has it been tested on all systems.

-----------------------------------------------------------------------------------------------
Requirements:

1. FASTA files from a reference genome.  Currently tested with human reference GRCh37 (any human reference will work).

2. Database filled with either analysis that provides bins separated by GC content and the
variations that are defined. The installation comes with a Derby database pre-defined. MySQL database dump is available
 for download if preferred (derby is recommended for cluster setup).

3. Alter genome.properties file appropriately to change the input FASTA directory (dir.assembly), output mutated genome
 directory (dir.insilico), or database connection information. Provided setup expects FASTA files in the ${build.finalName}-${project.version}/data/FASTA
  directory and all output files will be written to ${build.finalName}-${project.version}/data/Insilico.


Note:

The example installation includes a FASTA file for chromosome 22 from GRCh37 and pre-defined Derby database.

-----------------------------------------------------------------------------------------------

To Run:

Usage: java -jar igcsa.jar <options>

-n, --name        Genome directory name, if not provided a random name is generated.
-o, --overwrite   Overwrite genome directory if name already exists.
-c, --chromosome  List of chromosomes to use/mutate, comma-separated (e.g.  21,3,X). If not included chromosomes will be determined by
the fasta files found in the dir.assembly directory (see genome.properties).
-t, --threads     Number of concurrent threads, default is 5. Each thread handles a single chromosome.
-f, --fragment    Apply fragment (small) level variations, default is true.

-h, --help        Print usage help.


-- Example using the provided shell script: --

# Generate mutations for chromosome 22, overwrite previous named run.
figg.sh -n MyChr17 -o -c 22

# Generate mutations for all chromosomes in the dir.assembly directory using 12 threads.  Don't overwrite.
figg.sh -n MyGenome -t 12


-- Example calling the jar file directly, props.path is the directory where genome.properties and log4j.properties both exist.  The shell
script assumes this to be the same directory as the jar file, but it must be provided when calling the jar directly: --

java -Dprops.path=<properties path> -jar -Xms256m -Xmx1024m ${build.finalName}-${project.version}.jar -n MyGenome -t 12 -f
