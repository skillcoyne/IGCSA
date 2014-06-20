package org.lcsb.lu.igcsa.hbase.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.genome.Band;
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
  private static final Log log = LogFactory.getLog(AberrationLocationFilter.class);

  // this FilterList will contain nested filter lists that have all of the necessary locations
  protected FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
  protected List<Location> locationList = new ArrayList<Location>();

  public FilterList getFilterList()
    {
    return filterList;
    }

  public FilterList createFiltersFor(String genomeName, Location location)
    {
    filterList.addFilter(createFilter(genomeName, location, location.getStart(), location.getEnd()));
    locationList.add(location);
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
        if (a.getBandName().equals(b.getBandName()))
          return 0;
        if (!a.whichArm().equals(b.whichArm()))
          return a.whichArm().compareTo(b.whichArm());
        else
          return a.getLocation().compareTo(b.getLocation());
        }
      });
      }

    for (Band b : bands)
      {
      locationList.add(b.getLocation());
      filterList.addFilter(createFilter(genomeName, b.getLocation(), b.getLocation().getStart(), b.getLocation().getEnd()));
      }

    log.info(locationList);
    return filterList;
    }

  public FilterList getFilter(List<Location> locations, GenomeResult genome) throws IOException
    {
    return getFilter(locations, genome.getName());
    }

  // This is ONLY getting segments for the specific bands themselves
  public FilterList getFilter(List<Location> locations, String genomeName) throws IOException
    {
    // Create the filter for each location
    for (Location loc : locations)
      {
      locationList.add(loc);
      filterList.addFilter( createFilter(genomeName, loc, loc.getStart(), loc.getEnd()) );
      }

    return filterList;
    }

  public List<Location> getFilterLocationList()
    {
    return locationList;
    }

  protected FilterList createFilter(String genomeName, Location loc, long start, long stop)
    {
    FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("info"), Bytes.toBytes("genome"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(genomeName)));
    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("chr"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes(loc.getChromosome())));

    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("start"), CompareFilter.CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(start)));

    filter.addFilter(new SingleColumnValueFilter(Bytes.toBytes("loc"), Bytes.toBytes("end"), CompareFilter.CompareOp.LESS_OR_EQUAL, Bytes.toBytes(stop)));

    return filter;
    }

  }
