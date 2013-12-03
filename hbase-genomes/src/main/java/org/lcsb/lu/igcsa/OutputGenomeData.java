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
import org.lcsb.lu.igcsa.hbase.HBaseSequence;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.util.Iterator;
import java.util.List;

public class OutputGenomeData
  {
  static Logger log = Logger.getLogger(OutputGenomeData.class.getName());


  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
//    conf.setInt("timeout", 120000);
//    conf.set("hbase.master", "*" + "bmf00004.uni.lux" + ":9000*");
//    conf.set("hbase.zookeeper.quorum", "bmf00004.uni.lux");
//    conf.set("hbase.zookeeper.property.clientPort", "2181");
    HBaseGenomeAdmin admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(conf);

    HBaseGenome genome = admin.getGenome("test");

    for (HBaseChromosome chr : genome.getChromosomes())
      log.info(chr.getChromosome().getChrName() + " " + chr.getChromosome().getSegmentNumber() + " " + chr.getChromosome().getLength());


    //genome.getChromosome("21").getChromosome().
    HBaseChromosome chr = genome.getChromosome("21");

    long start = 1;
    List<String> rowIds = chr.getSequenceRowIds(start, start+10000);
    while (rowIds != null && rowIds.size() > 0)
      {
      log.info(rowIds.size());
      for (String id: rowIds)
        {
        SequenceResult seq = admin.getSequenceTable().queryTable(id);
        log.info(seq.getChr() + " " + seq.getStart() + "-" + seq.getEnd());
        }
      start += 10000;
      rowIds =  chr.getSequenceRowIds(start, start+10000);
      }



    //Iterator<Result> rI = genome.getChromosome("21").getSequences();
//    log.info(rI.toString());
//    while (rI.hasNext())
//      {
//      log.info(Bytes.toString(rI.next().getRow()));
//      }

    }


  }
