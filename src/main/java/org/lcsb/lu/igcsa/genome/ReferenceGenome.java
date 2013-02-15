package org.lcsb.lu.igcsa.genome;

import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.Genome;
import org.lcsb.lu.igcsa.prob.ProbabilityList;
import org.lcsb.lu.igcsa.utils.FileUtils;
import org.lcsb.lu.igcsa.variation.Variation;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenome extends Genome
  {
  private String build;
  private Map<Variation, ProbabilityList> variationProbabilities = new HashMap<Variation, ProbabilityList>();

  protected ReferenceGenome(String build)
    {
    this.build = build;
    }

  public ReferenceGenome(String buildName, String fastaDir)
    {
    this(buildName);
//    try
//      { buildChromosomesFromFASTA(new File(fastaDir)); }
//    catch (FileNotFoundException fne)
//      { fne.printStackTrace(); }
    }

  public void addVariationType(Variation v, ProbabilityList pl)
    {
    this.variationProbabilities.put(v, pl);
    }



  public void addChromosomeFromFASTA(File file)
    {
    String chr = file.getName().replace("chr", "").replace(".fa", "");
    this.addChromosome(new Chromosome(chr, file));
    }


  public Chromosome[] buildChromosomesFromFASTA(File fastaDir) throws FileNotFoundException
    {
    for (File file : FileUtils.listFASTAFiles(fastaDir))
      {
      String chr = file.getName().replace("chr", "").replace(".fa", "");
      this.addChromosome(new Chromosome(chr, file));
      }
    return this.getChromosomes().toArray( new Chromosome[this.getChromosomes().size()] );
    }





  }



