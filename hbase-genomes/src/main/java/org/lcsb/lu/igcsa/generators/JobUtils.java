/**
 * org.lcsb.lu.igcsa.generators
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.filters.AberrationLocationFilter;
import org.lcsb.lu.igcsa.karyotype.database.KaryotypeDAO;
import org.lcsb.lu.igcsa.karyotype.generator.Aberration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JobUtils
  {
  private static final Log log = LogFactory.getLog(JobUtils.class);


  public static Scan getScanFor(List<Location> locations, String genomeName) throws IOException
    {
    // this FilterList will contain nested filter lists that putt all of the necessary locations
    AberrationLocationFilter alf = new AberrationLocationFilter();

    Scan scan = new Scan();
    scan.setFilter(alf.getFilter(locations, genomeName));

    return scan;
    }

  public static List<Location> getBandLocations(List<Band> bands)
    {
    List<Location> locations = new ArrayList<Location>();
    for(Band b: bands)
      locations.add(b.getLocation());
    return locations;
    }

  public static List<Location> getAllLocations(List<Band> bands, KaryotypeDAO dao)
    {
    List<Location> locations = new ArrayList<Location>();

    if (bands.get(0).getStart() > 0)
      locations.add( new Location(bands.get(0).getChromosomeName(), 1, bands.get(0).getEnd()) );

    for (Band b: bands)
      locations.add( b.getLocation() );

    Band lastBand = dao.getBandDAO().getLastBand(bands.get(bands.size()-1).getChromosomeName());

    if (bands.get(bands.size()-1).getEnd() < lastBand.getEnd())
      locations.add( new Location( lastBand.getChromosomeName(), bands.get(bands.size()-1).getEnd(), lastBand.getEnd()) );

    return locations;
    }



  }
