package org.lcsb.lu.igcsa;

import com.m6d.filecrush.crush.Crush;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.mapreduce.bwa.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;



/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
 // NOTE: Potential problem, some runs of this have resulted in sam files that were too small, but this isn't consistent
public class BWAAlign extends BWAJob
  {
  static Logger log = Logger.getLogger(BWAAlign.class.getName());

  private String refGenome;
  private Path outputPath, referencePath, readPairTSV;

  public BWAAlign()
    {
    super(new Configuration());

    Option genome = new Option("n", "name", true, "Reference genome name.");
    genome.setRequired(true);
    this.addOptions( genome );

    Option ref = new Option("i", "reference", true, "Reference genome path.");
    ref.setRequired(true);
    this.addOptions( ref );

    Option reads = new Option("r", "reads", true, "Path to tsv file of read-pairs.");
    reads.setRequired(true);
    this.addOptions( reads );

    Option output = new Option("o", "output", true, "Path for output");
    output.setRequired(true);
    this.addOptions(output);
    }

  public Path getOutputPath()
    {
    return outputPath;
    }

  private void setup() throws URISyntaxException, IOException
    {
    getConf().setBoolean(SAMOutputFormat.HEADER_OUTPUT, false);

    FileSystem fs = FileSystem.get(outputPath.toUri(), getConf());

    if (!fs.exists(outputPath))
      fs.mkdirs(outputPath);

    if (!fs.exists(readPairTSV))
      throw new IOException("Read pair TSV file does not exist: " + readPairTSV.toUri());

    String readPairName = readPairTSV.getName().replace(".tsv", "");
    outputPath = new Path(new Path(outputPath, readPairName), refGenome);
    if (fs.exists(outputPath))
      fs.delete(outputPath, true);

    // reference
//    if (!fs.isFile(referencePath))
//      throw new IOException(referencePath + " is not a file.");
//    referencePath = referencePath.makeQualified(fs);

    URI uri = new URI(referencePath.toUri().toASCIIString() + "#reference");
    addArchive(uri, true);
    }

  @Override
  public int run(String[] args) throws Exception
    {
    GenericOptionsParser gop = this.parseHadoopOpts(args);
    CommandLine cl = this.parser.parseOptions(gop.getRemainingArgs());

    referencePath = new Path(cl.getOptionValue("i"));
    readPairTSV = new Path(cl.getOptionValue('r'));
    refGenome =  cl.getOptionValue('n');
    outputPath = new Path(cl.getOptionValue("o"));

    setupBWA(cl.getOptionValue('b'));
    setup();

    String inputs = "Reference: " + referencePath + "\n" +
        "Read TSV: " + readPairTSV + "\n" +
        "Ref genome: " + refGenome + "\n" +
        "Output path: " + outputPath + "\n";
    log.info(inputs);

    // set up the job
    Job job = new Job(getConf(), "Align read pairs " + readPairTSV.getName() + " with " + refGenome);

    ReadPairMapper.setReferenceName(referencePath.getName().replace(".tgz", ".fa"), job);

    job.setJarByClass(BWAAlign.class);
    job.setMapperClass(ReadPairMapper.class);

    job.setInputFormatClass(ReadPairTSVInputFormat.class);
    TextInputFormat.addInputPath(job, readPairTSV);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);

    job.setReducerClass(SAMReducer.class);

    job.setOutputFormatClass(SAMOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    FileOutputFormat.setOutputPath(job, outputPath);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    BWAAlign align = new BWAAlign();
    ToolRunner.run(align, args);

    FileSystem fs = align.getJobFileSystem(align.getOutputPath().toUri());

    for (FileStatus status : fs.listStatus(align.getOutputPath()) )
      {
      if (status.getLen() <= 0)
        fs.delete(status.getPath(), true);
      }

    Path tmpPath = new Path("/tmp/" + System.currentTimeMillis(), "merged.sam");
    log.info("Merge all to " + tmpPath.toString());
    FileUtil.copyMerge(fs, align.getOutputPath(), fs, tmpPath, true, align.getConf(), "");
    FileUtil.copy(fs, tmpPath, fs, new Path(align.getOutputPath(), "merged.sam"), true, align.getConf());
    }


  }
