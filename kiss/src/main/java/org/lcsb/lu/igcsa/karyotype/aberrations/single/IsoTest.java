/**
 * org.lcsb.lu.igcsa.aberrations.single
 * Author: sarah.killcoyne
 * Copyright University of Luxembourg and Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */


package org.lcsb.lu.igcsa.karyotype.aberrations.single;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.karyotype.database.Band;
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
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = {"classpath:kiss-test-spring-config.xml"})
public class IsoTest
  {
  static Logger log = Logger.getLogger(IsoTest.class.getName());

  private Iso abr;
  private File fastaFile;

  private FASTAWriter writer;

  @Autowired
  private Properties testProperties;


  @Before
  public void setUp() throws Exception
    {
    URL testUrl = ClassLoader.getSystemResource("fasta/wiki-text.fa");
    fastaFile = new File(testUrl.toURI());

    File insilicoTest = new File(testProperties.getProperty("dir.karyotype"));
    writer = new FASTAWriter(new File(insilicoTest, "iso-test.fa"), new FASTAHeader("test", "iso", "1", "none"));

    abr = new Iso();
    assertNotNull(abr);
    }

  @After
  public void tearDown() throws Exception
    {
    //writer.getFASTAFile().delete();
    }

  // output everything from 0 to the end of the band then output in reverse
  @Test
  public void testApplyP() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    Location location =  new Location(144, 608);
    abr.addFragment(new Band(chr.getName(), "p11", location));

    abr.applyAberrations(dchr, writer, null);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());

    String originalStr = chr.getFASTAReader().readSequenceFromLocation(0, location.getEnd()-1);


    assertEquals(reader.readSequenceFromLocation(0, location.getEnd()-1), originalStr);

    StringBuffer buff = new StringBuffer(reader.readSequenceFromLocation(location.getEnd(), 1000));
    assertEquals(buff.reverse().toString(), originalStr);
    }


  // output everything in reverse from the end of the band to the end of the chromosome, then output forward from end of band to end of chromosome
  @Test
  public void testApplyQ() throws Exception
    {
    Chromosome chr = new Chromosome("test", fastaFile);
    DerivativeChromosome dchr = new DerivativeChromosome("test", chr);

    Location location =  new Location(144, 608);
    abr.addFragment(new Band(chr.getName(), "q11", location));

    abr.applyAberrations(dchr, writer, null);

    FASTAReader reader = new FASTAReader(writer.getFASTAFile());

    String originalStr = chr.getFASTAReader().readSequenceFromLocation(0, location.getEnd()-1);

    StringBuffer buff = new StringBuffer( reader.readSequenceFromLocation(0, location.getEnd()-1) );
    assertEquals(buff.reverse().toString(), originalStr);

    assertEquals(reader.readSequenceFromLocation(location.getEnd(), 1000), originalStr);
    }


  }
