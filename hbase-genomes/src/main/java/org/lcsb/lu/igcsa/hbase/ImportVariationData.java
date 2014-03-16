/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.GenericOptionsParser;
import org.lcsb.lu.igcsa.IGCSACommandLineParser;
import org.lcsb.lu.igcsa.hbase.tables.variation.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImportVariationData
  {
  private static final Log log = LogFactory.getLog(ImportVariationData.class);

  private static CommandLine parseCommandLine(String[] args) throws IOException, ParseException
    {
    Options options = new Options();
    options.addOption(new Option("v", "variations", true, "Var file"));
    options.addOption(new Option("s", "snv", true, "SNV probs"));
    options.addOption(new Option("z", "size", true, "Size probs"));
    options.addOption(new Option("f", "fragments", true, "Var per gc bin"));
    options.addOption(new Option("g", "gc", true, "GC bins"));
    options.addOption(new Option("e", "export", false, "Export tables instead."));

    String[] otherArgs = new GenericOptionsParser(HBaseConfiguration.create(), args).getRemainingArgs();
    CommandLine cl = new BasicParser().parse(options, otherArgs);

    HelpFormatter help = new HelpFormatter();
    if (!cl.hasOption("e"))
      {
      for (String opt : new String[]{"v", "s", "z", "f", "g"})
        {
        if (!cl.hasOption(opt))
          {
          help.printHelp("Missing required option: -" + opt + " ", IGCSACommandLineParser.getParser().getOptions());
          System.exit(-1);
          }
        }
      }
    return cl;
    }

  public static void main(String[] args) throws Exception
    {
    // TODO change this...these are only required for an import
    CommandLine cl = parseCommandLine(args);

    if (cl.hasOption("e"))
      {
      HBaseUtility.main(new String[]{"-d", "/hbase-backup", "-c", "export", "-t", StringUtils.join(VariationTables.getTableNames(), ",")});
      }
    new ImportVariationData().runImport(cl);
    }

  private Map<Integer, String[]> variations = new HashMap<Integer, String[]>();
  private Map<String, IntRange> gcBins = new HashMap<String, IntRange>();
  private Map<String, Integer> fragmentCountPerBin = new HashMap<String, Integer>();

  private VariationAdmin admin;


  public void runImport(CommandLine cl) throws IOException
    {
    admin = VariationAdmin.getInstance();
    admin.deleteTables();
    admin.createTables();

    setupVariations(cl.getOptionValue("v"));
    importGCBins(cl.getOptionValue("g"));
    importSNVProb(cl.getOptionValue("s"));
    importSizeProb(cl.getOptionValue("z"));
    importFragments(cl.getOptionValue("f"));
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

  private void importGCBins(String gc) throws IOException
    {
    GCBin table = (GCBin) admin.getTable(VariationTables.GC.getTableName());

    BufferedReader reader = new BufferedReader(new FileReader(gc));

    String line;
    while ((line = reader.readLine()) != null)
      {
      String[] bins = line.split("\t");
      if (bins[0].equals("") || bins[0].startsWith("[A-Za-z]")) continue;

      String key = bins[1] + ":" + bins[2];
      int min = Integer.parseInt(bins[3]);
      int max = Integer.parseInt(bins[4]);
      int fragCount = Integer.parseInt(bins[5]);

      table.addBin(bins[1], min, max, fragCount);

      gcBins.put(key, new IntRange(min, max));
      fragmentCountPerBin.put(key, fragCount);
      }
    }

  private void importSNVProb(String snv) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader(snv));

    SNVProbability table = (SNVProbability) admin.getTable(VariationTables.SNVP.getTableName());

    List<String> snvOrder = new ArrayList<String>();
    List<String[]> snvs = new ArrayList<String[]>();
    String line;
    while ((line = reader.readLine()) != null)
      {
      if (line.split("\t")[0].equals("")) continue;
      snvs.add(line.split("\t"));
      snvOrder.add(line.split("\t")[0]);
      }

    for (String[] prob : snvs)
      {
      String from = prob[0];
      for (int i = 0; i < snvOrder.size(); i++)
        {
        double p = Double.parseDouble(prob[i + 1]);
        String rowId = table.addSNV(from, snvOrder.get(i), p);
        if (rowId == null) throw new IOException("Failed to add snv probability for " + from + "-" + snvOrder.get(i));
        }
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
      if (sizes[0].equals("") || sizes[0].matches("[A-Za-z]+")) continue;
      String[] variation = variations.get(Integer.parseInt(sizes[1]));
      int maxbp = Integer.parseInt(sizes[0]);
      if (maxbp < 10) throw new IOException("Incorrect formatting or parsing, maxbp should never be <10");
      double prob = Double.parseDouble(sizes[2]);

      String rowId = table.addSizeProbabiilty(variation[0], maxbp, prob);
      if (rowId != null) ++count;
      }
    log.info("Added " + count + " rows to SIZE");
    }

  /* NOTE:
  These import methods are tied to the old files generated for the SQL versions of this database.
   */
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
      int fragmentCount = fragmentCountPerBin.get(chr + ":" + frag[1]);

      String rowId = table.addFragment(chr, variation[0], variation[1], count, gcRange.getMinimumInteger(), gcRange.getMaximumInteger(),
                                       fragmentCount);
      if (rowId == null) throw new IOException("Failed to add line: " + line);
      else ++rowCount;
      log.info(rowId);
      }

    log.info(rowCount + " rows added.");
    }

  }


