package org.lcsb.lu.igcsa.aberrations;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.ChromosomeFragment;
import org.lcsb.lu.igcsa.genome.Location;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.aberrations
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
// Translocations are special cases of an aberration. This may need to be a different class (not extending aberration) altogether.
public class Translocation extends Aberration
  {
  static Logger log = Logger.getLogger(Translocation.class.getName());

  // for a translocation fragments have to be in order
  private Map<ChromosomeFragment, Chromosome> fragments = new LinkedHashMap<ChromosomeFragment, Chromosome>();


  public Map<ChromosomeFragment, Chromosome> getFragments()
    {
    return fragments;
    }

  @Override
  public void addFragment(ChromosomeFragment fragment)
    {
    throw new RuntimeException("addFragment(ChromosomeFragment, Chromosome) is required for a Translocation Aberration");
    }

  public void addFragment(ChromosomeFragment fragment, Chromosome chr)
    {
    fragments.put(fragment, chr);
    }


  public void applyAberrations(Chromosome derivativeChr, FASTAWriter writer, MutationWriter mutationWriter)
    {
    boolean first = true;
    int n = 1;
    try
      {
      for (Map.Entry<ChromosomeFragment, Chromosome> entry : fragments.entrySet())
        {
        Chromosome chr = entry.getValue();
        Location fragmentLocation = entry.getKey().getBandLocation();

        // First chromosome, read from beginning to start location
        if (first && fragmentLocation.getStart() > 0)
          chr.getFASTAReader().streamToWriter(0, fragmentLocation.getStart()-1 , writer);

        chr.getFASTAReader().streamToWriter(fragmentLocation.getStart(), fragmentLocation.getEnd(), writer);

        // Last chromosome, read from end location to the end TODO it's possible that this should not be the case for translocations
        // hard to say at the moment
        if (n == fragments.size())
          writeRemainder(chr.getFASTAReader(), fragmentLocation.getEnd(), writer, mutationWriter);

        first = false;
        ++n;
        }
      writer.flush();
      writer.close();
      }
    catch (IOException ioe)
      {
      log.error(ioe);
      }
    }

    }
