/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.population.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.karyotype.generator.Aneuploidy;

import java.util.*;

public class KaryotypeCandidate
  {
  static Logger log = Logger.getLogger(KaryotypeCandidate.class.getName());

  private Set<Band> breakpoints = new HashSet<Band>();
  private Map<String, Aneuploidy> aneuploidy = new HashMap<String, Aneuploidy>();

  public KaryotypeCandidate()
    {
    }

  public KaryotypeCandidate clone()
    {
    return new KaryotypeCandidate(new HashSet<Band>(this.breakpoints), new HashMap<String, Aneuploidy>(this.aneuploidy));
    }

  private KaryotypeCandidate(Set<Band> breakpoints, Map<String, Aneuploidy> aneuploidy)
    {
    this.breakpoints = breakpoints;
    this.aneuploidy = aneuploidy;
    }

  public boolean addBreakpoints(Collection<Band> bands)
    {
    return this.breakpoints.addAll(bands);
    }

  public boolean addBreakpoint(Band band)
    {
    return this.breakpoints.add(band);
    }

  public boolean removeBreakpoint(Band band)
    {
    return this.breakpoints.remove(band);
    }

  public void gainChromosome(String chr)
    {
    alterPloidy(chr, 1);
    }

  public void loseChromosome(String chr)
    {
    alterPloidy(chr, -1);
    }

  public boolean hasBreakpoint(Band band)
    {
    return breakpoints.contains(band);
    }

  public Collection<Band> getBreakpoints()
    {
    List<Band> bps = new ArrayList<Band>(breakpoints);
    Collections.sort(bps);
    return bps;
    }

  public Collection<Aneuploidy> getAneuploidies()
    {
    return aneuploidy.values();
    }

  public void addAneuploidy(Aneuploidy ploidy)
    {
    alterPloidy(ploidy.getChromosome(), ploidy.getCount());
    }

  public void removeChromosome(String str)
    {
    this.aneuploidy.remove(str);
    }

  public void removeAneuploidy(Aneuploidy ploidy)
    {
    removeChromosome(ploidy.getChromosome());
//    if (aneuploidy.containsKey(ploidy.getChromosome()))
//      aneuploidy.remove(aneuploidy.get(ploidy.getChromosome())); // count doesn't need to match, just the chromosome
    }

  public Aneuploidy getAneuploidy(String chr)
    {
    if (aneuploidy.containsKey(chr))
      return aneuploidy.get(chr);
    else
      return new Aneuploidy(chr, 0);
    }

  private void alterPloidy(String chr, int n)
    {
    if (!aneuploidy.containsKey(chr))
      aneuploidy.put(chr, new Aneuploidy(chr, 0));

    if (n > 0)
      aneuploidy.get(chr).gain(Math.abs(n));
    else
      aneuploidy.get(chr).lose(Math.abs(n));

    if (aneuploidy.get(chr).getCount() == 0)
      aneuploidy.remove(chr);
    }


  @Override
  public String toString()
    {
    return this.getClass().getSimpleName() + "[" + this.hashCode() + "] " + this.getBreakpoints() + "\t" + this.getAneuploidies();
    }


  }
