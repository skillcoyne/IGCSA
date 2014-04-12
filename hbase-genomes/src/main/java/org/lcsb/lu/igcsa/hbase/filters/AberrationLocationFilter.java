package org.lcsb.lu.igcsa.hbase.filters;

import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.database.Band;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.tables.genomes.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.genomes.GenomeResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.hbase.filters
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class AberrationLocationFilter
  {
  // this FilterList will contain nested filter lists that have all of the necessary locations
  protected FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
  protected List<Location> locationList = new ArrayList<Location>();

  public FilterList getFilterList()
    {
    return filterList;
    }

  public FilterList createFiltersFor(String genomeName, List<Band> bands, boolean sort)
    {
    if (sort)
      {
      Collections.sort(bands, new Comparator<Band>()
      {
      @Override
      public int compare(Band a, Band b)
        {
        if (a.getBandName().equals(b.getBandName())) return 0;
        if (!a.whichArm().equals(b.whichArm())) return a.whichArm().compareTo(b.whichArm());
        else return a.getLocation().compareTo(b.getLocation());
        }
      });

      List<Location> locations = new ArrayList<Location>();
      for (Band b : bands)
        locationList.add(b.getLocation());

      for (Band b: bands)
        addFilters(genomeName, b.getLocation(), b.getLocation().getStart(), b.getLocation().getEnd());

      }
    return filterList;
    }

  public FilterList getFilter(Aberration aberration, GenomeResult genome, List<ChromosomeResult> chromosomes,
                              boolean includeFinal) throws IOException
    {
    String genomeName = genome.getName();
    List<Location> locations = new ArrayList<Location>();
    for (Band b : aberration.getBands())
      locations.add(b.getLocation());

    // get all segments up to first location
    if (locations.get(0).getStart() > 1) getInitialLocationFilters(locations.get(0), genomeName);

    // get subsequent locations
    for (Location loc : locations)
      {
      locationList.add(loc);
      addFilters(genomeName, loc, loc.getStart(), loc.getEnd());
      }
    // get the rest of the chromosome -- NOTE it's unclear that this is really necessary in translocaions.
    if (includeFinal) getFinalLocationFilter(locations.get(locations.size() - 1), genome, chromosomes);

    return filterList;
    }

  public List<Location> getFilterLocationList()
    {
    return locationList;
    }


  protected void getInitialLocationFilters(Location loc, String genomeName)
    {
    // get first location
    long start = 1;
    long stop = loc.getStart();

    locationList.add(new Location(loc.getChromosome(), start, stop));
    addFilters(genomeName, loc, start, stop);
    }

  protected void getFinalLocationFilter(Location loc, GenomeResult genome, List<ChromosomeResult> chromosomes) throws IOException
    {
    ChromosomeResult currentChr = null;
    for (ChromosomeResult chr : chromosomes)
      {
      if (chr.getChrName().equals(loc.getChromosome()))
        {
        currentChr = chr;
        break;
        }
      }
    if (currentChr == null)
      throw new IOException("Chromosome list for genome " + genome.getName() + " did not include chromosome " + loc.getChromosome());

    if (loc.getEnd() < currentChr.getLength())
      {
      long start = loc.getEnd();
      long stop = currentChr.getLength();

      locationList.add(new Location(loc.getChromosome(), start, stop));

      addFilters(genome.getName(), loc, start, stop);
      }
    }

  protected void addFilters(String genomeName, Location loc, long start, long stop)
    {
    FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL,
                                                 Bytes.toBytes(genomeName)));
    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL,
                                                 Bytes.toBytes(loc.getChromosome())));

    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.GREATER_OR_EQUAL,
                                                 Bytes.toBytes(start)));

    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("end"), CompareFilter.CompareOp.LESS_OR_EQUAL, Bytes.toBytes(stop)));

    filterList.addFilter(filter);
    }

  }
