package org.lcsb.lu.igcsa.variation.structural;

import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.genome.DNASequence;
import org.lcsb.lu.igcsa.genome.Location;

import java.util.Random;

/**
 * org.lcsb.lu.igcsa.variation.structural
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class CopyNumberGain extends StructuralVariation
  {
  static Logger log = Logger.getLogger(CopyNumberGain.class.getName());

  @Override
  public DNASequence mutateSequence(String sequence)
    {
    // Grab the section that needs to be duplicated
    String gainSection = sequence;
    if (sequence.length() > variationLocation.getLength())
      gainSection = sequence.substring(variationLocation.getStart(), variationLocation.getEnd());

    // TODO this is just for now.  There should be some info in the data I have regarding how many copies may be made
    int copyNumber = new Random().nextInt(10);

    String insertSequence = "";
    for (int i=1; i<=copyNumber; i++)
      insertSequence = insertSequence + gainSection;

    // insert sequence if the section provided was larger than the location provided
    String newSequence = "";
    if (sequence.length() > variationLocation.getLength())
      {
      newSequence = sequence.substring(0, variationLocation.getStart());
      newSequence = newSequence + insertSequence;
      newSequence = newSequence + sequence.substring(variationLocation.getEnd(), sequence.length());
      }
    else
      newSequence = insertSequence;

    return new DNASequence(newSequence);
    }
  }
