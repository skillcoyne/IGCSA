/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generator;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

public class AberrationRules
  {
  static Logger log = Logger.getLogger(AberrationRules.class.getName());

  // combination sizes, not a lot of point in doing greater than pairs currently
  static int SET_SIZE = 2;

  // unique aberrations, e.g. [trans, [22q11, 9q34]] only shows up once in the list.  This should already be true
  static boolean UNIQUE_ABR = true;
  // unique breakpoint pairs
  static boolean UNIQUE_BP_PAIRS = true;

  private static Object[] ABR_TYPES = new String[]{"trans", "inv", "del", "dup", "ins"}; // default

  private BreakpointCombinatorial combinatorial;
  private Set<Object> aberrationClasses = new HashSet<Object>();

  private Map<String, List<Band>> chromosomes = new HashMap<String, List<Band>>();
  private List<ICombinatoricsVector<Aberration>> aberrations;


  public AberrationRules()
    {
    for (Object o : ABR_TYPES)
      aberrationClasses.add(o);
    }

  /* ------ Getters and setters ------ */
  /* Ultimately the rules should be set through here somehow, for now we'll just set up the base set */
  public void setAberrationClasses(Object[] abrTypes)
    {
    aberrationClasses.clear();
    for (Object o : abrTypes)
      aberrationClasses.add(o);
    }

  public Map<Object, Vector<Band>> getOrderedAberrations()
    {
    Map<Object, Vector<Band>> map = new HashMap<Object, Vector<Band>>();
    for (ICombinatoricsVector<Aberration> list: aberrations)
      {
      log.info(list);
//      if (!map.containsKey(abr)) map.put(abr, new Vector<Band>());
//
//      Vector<Band> curr = map.get(abr);
//

      }
    }

  public List<ICombinatoricsVector<Aberration>> getAberrations()
    {
    return aberrations;
    }

  public Object[] getAberrationClasses()
    {
    return aberrationClasses.toArray(new Object[aberrationClasses.size()]);
    }

  /* -- Application of rules -- */
  public void applyRules(Band[] bands)
    {
    for (Band b : bands)
      {
      if (!chromosomes.containsKey(b.getChromosomeName()))
        chromosomes.put(b.getChromosomeName(), new ArrayList<Band>());

      List<Band> current = chromosomes.get(b.getChromosomeName());
      current.add(b);
      chromosomes.put(b.getChromosomeName(), current);
      }
    log.debug(chromosomes);

    // start with the combinatorial list of breakpoints only, deal with unique rule
    List<ICombinatoricsVector<Band>> bandSets = ruleUniqueBreakpointPairs(bands);

    if (chromosomes.size() == 1)
      ruleSingleChromosome();

    // get aberrations with the bands, deal with unique rule
    aberrations = ruleUniqueAberrations(bandSets);
    log.debug(aberrations);
    }

  // Exactly what it sounds like.  Avoid pairs like [a, a]  (or don't...)
  private List<ICombinatoricsVector<Band>> ruleUniqueBreakpointPairs(Band[] bands)
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
      BreakpointCombinatorial.GENERATOR = BreakpointCombinatorial.SIMPLE_GEN;
      }

    combinatorial = new BreakpointCombinatorial();
    return combinatorial.getCombinations(bands, SET_SIZE);
    }

  // Avoid duplicate aberrations: [t, [1,2]], [t, [1,2]]  (or not...)
  private List<ICombinatoricsVector<Aberration>> ruleUniqueAberrations(List<ICombinatoricsVector<Band>> bandSets)
    {
    // get aberrations with the bands
    List<ICombinatoricsVector<Aberration>> abr = combinatorial.getAberrationCombination(aberrationClasses.toArray(new Object[aberrationClasses.size()]), bandSets);
    if (UNIQUE_ABR)
      {
      Collection<ICombinatoricsVector<Aberration>> uniqeAbr = new HashSet<ICombinatoricsVector<Aberration>>();
      for (ICombinatoricsVector<Aberration> o : abr)
        uniqeAbr.add(o);

      abr = new ArrayList<ICombinatoricsVector<Aberration>>(uniqeAbr);
      }
    return abr;
    }

  /*
  Rules for dealing with a single chromosome. This is a bit of a special case and doesn't currently have set-able rules
    1. In all cases a translocation cannot occur.
    2. Single band & centromere == isochromosome
    3. Multiple centromeric bands cannot be insertions
   */
  private void ruleSingleChromosome()
    {
    // only one chromosome
    log.debug("Only 1 chr");
    BandCollection cBands = new BandCollection(chromosomes.get(chromosomes.keySet().iterator().next()));

    // can't translocation when there's only 1 chromosome
    aberrationClasses.remove("trans");

    // single centromere
    if (cBands.size() == 1)
      {
      if (cBands.getCentromeres().size() == 1)
        {
        log.debug("Iso around " + cBands.getCentromeres());
        aberrationClasses.clear(); // only isochromosome
        aberrationClasses.add("iso");
        }
      }
    else if (cBands.getCentromeres().size() > 1 && cBands.getCentromeres().size() == cBands.size())
      {
      // multiple centromeres...what do I do here?
      log.debug("Only centromeres");

      // no translocations or insertions when only centromeres are involved
      aberrationClasses.remove("ins");
      }
    }


  }
