package org.lcsb.lu.igcsa.hbase;

import com.amazonaws.services.rds.model.OptionSetting;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.Export;
import org.apache.hadoop.hbase.mapreduce.Import;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.aws.AWSProperties;
import org.lcsb.lu.igcsa.hbase.tables.genomes.IGCSATables;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class HBaseUtility
  {
  static Logger log = Logger.getLogger(HBaseUtility.class.getName());

  private static Configuration setAWSProps(Configuration conf)
    {
    AWSProperties props = AWSProperties.getProperties();
    conf.set("fs.s3n.awsAccessKeyId", props.getAccessKey());
    conf.set("fs.s3n.awsSecretAccessKey", props.getSecretKey());

    //conf.setInt("hbase.regionserver.handler.count", 100);
    conf.setInt("hbase.rpc.timeout", 360000);

    return conf;
    }


  public static void main(String[] args) throws Exception
    {
    Options options = new Options();
    options.addOption(new Option("d", "dir", true, "Directory to import/export hbase tables."));
    options.addOption(new Option("c", "command", true,"IMPORT or EXPORT tables to the directory."));
    options.addOption(new Option("t", "tables", true, "Comma separated list of tables. Default is all in hbase or in the import directory."));

    Configuration conf = HBaseConfiguration.create();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = new BasicParser().parse(options, otherArgs);

    if (!cl.hasOption("d") || !cl.hasOption("c"))
      {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("Missing required option 'd' or 'c'", options);
      System.exit(-1);
      }

    if (cl.getOptionValue("d").startsWith("s3"))
      conf = setAWSProps(conf);

    Path path = new Path(cl.getOptionValue("d"));
    String cmd = cl.getOptionValue("c");

    String[] tables = new String[0];
    if (cl.hasOption("t"))
      tables = cl.getOptionValue("t").split(",");

    if (cmd.equalsIgnoreCase("export")) exportData(path, tables, conf);
    else if (cmd.equalsIgnoreCase("import")) importData(path, tables, conf);
    }

  private static void exportData(Path path, String[] tables, Configuration conf) throws Exception
    {
    if (tables.length <= 0)
      tables = HBaseGenomeAdmin.getHBaseGenomeAdmin().listTables();

    System.out.println(tables);

    path = new Path(path, new SimpleDateFormat("yyyyMMddzHHmm").format( new Date() ));

    for (String table: tables)
      {
      String tableDir = new Path(path, table).toString();
      System.out.println(" ********* Export data from " + table + " to " + tableDir + " *********");

      Job job = Export.createSubmittableJob(conf, new String[]{table, tableDir});
      job.waitForCompletion(true);
      }
    }

  private static void importData(Path path, String[] tables, Configuration conf) throws Exception
    {
    if (!HBaseGenomeAdmin.getHBaseGenomeAdmin().tablesExist()) HBaseGenomeAdmin.getHBaseGenomeAdmin().createTables();
    if (!VariationAdmin.getInstance().tablesExist()) VariationAdmin.getInstance().createTables();

    if (tables.length <= 0)
      {
      FileSystem fs = FileSystem.get(conf);
      FileStatus[] statuses = fs.listStatus(path);
      tables = new String[statuses.length];
      for (int i=0; i<statuses.length; i++)
        tables[i] = statuses[i].getPath().getName();
      }
    System.out.println(tables);

    for (String table: tables)
      {
      String tableDir = new Path(path, table).toString();
      System.out.println(" ********* Import data from " + tableDir + " to " + table + " *********");

      Job job = Import.createSubmittableJob(conf, new String[]{table, tableDir});
      job.waitForCompletion(true);
      }
    }

  }
