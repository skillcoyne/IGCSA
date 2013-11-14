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

import org.lcsb.lu.igcsa.hbase.*;
import org.lcsb.lu.igcsa.hbase.rows.ChromosomeRow;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeResult;
import org.lcsb.lu.igcsa.hbase.tables.ChromosomeTable;
import org.lcsb.lu.igcsa.hbase.rows.GenomeRow;
import org.lcsb.lu.igcsa.hbase.tables.GenomeResult;
import org.lcsb.lu.igcsa.hbase.tables.GenomeTable;

import java.util.List;

public class TestHBase
  {
  static Logger log = Logger.getLogger(TestHBase.class.getName());

  public static void main(String[] args) throws Exception
    {
    Configuration conf = HBaseConfiguration.create();
    conf.setInt("timeout", 10);

    HBaseAdmin admin = new HBaseAdmin(conf);

    GenomeTable genome = new GenomeTable(conf, admin, "genome", true);
    //HTable table = new HTable(conf, "tablename");

    print("total rows:" + genome.getRows().size());

    GenomeResult result = genome.queryTable("igcsa7");
    print(result.getName() + " p:" + result.getParent() + " chr:" + result.getChromosomes());


    List<GenomeResult> results = genome.queryTable(new Column("info"));
    for (GenomeResult r : results)
      print("Col info: " + r.getName() + " " + r.getParent() + " " + r.getChromosomes());

    GenomeRow row = new GenomeRow("kt122");
    row.addParentColumn("igcsa10");
    row.addChromosomeColumn("1", "2", "3");

    genome.addRow(row);


    ChromosomeTable chrTable = new ChromosomeTable(conf, admin, "chromosome", true);

    ChromosomeRow cRow = new ChromosomeRow(ChromosomeRow.createRowId(result.getName(), "2"));
    cRow.addChromosomeInfo("2", 298, 5);
    cRow.addGenome(result.getName());
    chrTable.addRow(cRow);

    ChromosomeResult chrResult = chrTable.queryTable(ChromosomeRow.createRowId(result.getName(), "2"));
    print(chrResult.getChrName() + " " + chrResult.getGenomeName() + " " + chrResult.getLength() + " " + chrResult.getSegmentNumber());


    //      admin.disableTable(tableName);
    //      admin.deleteTable(tableName);
    admin.close();
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
