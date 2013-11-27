package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentKey;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LoadFromFASTA extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(LoadFromFASTA.class.getName());

  @Override
  public int run(String[] args) throws Exception
    {
    if (args.length < 2)
      throw new Exception("Missing required parameters: <genome name>, <chromosome>, <fasta file path>");

    String genomeName = args[0];
    String chr = args[1];
    String fastaPath = args[2];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", genomeName);
    config.set("chromosome", chr);

    genomeAdmin.getGenome(genomeName).addChromosome(chr, 0, 0);

    Job job = new Job(config, "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FragmentMapper.class);

    job.setMapOutputKeyClass(ImmutableBytesWritable.class);
    job.setMapOutputValueClass(Text.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    FileInputFormat.addInputPath(job, new Path(fastaPath));

     // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);


    //job.submit();
    return (job.waitForCompletion(true) ? 0 : 1);
    //return 0;
    }

  public static class FragmentMapper extends Mapper<ImmutableBytesWritable, Text, ImmutableBytesWritable, Text>
    {
    private static Logger log = Logger.getLogger(FragmentMapper.class.getName());

    private HBaseGenomeAdmin admin;
    private String genomeName;
    private String chr;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException
      {
      super.setup(context);
      admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());

      genomeName = context.getConfiguration().get("genome");
      chr = context.getConfiguration().get("chromosome");
      }

    @Override
    protected void map(ImmutableBytesWritable key, Text value, Context context) throws IOException, InterruptedException
      {
      // pretty much just chopping the file up and spitting it back out, bet I don't even need a reducer
      log.info(FragmentKey.fromBytes(key.get()).toString() + value.toString().length());
      //log.info(value.toString());

      FragmentKey fragment = FragmentKey.fromBytes(key.get());
      this.admin.getGenome(genomeName).getChromosome(chr).addSequence((int)fragment.getStart(), (int)fragment.getEnd(), value.toString());
      this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName, chr), 1, (fragment.getEnd() - fragment.getStart())) ;
      context.write(key, value);
      }
    }

  public static void main(String[] args) throws Exception
    {
    //args = new String[]{"GRCh37", "/Users/sarah.killcoyne/Data/FASTA"};
    //args = new String[]{"GRCh37", "/Users/skillcoyne/Data/FASTA"};

    //args = new String[]{"GRCh37", "s3n://insilico/FASTA"};

    if (args.length < 2)
      {
      System.err.println("Usage: LoadFromFASTA <genome name> <fasta directory>");
      System.exit(-1);
      }

    HBaseGenomeAdmin.getHBaseGenomeAdmin().createTables();

    String genomeName = args[0];
    String fastaDir = args[1];

    new HBaseGenome(genomeName, null);

    Map<String, File> files = org.lcsb.lu.igcsa.utils.FileUtils.getFASTAFiles(new File(fastaDir));

    // ideally I would do this all in one job but I'll work that one out later
    for (String chr: files.keySet())
      {
      final long startTime = System.currentTimeMillis();
      ToolRunner.run(new LoadFromFASTA(),  new String[]{genomeName, chr, files.get(chr).getAbsolutePath()});
      final long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("Finished job " + elapsedTime / 1000 + " seconds");
      }
    }
  }




