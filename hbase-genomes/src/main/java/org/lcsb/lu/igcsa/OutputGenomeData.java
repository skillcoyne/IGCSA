/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.util.Iterator;

public class OutputGenomeData
  {
  static Logger log = Logger.getLogger(OutputGenomeData.class.getName());


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

    HBaseGenome genome = admin.getGenome("GRCh37");

    for (HBaseChromosome chr: genome.getChromosomes())
      log.info( chr.getChromosome().getChrName() + " " + chr.getChromosome().getSegmentNumber() + " " + chr.getChromosome().getLength());


    Iterator<Result> rI = genome.getChromosome("1").getSequences(16550001, 249250622);
    log.info(rI.toString());
    while (rI.hasNext())
      {
      log.info(Bytes.toString(rI.next().getRow()) );
      }

    }




  }
