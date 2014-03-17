package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.Export;
import org.apache.hadoop.hbase.mapreduce.Import;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
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

  public static void main(String[] args) throws Exception
    {
    for (Option o : new Option[]{
        new Option("d", "dir", true, "Directory to import/export hbase tables."),
        new Option("c", "command",true,"IMPORT or EXPORT tables to the directory.")})
      {
      o.setRequired(true);
      IGCSACommandLineParser.getParser().addOptions(o);
      }
    IGCSACommandLineParser.getParser().addOptions(new Option("t", "tables", true, "Comma separated list of tables. Default is all."));

    Configuration conf = HBaseConfiguration.create();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = IGCSACommandLineParser.getParser().parseOptions(otherArgs);

    Path path = new Path(cl.getOptionValue("d"), new SimpleDateFormat("yyyyMMddzHHmm").format( new Date() ));
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
      tables = IGCSATables.getTableNames();

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
      tables = IGCSATables.getTableNames();

    for (String table: tables)
      {
      String tableDir = new Path(path, table).toString();
      System.out.println(" ********* Import data from " + tableDir + " to " + table + " *********");

      Job job = Import.createSubmittableJob(conf, new String[]{table, tableDir});
      job.waitForCompletion(true);
      }
    }

  }
