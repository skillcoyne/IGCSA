/**
 * org.lcsb.lu.igcsa.genome.concurrency
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lcsb.lu.igcsa.database.normal.Bin;
import org.lcsb.lu.igcsa.database.normal.Fragment;
import org.lcsb.lu.igcsa.database.normal.FragmentDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.variation.fragment.Variation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

public class HBaseMutableSequence implements Callable<HBaseSequence>
  {
  private static final Log log = LogFactory.getLog(HBaseMutableSequence.class);

  // Database connections
  private GCBinDAO binDAO;
  private FragmentDAO fragmentDAO;

  private HBaseSequence hBaseSequence;
  private List<Variation> variantList;

  public HBaseMutableSequence(GCBinDAO binDAO, FragmentDAO fragmentDAO, HBaseSequence sequence, List<Variation> variantList)
    {
    this.binDAO = binDAO;
    this.fragmentDAO = fragmentDAO;
    this.hBaseSequence = sequence;
    this.variantList = variantList;
    }

  @Override
  public HBaseSequence call() throws Exception
    {
    mutateFragment();
    return hBaseSequence;
    }


  // Mutates the sequence based on the information provided in the database
  private void mutateFragment() throws IOException
    {
    String chromosmeName = hBaseSequence.getSequence().getChr();
    //long start = hBaseSequence.getSequence().getStart(), end = hBaseSequence.getSequence().getEnd();

    log.info("Mutating " + chromosmeName + " " + hBaseSequence.getSequence().getStart());

    long timeStart = System.currentTimeMillis();
    DNASequence mutatedSequence = new DNASequence(hBaseSequence.getSequence().getSequence());
    Random randomFragment = new Random();

    // get the GC content in order to select the correct fragment bin
    // might speed things up to skip anything that is > 70% (maybe less even) unknown/gap
    int totalNucleotides = mutatedSequence.calculateNucleotides();
    if (totalNucleotides > (0.3 * mutatedSequence.getLength()))
      {
      Bin gcBin = this.binDAO.getBinByGC(chromosmeName, mutatedSequence.calculateGC()); // TODO some of these calls take > 20ms (not even most though)

      // get random fragment within the GC bin for each variation
      Map<String, Fragment> fragmentVarMap = fragmentDAO.getFragment(chromosmeName, gcBin.getBinId(), randomFragment.nextInt(gcBin.getSize()));

      // apply the variations to the sequence, each of them needs to apply to the same fragment
      // it is possible that one could override another (e.g. a deletion removes SNVs)
      for (Variation variation : variantList)
        {
        Fragment fragment = fragmentVarMap.get(variation.getVariationName());
        variation.setMutationFragment(fragment);

        mutatedSequence = variation.mutateSequence(mutatedSequence);

        for (Location loc: variation.getLastMutations().keySet())
          hBaseSequence.addSmallMutation(variation, loc.getStart(), loc.getEnd(), variation.getLastMutations().get(loc).getSequence());
        }

      long elapsed = System.currentTimeMillis() - timeStart;
      //log.info(chromosmeName + " " + start + "-" + end + " took " + elapsed);
      }
    }

  }


