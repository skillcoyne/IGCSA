package org.lcsb.lu.igcsa.hbase;

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

  private static void usage(String msg, Options options)
    {
    HelpFormatter help = new HelpFormatter();
    help.printHelp(msg, options);
    System.exit(-1);
    }

  public static void main(String[] args) throws Exception
    {
    Options options = new Options();
    options.addOption(new Option("d", "dir", true, "Directory to import/export hbase tables."));
    options.addOption(new Option("c", "command", true,"IMPORT or EXPORT tables to the directory."));

    Option tb = new Option("t", "tables", true, "Table to import. Default is all in hbase or in the import directory. If using s3 a list of tables is required!");
    tb.setRequired(true);
    options.addOption(tb);

    Configuration conf = HBaseConfiguration.create();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = new GnuParser().parse(options, otherArgs);

    if (!cl.hasOption("d") || !cl.hasOption("c"))
      usage("Missing required option 'd' or 'c'", options);

    String dir = cl.getOptionValue("d");
    if (dir.startsWith("s3"))
      {
      dir = dir.replace("s3://", "s3n://");
      if (!cl.hasOption("t"))
        usage("When using s3 a list of tables is required.", options);
      }

    Path path = new Path(dir);
    String cmd = cl.getOptionValue("c");

    String[] tables = new String[0];
    if (cl.hasOption("t"))
      tables = cl.getOptionValues("t");

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
    if (!HBaseGenomeAdmin.getHBaseGenomeAdmin().tablesExist())
      HBaseGenomeAdmin.getHBaseGenomeAdmin().createTables();
    if (!VariationAdmin.getInstance().tablesExist()) VariationAdmin.getInstance().createTables();

    if (tables.length <= 0)
      {
      FileSystem fs = FileSystem.get(conf);

      if (path.getName().startsWith("s3"))
        fs = FileSystem.get(path.toUri(), conf);

      FileStatus[] statuses = fs.listStatus(path);
      tables = new String[statuses.length];
      for (int i=0; i<statuses.length; i++)
        tables[i] = statuses[i].getPath().getName();
      }

    for (String table: tables)
      {
      String tableDir = new Path(path, table).toString();
      System.out.println(" ********* Import data from " + tableDir + " to " + table + " *********");

      Job job = Import.createSubmittableJob(conf, new String[]{table, tableDir});
      job.waitForCompletion(true);
      }
    }
  }

