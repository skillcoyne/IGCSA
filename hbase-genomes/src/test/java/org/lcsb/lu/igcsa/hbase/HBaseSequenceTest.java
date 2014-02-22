package org.lcsb.lu.igcsa.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.lcsb.lu.igcsa.hbase.tables.SequenceResult;

import java.util.List;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.hbase
 * Author: Sarah Killcoyne
 * Copyright University of Luxembourg, Luxembourg Centre for Systems Biomedicine 2014
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class HBaseSequenceTest
  {
  static Logger log = Logger.getLogger(HBaseSequenceTest.class.getName());

  HBaseGenomeAdmin admin;


  @Before
  public void setUp() throws Exception
    {
    admin = HBaseGenomeAdmin.getHBaseGenomeAdmin(new Configuration());
    }

  @Test
  public void getByRange() throws Exception
    {
    List<String> rowIds = admin.getGenome("Mini").getChromosome("9").getSequenceRowIds(1, 5001);
    assertNotNull(rowIds);
    assertEquals(rowIds.size(), 5);

    for (String row: rowIds)
      {
      SequenceResult seq = admin.getSequenceTable().queryTable(row);
      assertNotNull(seq);
      assertEquals(seq.getRowId(), row);
      assertEquals(seq.getChr(), "9");
      assertTrue(seq.getStart() >= 1 && seq.getEnd() <= 5001);
      }

    }

  }
