/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;

import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.GenomeResult;
import org.lcsb.lu.igcsa.hbase.tables.KaryotypeIndexResult;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;

import java.util.ArrayList;
import java.util.List;

import static org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil.*;

public class GenerateDerivativeChromosomes extends BWAJob
  {
  static Logger log = Logger.getLogger(GenerateDerivativeChromosomes.class.getName());

  public GenerateDerivativeChromosomes()
    {
    super(new Configuration());
    Option kt = new Option("k", "karyotype", true, "Karyotype name.");
    kt.setRequired(true);
    this.addOptions(kt);
    }

  @Override
  public int run(String[] strings) throws Exception
    {
    log.info("No job to run for " + this.getClass().getSimpleName());
    return 1;
    }

  /*
 What this script actually should do is grab <all> karyotypes for a given parent and spin off jobs to generate each.
 Maybe...
  */
  public void generationKaryotypeGenome(String[] args) throws Exception, ParseException
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());
    if (args.length < 1)
      {
      System.err.println("Usage: " + GenerateDerivativeChromosomes.class.getSimpleName() + " <karyotype name>");
      System.exit(-1);
      }
    String karyotypeName = cl.getOptionValue("k");

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());

    KaryotypeIndexResult karyotypeDef = admin.getKaryotypeIndexTable().getKaryotype(karyotypeName);
    if (karyotypeDef == null)
      throw new Exception("No karyotype found for " + karyotypeName);

    GenomeResult parentGenome = admin.getGenomeTable().getGenome(karyotypeDef.getParentGenome());
    List<ChromosomeResult> chromosomes = admin.getChromosomeTable().getChromosomesFor(parentGenome.getName());

    Path basePath = new Path(Paths.GENOMES.getPath()); // TODO this should probably be an arg
    if (!getJobFileSystem().getUri().toASCIIString().startsWith("hdfs"))
      basePath = new Path("/tmp/" + basePath.toString());

    Path karyotypePath = new Path(basePath, karyotypeName);
    if (karyotypePath.getFileSystem(getConf()).exists(karyotypePath))
      karyotypePath.getFileSystem(getConf()).delete(karyotypePath, true);

    for (Aberration aberration : karyotypeDef.getAberrations())
      {
      AberrationTypes aberrationType = aberration.getAberration();
      if (aberrationType.getCytogeneticDesignation().equals("iso"))
        {
        log.warn("ISO ABERRATION, SKIPPING. We don't know yet how to deal with these");
        continue;
        }

      String fastaName = "der" + aberration.getBands().get(0).getChromosomeName();
      log.info("Writing new derivative: " + fastaName);
      log.info(aberrationType + " " + aberration.getWithLocations());

      // this FilterList will contain nested filter lists that putt all of the necessary locations
      AberrationLocationFilter alf = new AberrationLocationFilter();
      FilterList filterList = alf.getFilter(aberration, parentGenome, chromosomes);

      log.info(filterList);

      // to create an appropriate FASTA header                        --
      List<String> abrs = new ArrayList<String>();
      for (Band band : aberration.getBands() )// aberration.getAberrationDefinitions())
        abrs.add(band.getChromosomeName() + ":" + band.getLocation().getStart() + "-" + band.getLocation().getEnd());
      String abrDefinitions = aberrationType.getCytogeneticDesignation() + ":" + StringUtils.join(abrs.iterator(), ",");

      Scan scan = new Scan();
      scan.setFilter(filterList);

      Path baseOutput = new Path(karyotypePath, fastaName);

      // Generate the segments for the new FASTA file
      DerivativeChromosomeJob gdc = new DerivativeChromosomeJob(getConf(), scan, baseOutput, alf.getFilterLocationList(), aberrationType.getCytogeneticDesignation(), new FASTAHeader(fastaName, karyotypeDef.getKaryotype(), "parent=" + parentGenome.getName(), abrDefinitions));

      ToolRunner.run(gdc, null);
      fixOutputFiles(aberrationType, baseOutput, alf.getFilterLocationList().size());
      }

    //Create BWA index with ONLY the derivative chromosomes
    final Path mergedFASTA = new Path(new Path(basePath, karyotypeName), "reference.fa");
    // Create a single merged FASTA file for use in the indexing step
    FASTAUtil.mergeFASTAFiles(basePath.getFileSystem(getConf()), new Path(basePath, karyotypeName).toString(), mergedFASTA.toString());

    // Run BWA
    Path tmp = BWAIndex.writeReferencePointerFile(mergedFASTA, FileSystem.get(getConf()));
    ToolRunner.run(new BWAIndex(), (String[]) ArrayUtils.addAll(args, new String[]{"-f", tmp.toString()}));
    FileSystem.get(getConf()).delete(tmp, false);
    }

  public static void main(String[] args) throws Exception
    {
    new GenerateDerivativeChromosomes().generationKaryotypeGenome(args);
    }

  // just to clean up the main method a bit
  protected void fixOutputFiles(AberrationTypes abrType, Path output, int numLocs) throws Exception
    {
    // CRC files mess up any attempt to directly read/write from an unchanged file which means copying/moving fails too. Easiest fix right now is to dump the file.
    deleteChecksumFiles(this.getJobFileSystem(), output);
    /*
  We now have output files.  In most cases the middle file(s) will be the aberration sequences.
  In many cases they can just be concatenated as is. Exceptions:
    - duplication: the middle file needs to be duplicated before concatenation
    - iso: there should be only 1 file, it needs to be duplicated in reverse before concatenation
    */
    FileSystem jobFS = this.getJobFileSystem();

    log.info(jobFS.getWorkingDirectory());
    if (abrType.getCytogeneticDesignation().equals("dup"))
      {
      log.info("DUP aberration");
      if (numLocs > 3)
        throw new RuntimeException("This should not happen: dup has more than 3 locations");

      // move subsequent files
      for (int i = numLocs - 1; i > 1; i--)
        {
        // move files
        FileUtil.copy(jobFS, new Path(output, Integer.toString(i)), jobFS, new Path(output, Integer.toString(i + 1)), true, false, jobFS.getConf());
        }
      //then copy the duplicated segment
      FileUtil.copy(jobFS, new Path(output, Integer.toString(1)), jobFS, new Path(output, Integer.toString(2)), false, false, jobFS.getConf());
      }
    // TODO Iso is different from inv in that I take the same segment and copy it in reverse.  But right now all I do is write out one segment.  Not sure quite how to handle that.
    if (abrType.getCytogeneticDesignation().equals("iso"))
      {
      log.info("ISO aberration");
      if (numLocs > 2)
        throw new RuntimeException("This should not happen: iso has more than 2 locations");
      }

    // create merged FASTA at chromosome level -- there is an issue here that it just concatenates the files which means at the merge points there are strings of different lengths.  This is an issue in samtools.
    FASTAUtil.mergeFASTAFiles(jobFS, output.toString(), output.toString() + ".fa");
    jobFS.delete(output, true);
    }

  }
