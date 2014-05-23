package org.lcsb.lu.igcsa.karyotype.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lcsb.lu.igcsa.database.normal.SNVProbabilityDAO;
import org.lcsb.lu.igcsa.prob.Probability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.*;
/**
 * org.lcsb.lu.igcsa.database.normal
 * Author: sarah.killcoyne
 * Copyright Luxembourg Centre for Systems Biomedicine 2013
 * Open Source License Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations={"classpath:test-spring-config.xml"})
public class SNVProbabilityDAOTest
  {
  @Autowired
  private SNVProbabilityDAO testSNVDAO;


  @Test
  public void testGetByNucleotide() throws Exception
    {
    Probability freq = testSNVDAO.getByNucleotide("A");
    assertNotNull(freq);

    Map.Entry<Double, Object> first = freq.getProbabilities().firstEntry();
    assertEquals( first.getKey().doubleValue(), 0.16, 0.0);
    assertEquals( first.getValue(), 'T');

    Map.Entry<Double, Object> last = freq.getProbabilities().lastEntry();
    assertEquals( last.getKey().doubleValue(), 1.0, 0.0);
    assertEquals(last.getValue(), 'C');
    }
  }
