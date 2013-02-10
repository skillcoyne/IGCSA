package org.lcsb.lu.igcsa.utils;

import org.biojava3.core.sequence.io.FastaReader;
import org.lcsb.lu.igcsa.genome.Chromosome;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;


/**
 * org.lcsb.lu.igcsa.utils
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class ReferenceGenome
  {
  private String build;
  private Collection<Chromosome> chromosomes;

  protected ReferenceGenome(String build)
    {
    this.build = build;
    chromosomes = new ArrayList<Chromosome>();
    }

  public ReferenceGenome(String buildName, String fastaDir)
    {
    this(buildName);
    try
      { readFASTADir(new File(fastaDir)); }
    catch (FileNotFoundException fne)
      { fne.printStackTrace(); }
    }

  public Collection<Chromosome> getChromosomes()
    {
    return chromosomes;
    }


  private void readFASTADir(File fastaDir) throws FileNotFoundException
    {
    for (File file : FileUtils.listFASTAFiles(fastaDir))
      {
      String chr = file.getName().replace("chr", "").replace(".fa", "");
      chromosomes.add( new Chromosome(chr, file) );
      }
    }


  private void readFASTA(File fastaFile) throws FileNotFoundException
    {



    }




  }



