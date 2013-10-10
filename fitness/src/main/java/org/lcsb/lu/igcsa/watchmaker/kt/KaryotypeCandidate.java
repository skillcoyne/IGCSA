/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.watchmaker.kt;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.Band;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.zip.GZIPOutputStream;

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

  public void addBreakpoints(Collection<Band> bands)
    {
    BreakpointWatcher.getInstance().addAll(bands);
    this.breakpoints.addAll(bands);
    }

  public void addBreakpoint(Band band)
    {
    BreakpointWatcher.getInstance().add(band);
    this.breakpoints.add(band);
    }

  public void removeBreakpoint(Band band)
    {
    BreakpointWatcher.getInstance().remove(band);
    this.breakpoints.remove(band);
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

  public void compress()
    {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gz = null;
    try
      {
      gz = new GZIPOutputStream(baos);
      ObjectOutputStream oos = new ObjectOutputStream(gz);

      oos.writeObject(this.getBreakpoints());
      oos.close();


      }
    catch (IOException e)
      {
      log.error(e);
      }




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
    if (n != 0)
      {
      if (!aneuploidy.containsKey(chr))
        aneuploidy.put(chr, new Aneuploidy(chr, 0));

      Aneuploidy pl = aneuploidy.get(chr);
      pl.addToCount(n);

      if (pl.getCount() == 0)
        this.removeChromosome(pl.getChromosome());
      else
        aneuploidy.put(chr, pl);
      }
    }


  @Override
  public String toString()
    {
    return this.getClass().getSimpleName() + "[" + this.hashCode() + "] " + this.getBreakpoints() + "\t" + this.getAneuploidies();
    }




  public class Aneuploidy
    {
    private String chr;
    private int count;

    private int max = 6;
    private int min = -2;

    protected Aneuploidy(String chr, int count)
      {
      this.chr = chr;
      this.count = count;
      }

    protected void addToCount(int count)
      {
      this.count += count;

      if (this.count > max)
        this.count = max;
      if (this.count < min)
        this.count = min;
      }

    public String getChromosome()
      {
      return chr;
      }

    public int getCount()
      {
      return count;
      }

    public boolean isGain()
      {
      return (count > 0) ? true : false;
      }

    @Override
    public boolean equals(Object o)
      {
      Aneuploidy obj = (Aneuploidy) o;
      return (obj.getChromosome().equals(this.getChromosome())) ? true : false;
      }

    @Override
    public String toString()
      {
      return this.getChromosome() + "(" + this.getCount() + ")";
      }
    }


  }
