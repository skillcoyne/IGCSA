package org.lcsb.lu.igcsa.genome;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * org.lcsb.lu.igcsa.tables
 * Author: skillcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class ChromosomeTest
  {
  private Chromosome chromosome;

  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/test.fa");
    File file = new File(testUrl.toURI());

    chromosome = new Chromosome("test", file);
    }

  @Test
  public void testSequenceByWindow() throws Exception
    {
    int window = 100;
    String seq = chromosome.readSequence(window).getSequence();
    assertNotNull(seq);
    assertEquals(seq.length(), window);
    assertNotSame(chromosome.readSequence(window), seq);
    }

  @Test
  public void testGetAllSequenceByWindow() throws Exception
    {
    int window = 50;
    String currentSeq;
    String all = "";

    while(true)
      {
      currentSeq = chromosome.readSequence(window).getSequence();
      all = all + currentSeq;
      if (currentSeq.length() < window) break;
      }
    assertEquals(all.length(), 644);
    }

  @Test
  public void testRetrieveFullSequence() throws Exception
    {
    DNASequence sequence = chromosome.retrieveFullSequence();
    assertEquals(sequence.getLength(), 644);
    }

  }
