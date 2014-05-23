/**
 * org.lcsb.lu.igcsa.dist
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.generator;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.karyotype.aberrations.AberrationTypes;
import org.lcsb.lu.igcsa.genome.Band;
import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.*;

public class BreakpointCombinatorial
  {
  static Logger log = Logger.getLogger(BreakpointCombinatorial.class.getName());

  public static final int SIMPLE_GEN = 1;
  public static final int MULTI_GEN = 2;

  public static int GENERATOR = MULTI_GEN;

  private Map<Object, List<ICombinatoricsVector<Band>>> orderedAberrations;

  public BreakpointCombinatorial()
    {
    }

  /**
   * Takes a collection of breakpoints as bands and creates k-combinations (with set repetitions)
   * Uses the combinatorics library from https://code.google.com/p/combinatoricslib
   *
   * @param bands
   * @param setSize
   * @return
   */
  public List<ICombinatoricsVector<Band>> getCombinations(Band[] bands, int setSize)
    {
    Collection<Band> collection = new HashSet<Band>();
    for (Band o : bands)
      collection.add(o);
    return getCombinations(collection, setSize);
    }

  /**
   * Takes a collection of breakpoints as bands and creates k-combinations (with set repetitions): e.g. [3q42, 3q42], [3q42, 12p31], [3q42, 22q11]
   * Uses the combinatorics library from https://code.google.com/p/combinatoricslib
   *
   * @param bands
   * @param setSize
   * @return
   */
  public List<ICombinatoricsVector<Band>> getCombinations(Collection<Band> bands, int setSize)
    {
    log.debug("get combinations of " + bands.toString());
    ICombinatoricsVector<Band> initialVector = Factory.createVector(bands);

    Generator<Band> gen;
    if (GENERATOR == SIMPLE_GEN)
      gen = Factory.createSimpleCombinationGenerator(initialVector, setSize); // this one will NOT include sets of same objects (foo, foo)
    else if (GENERATOR == MULTI_GEN)
      gen = Factory.createMultiCombinationGenerator(initialVector, setSize); // this one will include sets of same objects (foo, foo)

    else
      throw new IllegalArgumentException("No generator type available.");

    List<ICombinatoricsVector<Band>> combinatoricsVectors = gen.generateAllObjects();

    // combinatorics don't work on a single object for obvious reasons, but I still need the object filled
    if (combinatoricsVectors.size() == 0 && bands.size() == 1)
      {
      ICombinatoricsVector<Band> v = new CombinatoricsVector<Band>();
      v.addValue(bands.iterator().next());
      combinatoricsVectors.add(v);
      }
    log.debug(combinatoricsVectors);

    return combinatoricsVectors;
    }


  /**
   * Runs getCombinations(bands, setSize) on the breakpoints and then combines each set with the provided aberrations.
   * Example:   [del, [3q42, 3q42]], [del, [3q42, 12p31]] etc...
   * Uses the combinatorics library from https://code.google.com/p/combinatoricslib
   *
   * @param abrs
   * @param bands
   * @param setSize
   * @return
   */
  public List<ICombinatoricsVector<Aberration>> getAberrationCombination(AberrationTypes[] abrs, Band[] bands, int setSize)
    {
    Collection<Band> collection = new HashSet<Band>();
    for (Band o : bands)
      collection.add(o);
    return getAberrationCombination(abrs, collection, setSize);
    }

  /**
   * Runs getCombinations(bands, setSize) on the breakpoints and then combines each set with the provided aberrations.
   * Example:   [del, [3q42, 3q42]], [del, [3q42, 12p31]] etc...
   * Uses the combinatorics library from https://code.google.com/p/combinatoricslib
   *
   * @param abrs
   * @param bands
   * @param setSize
   * @return
   */
  public List<ICombinatoricsVector<Aberration>> getAberrationCombination(AberrationTypes[] abrs, Collection<Band> bands, int setSize)
    {
    List<ICombinatoricsVector<Band>> bandsVector = getCombinations(bands, setSize);
    return getAberrationCombination(abrs, bandsVector);
    }

  public List<ICombinatoricsVector<Aberration>> getAberrationCombination(AberrationTypes[] abrs, List<ICombinatoricsVector<Band>> bandsVector)
    {
    orderedAberrations = new HashMap<Object, List<ICombinatoricsVector<Band>>>();

    List<ICombinatoricsVector<Aberration>> abrList = new ArrayList<ICombinatoricsVector<Aberration>>();

    for (AberrationTypes o : abrs)
      {
      List<ICombinatoricsVector<Band>> bandList = new ArrayList<ICombinatoricsVector<Band>>();
      for (ICombinatoricsVector<Band> vector : bandsVector)
        {
        ICombinatoricsVector<Aberration> comb = new CombinatoricsVector<Aberration>();
        comb.addValue(new Aberration(vector.getVector(), o));
        abrList.add(comb);
        bandList.add(vector);
        }

      orderedAberrations.put(o, bandList);
      }

    return abrList;
    }

  protected Map<Object, List<ICombinatoricsVector<Band>>> getOrderedAberrations()
    {
    if (orderedAberrations == null)
      log.warn("Aberrations have not been defined, need to call getAberrationCombination(...) first");

    return orderedAberrations;
    }
  }
