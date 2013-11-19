/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.HBaseSequence;
import org.lcsb.lu.igcsa.hbase.tables.GenomeResult;

import java.io.IOException;

public class OutputGenomeData
  {
  static Logger log = Logger.getLogger(OutputGenomeData.class.getName());


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

    for (HBaseGenome gr: admin.retrieveGenomes())
      {
      log.info( gr.getGenome().getName() + " " + gr.getGenome().getChromosomes() );
      for (HBaseChromosome chr: gr.getChromosomes())
        {
        log.info( chr.getChromosome().getChrName() + " length:" + chr.getChromosome().getLength() + " num segments:" + chr.getChromosome().getSegmentNumber());

        // first and last just to check
//        HBaseSequence seq = chr.getSequence(1);
//        log.info(seq.getSequence().getSegment() + " :" + seq.getSequence().getStart() + "-" + seq.getSequence().getEnd());
//        seq = chr.getSequence( chr.getChromosome().getSegmentNumber() );
//        log.info(seq.getSequence().getSegment() + " :" + seq.getSequence().getStart() + "-" + seq.getSequence().getEnd());


        }



      }

    }

  }
