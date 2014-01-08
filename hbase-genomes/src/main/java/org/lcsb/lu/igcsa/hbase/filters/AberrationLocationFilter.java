package org.lcsb.lu.igcsa.hbase.filters;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.hbase.filters
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public class AberrationLocationFilter
  {
  // this FilterList will contain nested filter lists that putt all of the necessary locations
  protected FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
  protected List<Location> locationList = new ArrayList<Location>();

  public FilterList getFilter(AberrationResult aberration, HBaseGenome genome) throws IOException
    {
    String genomeName = genome.getGenome().getName();
    List<Location> locations = aberration.getAberrationDefinitions();

    // get all segments up to first location
    if (locations.get(0).getStart() > 1)
      getInitialLocationFilters(locations.get(0), genomeName);

    // get subsequent locations
    for (Location loc : locations)
      {
      long start = (loc.getStart() + 1000) / 1000;
      long stop = (loc.getEnd() + 1000) / 1000;

      locationList.add(loc);

      addFilters(genomeName, loc, start, stop);
      }

    // get the rest of the chromosome -- NOTE it's unclear that this is really necessary in translocaions.
    getFinalLocationFilter(locations.get(locations.size() - 1), genome);

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
    long stop = (loc.getStart() + 1000) / 1000;

    locationList.add( new Location(loc.getChromosome(), start, loc.getStart() + 1000) );
    addFilters(genomeName, loc, start, stop);
    }

  protected void getFinalLocationFilter(Location loc, HBaseGenome genome) throws IOException
    {
    if (loc.getEnd() < genome.getChromosome(loc.getChromosome()).getChromosome().getLength())
      {
      //long start = (loc.getEnd() + 1000) / 1000;
      long start = loc.getEnd()/1000;
      long stop = (genome.getChromosome(loc.getChromosome()).getChromosome().getLength() + 1000) / 1000;

      locationList.add( new Location(loc.getChromosome(), loc.getEnd(), genome.getChromosome(loc.getChromosome()).getChromosome().getLength()) );

      addFilters(genome.getGenome().getName(), loc, start, stop);
      }
    }

  protected void addFilters(String genomeName, Location loc, long start, long stop)
    {
    FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL);

    RowFilter range1 = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(SequenceRow.createRowId(genomeName, loc.getChromosome(), start))));
    RowFilter range2 = new RowFilter(CompareFilter.CompareOp.LESS, new BinaryComparator(Bytes.toBytes(SequenceRow.createRowId(genomeName, loc.getChromosome(), stop))));

    filter.addFilter(range1);
    filter.addFilter(range2);

    filterList.addFilter(filter);
    }

  }
