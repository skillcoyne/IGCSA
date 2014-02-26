package org.lcsb.lu.igcsa;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aws.AWSProperties;
import org.lcsb.lu.igcsa.aws.AWSUtils;
import org.lcsb.lu.igcsa.mapreduce.fasta.*;
import org.lcsb.lu.igcsa.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * org.lcsb.lu.igcsa
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class GenerateTSV extends JobIGCSA
  {
  static Logger log = Logger.getLogger(GenerateTSV.class.getName());

  private Collection<Path> paths;
  private Configuration conf;
  private String genomeName;

  public GenerateTSV(Configuration conf)
    {
    super(conf);
    }

  public GenerateTSV(String genomeName, Collection<Path> paths)
    {
    super(new Configuration());
    conf = getConf();
    this.paths = paths;
    this.genomeName = genomeName;
    conf.set("genome", genomeName);
    if (paths.iterator().next().toString().contains("s3")) setAWSProps();
    }

  private void setAWSProps()
    {
    AWSProperties props = AWSProperties.getProperties();
    conf.set("fs.s3n.awsAccessKeyId", props.getAccessKey());
    conf.set("fs.s3n.awsSecretAccessKey", props.getSecretKey());
    }


  @Override
  public int run(String[] strings) throws Exception
    {
    Job job = new Job(conf, "Reference Genome Fragmentation");

    job.setJarByClass(LoadFromFASTA.class);

    job.setMapperClass(TSVFragmentMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(LongWritable.class);

    job.setInputFormatClass(FASTAFragmentInputFormat.class);

    for (Path path : paths)
      FileInputFormat.addInputPath(job, path);

    job.setReducerClass(TSVReducer.class);
    job.setNumReduceTasks(paths.size());

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    Path output = new Path("/tmp/bulkload");
    if (output.getFileSystem(conf).exists(output)) output.getFileSystem(conf).delete(output, true);

    FileOutputFormat.setOutputPath(job, output);

    for (String table : new String[]{"chromosome", "sequence"})
      MultipleOutputs.addNamedOutput(job, table, TextOutputFormat.class, Text.class, Text.class);

    return (job.waitForCompletion(true) ? 0 : 1);
    }

  public static void main(String[] args) throws Exception
    {
    GenericOptionsParser parser = new GenericOptionsParser(new Configuration(), args);

    args = parser.getRemainingArgs();
    if (args.length < 2)
      {
      System.err.println("Usage: LoadFromFASTA <genome name> <fasta directory>");
      System.exit(-1);
      }

    Configuration conf = parser.getConfiguration();//HBaseConfiguration.create();
    //    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);
    //    admin.createTables();

    String genomeName = args[0];
    String fastaDir = args[1];

    //    if (admin.getGenomeTable().getGenome(genomeName) != null)
    //      {
    //      System.out.println("Genome '" + genomeName + "' already exists, overwrites are not allowed. Deleting genome.");
    //      //System.exit(-1);
    //      admin.deleteGenome(genomeName);
    //      }
    //
    //    // create genome if it doesn't exist
    //    if (admin.getGenomeTable().getGenome(genomeName) == null)
    //      admin.getGenomeTable().addGenome(genomeName, null);

    Collection<Path> filePaths = new ArrayList<Path>();
    if (fastaDir.startsWith("s3"))
      {
      Pattern s3pattern = Pattern.compile("^s3n?:\\/\\/(\\w+[-\\w+]*).*");
      Matcher s3file = s3pattern.matcher(fastaDir);
      if (!s3file.matches()) throw new Exception("A S3 bucket name could not be determined from " + fastaDir);

      String bucket = s3file.group(1);
      Map<String, S3ObjectSummary> chromosomes = AWSUtils.listFASTAFiles(bucket);
      for (S3ObjectSummary s3Object : chromosomes.values())
        filePaths.add(new Path("s3n://" + s3Object.getBucketName() + "/" + s3Object.getKey()));
      }
    else //if (fastaDir.startsWith("hdfs"))
      {
      FileSystem fs = FileSystem.get(conf);
      FileStatus[] statuses = fs.listStatus(new Path(fastaDir), new PathFilter()
      {
      @Override
      public boolean accept(Path path)
        {
        return FileUtils.FASTA_FILE.accept(null, path.getName());
        }
      });
      for (FileStatus status : statuses)
        filePaths.add(status.getPath());
      }

    long start = System.currentTimeMillis();
    GenerateTSV gt = new GenerateTSV(genomeName, filePaths);
    ToolRunner.run(gt, null);
    long end = System.currentTimeMillis() - start;
    log.info("Took " + end / 1000 + " seconds to complete.");

    Path output = new Path("/tmp/bulkload");
    FSDataOutputStream os = gt.getJobFileSystem().create(new Path(output, "genome/genome.txt"));
    os.write((genomeName + "\t" + genomeName + "\t" + "" + "\n").getBytes());
    os.close();

    FileStatus[] chrPaths = gt.getJobFileSystem().listStatus(output, new PathFilter()
    {
    @Override
    public boolean accept(Path path)
      {
      if (path.getName().contains("chromosome")) return true;
      return false;
      }
    });
    moveFiles(gt.getJobFileSystem(), new Path(output, "chr"), chrPaths);

    FileStatus[] seqStatus = gt.getJobFileSystem().listStatus(output, new PathFilter()
    {
    @Override
    public boolean accept(Path path)
      {
      if (path.getName().contains("sequence")) return true;
      return false;
      }
    });
    moveFiles(gt.getJobFileSystem(), new Path(output, "seq"), seqStatus);
    }


  private static void moveFiles(FileSystem fs, Path destDir, FileStatus[] paths) throws IOException
    {
    for (FileStatus status: paths)
      {
      fs.mkdirs(destDir);
      FileUtil.copy(fs, status.getPath(),
                    fs, new Path(destDir, status.getPath().getName()),
                    true, false, fs.getConf());
      }

    }

  }

