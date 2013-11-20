package org.lcsb.lu.igcsa;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.HBaseConfiguration;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentKey;

import java.io.File;
import java.io.IOException;


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
    final long startTime = System.currentTimeMillis();

    String chr = args[0];
    String fastaPath = args[1];

    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", "GRCh37");
    config.set("chromosome", chr);

    HBaseGenome genome = new HBaseGenome("GRCh37", null);
    genome.addChromosome(chr, 0, 0);

    Job job = new Job(config, "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FragmentMapper.class);

    job.setMapOutputKeyClass(ImmutableBytesWritable.class);
    job.setMapOutputValueClass(Text.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    FileInputFormat.addInputPath(job, new Path(fastaPath));

     // because we aren't emitting anything from mapper
    job.setOutputFormatClass(NullOutputFormat.class);

    job.submit();
    final long elapsedTime = System.currentTimeMillis() - startTime;
    log.info("Finished job " + elapsedTime / 1000 + " seconds");

    return 1;
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
      this.admin.getGenome(genomeName).getChromosome(chr).addSequence((int)fragment.getStart(), (int)fragment.getEnd(), fragment.getSegment(), value.toString());
      context.write(key, value);
      }
    }

  public static void main(String[] args) throws Exception
    {
    FileUtils.deleteDirectory(new File("/tmp/figg2"));

    for (String s: new String[]{"22","1", "2", "3", "4", "5"})
      {
      String path = "/Users/skillcoyne/Data/FASTA/chr" + s + ".fa.gz";
      if ( !(new File(path).exists()))
        throw new IOException(path + " does not exist");
      String[] pathArgs = (String[]) ArrayUtils.addAll(args, new String[]{s, path});
      ToolRunner.run(new LoadFromFASTA(), pathArgs);

      break;
      }
    }


  }






//  public static class FragmentReducer extends TableReducer<ImmutableBytesWritable, Text, ImmutableBytesWritable>
//    {
//    private String chr;
//    private String genomeName;
//
//    private HBaseGenomeAdmin admin;
//
//    @Override
//    protected void setup(Context context) throws IOException, InterruptedException
//      {
//      super.setup(context);
//
//      admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(context.getConfiguration());
//
//      genomeName = context.getConfiguration().get("genome");
//      chr = context.getConfiguration().get("chromosome");
//
//      //tables = (MutableGenome) springContext.getBean("tables");
//
//      log.info("CHROMOSOME " + chr);
//      }
//
//    @Override
//    public void reduce(ImmutableBytesWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException
//      {
//      FragmentKey fragment = FragmentKey.fromBytes(key.get());
//
//      String s = "";
//      for (Text v: values)
//        s += v.toString();
//
//      //Text result = new Text();
//      /*
//      so I don't understand what's going on here.  Each mapper should have only one value per key but if I iterate over the values I get
//      each value duplicated for each key.
//      for now I just won't iterate since I'm missing something
//      */
//
//      Put put = this.admin.getGenome(genomeName).getChromosome(chr).add((int)fragment.getStart(), (int)fragment.getEnd(), fragment.getSegment(), s);
//
//      //result.set(values.iterator().next().toString());
//      context.write(null, put);
//      }
//    }
