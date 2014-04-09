/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.database.Band;
import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

import static org.lcsb.lu.igcsa.aberrations.AberrationTypes.*;

public class AberrationRules
  {
  static Logger log = Logger.getLogger(AberrationRules.class.getName());

  // combination sizes, not a lot of point in doing greater than pairs currently
  public static int SET_SIZE = 2;

  // allow for singletons
  public static boolean SINGLETONS = true;

  // unique breakpoint pairs
  public static boolean UNIQUE_BP_PAIRS = true;

  // SequenceAberration rules
  static final AberrationTypes[] SINGLE_CENTROMERE = new AberrationTypes[]{ISOCENTRIC, DUPLICATION, DELETION};
  static final AberrationTypes[] TWO_CENTROMERES = new AberrationTypes[]{DICENTRIC, DUPLICATION, DELETION, INVERSION};

  static final AberrationTypes[] ONE_CHROMOSOME = new AberrationTypes[]{DELETION, DUPLICATION, INVERSION};
  static final AberrationTypes[] MULTI_CHROMOSOME = new AberrationTypes[]{TRANSLOCATION};

  static final AberrationTypes[] NO_CENT_SPAN = new AberrationTypes[]{DELETION};

  private BreakpointCombinatorial combinatorial;
  private List<ICombinatoricsVector<Aberration>> aberrations;
  private List<ICombinatoricsVector<Band>> breakpointSets;

  /* ------ Getters and setters ------ */
  public void setRules(Properties properties)
    {
    log.warn("setRules() not yet implemented. Defaults in use.");
    }

  public Map<List<Band>, List<ICombinatoricsVector<Aberration>>> getOrderedBreakpointSets()
    {
    Map<List<Band>, List<ICombinatoricsVector<Aberration>>> map = new HashMap<List<Band>, List<ICombinatoricsVector<Aberration>>>();
    for (ICombinatoricsVector<Aberration> vector : aberrations)
      {
      List<Band> bandList = vector.getVector().get(0).getBands();

      if (!map.containsKey(bandList))
        map.put(bandList, new ArrayList<ICombinatoricsVector<Aberration>>());

      List<ICombinatoricsVector<Aberration>> abrList = map.get(bandList);
      abrList.add(vector);

      map.put(bandList, abrList);
      }

    return map;
    }

  public Map<AberrationTypes, List<ICombinatoricsVector<Band>>> getOrderedAberrationSets()
    {
    Map<AberrationTypes, List<ICombinatoricsVector<Band>>> map = new HashMap<AberrationTypes, List<ICombinatoricsVector<Band>>>();

    for (ICombinatoricsVector<Aberration> vector : aberrations)
      {
      for (Aberration abr : vector.getVector())
        {
        if (!map.containsKey(abr.getAberration()))
          map.put(abr.getAberration(), new ArrayList<ICombinatoricsVector<Band>>());

        List<ICombinatoricsVector<Band>> list = map.get(abr.getAberration());
        list.add(new CombinatoricsVector<Band>(abr.getBands()));

        map.put(abr.getAberration(), list);
        }
      }
    return map;
    }

  public List<ICombinatoricsVector<Band>> getBreakpointSets()
    {
    return breakpointSets;
    }

  public List<ICombinatoricsVector<Aberration>> getAberrations()
    {
    return aberrations;
    }

  public AberrationTypes[] getAberrationClasses()
    {
    Set<AberrationTypes> classes = new HashSet<AberrationTypes>();

    classes.addAll(Arrays.asList(SINGLE_CENTROMERE));
    classes.addAll(Arrays.asList(TWO_CENTROMERES));
    classes.addAll(Arrays.asList(ONE_CHROMOSOME));
    classes.addAll(Arrays.asList(MULTI_CHROMOSOME));

    return classes.toArray(new AberrationTypes[classes.size()]);
    }

  /* -- Application of rules -- */
  public void applyRules(Band[] bands)
    {
    Set<String> chromosomes = new HashSet<String>();

    for (Band b : bands)
      {
      if (b == null)
        throw new RuntimeException("something fucked up");
      chromosomes.add(b.getChromosomeName());
      }

    // start with the combinatorial list of breakpoints only, deal with unique rule
    List<ICombinatoricsVector<Band>> bandSets = ruleUniqueBreakpointPairs(bands, SET_SIZE);

    // add all of the breakpoints as singletons...
    if (SINGLETONS)
      {
      for (ICombinatoricsVector<Band> bvector : ruleUniqueBreakpointPairs(bands, 1))
        {
        if (bandSets.indexOf(bvector) < 0)
          bandSets.add(bvector);
        }
      }
    breakpointSets = bandSets;

    if (chromosomes.size() == 1)
      aberrations = ruleSingleChromosome(bandSets);
    else
      aberrations = ruleMultiChromosome(bandSets);

    //ruleSpanningCentromere();
    }

  // Some aberrations cannot span a centromere -- at the moment this doesn't seem to be a problem.  I think I'm not allowing multiple bands for some aberrations anyhow
  private void ruleSpanningCentromere()
    {
    // TODO implement
    log.warn("ruleSpanningCentromere() not yet implemented");
    for (ICombinatoricsVector<Aberration> a : aberrations)
      {
      Iterator<Aberration> abrI = a.getVector().iterator();
      while (abrI.hasNext())
        {
        Aberration abr = abrI.next();
        if (abr.getBands().size() > 1)
          {
          for (AberrationTypes type : NO_CENT_SPAN)
            {
            // bands are not on the same arm and at least one is a centromere, remove
            if (abr.getAberration().equals(type) && !abr.bandsOnSameArm() && abr.bandsIncludeCentromere())
              abrI.remove();
            }
          }
        }
      }
    }

  // Exactly what it sounds like.  Avoid pairs like [a, a]  (or don't...)
  private List<ICombinatoricsVector<Band>> ruleUniqueBreakpointPairs(Band[] bands, int setSize)
    {
    List<ICombinatoricsVector<Band>> bandSets;
    if (UNIQUE_BP_PAIRS) // avoid duplicates
      {
      log.debug("ruleUniqueBreakpointPairs: avoid duplicates");
      BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.SIMPLE_GEN;
      }
    else
      {
      log.debug("ruleUniqueBreakpointPairs: allow duplicates");
      BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.MULTI_GEN;
      }

    combinatorial = new BreakpointCombinatorial();
    return combinatorial.getCombinations(bands, setSize);
    }

  /*
  Rules for dealing with a single chromosome. This is a bit of a special case and doesn't currently have set-able rules
    1. In all cases a translocation cannot occur.
    2. Single band & centromere == ISO/DIC
    3. Pair of centromeric bands == DIC, DEL, DUP, INS, or INV
    4. Multiple bands DEL, DUP, INS, or INV.  Deletions cannot occur ACROSS a centromere however.
   */
  private List<ICombinatoricsVector<Aberration>> ruleSingleChromosome(List<ICombinatoricsVector<Band>> bandsVector)
    {
    List<ICombinatoricsVector<Aberration>> aberrations = new ArrayList<ICombinatoricsVector<Aberration>>();

    // only single centromere
    if (bandsVector.size() == 1 && bandsVector.get(0).getVector().get(0).isCentromere())
      aberrations = ruleSingleCentromere(bandsVector);
    else
      {
      // multiple bands
      for (ICombinatoricsVector<Band> vector : bandsVector)
        {
        // singleton is a centromere
        if (vector.getSize() == 1)
          {
          if (vector.getVector().get(0).isCentromere())
            aberrations.addAll(ruleSingleCentromere(Collections.singletonList(vector)));
          else
            aberrations.addAll(ruleSingleArm(Collections.singletonList(vector)));
          }
        else
          {
          boolean centromeres = true;
          for (Band b : vector.getVector())
            if (!b.isCentromere())
              centromeres = false;

          // pair of centromeres
          if (centromeres && vector.getSize() == 2)
            aberrations.addAll(ruleCentromerePair(Collections.singletonList(vector)));
          // anything else
          else
            aberrations.addAll(ruleSingleArm(Collections.singletonList(vector)));
          }
        }
      }
    return aberrations;
    }

  /*
   2) Where there are multiple chromosomes with breakpoints
   a)	DIC is possible between 2 chromsomes with centromeric bands
   b)	TRANS are possible between 2+ chromosomes at any band
   c)	DEL, DUP, INS, or INV are possible in breakpoints within a single chromosome
  */
  private List<ICombinatoricsVector<Aberration>> ruleMultiChromosome(List<ICombinatoricsVector<Band>> bandsVector)
    {
    List<ICombinatoricsVector<Aberration>> aberrations = new ArrayList<ICombinatoricsVector<Aberration>>();

    for (ICombinatoricsVector<Band> vector : bandsVector)
      {
      if (vector.getSize() == 1) // obviously it can only be a single chromosome
        aberrations.addAll(ruleSingleChromosome(Collections.singletonList(vector)));
      else
        {
        boolean sameChr = true;
        Band prev = null;
        for (Band b : vector.getVector())
          {
          if (prev != null && !prev.getChromosomeName().equals(b.getChromosomeName()))
            sameChr = false;
          prev = b;
          }
        if (sameChr) // single chromosome
          aberrations.addAll(ruleSingleChromosome(Collections.singletonList(vector)));
        else
          aberrations.addAll(combinatorial.getAberrationCombination(MULTI_CHROMOSOME, Collections.singletonList(vector)));
        }
      }
    return aberrations;
    }


  private List<ICombinatoricsVector<Aberration>> ruleSingleCentromere(List<ICombinatoricsVector<Band>> bandVector)
    {
    return combinatorial.getAberrationCombination(SINGLE_CENTROMERE, bandVector);
    }

  private List<ICombinatoricsVector<Aberration>> ruleCentromerePair(List<ICombinatoricsVector<Band>> bandsVector)
    {
    return combinatorial.getAberrationCombination(TWO_CENTROMERES, bandsVector);
    }

  private List<ICombinatoricsVector<Aberration>> ruleSingleArm(List<ICombinatoricsVector<Band>> bandVector)
    {
    return combinatorial.getAberrationCombination(ONE_CHROMOSOME, bandVector);
    }

  }
