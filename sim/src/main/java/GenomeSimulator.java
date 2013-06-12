import org.apache.log4j.Logger;
import org.apache.commons.cli.*;
import org.lcsb.lu.igcsa.FragmentInsilicoGenome;
import org.lcsb.lu.igcsa.KaryotypeInsilicoGenome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * sim
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenomeSimulator
  {
  static Logger log = Logger.getLogger(GenomeSimulator.class.getName());

  private ApplicationContext context;
  private Properties genomeProperties;

  private String genomeName;
  private boolean overwriteGenome = false;

  public static void main(String[] args) throws Exception
    {
    final long startTime = System.currentTimeMillis();

    new GenomeSimulator(args);

    final long elapsedTimeMillis = System.currentTimeMillis() - startTime;
    log.info("Elapsed time (seconds): " + elapsedTimeMillis / 1000);
    }

  protected GenomeSimulator(String args[]) throws Exception
    {
    CommandLine cl = parseCommandLine(args);
    context = new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-config.xml", "classpath*:/conf/genome.xml", "classpath*:/conf/database-config.xml"});
    init();

    // Genome name, overwrite
    if (cl.hasOption('n') || cl.hasOption("name"))
      genomeName = cl.getOptionValue('n');
    if (cl.hasOption('o') || cl.hasOption("overwrite"))
      overwriteGenome = true;

    setupGenomeDirectory(genomeName, overwriteGenome);

    if (cl.hasOption("f"))
      {
      FragmentInsilicoGenome insGen = new FragmentInsilicoGenome(context, cl);
      insGen.applyMutations();
      }

    if (cl.hasOption("s"))
      {
      KaryotypeInsilicoGenome karGen = new KaryotypeInsilicoGenome(context, cl);
      karGen.applyMutations();
      }

    log.info("FINISHED mutating genome " + genomeName);
    }


  // Create the directory where all fasta and mutation files will write
  private void setupGenomeDirectory(String name, boolean overwrite) throws IOException
    {
    File genomeDirectory = new File(genomeProperties.getProperty("dir.insilico"), name);

    if (genomeDirectory.exists() && overwrite)
      {
      log.warn("Overwriting " + genomeDirectory.getAbsolutePath());
      org.apache.commons.io.FileUtils.deleteDirectory(genomeDirectory);
      }
    else if (genomeDirectory.exists() && !overwrite)
      throw new IOException(genomeDirectory + " exists, cannot overwrite");

    genomeDirectory.mkdir();
    }


  // Variable initialization. Most of it is done in the Spring configuration files.
  private void init()
    {
    // be nice to autowire this so I don't have to make calls into Spring but not important for now
    genomeProperties = (Properties) context.getBean("genomeProperties");

    if (!genomeProperties.containsKey("dir.insilico") ||
        !genomeProperties.containsKey("dir.assembly") ||
        !genomeProperties.containsKey("dir.karyotype") ||
        !genomeProperties.containsKey("dir.tmp"))
      {
      log.error("One of the directory definitions (dir.insilico, dir.assembly, dir.karyotype, dir.tmp) are missing from the properties file. Aborting.");
      System.exit(-1);
      }
    }


  // exactly what it sounds like...
  private CommandLine parseCommandLine(String[] args)
    {
    Options options = new Options();
    options.addOption("n", "name", true, "Genome directory name, if not provided a random name is generated.");
    options.addOption("o", "overwrite", false, "Overwrite genome directory if name already exists. [false]");
    options.addOption("t", "threads", true, "Number of concurrent threads. Each thread handles a single chromosome. [5]");
    options.addOption("c", "chromosome", true, "List of chromosomes to use/mutate, comma-separated (e.g.  21,3," +
        "X). If not included chromosomes will be determined by \n" + "the fasta files found in the" +
        " dir.assembly directory (see genome.properties).");
    options.addOption("s", "structural-variation", false, "Apply structural variations to genome. [false]");
    options.addOption("f", "fragment-variation", false, "Apply small scale (fragment) variation to genome. [false]");

    options.addOption("h", "help", false, "print usage help");

    CommandLineParser clp = new BasicParser();
    CommandLine cl = null;

    try
      {
      cl = clp.parse(options, args);
      HelpFormatter help = new HelpFormatter();
      if (cl.hasOption('h') || cl.hasOption("help"))
        {
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      if (!cl.hasOption('s') && !cl.hasOption('f'))
        {
        System.out.println("One of the following (or both) options is required: -s or -f");
        help.printHelp("<jar file>", options);
        System.exit(0);
        }
      }
    catch (ParseException e)
      {
      log.error(e);
      }

    log.info("Running " + this.getClass().getName() + " with the following parameters: ");
    for (Option opt : cl.getOptions())
      log.info("\t-" + opt.getLongOpt() + " '" + opt.getValue("true") + "'");

    return cl;
    }


  }
