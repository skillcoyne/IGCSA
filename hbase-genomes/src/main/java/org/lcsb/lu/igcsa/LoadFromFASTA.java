package org.lcsb.lu.igcsa;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.model.S3Storage;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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

import org.lcsb.lu.igcsa.aws.AWSProperties;
import org.lcsb.lu.igcsa.aws.AWSUtils;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.mapreduce.FASTAInputFormat;
import org.lcsb.lu.igcsa.mapreduce.FragmentKey;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LoadFromFASTA extends Configured implements Tool
  {
  static Logger log = Logger.getLogger(LoadFromFASTA.class.getName());

  private String genomeName;
  private String chr;

  private File file;
  private S3Object s3Object;

  public LoadFromFASTA(String genomeName, String chr, File file)
    {
    this.genomeName = genomeName;
    this.chr = chr;
    this.file = file;
    }

  public LoadFromFASTA(String genomeName, String chr, S3Object s3Object)
    {
    this.genomeName = genomeName;
    this.chr = chr;
    this.s3Object = s3Object;
    }


  @Override
  public int run(String[] args) throws Exception
    {
    Configuration config = HBaseConfiguration.create();
    HBaseGenomeAdmin genomeAdmin = HBaseGenomeAdmin.getHBaseGenomeAdmin(config);

    config.set("genome", genomeName);
    config.set("chromosome", chr);

    Path path;
    if (s3Object != null)
      {
      AWSProperties props = AWSProperties.getProperties();
      path = new Path("s3n://" + s3Object.getBucketName() + "/" + s3Object.getKey());
      config.set("fs.s3n.awsAccessKeyId", props.getAccessKey());
      config.set("fs.s3n.awsSecretAccessKey", props.getSecretKey());
      }
    else
      path = new Path(file.getAbsolutePath());

    genomeAdmin.getGenome(genomeName).addChromosome(chr, 0, 0);

    Job job = new Job(config, "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);
    job.setMapperClass(FragmentMapper.class);

    job.setMapOutputKeyClass(ImmutableBytesWritable.class);
    job.setMapOutputValueClass(Text.class);

    job.setInputFormatClass(FASTAInputFormat.class);
    FileInputFormat.addInputPath(job, path);

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
      this.admin.getGenome(genomeName).getChromosome(chr).addSequence((int) fragment.getStart(), (int) fragment.getEnd(), value.toString());
      this.admin.getChromosomeTable().incrementSize(ChromosomeRow.createRowId(genomeName, chr), 1, (fragment.getEnd() - fragment.getStart()));
      context.write(key, value);
      }
    }

  public static void main(String[] args) throws Exception
    {
    //args = new String[]{"GRCh37", "file:///Users/sarah.killcoyne/Data/FASTA"};
    //args = new String[]{"GRCh37", "/Users/skillcoyne/Data/FASTA"};
    //args = new String[]{"GRCh37", "s3n://insilico/FASTA"};

    if (args.length < 2)
      {
      System.err.println("Usage: LoadFromFASTA <genome name> <fasta directory>");
      System.exit(-1);
      }

    Configuration conf = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);
    admin.createTables();

    String genomeName = args[0];
    String fastaDir = args[1];

    HBaseGenome genome = admin.getGenome(genomeName);
    if (genome == null)
      genome = new HBaseGenome(genomeName, null);

    if (fastaDir.startsWith("s3"))
      {
      Pattern s3pattern = Pattern.compile("^s3n?:\\/\\/(\\w+[-\\w+]*).*");
      Matcher s3file = s3pattern.matcher(fastaDir);
      if (!s3file.matches())
        throw new Exception("A S3 bucket name could not be determined from " + fastaDir);

      String bucket = s3file.group(1);
      Map<String, S3ObjectSummary> chromosomes = AWSUtils.listFASTAFiles(bucket);
      for (String chr : chromosomes.keySet())
        runTool(genome, chr, new LoadFromFASTA(genomeName, chr, AWSUtils.getS3Object(chromosomes.get(chr))));
      }
    else // local files
      {
      Map<String, File> files = org.lcsb.lu.igcsa.utils.FileUtils.getFASTAFiles(new File(fastaDir));
      for (String chr : files.keySet())
        runTool(genome, chr, new LoadFromFASTA(genomeName, chr, files.get(chr)));
      }


    }

  private static void runTool(HBaseGenome genome, String chr, LoadFromFASTA lff) throws Exception
    {
    if (genome.getChromosome(chr) == null)
      {
      final long startTime = System.currentTimeMillis();
      ToolRunner.run(lff, null);
      final long elapsedTime = System.currentTimeMillis() - startTime;
      log.info("Finished job " + elapsedTime / 1000 + " seconds");
      }
    else
      log.info("Chromosome " + chr + " already exists. Skipping job.");
    }

  }




