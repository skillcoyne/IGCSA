package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public abstract class SequenceAberration
  {
  static Logger log = Logger.getLogger(SequenceAberration.class.getName());

  // no, not the ideal way to do this but it'll work for now
  private static Map<String, Class<?>> aberrationClassMap;
  static {
    Map<String, Class<?>> map = new HashMap<String, Class<?>>();
  try
    {
    map.put("dup", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Duplication"));
    map.put("del", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Deletion"));
    map.put("inv", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Inversion"));
    map.put("add", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Addition"));
    map.put("iso", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Iso"));
    map.put("ins", Class.forName("org.lcsb.lu.igcsa.aberrations.single.Insertion"));

    map.put("trans", Class.forName("org.lcsb.lu.igcsa.aberrations.multiple.Translocation"));
    map.put("dic", Class.forName("org.lcsb.lu.igcsa.aberrations.multiple.Dicentric"));

    }
  catch (ClassNotFoundException e)
    {
    e.printStackTrace();
    }

  aberrationClassMap = (Map<String, Class<?>>) Collections.unmodifiableMap(map);
  }

  public static boolean hasClassForAberrationType(String str)
    {
    return aberrationClassMap.containsKey(str);
    }

  public static Class<?> getClassForAberrationType(String str)
    {
    return aberrationClassMap.get(str);
    }

  public abstract void addFragment(Band band, Chromosome chromosome);

  public abstract Collection<Band> getFragments();

  protected void writeRemainder(FASTAReader reader, int startLocation, FASTAWriter writer, MutationWriter mutationWriter) throws IOException
    {
    log.info("Write file remainder");
    // write the remainder of the file
    int window = 5000;
    String seq = reader.readSequenceFromLocation(startLocation, window);
    if (seq != null)
      {
      writer.write(seq);
      while ((seq = reader.readSequence(window)) != null)
        {
        writer.write(seq);
        if (seq.length() < window) break;
        }
      }
    }

  public abstract void applyAberrations(DerivativeChromosome derivativeChromosome, FASTAWriter writer, MutationWriter mutationWriter);
  }
