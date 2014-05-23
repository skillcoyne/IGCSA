/**
 * org.lcsb.lu.igcsa.utils
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.utils;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.aberrations.SequenceAberration;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Karyotype;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KaryotypeWriter
  {
  static Logger log = Logger.getLogger(KaryotypeWriter.class.getName());

  private Karyotype karyotype;
  private File outFile;

  private BufferedWriter bufferedWriter;
  private FileWriter fileWriter;


  public KaryotypeWriter(Karyotype karyotype, File outFile)
    {
    this.karyotype = karyotype;
    this.outFile = outFile;
    }

  public void write() throws IOException
    {
    createFile(outFile);

    StringBuffer buff = new StringBuffer();
    buff.append( karyotype.getBuildName() + "\n" );
    buff.append( "Parent tables: " + karyotype.getParentGenomePath() + "\n");
    buff.append( "Allosome definition: " + karyotype.getAllosomes() + "\n\n");

    buff.append("Normal chromosomes:\n");
    for (Chromosome chr: karyotype.getChromosomes())
      buff.append( karyotype.getChromosomeCount(chr.getName()) + "x" + chr.getName() + "\n" );

    buff.append("\nDerivative chromosomes:\n");

    for (DerivativeChromosome dChr: karyotype.getDerivativeChromosomes())
      {
      buff.append(dChr.getName() + "\n");
      for (SequenceAberration abr: dChr.getSequenceAberrationList())
        {
        List<String> derivatives = new ArrayList<String>();

        buff.append(abr.getClass().getSimpleName() + "\t");
        for (Band band: abr.getFragments())
          derivatives.add(  band + "(" + band.getLocation().getStart() + "-" + band.getLocation().getEnd() + ")" );

        buff.append(derivatives.get(0));
        derivatives.remove(0);
        for (String str: derivatives)
          buff.append(" -> " + str);
        buff.append("\n");
        }
      }

    bufferedWriter.write(buff.toString());
    bufferedWriter.flush();
    bufferedWriter.close();
    }



  private File createFile(File file) throws IOException
    {
    File parentDir = new File(file.getParent());
    if (!file.exists())
      {
      parentDir.mkdirs();
      try
        { file.createNewFile(); }
      catch (IOException ioe) { throw new IOException(ioe.getMessage() + " " + file.getAbsolutePath()); }
      }
    fileWriter = new FileWriter(file.getAbsoluteFile());
    bufferedWriter = new BufferedWriter(fileWriter);
    return file;
    }


  }
