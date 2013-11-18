/**
 * org.lcsb.lu.igcsa
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

package org.lcsb.lu.igcsa;

import org.apache.log4j.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.util.*;

import org.lcsb.lu.igcsa.hbase.HBaseGenome;
import org.lcsb.lu.igcsa.hbase.HBaseGenomeAdmin;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.lcsb.lu.igcsa.hbase.tables.*;

import java.util.List;

public class TestHBase
  {
  static Logger log = Logger.getLogger(TestHBase.class.getName());

  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    //conf.setInt("timeout", 1);

    //    conf.set("hbase.zookeeper.quorum", "10.79.5.22");
    //    conf.set("hbase.zookeeper.property.clientPort","2182");
    //    conf.set("hbase.master", "10.79.5.22:60000");


    HBaseGenomeAdmin admin = new HBaseGenomeAdmin(conf);


    HBaseGenome genome = admin.addGenome("GRCh37", null);

    genome.addChromosome("X", 200200, 2919);
    genome.addChromosome("1", 5000, 200);




//    // create genome
//    List<String> chrRowIds = hBaseGenome.addGenome("GRCh37", null, new ChromosomeResult("1", 1000, 10), new ChromosomeResult("2", 2000, 20), new ChromosomeResult("3", 3000, 30));
//
//    // add sequences
//    String sequenceRowId = null; // no, this isn't the way it should be done. Just testing
//    for (String chrRowId : chrRowIds)
//      {
//      sequenceRowId = hBaseGenome.addSequence(chrRowId, 1, 1, 100, "AAAACCCTGC");
//      hBaseGenome.addSequence(chrRowId, 1, 100, 200, "AAAACCCTGC");
//      }
//
//    // adds mutations
//    hBaseGenome.addSmallMutation(sequenceRowId, 10, 10, SmallMutationRow.SmallMutation.SNV, "A");
//    hBaseGenome.addSmallMutation(sequenceRowId, 100, 120, SmallMutationRow.SmallMutation.DEL, null);
//
//    // Karyotype!
//

    hBaseGenome.closeConections();
    }

  private static void print(byte[] str)
    {
    System.out.println(Bytes.toString(str));
    }

  private static void print(String str)
    {
    System.out.println(str);
    }

  }
