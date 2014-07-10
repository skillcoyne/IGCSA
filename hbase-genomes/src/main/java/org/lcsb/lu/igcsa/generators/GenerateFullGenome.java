/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generators;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.lcsb.lu.igcsa.BWAIndex;
import org.lcsb.lu.igcsa.JobIGCSA;
import org.lcsb.lu.igcsa.NormalChromosomeJob;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.mapreduce.fasta.FASTAUtil;

import java.io.IOException;
import java.util.*;


public class GenerateFullGenome extends JobIGCSA
  {
  public static void main(String[] args) throws Exception
    {
    new GenerateFullGenome().run(args);
    }


  private static final Log log = LogFactory.getLog(GenerateFullGenome.class);

  private Path output;
  private String genomeName;

  public GenerateFullGenome()
    {
    super(new Configuration());

    Option genome = new Option("g", "genome", true, "Genome name.");
    genome.setRequired(true);
    this.addOptions(genome);

    Option output = new Option("o", "Output path", true, "Fully qualified path in HDFS or S3 to write FASTA files.");
    output.setRequired(true);
    this.addOptions(output);

    Option bwa = new Option("b", "bwa", true, "Path to bwa.tgz, optional.");
    bwa.setRequired(false);
    this.addOptions(bwa);
    }

  public Path getOutputPath()
    {
    return output;
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs(),this.getClass());

    genomeName = cl.getOptionValue("g");
    output = new Path(new Path(cl.getOptionValue("o"), new Path("/genomes")), genomeName);

    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(getConf());
    if (admin.getGenomeTable().getGenome(genomeName) == null)
      throw new IOException("No genome found for '" + genomeName + "'");

    List<ChromosomeResult> chrs = admin.getChromosomeTable().getChromosomesFor(genomeName);
    if (chrs == null || chrs.size() <= 0)
      throw new IOException("Failed to retrieve chromosomes for " + genomeName);

    for (ChromosomeResult chr : chrs)
      {
      NormalChromosomeJob ncj = new NormalChromosomeJob(getConf());
      ncj.createJob((String[]) ArrayUtils.addAll(args, new String[]{"-c", chr.getChrName()}));
      log.info("Running " + ncj.getJob().getJobName());
      ncj.getJob().waitForCompletion(false);
      ncj.renameToFASTA();
      log.info("Finished " + ncj.getJob().getJobName() );
      }

    // Create a single merged FASTA file for use in the indexing step
    FASTAUtil.deleteChecksumFiles(getJobFileSystem(), output);
    Path mergedFasta = new Path(getOutputPath(), "reference.fa");
    FASTAUtil.mergeFASTAFiles(getJobFileSystem(mergedFasta.toUri()), getOutputPath().toString(), mergedFasta.toString());

    // Run BWA index
    if (cl.hasOption("b"))
      {
      Path tmp = BWAIndex.writeReferencePointerFile(mergedFasta, getJobFileSystem(mergedFasta.toUri()));
      ToolRunner.run(new BWAIndex(), (String[]) ArrayUtils.addAll(args, new String[]{"-f", tmp.toString()}));
      getJobFileSystem(tmp.toUri()).delete(tmp, true);
      }

    return 1;
    }

  }
