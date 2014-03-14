/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.GenericOptionsParser;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.hbase.tables.variation.SNVProbability;
import org.lcsb.lu.igcsa.hbase.tables.variation.SizeProbability;
import org.lcsb.lu.igcsa.hbase.tables.variation.VariationCountPerBin;
import org.lcsb.lu.igcsa.hbase.tables.variation.VariationTables;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ImportVariationData
  {
  private static final Log log = LogFactory.getLog(ImportVariationData.class);

  public static void main(String[] args) throws Exception
    { // TODO change this...these are only required for an import
    for (Option o : new Option[]{
        new Option("v", "variations", true, "Var file"),
        new Option("s", "snv", true, "SNV probs"),
        new Option("z", "size", true, "Size probs"),
        new Option("f", "fragments", true, "Var per gc bin"),
        new Option("g", "gc", true, "GC bins") } )
      {
      o.setRequired(true);
      IGCSACommandLineParser.getParser().addOptions(o);
      }
    IGCSACommandLineParser.getParser().addOptions(new Option("e", "export", false, "Export tables instead."));

    Configuration conf = HBaseConfiguration.create();

    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cl = IGCSACommandLineParser.getParser().parseOptions(otherArgs);
    }

  private Map<Integer, String[]> variations = new HashMap<Integer, String[]>();
  private Map<String, IntRange> gcBins = new HashMap<String, IntRange>();

  private VariationAdmin admin = VariationAdmin.getInstance();


  public ImportVariationData(CommandLine cl) throws IOException
    {

    cl.getOptionValue("s");
    cl.getOptionValue("z");
    cl.getOptionValue("f");
    cl.getOptionValue("g");

    setupVariations(cl.getOptionValue("v"));
    setupGCBins(cl.getOptionValue("g"));

    admin.createTables();
    }

  private void setupVariations(String vars) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(vars));

    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] db = line.split("\t");
      if (db[0].equals("")) continue;
      variations.put(Integer.parseInt(db[0]), new String[]{db[1], db[2]});
      }
    }

  private void setupGCBins(String gc) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(gc));

    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] bins = line.split("\t");
      if (bins[0].equals("") || bins[0].startsWith("[A-Za-z]")) continue;

      String key = bins[1] + ":" + bins[2];
      int min = Integer.parseInt(bins[3]);
      int max = Integer.parseInt(bins[4]);

      gcBins.put(key, new IntRange(min, max));
      }
    }

  private void importSNVProb(String snv) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(snv));

    SNVProbability table = (SNVProbability) admin.getTable(VariationTables.SNVP.getTableName());

    int count = 0;
    String line;
    while ((line = reader.readLine()) != null)
      {

      }

    }

  private void importSizeProb(String size) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(size));

    SizeProbability table = (SizeProbability) admin.getTable(VariationTables.SIZE.getTableName());

    int count = 0;
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] sizes = line.split("\t");
      if (sizes[0].equals("") || sizes[0].startsWith("[A-Za-z]")) continue;
      String[] variation = variations.get( Integer.parseInt(sizes[1]) );
      int maxbp = Integer.parseInt(sizes[1]);
      long prob = Long.parseLong(sizes[2]);

      String rowId = table.addSizeProbabiilty(variation[0], maxbp, prob);
      if (rowId != null) ++count;
      }
    log.info("Added " + count + " rows to SIZE");
    }

  private void importFragments(String fragFile) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(fragFile));

    VariationCountPerBin table = (VariationCountPerBin) admin.getTable(VariationTables.VPB.getTableName());

    int rowCount = 0;
    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] frag = line.split("\t");
      if (frag[0].equals("chr")) continue;

      String chr = frag[0];
      IntRange gcRange = gcBins.get(chr + ":" + frag[1]);
      String[] variation = variations.get(Integer.parseInt(frag[2]));
      int count = Integer.parseInt(frag[3]);

      String rowId = table.addFragment(chr, variation[0], variation[1], count, gcRange.getMinimumLong(), gcRange.getMaximumLong());
      if (rowId == null)
        throw new IOException("Failed to add line: " + line);
      else
        ++rowCount;
      }

    log.info(rowCount + " rows added.");
    }

  }


