package org.lcsb.lu.igcsa.fasta;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * org.lcsb.lu.igcsa.fasta
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class FASTAHeaderTest
  {
  static Logger log = Logger.getLogger(FASTAHeaderTest.class.getName());

  String str = ">gi|224384747|gb|CM000684.1| Homo sapiens chromosome 22, GRC primary reference assembly";
  FASTAHeader header;

  @Before
  public void setUp() throws Exception
    {
    header = new FASTAHeader(str);
    assertNotNull(header);
    }

  @Test
  public void testHeader() throws Exception
    {
    assertEquals(header.toString(), str.replace(">", ""));
    }

  @Test
  public void newHeader() throws Exception
    {
    header = new FASTAHeader("myDb", "nada", null, "something or another.");

    assertEquals(header.getDB(), "myDb");
    assertEquals(header.getAccession(), "nada");
    assertEquals(header.getLocus(), "");
    assertEquals(header.getFormattedHeader(), ">myDb|nada||something.or.another.");
    }



  }
