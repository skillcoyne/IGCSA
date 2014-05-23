/**
 * org.lcsb.lu.igcsa.aberrations.multiple
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.aberrations.multiple;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.genome.Band;
import org.lcsb.lu.igcsa.fasta.FASTAHeader;
import org.lcsb.lu.igcsa.fasta.FASTAReader;
import org.lcsb.lu.igcsa.fasta.FASTAWriter;
import org.lcsb.lu.igcsa.genome.Chromosome;
import org.lcsb.lu.igcsa.genome.DerivativeChromosome;
import org.lcsb.lu.igcsa.genome.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith (SpringJUnit4ClassRunner.class) @ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class DicentricTest
  {
  static Logger log = Logger.getLogger(DicentricTest.class.getName());

  private FASTAWriter writer;

  private File fastaOne;
  private File fastaTwo;

  private Chromosome chr1;
  private Chromosome chr2;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
    fastaOne = new File(ClassLoader.getSystemResource("fasta/wiki-text.fa").toURI());
    fastaTwo = new File(ClassLoader.getSystemResource("fasta/wiki-text2.fa").toURI());

    chr1 = new Chromosome("2", fastaOne);
    chr2 = new Chromosome("5", fastaTwo);

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    writer = new FASTAWriter(new File(insilicoTest, "dic-test.fa"), new FASTAHeader("test", "dic", "1", "none"));
    }

  @After
  public void tearDown() throws Exception
    {
    writer.getFASTAFile().delete();
    }

  @Test
  public void testApplyAberrations() throws Exception
    {
    DerivativeChromosomeAberration abr = new Dicentric();

    // first band is p and second arm is q or both are p it will be output in reverse order
    Band[] bands = new Band[]{
        new Band(chr1.getName(), "p11", new Location(144, 607)),
        new Band(chr2.getName(), "q12", new Location(702, 1063))};

    abr.addFragment(bands[0], chr1);
    abr.addFragment(bands[1], chr2);

    abr.applyAberrations(new DerivativeChromosome("dicentric"), writer, null);
    assertTrue(writer.getFASTAFile().length() > 20);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());

    // beginning should match the 2nd band
    assertEquals( reader.readSequenceFromLocation(702, 362), chr2.getFASTAReader().readSequenceFromLocation(702, 362) );
    assertEquals( reader.readSequenceFromLocation(0, 702+361), chr2.getFASTAReader().readSequenceFromLocation(0, 702+361) );

    // end should match 1st band
    assertEquals(reader.readSequenceFromLocation(bands[1].getLocation().getEnd()+1, 1000), chr1.getFASTAReader().readSequenceFromLocation(bands[0].getLocation().getStart(), 1000));
    }
  }
