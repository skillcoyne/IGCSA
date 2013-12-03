/**
 * org.lcsb.lu.igcsa.hbase.fasta
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase.fasta;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Location;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.AberrationResult;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AberrationWriter
  {
  static Logger log = Logger.getLogger(AberrationWriter.class.getName());

  public static void writeDeletion(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("inv"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not a deletion");
    if (aberration.getAberrationDefinitions().size() > 1)
      throw new IOException("Deletions can only be performed on a single location");

    Location location = aberration.getAberrationDefinitions().get(0);
    int total = 0;
    HBaseChromosome chr = genome.getChromosome(location.getChromosome());

    // write from the start of the chromosome to the start of the location
    if (location.getStart() > 1)
      {
      Iterator<Result> seqI = chr.getSequences(1, location.getStart());
      total += write(seqI, writer);
      }
    // skip the location then write the rest of the chromosome from the end location
    Iterator<Result> seqI = chr.getSequences(location.getEnd(), (int) chr.getChromosome().getLength());
    total += write(seqI, writer);

    log.info("Total written: " + total);
    }

  public static void writeIsochromosome(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("inv"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not an isochromosome");
    if (aberration.getAberrationDefinitions().size() > 1)
      throw new IOException("Inversions can only be performed on a single location");

    Location location = aberration.getAberrationDefinitions().get(0);
    int total = 0;
    HBaseChromosome chr = genome.getChromosome(location.getChromosome());

    /*
    TODO  I don't know what to do with this one.  An isochromosome duplicates one arm around the centromere. However, without knowing which centromere this is
    on it may be that the best I can do would result in writing two isochromosomes, one for each arm...
     */
    log.warn("writeIsochromosome is not yet implemented");

    }

  public static void writeInversion(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("inv"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not an inversion");
    if (aberration.getAberrationDefinitions().size() > 1)
      throw new IOException("Inversions can only be performed on a single location");

    Location location = aberration.getAberrationDefinitions().get(0);
    int total = 0;
    HBaseChromosome chr = genome.getChromosome(location.getChromosome());
    // write from the start of the chromosome to the start of the location
    if (location.getStart() > 1)
      {
      Iterator<Result> seqI = chr.getSequences(1, location.getStart());
      total += write(seqI, writer);
      }

    // grab the sequences in reverse order, and output the actual segments in reverse as well
    List<String> orderedSequences = chr.getSequenceRowIds(location.getStart(), location.getEnd());

    ListIterator<String> reverseI = orderedSequences.listIterator(orderedSequences.size());
    total += reverseWrite(reverseI, writer);

    // write the remainder
    if (location.getEnd() < chr.getChromosome().getLength())
      {
      Iterator<Result> seqI = chr.getSequences(location.getEnd(), (int) chr.getChromosome().getLength());
      total += write(seqI, writer);
      }

    log.info("Total written: " + total);
    }

  // duplication involves copying an entire segment (location) and tacking it on to the end of the given location
  // so read from the start to the end of the location, then tack that location on. This means that there can only be
  // on location in a given duplication
  public static void writeDuplication(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("dup"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not a duplication");

    if (aberration.getAberrationDefinitions().size() > 1)
      throw new IOException("Duplications can only be performed on a single location");

    Location location = aberration.getAberrationDefinitions().get(0);
    int total = 0;
    HBaseChromosome chr = genome.getChromosome(location.getChromosome());
    // write from the start to the end of the location, then grab the start of the location to duplicate
    Iterator<Result> seqI = chr.getSequences(1, location.getEnd());
    total += write(seqI, writer);

    // duplication to the end of the chromosome
    seqI = chr.getSequences(location.getStart(), (int) chr.getChromosome().getLength());
    total += write(seqI, writer);

    log.info("Total written: " + total);
    }

  public static void writeTranslocation(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("trans"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not a translocation");

    Location firstLoc = aberration.getAberrationDefinitions().get(0);

    int total = 0;
    HBaseChromosome chr = genome.getChromosome(firstLoc.getChromosome());
    // write start to first location
    if (firstLoc.getStart() > 1)
      {
      log.info("Writing from start to " + firstLoc.getChromosome() + " " + firstLoc.getStart());

      //List<String> rowIds = chr.getSequenceRowIds(1, firstLoc.getStart());

//      Iterator<Result> seqI = chr.getSequences(1, firstLoc.getStart());
//      total += write(seqI, writer);
      }
    log.info(total + " written from " + firstLoc.getChromosome());

    // write each translocated fragment, at the point there's only going to be two
    Location finalLoc = firstLoc;
    for (Location loc : aberration.getAberrationDefinitions())
      {
      chr = genome.getChromosome(loc.getChromosome());
      log.info("Writing " + loc);
//      Iterator<Result> seqI = chr.getSequences(loc.getStart(), loc.getEnd() + 1);
//      int currentTotal = write(seqI, writer);
//      total += currentTotal;
//      log.info(currentTotal + " written from " + chr.getChromosome().getChrName());

      finalLoc = loc;
      }

    // write the remainder of the final chromosome
    log.info("Writing remaining " + finalLoc);
    chr = genome.getChromosome(finalLoc.getChromosome());

    Iterator<Result> allI = chr.getSequences();
    while (allI.hasNext())
      log.info(Bytes.toString(allI.next().getRow()));

//    Iterator<Result> seqI = chr.getSequences(finalLoc.getEnd(), chr.getChromosome().getLength());
//    total += write(seqI, writer);

    log.info("Total written: " + total);
    }

  public static void writeDicentric(AberrationResult aberration, HBaseGenome genome, FASTAWriter writer) throws IOException
    {
    if (!aberration.getAbrType().equals("trans"))
      throw new IOException("Aberration " + aberration.getAbrType() + " is not a dicentric");
    if (aberration.getAberrationDefinitions().size() > 2)
      throw new IOException("Dicentric chromosomes can only include 2 bands");

    int total = 0;
    Location firstLoc = aberration.getAberrationDefinitions().get(0);
    HBaseChromosome chr = genome.getChromosome(firstLoc.getChromosome());
    Iterator<Result> seqI = chr.getSequences(1, firstLoc.getEnd());
    total += write(seqI, writer);

    Location secondLoc = aberration.getAberrationDefinitions().get(0);
    chr = genome.getChromosome(secondLoc.getChromosome());
    seqI = chr.getSequences(secondLoc.getStart(), (int) chr.getChromosome().getLength());
    total += write(seqI, writer);

    log.info("Total written: " + total);
    }

  private static int write(Iterator<Result> rI, FASTAWriter writer) throws IOException
    {
    int total = 0;
    int count = 0;
    while (rI.hasNext())
      {
      SequenceResult seq = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().createResult(rI.next());
      log.info(seq.getChr() + " " + seq.getStart() + "-" + seq.getEnd());
      writer.write(seq.getSequence());
      total += seq.getSequenceLength();
      ++count;
      log.info(count);
      }

    return total;
    }





  private static int reverseWrite(ListIterator<String> seqRowIds, FASTAWriter writer) throws IOException
    {
    int total = 0;
    while (seqRowIds.hasPrevious())
      {
      String rowId = seqRowIds.previous();
      SequenceResult sequence = HBaseGenomeAdmin.getHBaseGenomeAdmin().getSequenceTable().queryTable(rowId);
      writer.write(StringUtils.reverse(sequence.getSequence()));
      total += sequence.getSequenceLength();
      }
    return total;
    }

  }
