package org.lcsb.lu.igcsa.hbase.filters;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.rows.SequenceRow;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.GenomeResult;

import java.io.IOException;
import java.util.List;

/**
 * org.lcsb.lu.igcsa.hbase.filters
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


public  class AberrationLocationFilter
  {
  // this FilterList will contain nested filter lists that putt all of the necessary locations
  protected FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

  //public abstract FilterList getFilter(AberrationResult aberration, HBaseGenome genome) throws IOException;

  public FilterList getFilter(AberrationResult aberration, HBaseGenome genome) throws IOException
    {
    String genomeName = genome.getGenome().getName();
    List<Location> locations = aberration.getAberrationDefinitions();

    // get all segments up to first location
    if (locations.get(0).getStart() < 1)
      getInitialLocationFilters(locations.get(0), genomeName);

    // get subsequent locations
    for (Location loc : locations)
      {
      long start = (loc.getStart() + 1000) / 1000;
      long stop = (loc.getEnd() + 1000) / 1000;

      addFilters(genomeName, loc, start, stop);
      }

    // get the rest of the chromosome
    Location lastLoc = locations.get(locations.size() - 1);
    genome.getChromosome(lastLoc.getChromosome());
    if (lastLoc.getEnd() < genome.getChromosome(lastLoc.getChromosome()).getChromosome().getLength())
      {
      long start = (lastLoc.getEnd() + 1000) / 1000;
      long stop = (genome.getChromosome(lastLoc.getChromosome()).getChromosome().getLength() + 1000) / 1000;

      addFilters(genomeName, lastLoc, start, stop);
      }

    return filterList;
    }



  protected void getInitialLocationFilters(Location loc, String genomeName)
    {
    // get first location
    long start = 1;
    long stop = (loc.getStart() + 1000) / 1000;

    addFilters(genomeName, loc, start, stop);
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
