/**
 * org.lcsb.lu.igcsa.hbase.filters
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.filter.FilterList;
import org.lcsb.lu.igcsa.generator.Aberration;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.GenomeResult;

import java.io.IOException;
import java.util.List;


public class Inversion extends AberrationLocationFilter
  {
  private static final Log log = LogFactory.getLog(Inversion.class);


  @Override
  public FilterList getFilter(AberrationResult aberration, HBaseGenome genome) throws IOException
    {
    String genomeName = genome.getGenome().getName();
    List<Location> locations = aberration.getAberrationDefinitions();

    if (locations.get(0).getStart() > 1)
      getInitialLocationFilters(locations.get(0), genomeName);

    return filterList;
    }
  }
