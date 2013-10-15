/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.watchmaker.kt.KaryotypeCandidate;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopulationAneuploidy
  {
  static Logger log = Logger.getLogger(PopulationAneuploidy.class.getName());

  private Map<String, Integer> gains = new HashMap<String, Integer>();
  private Map<String, Integer> losses = new HashMap<String, Integer>();
  private final int totalKaryotypes;

  public PopulationAneuploidy(List<EvaluatedCandidate<KaryotypeCandidate>> candidates)
    {
    totalKaryotypes = candidates.size();
    getCounts(candidates);
    }

  public int getGains(String chr)
    {
    return gains.get(chr);
    }

  public int getLosses(String chr)
    {
    return losses.get(chr);
    }

  public void write(File outFile) throws IOException
    {
    if (!outFile.exists())
      outFile.createNewFile();

    FileWriter fileWriter = new FileWriter(outFile.getAbsoluteFile());
    //BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

    fileWriter.write(outputString());
    fileWriter.flush();
    fileWriter.close();
    }

  public void write()
    {
    System.out.println(outputString());
    }

   private String outputString()
     {
     StringBuffer buf = new StringBuffer();
     buf.append("chromosome\tgain\tloss\tkaryotypes\n");
     for (String chr: gains.keySet())
       buf.append(chr + "\t" + gains.get(chr) + "\t" + losses.get(chr) + "\t" + (gains.get(chr)+losses.get(chr)) + "\n");

     return buf.toString();
     }

   private void getCounts(List<EvaluatedCandidate<KaryotypeCandidate>> candidates)
     {
     for (EvaluatedCandidate c: candidates)
       {
       KaryotypeCandidate kc = (KaryotypeCandidate) c.getCandidate();
       for(KaryotypeCandidate.Aneuploidy ap: kc.getAneuploidies())
         {
         initChr(ap.getChromosome());

         if (ap.isGain())
           gains.put(ap.getChromosome(), gains.get(ap.getChromosome())+ap.getCount());
         else
           losses.put(ap.getChromosome(), losses.get(ap.getChromosome())+Math.abs(ap.getCount()));
         }
       }
     }

  private void initChr(String chr)
    {
    if (!gains.containsKey(chr))
      gains.put(chr, 0);
    if (!losses.containsKey(chr))
      losses.put(chr, 0);
    }

  }
