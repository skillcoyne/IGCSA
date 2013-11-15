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
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.*;

import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.rows.SmallMutationRow;
import org.lcsb.lu.igcsa.hbase.tables.*;
import org.lcsb.lu.igcsa.hbase.rows.GenomeRow;

import java.util.List;
import java.util.Map;

public class TestHBase
  {
  static Logger log = Logger.getLogger(TestHBase.class.getName());

  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    conf.setInt("timeout", 10);

    HBaseGenome hBaseGenome = new HBaseGenome(conf);

    // create genome
    List<String> chrRowIds = hBaseGenome.addGenome("GRCh37", null,
                                                   new ChromosomeResult("1", 1000, 10),
                                                   new ChromosomeResult("2", 2000, 20),
                                                   new ChromosomeResult("3", 3000, 30));

    // add sequences
    String sequenceRowId = null; // no, this isn't the way it should be done. Just testing
    for (String chrRowId: chrRowIds)
      {
      sequenceRowId = hBaseGenome.addSequence(chrRowId, 1, 1, 100, "AAAACCCTGC");
      hBaseGenome.addSequence(chrRowId, 1, 100, 200, "AAAACCCTGC");
      }

    // adds mutations
    hBaseGenome.addSmallMutation(sequenceRowId, 10, 10, SmallMutationRow.SmallMutation.SNV, "A");
    hBaseGenome.addSmallMutation(sequenceRowId, 100, 120, SmallMutationRow.SmallMutation.DEL, null);

    // Karyotype!




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
