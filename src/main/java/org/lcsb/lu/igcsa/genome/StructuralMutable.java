package org.lcsb.lu.igcsa.genome;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.database.normal.FragmentVariationDAO;
import org.lcsb.lu.igcsa.database.normal.GCBinDAO;
import org.lcsb.lu.igcsa.database.normal.SizeDAO;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.fasta.MutationWriter;
import org.lcsb.lu.igcsa.variation.structural.StructuralVariation;

import java.io.IOException;
import java.util.*;

/**
 * org.lcsb.lu.igcsa.genome
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class StructuralMutable implements Runnable
  {
  static Logger log = Logger.getLogger(StructuralMutable.class.getName());

  private Chromosome chromosome;

  private FASTAReader mutatedFASTA;
  private FASTAWriter writer;
  private MutationWriter mutationWriter;

  // Database connections
//  private GCBinDAO binDAO;
//  private FragmentVariationDAO variationDAO;
//  private SizeDAO sizeDAO;

  public void setMutatedFASTA(FASTAReader reader)
    {
    this.mutatedFASTA = reader;
    }


  public void setWriters(FASTAWriter writer, MutationWriter mutationWriter)
    {
    this.writer = writer;
    this.mutationWriter = mutationWriter;
    }

//  public void setConnections(GCBinDAO bin, FragmentVariationDAO fragment, SizeDAO size)
//    {
//    this.binDAO = bin;
//    this.variationDAO = fragment;
//    this.sizeDAO = size;
//    }


  private NavigableMap<Location, StructuralVariation> structuralVarLocations()
    {
    NavigableMap<Location, StructuralVariation> locationMap = new TreeMap<Location, StructuralVariation>();

    for (StructuralVariation sv: chromosome.getStructuralVariations())
      locationMap.put(sv.getLocation(), sv);

    return locationMap;
    }



  /*
  Run through all structural variations and mutate accordingly.
  TODO: This has to read in the mutated FASTA files and output new mutated files.
  Especially as there will be cases where sequence translocates from one chromosome to another.
  Which may mean that the variation class needs to know about writing?? Not sure about that.
   */
  public void run()
    {
    //NavigableMap<Location, StructuralVariation> locationMap;

    for(Map.Entry<Location, StructuralVariation> entry: structuralVarLocations().entrySet())
      {
      Location loc = entry.getKey();
      StructuralVariation variation = entry.getValue();
      try
        {
        String sequence = mutatedFASTA.readSequenceFromLocation(loc.getStart(), loc.getLength() );

        DNASequence mutatedSequence = variation.mutateSequence(sequence);




        }
      catch (IOException e)
        {
        log.error(e);
        }


      }


    }
  }
