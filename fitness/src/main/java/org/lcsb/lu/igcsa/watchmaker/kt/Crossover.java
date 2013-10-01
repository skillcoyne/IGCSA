/**
 * org.lcsb.lu.igcsa.watchmaker.kt
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.KaryotypeCandidate;
import org.lcsb.lu.igcsa.database.Band;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.*;

/**
 * This will do a differential evolution crossover rather than a "mating".
 *
 * Select 3 members of the population at random.
 * v1 = x3 + F * (x1 - x2);
 *
 */
public class Crossover implements EvolutionaryOperator<KaryotypeCandidate>
  {
  static Logger log = Logger.getLogger(Crossover.class.getName());

  private double F = 0.5; // between 0,2
  private double CR = 0.5;

  private FitnessEvaluator<KaryotypeCandidate> fitness;

  private final Range FLIMIT = new DoubleRange(0, 2);
  private final Range CRLIMIT = new DoubleRange(0, 1);


  public Crossover(double f, double CR, FitnessEvaluator<KaryotypeCandidate> fitness)
    {
    if (!FLIMIT.containsDouble(f) || !CRLIMIT.containsDouble(CR))
      throw new IllegalArgumentException("F must be between 0,2 and CR must be between 0,1");

    F = f;
    this.CR = CR;
    this.fitness = fitness;
    }

  @Override
  public List<KaryotypeCandidate> apply(List<KaryotypeCandidate> karyotypeCandidates, Random random)
    {
    /* normally the crossover for DE takes only 3 candidates.  But as the code is written in Watchmaker it's simpler to let this
    class take all candidates and select 3 at random.*/
    Collections.shuffle(karyotypeCandidates, random);

    KaryotypeCandidate xr1 = karyotypeCandidates.get(0);
    KaryotypeCandidate xr2 = karyotypeCandidates.get(1);
    KaryotypeCandidate xr3 = karyotypeCandidates.get(2);

    /*
    There are two things to consider in the crossover, breakpoints and ploidy. What the crossover really does is just swap things around. So I can deal with them independently.
     */

    // this could include duplicate bands, I think for crossover that's actually a good idea since if they occur more frequently they already have a higher likelihood of being a breakpoint
    List<Band> breakpoints = new ArrayList<Band>(xr1.getBreakpoints());
    breakpoints.addAll(xr2.getBreakpoints());
    breakpoints.addAll(xr3.getBreakpoints());

    // each ploidy is independent, this means that 12x1 (or 19x-3) could show up multiple times or not at all for crossover anyhow
    List<KaryotypeCandidate.Aneuploidy> ploidy = new ArrayList<KaryotypeCandidate.Aneuploidy>(xr1.getAneuploidies());
    ploidy.addAll(xr2.getAneuploidies());
    ploidy.addAll(xr3.getAneuploidies());


    KaryotypeCandidate trial = xr3.clone();
    crossBreakpoints(breakpoints, new KaryotypeCandidate[]{xr1, xr2, xr3}, trial, random);
    crossAneuploidy(ploidy, new KaryotypeCandidate[]{xr1, xr2, xr3}, trial, random);


    // If fitness improves add it.  In this case we are actually looking for a lower fitness score otherwise we end up with massively mutated individuals and no real variation
    if (fitness.getFitness(trial, karyotypeCandidates) <= fitness.getFitness(xr3, karyotypeCandidates))
      {
      karyotypeCandidates.remove(xr3);
      karyotypeCandidates.add(trial);
      log.info("adding XO individual");
      }

    return karyotypeCandidates;
    }

  private void crossAneuploidy(List<KaryotypeCandidate.Aneuploidy> ploidy, KaryotypeCandidate[] candidates, KaryotypeCandidate trial, Random random)
    {
    Collections.shuffle(ploidy, random); // "noisy random vector"

    Iterator<KaryotypeCandidate.Aneuploidy> pI = ploidy.iterator();
    while(pI.hasNext())
      {
      KaryotypeCandidate.Aneuploidy aneuploidy = pI.next();

      if (random.nextDouble() <= CR)
        {
        int x1 = candidates[0].getAneuploidy(aneuploidy.getChromosome()).getCount();
        int x2 = candidates[1].getAneuploidy(aneuploidy.getChromosome()).getCount();
        int x3 = candidates[2].getAneuploidy(aneuploidy.getChromosome()).getCount();

        double v1 = x3 + F * (x1 - x2);

        if (v1 >= 0)
          trial.addAneuploidy(aneuploidy);
        if (v1 < 0)
          trial.removeAneuploidy(aneuploidy);
        }

      pI.remove();
      Collections.shuffle(ploidy, random); // "noisy random vector"
      }
    }

  private void crossBreakpoints(List<Band> breakpoints, KaryotypeCandidate[] candidates, KaryotypeCandidate trial, Random random)
    {
    Collections.shuffle(breakpoints, random); // "noisy random vector"

    Iterator<Band> bI = breakpoints.iterator();
    while (bI.hasNext())
      {
      Band band = bI.next();

      if (random.nextDouble() <= CR) // each band in the list has a chance of being swapped
        {
        //log.info("Crossing over");
        int x1 = (candidates[0].hasBreakpoint(band)) ? 1 : 0;
        int x2 = (candidates[1].hasBreakpoint(band)) ? 1 : 0;
        int x3 = (candidates[2].hasBreakpoint(band)) ? 1 : 0;

        //  v1 is generated by adding the differences of xr1 and xr2 with a weighting factor F (0,2)
        double v1 = x3 + F * (x1 - x2);

        if (v1 >= 0)
          trial.addBreakpoint(band);
        if (v1 < 0 && trial.hasBreakpoint(band))
          trial.removeBreakpoint(band);
        }

      bI.remove();
      Collections.shuffle(breakpoints, random); // "noisy random vector"
      }

    }


  }
