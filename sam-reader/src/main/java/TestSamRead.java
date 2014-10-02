import net.sf.samtools.Cigar;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.apache.log4j.Logger;

import java.io.File;


/**
 * PACKAGE_NAME
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TestSamRead
  {
  static Logger log = Logger.getLogger(TestSamRead.class.getName());

  public static void main(String[] args)
    {

    double[][] vv = new double[2][5];


    SAMFileReader fileReader = new SAMFileReader(new File("/Users/skillcoyne/Analysis/5q13-8q24/FASTQ.bam"));
    SAMRecordIterator iterator = fileReader.iterator();

    while (iterator.hasNext())
      {
      SAMRecord record = iterator.next();
      if (!record.getReadUnmappedFlag() && !record.getMateUnmappedFlag())
        {
        int qualSum = 0;
        Cigar cg = record.getCigar();

        for (byte b : record.getBaseQualities()) qualSum += b;

        String orient = "";
        orient = (record.getReadNegativeStrandFlag()) ? "R" : "F";
        orient += (record.getMateNegativeStrandFlag()) ? "R" : "F";

        record.getProperPairFlag();
        record.getReadName();
        record.getAlignmentStart();
        record.getMateAlignmentStart();
        record.getInferredInsertSize();
        record.getMappingQuality();


        }
      }
    }

  }
