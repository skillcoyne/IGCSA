/**
 * org.lcsb.lu.igcsa.hbase
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.lcsb.lu.igcsa.hbase.HBaseChromosome;
import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.HBaseSequence;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.util.Iterator;
import java.util.List;


public class OutputGenome
  {
  private static final Log log = LogFactory.getLog(OutputGenome.class);

  /**
   * org.lcsb.lu.igcsa
   * Author: sarah.killcoyne
   * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
   * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
   */


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    //    conf.setInt("timeout", 120000);
    //    conf.set("hbase.master", "*" + "bmf00004.uni.lux" + ":9000*");
    //    conf.set("hbase.zookeeper.quorum", "bmf00004.uni.lux");
    //    conf.set("hbase.zookeeper.property.clientPort", "2181");
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

    //  SequenceResult sr =     admin.getSequenceTable().queryTable("GRCh37-11:00043501");
    //    log.info(sr);

    HBaseGenome genome = admin.getGenome("GRCh37");

    for (HBaseChromosome chr : genome.getChromosomes())
      {
      String c = chr.getChromosome().getChrName();
      log.info(c + " " + chr.getChromosome().getSegmentNumber() + " " + chr.getChromosome().getLength());

      long segments = genome.getChromosome(c).getChromosome().getSegmentNumber();
      for (int i = 1; i <= segments; i++)
        {
        HBaseSequence seq = genome.getChromosome(c).getSequence(i);
        log.info( seq.getSequence().getStart() + ", " + seq.getSequence().getEnd() );
        }


      }
    //
    //    String c = "21";
    //    long segments = genome.getChromosome(c).getChromosome().getSegmentNumber();
    //    for (int i=1; i<=segments; i++)
    //      {
    //      HBaseSequence seq = genome.getChromosome(c).getSequence(i);
    //      log.info(seq.getSequence().toString());
    //      }
    //    log.info(segments);


    //    Iterator<Result> rI = genome.getChromosome("21").getSequences();
    //    log.info(rI.toString());
    //    while (rI.hasNext())
    //      {
    //      log.info(Bytes.toString(rI.next().getRow()));
    //      }

    }


  }
