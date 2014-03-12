package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.Export;
import org.apache.hadoop.hbase.mapreduce.Import;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.hbase.tables.IGCSATables;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class HBaseUtility
  {
  static Logger log = Logger.getLogger(HBaseUtility.class.getName());

  public static void main(String[] args) throws Exception
    {
    for (Option o : new Option[]{
        new Option("d", "dir", true, "Directory to import/export hbase tables."),
        new Option("c", "command",true,"IMPORT or EXPORT tables to the directory.")})
      {
      o.setRequired(true);
      IGCSACommandLineParser.getParser().addOptions(o);
      }

    Configuration conf = HBaseConfiguration.create();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = IGCSACommandLineParser.getParser().parseOptions(otherArgs);

    Path path = new Path(cl.getOptionValue("d"));
    String cmd = cl.getOptionValue("c");

    if (cmd.equalsIgnoreCase("export")) exportData(path, conf);
    else if (cmd.equalsIgnoreCase("import")) importData(path, conf);
    }

  private static void exportData(Path path, Configuration conf) throws Exception
    {
    for (IGCSATables table : IGCSATables.values())
      {
      String tableDir = new Path(path, table.getTableName()).toString();
      System.out.println(" ********* Export data from " + table.getTableName() + " to " + tableDir + " *********");

      Job job = Export.createSubmittableJob(conf, new String[]{table.getTableName(), tableDir});
      job.waitForCompletion(true);

      //ToolRunner.run(new Export(), new String[]{table.getTableName(), tableDir.getName()});
      //org.apache.hadoop.hbase.mapreduce.Export.main(new String[]{table.getTableName(), tableDir});
      }
    }

  private static void importData(Path path, Configuration conf) throws Exception
    {
    if (!HBaseGenomeAdmin.getHBaseGenomeAdmin().tablesExist()) HBaseGenomeAdmin.getHBaseGenomeAdmin().createTables();

    for (IGCSATables table : IGCSATables.values())
      {
      String tableDir = new Path(path, table.getTableName()).toString();
      System.out.println(" ********* Import data from " + tableDir + " to " + table.getTableName() + " *********");

      Job job = Import.createSubmittableJob(conf, new String[]{table.getTableName(), tableDir});
      job.waitForCompletion(true);

      //org.apache.hadoop.hbase.mapreduce.Import.main(new String[]{table.getTableName(), tableDir});
      }
    }

  }
